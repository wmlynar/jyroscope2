package com.github.jy2.workqueue;

public class CircularBuffer<T> {

	private final T[] buffer;
	private int start = 0;
	private int end = 0;
	private boolean full = false;
	private T marker;

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

	public void setMarker(T value) {
		marker = value;
	}

	public T pollFirst() {
		if (marker != null) {
			T value = marker;
			marker = null;
			return value;
		}

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

	public void clear() {
		marker = null;

		start = 0;
		end = 0;
		full = false;
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = null;
		}
	}

}
