package ca.concordia.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SimpleList<T> implements Iterable<T> {
    private Object[] data;
    private int size;

    public SimpleList() {
        data = new Object[16];
        size = 0;
    }

    public void add(T value) {
        ensureCapacity(size + 1);
        data[size++] = value;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return (T) data[index];
    }

    public int size() {
        return size;
    }

    public Object[] rawArray() {
        return data;
    }

    private void ensureCapacity(int required) {
        if (required <= data.length) {
            return;
        }
        int newCapacity = data.length * 2;
        if (newCapacity < required) {
            newCapacity = required;
        }
        Object[] next = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            next[i] = data[i];
        }
        data = next;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) data[cursor++];
            }
        };
    }
}
