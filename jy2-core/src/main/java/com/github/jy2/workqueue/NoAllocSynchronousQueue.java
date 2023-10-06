package com.github.jy2.workqueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of a synchronous queue that aims to minimize memory
 * allocations. This implementation does not allocate memory upon item transfer
 * but may still perform allocations during lock contention and thread
 * management.
 *
 * Note: This implementation does not implement the full BlockingQueue interface
 * and should be used with caution.
 *
 * @param <E> The type of elements held in this queue
 */
public class NoAllocSynchronousQueue<E> implements BlockingQueue<E> {

	private E item = null;
	private final Lock lock = new ReentrantLock();
	private final Condition notEmpty = lock.newCondition();
	private final Condition notFull = lock.newCondition();

	@Override
	public void put(E x) throws InterruptedException {
		lock.lock();
		try {
			while (item != null) {
				notFull.await();
			}
			item = x;
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E take() throws InterruptedException {
		lock.lock();
		try {
			while (item == null) {
				notEmpty.await();
			}
			E x = item;
			item = null;
			notFull.signal();
			return x;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean offer(E e) {
		lock.lock();
		try {
			if (item == null && e != null) { // check for an existing item and null
				item = e;
				notEmpty.signal();
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		lock.lock();
		try {
			while (item == null) {
				if (nanos <= 0L)
					return null;
				nanos = notEmpty.awaitNanos(nanos);
			}
			E x = item;
			item = null;
			notFull.signal();
			return x;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E poll() {
		lock.lock();
		try {
			if (item == null) {
				return null;
			}
			E x = item;
			item = null;
			notFull.signal();
			return x;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int remainingCapacity() {
		lock.lock();
		try {
			return (item == null) ? 1 : 0;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int size() {
		lock.lock();
		try {
			return (item == null) ? 0 : 1;
		} finally {
			lock.unlock();
		}
	}

	// Other BlockingQueue methods need to be implemented to fully satisfy the
	// interface.
	// The methods below are not implemented and would throw
	// UnsupportedOperationException.

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E element() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E peek() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		throw new UnsupportedOperationException();
	}
}
