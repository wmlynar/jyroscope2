package com.github.jy2.workqueue;

public class CircularBuffer<T> {

	private final T[] buffer;
	private int start = 0;
	private int end = 0;
	private boolean full = false;

	@SuppressWarnings("unchecked")
	public CircularBuffer(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("Size must be greater than 0.");
		}

		this.buffer = (T[]) new Object[maxSize];
	}

	public void addLast(T value) {
		buffer[end] = value;
		end = (end + 1) % buffer.length;

		if (full) {
			start = (start + 1) % buffer.length;
		} else if (end == start) {
			full = true;
		}
	}

	public T pollFirst() {
		if (size() == 0) {
			return null;
		}

		T value = buffer[start];
		buffer[start] = null; // Clear the value
		start = (start + 1) % buffer.length;
		full = false; // The buffer cannot be full after a poll
		return value;
	}

	public int size() {
		if (full) {
			return buffer.length;
		} else {
			return (end - start + buffer.length) % buffer.length;
		}
	}

}
