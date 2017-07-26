package com.jmaerte.util;

public interface Evaluable<K,V> {
	
	public V evaluate(K k);
	
}
