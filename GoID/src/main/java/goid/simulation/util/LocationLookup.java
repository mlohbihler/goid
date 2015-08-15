package goid.simulation.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class LocationLookup<T> implements Iterable<T> {
    final Map<Integer, Map<Integer, T>> outer = new HashMap<>();

    public void put(int x, int y, T o) {
        Map<Integer, T> inner = outer.get(x);
        if (inner == null) {
            inner = new HashMap<>();
            outer.put(x, inner);
        }
        inner.put(y, o);
    }

    public T get(int x, int y) {
        Map<Integer, T> inner = outer.get(x);
        if (inner == null)
            return null;
        return inner.get(y);
    }

    public T remove(int x, int y) {
        Map<Integer, T> inner = outer.get(x);
        if (inner == null)
            return null;
        T o = inner.remove(y);
        if (inner.isEmpty())
            outer.remove(x);
        return o;
    }

    public int size() {
        int sum = 0;
        for (Map<Integer, T> inner : outer.values())
            sum += inner.size();
        return sum;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<Entry<Integer, Map<Integer, T>>> outerIter = outer.entrySet().iterator();
            private Iterator<Entry<Integer, T>> innerIter;
            private Map<Integer, T> currentInner;

            {
                nextOuter();
            }

            @Override
            public boolean hasNext() {
                if (innerIter != null && innerIter.hasNext())
                    return true;
                return nextOuter();
            }

            private boolean nextOuter() {
                if (outerIter.hasNext()) {
                    currentInner = outerIter.next().getValue();
                    innerIter = currentInner.entrySet().iterator();
                    return true;
                }
                return false;
            }

            @Override
            public T next() {
                return innerIter.next().getValue();
            }

            @Override
            public void remove() {
                innerIter.remove();
                if (currentInner.isEmpty())
                    outerIter.remove();
            }
        };
    }

    public static void main(String[] args) {
        LocationLookup<String> lu = new LocationLookup<>();
        for (String s : lu)
            System.out.println(s);

        lu.put(1, 3, "A");
        lu.put(0, 4, "B");
        lu.put(4, 4, "C");
        lu.put(4, 2, "D");
        lu.put(0, 2, "E");

        Iterator<String> iter = lu.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if ("D".equals(s))
                iter.remove();
        }

        for (String s : lu)
            System.out.println(s);
        System.out.println("--");

        iter = lu.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if ("C".equals(s))
                iter.remove();
        }

        for (String s : lu)
            System.out.println(s);
        System.out.println("--");

        iter = lu.iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }

        for (String s : lu)
            System.out.println(s);
        System.out.println("--");
    }
}
