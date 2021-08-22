package com.jwd;


import java.util.*;
import java.util.function.Consumer;


public class MyArrayList<T> implements List<T>, Cloneable {

    private static final int STANDARD_CAPACITY = 10;
    private static final int MAX_SIZE = Integer.MAX_VALUE;
    private static final Object[] EMPTY_ARRAY = {};
    private Object[] array;
    private int arraySize;
    protected transient int modCount = 0; //вставлено пока только для итераторов


    //default constructor:
    public MyArrayList(int initialCapacity) {
        if (initialCapacity > 0 && initialCapacity < MAX_SIZE) {
            this.array = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.array = EMPTY_ARRAY;
        } else {
            throw new IllegalArgumentException("Invalid array capacity: " + initialCapacity);
        }
    }

    //empty constructor:
    public MyArrayList() {
        this.array = new Object[STANDARD_CAPACITY];
    }

    //constructor for collection
    public MyArrayList(Collection<? extends T> c) {
        Object[] a = c.toArray();
        if ((arraySize = a.length) != 0) {
            if (c.getClass() == ArrayList.class) {
                array = a;
            } else {
                array = Arrays.copyOf(a, arraySize, Object[].class);
            }
        } else {
            array = EMPTY_ARRAY;
        }
    }

    @Override
    public T get(int index) {
        validateIndex(index);
        return (T) this.array[index];
    }

    @Override
    public T set(int index, T element) {
        validateIndex(index);
        T oldValue = (T) array[index];
        this.array[index] = element;
        return oldValue;
    }

    private void validateIndex(int index) {
        if (index < 0 || index > this.arraySize) {
            throw new ArrayIndexOutOfBoundsException("Invalid array index");
        }
    }

    @Override
    public void add(int index, T element) {
        validateIndex(index);
        ensureCapacityInternal(arraySize + 1);
        int numMoved = arraySize - index;
        System.arraycopy(array, index, array, index + 1, numMoved);
        array[index] = element;
        arraySize++;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(array, minCapacity));
    }

    private static int calculateCapacity(Object[] array, int minCapacity) {
        if (array == EMPTY_ARRAY) {
            return Math.max(STANDARD_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - array.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        int oldCapacity = array.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        array = Arrays.copyOf(array, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_SIZE) ?
                Integer.MAX_VALUE : MAX_SIZE;
    }

    @Override
    public T remove(int index) {
        validateIndex(index);
        T oldValue = (T) this.array[index];
        int numMoved = arraySize - index - 1;
        if (numMoved > 0) {   //if not last element is removed
            System.arraycopy(array, index + 1, array, index, numMoved);
        }
        arraySize--;
        array[arraySize] = null;
        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < arraySize; i++)
                if (array[i] == null) return i;
        } else {
            for (int i = 0; i < arraySize; i++)
                if (o.equals(array[i])) return i;
        }
        return -1;
    }

    public int lastIndexOf(Object o) {      //метод обратный indexOf
        if (o == null) {
            for (int i = arraySize - 1; i >= 0; i--)
                if (array[i] == null)
                    return i;
        } else {
            for (int i = arraySize - 1; i >= 0; i--)
                if (o.equals(array[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        validateIndex(fromIndex);
        validateIndex(toIndex);
        if (fromIndex > toIndex) throw new IllegalArgumentException("Not valid arguments: fromIndex>toIndex");
        MyArrayList<T> subList = new MyArrayList<>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            subList.add(i - fromIndex, (T) array[i]);
        }
        return subList;
    }


    @Override
    public Object[] toArray() {
        return Arrays.copyOf(array, arraySize);
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < arraySize)
            return (T1[]) Arrays.copyOf(array, arraySize, a.getClass());
        System.arraycopy(array, 0, a, 0, arraySize);
        if (a.length > arraySize)
            a[arraySize] = null;
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Object[] a = c.toArray();
        int length = c.size();
        if (arraySize < length) return false;
        boolean contains = true;
        for (int i = 0; i < length; i++) {
            if (!this.contains(a[i])) {
                contains = false;
            }
        }
        return contains;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return partRemove(c, false);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return partRemove(c, true);
    }

    private boolean partRemove(Collection<?> c, boolean compl) {
        final Object[] array = this.array;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < arraySize; r++)
                if (c.contains(array[r]) == compl)
                    array[w++] = array[r];
        } finally {
            if (r != arraySize) {
                System.arraycopy(array, r,
                        array, w,
                        arraySize - r);
                w += arraySize - r;
            }
            if (w != arraySize) {
                for (int i = w; i < arraySize; i++)
                    array[i] = null;
                arraySize = w;
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public int size() {
        return arraySize;
    }

    @Override
    public boolean isEmpty() {
        return arraySize == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public boolean add(T t) {
        ensureCapacityInternal(arraySize + 1);
        array[arraySize] = t;
        arraySize++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            for (int i = 0; i < arraySize; i++)
                if (array[i] == null) {
                    remove(i);
                    return true;
                }
        } else {
            for (int i = 0; i < arraySize; i++)
                if (o.equals(array[i])) {
                    remove(i);
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(arraySize + numNew);
        System.arraycopy(a, 0, array, arraySize, numNew);
        arraySize += numNew;
        return numNew != 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        validateIndex(index);
        Object[] a = c.toArray();
        int numNew = a.length;  //длина входной коллекции
        ensureCapacityInternal(arraySize + numNew);
        int numMoved = arraySize - index;  //количество перемещаемых эл-тов листа, в который добавляем
        if (numMoved > 0)
            System.arraycopy(array, index, array, index + numNew, numMoved); //сдвиг массива

        System.arraycopy(a, 0, array, index, numNew);
        arraySize += numNew;
        return numNew != 0;
    }


    @Override
    protected Object clone() {
        try {
            super.clone();
            MyArrayList<T> copy = new MyArrayList<T>(this.arraySize);
            ;
            copy.array = Arrays.copyOf(this.array, arraySize);
            copy.arraySize = this.arraySize;
            return copy;
        } catch (CloneNotSupportedException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < arraySize; i++)
            this.array[i] = null;
        arraySize = 0;
    }

    @Override
    public String toString() {
        String message = "Array [ ";
        for (int i = 0; i < this.arraySize; i++)
            message += this.array[i] + " ";
        message += "]";
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyArrayList<?> that = (MyArrayList<?>) o;

        if (arraySize != that.arraySize) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(array, that.array);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(array);
        result = 31 * result + arraySize;
        return result;
    }

    private class Itr implements Iterator<T> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        Itr() {
        }

        public boolean hasNext() {
            return cursor != arraySize;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            checkForComodification();
            int i = cursor;
            if (i >= arraySize)
                throw new NoSuchElementException();
            Object[] array = MyArrayList.this.array;
            if (i >= array.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (T) array[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                MyArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            final int size = MyArrayList.this.arraySize;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] array = MyArrayList.this.array;
            if (i >= array.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((T) array[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends MyArrayList.Itr {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public T previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] array = MyArrayList.this.array;
            if (i >= array.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (T) array[lastRet = i];
        }

        public void set(T e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                MyArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(T e) {
            checkForComodification();

            try {
                int i = cursor;
                MyArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        return (ListIterator<T>) new ListItr(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        if (index < 0 || index > arraySize)
            throw new IndexOutOfBoundsException("Index: " + index);
        return (ListIterator<T>) new ListItr(index);
    }

    @Override
    public Iterator<T> iterator() {
        return new MyArrayList.Itr();
    }

}
