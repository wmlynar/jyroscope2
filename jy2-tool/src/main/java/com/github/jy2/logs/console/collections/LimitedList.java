package com.github.jy2.logs.console.collections;

public class LimitedList<T> {

	private T[] array;
	private int pos = -1;
	private int size = 0;

	@SuppressWarnings("unchecked")
	public LimitedList(int size) {
		this.array = (T[]) new Object[size];
	}

	public void add(T item) {
		pos = (pos + 1) % array.length;
		array[pos] = item;
		if (size < array.length) {
			size++;
		}
	}

	public T get(int i) {
		return array[Math.floorMod((pos - i), array.length)];
	}

	public int size() {
		return size;
	}

}
