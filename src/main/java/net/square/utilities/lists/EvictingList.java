package net.square.utilities.lists;

import lombok.Getter;

import java.util.LinkedList;

public class EvictingList<T> extends LinkedList<T> {

    @Getter
    private final int maxSize;

    public EvictingList(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T t) {
        if (size() >= getMaxSize()) removeFirst();
        return super.add(t);
    }

    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
