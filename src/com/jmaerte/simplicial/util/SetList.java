package com.jmaerte.simplicial.util;

import javafx.collections.transformation.SortedList;

import java.util.*;

/**
 * Created by Julian on 22/06/2017.
 */
public class SetList<T> extends ArrayList<T> implements Set<T> {

    private Set<T> set;

    public SetList() {
        super();
        set = new HashSet<>();
    }

    public SetList(int size)
    {
        super(size);
        set = new HashSet<T>(size);
    }

    public SetList(Collection<? extends T> c)
    {
        super(c);
        set = new HashSet<T>(c);
        super.retainAll(set);
    }

    public boolean add(T t) {
        return set.add(t)?super.add(t):false;
    }

    public boolean addAll(Collection<? extends T> collection) {
        if(collection == null) return false;
        List<T> addQueue = new ArrayList<>(collection.size());
        for(T data : collection) {
            if(set.add(data)) addQueue.add(data);
        }
        return super.addAll(addQueue) && addQueue.size() > 0;
    }

    public T remove(int i) {
        T t = super.remove(i);
        set.remove(t);
        return t;
    }

    public boolean removeAll(Collection collection) {
        set.removeAll(collection);
        return super.removeAll(collection);
    }

    public boolean remove(Object obj) {
        set.remove(obj);
        return super.remove(obj);
    }

    public boolean retainAll(Collection collection) {
        set.retainAll(collection);
        return super.retainAll(collection);
    }

    public boolean contains(Object obj) {
        return set.contains(obj);
    }

    public boolean containsAll(Collection collection) {
        return set.containsAll(collection);
    }

    public void clear() {
        set.clear();
        super.clear();
    }

    public Spliterator<T> spliterator() {
        return super.spliterator();
    }
}
