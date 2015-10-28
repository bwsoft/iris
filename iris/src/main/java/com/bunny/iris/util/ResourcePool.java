package com.bunny.iris.util;

public interface ResourcePool<T extends Resource> {
	public T get();
	public void add(T resource);
}
