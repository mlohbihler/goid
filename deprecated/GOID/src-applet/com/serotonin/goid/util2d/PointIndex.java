/*
    Copyright (C) 2006-2007 Serotonin Software Technologies Inc.
 	@author Matthew Lohbihler
 */
package com.serotonin.goid.util2d;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Matthew Lohbihler
 */
public class PointIndex<E extends Point2D> implements Iterable<E> {
    private final int cacheSize;
    private final IndexNode root;
    
    /**
     * A map of all index elements by their point objects. This allows the mutability of point objects external to the
     * index - as long as the index is eventually notified of changes using the put method. Having this map allows
     * the point to be located so that the object itself can be found in the index even if its location has changed.
     */
    private final Map<E, Point2D> elementMap = new IdentityHashMap<E, Point2D>();
    
    public PointIndex() {
        this(3);
    }
    
    public PointIndex(int cacheSize) {
        this.cacheSize = cacheSize;
        root = new IndexNode();
    }
    
    public void put(E p) {
        Point2D location = elementMap.get(p);
        if (location == null) {
            root.add(p);
            elementMap.put(p, new Point2D.Double(p.getX(), p.getY()));
        }
        else if (location.getX() == p.getX() && location.getY() == p.getY())
            ; // no op
        else {
            location = root.modify(p, location);
            if (location == null) {
                location = root.modify(p, elementMap.get(p));
                throw new RuntimeException("Failed to locate point to modify");
            }
            if (root.bounds == null || !root.bounds.contains(p))
                root.add(p);
            elementMap.put(p, new Point2D.Double(p.getX(), p.getY()));
        }
    }
    
    public boolean remove(E p) {
        Point2D location = elementMap.get(p);
        if (location != null && root.remove(p, location)) {
            elementMap.remove(p);
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return root.toString();
    }
    
    public List<E> findContained(Shape s) {
        List<E> result = new ArrayList<E>();
        if (root.elementCount > 0)
            root.findContained(s, result);
        return result;
    }
    
    public void findContained(Shape s, List<E> result) {
        if (root.elementCount > 0)
            root.findContained(s, result);
    }
    
    public void gatherAll(List<E> result) {
        if (root.elementCount > 0)
            root.gatherAll(result);
    }
    
    public int size() {
        return root.elementCount;
    }
    
    public Rectangle2D getBounds() {
        BoundingRectangle bounds = root.bounds;
        if (bounds == null)
            return new Rectangle2D.Double();
        return new Rectangle2D.Double(bounds.x, bounds.y, bounds.w, bounds.h);
    }
    
    public int getMaxDepth() {
        if (root.elementCount == 0)
            return 0;
        return root.getMaxDepth();
    }
    
    public Iterator<E> iterator() {
        return new IndexIterator<E>(root);
    }

    private class IndexNode {
        private static final int CHILD_TOP_LEFT = 0;
        private static final int CHILD_TOP_RIGHT = 1;
        private static final int CHILD_BOTTOM_LEFT = 2;
        private static final int CHILD_BOTTOM_RIGHT = 3;
        
        private int elementCount;
        private final BoundingRectangle bounds = new BoundingRectangle();
        
        // Array of E
        private final Object[] cache = new Object[cacheSize];
        // Array of IndexNode
        private final Object[] childNodes = new Object[4];
        
        void add(E e) {
            // Housekeeping tasks.
            elementCount++;
            
            // Update the bounds of this node as necessary.
            bounds.add(e);
            
            // Do the real add.
            addImpl(e);
        }
        
        private void addImpl(E e) {
            double midx = bounds.x + (((double)bounds.w) / 2);
            double midy = bounds.y + (((double)bounds.h) / 2);
            
            // Check if this should be the one of the priority shapes.
            e = addOrReplaceInCache(e, midx, midy);
            if (e == null)
                return;
            
            // Check if the point fits into an existing child node.
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null && ((IndexNode)childNodes[i]).bounds.contains(e)) {
                    ((IndexNode)childNodes[i]).add(e);
                    return;
                }
            }
            
            // We need to add the point to the child nodes. Decide which one it should go into.
            if (e.getX() < midx) {
                if (e.getY() < midy) {
                    if (childNodes[CHILD_TOP_LEFT] == null)
                        childNodes[CHILD_TOP_LEFT] = new IndexNode();
                    ((IndexNode)childNodes[CHILD_TOP_LEFT]).add(e);
                }
                else {
                    if (childNodes[CHILD_BOTTOM_LEFT] == null)
                        childNodes[CHILD_BOTTOM_LEFT] = new IndexNode();
                    ((IndexNode)childNodes[CHILD_BOTTOM_LEFT]).add(e);
                }
            }
            else {
                if (e.getY() < midy) {
                    if (childNodes[CHILD_TOP_RIGHT] == null)
                        childNodes[CHILD_TOP_RIGHT] = new IndexNode();
                    ((IndexNode)childNodes[CHILD_TOP_RIGHT]).add(e);
                }
                else {
                    if (childNodes[CHILD_BOTTOM_RIGHT] == null)
                        childNodes[CHILD_BOTTOM_RIGHT] = new IndexNode();
                    ((IndexNode)childNodes[CHILD_BOTTOM_RIGHT]).add(e);
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        private E addOrReplaceInCache(E e, double midx, double midy) {
            // Look for empty slots in the list.
            for (int i=0; i<cacheSize; i++) {
                if (cache[i] == null) {
                    cache[i] = e;
                    return null;
                }
            }
            
            // See if there is a cached item that is farther from the midpoint.
            int replaceIndex = -1;
            double distanceSq = (e.getX() - midx) * (e.getX() - midx) + (e.getY() - midy) * (e.getY() - midy);
            for (int i=0; i<cacheSize; i++) {
                E ce = (E)cache[i];
                double ds = (ce.getX() - midx) * (ce.getX() - midx) + (ce.getY() - midy) * (ce.getY() - midy);
                if (distanceSq < ds) {
                    replaceIndex = i;
                    distanceSq = ds;
                }
            }
            
            if (replaceIndex != -1) {
                // Found a point that can be replaced.
                E temp = (E)cache[replaceIndex];
                cache[replaceIndex] = e;
                return temp;
            }
            
            return e;
        }
        
        @SuppressWarnings("unchecked")
        void findContained(Shape s, List<E> result) {
            // Check if this node intersects at all with s.
            if (!s.intersects(bounds.x, bounds.y, bounds.w, bounds.h))
                return;
            
            // Test the cache shapes.
            for (int i=0; i<cacheSize; i++) {
                if (cache[i] != null) {
                    if (s.contains((Point2D)cache[i]))
                        result.add((E)cache[i]);
                }
            }
            
            // Test the children
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null)
                    ((IndexNode)childNodes[i]).findContained(s, result);
            }
        }
        
        boolean remove(E p, Point2D location) {
            // Check if this node contains p.
            if (!bounds.contains(location))
                return false;
            
            // Test the cache shapes.
            for (int i=0; i<cacheSize; i++) {
                if (cache[i] != null && p == cache[i]) {
                    // Remove the shape from the cache. If the child nodes are not null, find the largest of them
                    // and promote up from it.
                    E replacement = null;
                    int childIndex = largestChildIndex();
                    if (childIndex != -1) {
                        IndexNode child = (IndexNode)childNodes[childIndex];
                        replacement = child.findPromotion();
                        if (child.elementCount == 0)
                            childNodes[childIndex] = null;
                    }
                    cache[i] = replacement;
                    
                    elementCount--;
                    recalculateBounds();
                    return true;
                }
            }
            
            // If the shape wasn't found in the cache, check the child nodes.
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null && ((IndexNode)childNodes[i]).remove(p, location)) {
                    if (((IndexNode)childNodes[i]).elementCount == 0)
                        childNodes[i] = null;
                    elementCount--;
                    recalculateBounds();
                    return true;
                }
            }
            return false;
        }
        
        E modify(E p, Point2D oldLocation) {
            if (!bounds.contains(oldLocation))
                return null;
            
            // Check for the element in the cache.
            for (int i=0; i<cacheSize; i++) {
                if (cache[i] != null && p == cache[i]) {
                    // Found it in the cache. If the new bounds still fit in this node's bounds, just update the
                    // index element object.
                    if (bounds.contains(p))
                        recalculateBounds();
                    else
                        // Otherwise remove the element and return true.
                        remove(p, oldLocation);
                    
                    return p;
                }
            }
            
            // Check for the element in the child nodes
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] == null)
                    continue;
                
                IndexNode child = (IndexNode)childNodes[i];
                E e = child.modify(p, oldLocation);
                if (e == null)
                    continue;
                
                if (child.bounds != null && child.bounds.contains(p))
                    // The child kept the node. Just recalculate bounds
                    recalculateBounds();
                else {
                    // The child node removed the element.
                    if (child.elementCount == 0)
                        childNodes[i] = null;
                    
                    // If it fits into the bounds here then add it.
                    if (bounds.contains(p))
                        addImpl(e);
                    else {
                        // Otherwise return the element after we update some stuff
                        elementCount--;
                        recalculateBounds();
                    }
                }
                
                return e;
            }
            
            return null;
        }
        
        private int largestChildIndex() {
            int childIndex = -1;
            int maxSize = -1;
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null) {
                    if (childIndex == -1 || ((IndexNode)childNodes[i]).bounds.size() > maxSize) {
                        childIndex = i;
                        maxSize = ((IndexNode)childNodes[childIndex]).bounds.size();
                    }
                }
            }
            return childIndex;
        }
        
        @SuppressWarnings("unchecked")
        private E findPromotion() {
            int childIndex = largestChildIndex();
            
            E e = null;
            if (childIndex != -1) {
                // Found a suitable child node.
                IndexNode child = (IndexNode)childNodes[childIndex];
                e = child.findPromotion();
                if (child.elementCount == 0)
                    childNodes[childIndex] = null;
            }
            else {
                // Return one of the cache items.
                for (int i=cacheSize-1; i>=0; i--) {
                    if (cache[i] != null) {
                        e = (E)cache[i];
                        cache[i] = null;
                        break;
                    }
                }
            }
            
            elementCount--;
            recalculateBounds();
            
            return e;
        }
        
        @SuppressWarnings("unchecked")
        private void recalculateBounds() {
            bounds.reset();
            for (int i=0; i<cacheSize; i++) {
                if (cache[i] != null)
                    bounds.add((E)cache[i]);
            }
            
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null)
                    bounds.add(((IndexNode)childNodes[i]).bounds);
            }
        }
        
        @SuppressWarnings("unchecked")
        private void gatherAll(List<E> list) {
            for (int i=0; i<cacheSize; i++) {
                if (cache[i] != null)
                    list.add((E)cache[i]);
            }
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null)
                    ((IndexNode)childNodes[i]).gatherAll(list);
            }
        }
        
        int getMaxDepth() {
            int max = 0;
            for (int i=0; i<childNodes.length; i++) {
                if (childNodes[i] != null) {
                    int childDepth = ((IndexNode)childNodes[i]).getMaxDepth();
                    if (max < childDepth)
                        max = childDepth;
                }
            }
            return max + 1;
        }
        
        @Override
        public String toString() {
            String s = "Node(size="+ elementCount +", bounds="+ bounds;
            if (childNodes[CHILD_TOP_LEFT] != null)
                s += ", topLeft="+ childNodes[CHILD_TOP_LEFT];
            if (childNodes[CHILD_BOTTOM_LEFT] != null)
                s += ", bottomLeft="+ childNodes[CHILD_BOTTOM_LEFT];
            if (childNodes[CHILD_TOP_RIGHT] != null)
                s += ", topRight="+ childNodes[CHILD_TOP_RIGHT];
            if (childNodes[CHILD_BOTTOM_RIGHT] != null)
                s += ", bottomRight="+ childNodes[CHILD_BOTTOM_RIGHT];
            return s + ")";
        }
        
        class NodeIterator {
            private final NodeIterator parent;
            private int cacheIndex = -1;
            private int nodeIndex = -1;
            
            NodeIterator(NodeIterator parent) {
                this.parent = parent;
            }

            @SuppressWarnings("unchecked")
            E next() {
                return (E)cache[cacheIndex];
            }

            NodeIterator updateNext() {
                while (++cacheIndex < cacheSize) {
                    if (cache[cacheIndex] != null)
                        return this;
                }
                
                while (++nodeIndex < childNodes.length) {
                    if (childNodes[nodeIndex] != null)
                        return ((IndexNode)childNodes[nodeIndex]).new NodeIterator(this).updateNext();
                }
                
                if (parent == null)
                    return null;
                return parent.updateNext();
            }
        }
    }
    
    static class IndexIterator<E extends Point2D> implements Iterator<E> {
        private PointIndex<E>.IndexNode.NodeIterator node;
        
        IndexIterator(PointIndex<E>.IndexNode root) {
            node = root.new NodeIterator(null).updateNext();
        }
        
        public boolean hasNext() {
            return node != null;
        }

        public E next() {
            E next = node.next();
            node = node.updateNext();
            return next;
        }

        public void remove() {
            throw new RuntimeException("not implemented");
        }
    }
    
    
    
    
    
    
    public static void main(String[] args) throws Exception {
        List<Point2D> testData = generatePointData(1000000);
        List<Rectangle2D> testQueries = generateRectangleData(1000);
        
        test(1, testData, testQueries);
        test(2, testData, testQueries);
        test(3, testData, testQueries);
        test(4, testData, testQueries);
        test(1, testData, testQueries);
        test(2, testData, testQueries);
        test(3, testData, testQueries);
        test(4, testData, testQueries);
        test(1, testData, testQueries);
        test(2, testData, testQueries);
        test(3, testData, testQueries);
        test(4, testData, testQueries);
    }
    
    
    
    
    public static void test(int cacheSize, List<Point2D> testData, List<Rectangle2D> testQueries) throws Exception {
        System.out.println("Cache size: "+ cacheSize);
        
//        Map<Rectangle2D, List<Point2D>> queryResults = new HashMap<Rectangle2D, List<Point2D>>();
//        
//        // Spatial index analysis
//        PointIndex<Point2D> pointIndex = new PointIndex<Point2D>();
//        for (Point2D item : testData)
//            pointIndex.put(item);
//        System.out.println(pointIndex.getMaxDepth());
//        
//        // Modify the points randomly
//        Random random = new Random();
//        for (int i=0; i<testData.size(); i++) {
//            int index = random.nextInt(testData.size());
//            Point2D p = testData.get(index);
//            
//            double adj = random.nextDouble() * 1000 - 500;
//            if (random.nextBoolean())
//                p.setLocation(p.getX() + adj, p.getY());
//            else
//                p.setLocation(p.getX(), p.getY() + adj);
//            pointIndex.put(p);
//        }
//        
//        System.out.println(pointIndex.getMaxDepth());
//        
//        
//        // Sequential analysis.
//        for (Rectangle2D query : testQueries) {
//            List<Point2D> result = new ArrayList<Point2D>();
//            queryResults.put(query, result);
//            for (Point2D item : testData) {
//                if (query.contains(item))
//                    result.add(item);
//            }
//        }
//        
//        Map<Integer, Integer> resultMap = new TreeMap<Integer, Integer>();
//        for (List<Point2D> query : queryResults.values()) {
//            Integer resultCount = query.size();
//            Integer instances = resultMap.get(resultCount);
//            if (instances == null)
//                instances = 0;
//            instances++;
//            resultMap.put(resultCount, instances);
//        }
//        
//        for (Integer resultCount : resultMap.keySet())
//            System.out.println("Result count="+ resultCount +", instances="+ resultMap.get(resultCount));
//        
//        for (Rectangle2D query : queryResults.keySet()) {
//            List<Point2D> seqResult = queryResults.get(query);
//            List<Point2D> spaResult = pointIndex.findContained(query);
//            
//            if (seqResult.size() != spaResult.size())
//                System.out.println("Error in query "+ query +", result sizes are different");
//            else {
//                for (Point2D p : spaResult)
//                    seqResult.remove(p);
//                if (seqResult.size() != 0)
//                    System.out.println("Error in query "+ query +", result contents are different");
//            }
//        }
//        
//        System.out.println("Finished contained test");
//        
//        int size = 0;
//        while (pointIndex.size() > 0) {
//            size++;
//            List<Point2D> result = pointIndex.findContained(new Rectangle2D.Double(250-size, 250-size, size+size, size+size));
//            while (result.size() > 0)
//                pointIndex.remove(result.remove(0));
//        }
//        
//        System.out.println("Finished removal test");
        
        
        long start = System.currentTimeMillis();
        PointIndex<Point2D> pointIndex = new PointIndex<Point2D>(cacheSize);
        for (Point2D ts : testData)
            pointIndex.put(ts);
        System.out.println("Done adding shapes to index: size="+ pointIndex.size() +", time="+ (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        for (int j=0; j<100; j++) {
            for (Rectangle2D ts : testQueries)
                pointIndex.findContained(ts);
        }
        System.out.println("Done searching: "+ (System.currentTimeMillis() - start));
        
        // Modify the points randomly
        Random random = new Random();
        start = System.currentTimeMillis();
        for (int i=0; i<testData.size(); i++) {
            int index = random.nextInt(testData.size());
            Point2D p = testData.get(index);
            
            double adj = random.nextDouble() * 1000 - 500;
            if (random.nextBoolean())
                p.setLocation(p.getX() + adj, p.getY());
            else
                p.setLocation(p.getX(), p.getY() + adj);
            pointIndex.put(p);
        }
        System.out.println("Done modifying: "+ (System.currentTimeMillis() - start));
        
        // Delete some stuff
        start = System.currentTimeMillis();
        int size = 0;
        while (pointIndex.size() > 0) {
            size++;
            List<Point2D> result = pointIndex.findContained(
                    new Rectangle2D.Double(5000-size, 5000-size, size+size, size+size));
            while (result.size() > 0)
                pointIndex.remove(result.remove(0));
        }
        System.out.println("done deleting: "+ (System.currentTimeMillis() - start));
        System.out.println();
    }
    
    static List<Point2D> generatePointData(int count) throws Exception {
        Random random = new Random();
        List<Point2D> data = new ArrayList<Point2D>();
        for (int i=0; i<count; i++) {
            double x = random.nextDouble() * 10000 - 5000;
            double y = random.nextDouble() * 10000 - 5000;
            data.add(new Point2D.Double(x, y));
        }
        return data;
    }
    
    static List<Rectangle2D> generateRectangleData(int count) throws Exception {
        Random random = new Random();
        List<Rectangle2D> data = new ArrayList<Rectangle2D>();
        for (int i=0; i<count; i++) {
            double g = random.nextGaussian();
            g *= g;
            double w = (g * 100) + 1;
            
            g = random.nextGaussian();
            g *= g;
            double h = (g * 100) + 1;
            
            double x = random.nextDouble() * (10000 - w) - 5000;
            double y = random.nextDouble() * (10000 - h) - 5000;
            
            data.add(new Rectangle2D.Double(x, y, w, h));
        }
        return data;
    }
}
