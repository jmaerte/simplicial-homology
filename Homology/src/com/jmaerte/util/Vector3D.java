package com.jmaerte.util;

public class Vector3D <K, V, W> extends Vector2D<K, V> {
	
	public W k;
	
	public Vector3D(K k, V v, W w) {
		super(k,v);
		this.k = w;
	}
	
}
