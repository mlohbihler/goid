/*
    Copyright (C) 2006-2007 Serotonin Software Technologies Inc.
 	@author Matthew Lohbihler
 */
package com.serotonin.goid.util2d;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author Matthew Lohbihler
 */
public class ShapeIndex<E extends Shape> implements Iterable<E> {
    private final int cacheSize;
    private final IndexNode root;

    /**
     * A map of all index elements by their shape objects. This allows the mutability of shape objects external to the
     * index - as long as the index is eventually notified of changes using the put method. Having this map allows the
     * old bounds of the shape to be located so that the object itself can be found in the index even if its bounds have
     * changed.
     */
    private final Map<E, IndexElement> elementMap = new IdentityHashMap<E, IndexElement>();

    public ShapeIndex() {
        this(3);
    }

    public ShapeIndex(int cacheSize) {
        this.cacheSize = cacheSize;
        root = new IndexNode();
    }

    public void put(E s) {
        IndexElement e = elementMap.get(s);
        if (e == null) {
            e = new IndexElement(s);
            root.add(e);
            elementMap.put(s, e);
        }
        else {
            e = root.modify(s, e.bounds);
            if (e == null) {
                e = root.modify(s, elementMap.get(s).bounds);
                throw new RuntimeException("Failed to locate shape to modify");
            }
            if (root.bounds == null || !root.bounds.contains(e.bounds))
                root.add(e);
        }

        // root.checkBounds();
    }

    public boolean remove(E s) {
        IndexElement e = elementMap.get(s);
        if (e != null && root.remove(s, e.bounds)) {
            elementMap.remove(s);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public List<E> findIntersecting(Shape s) {
        List<E> result = new ArrayList<E>();
        if (root.size > 0)
            root.findIntersecting(s, null, result);
        return result;
    }

    public void findIntersecting(Shape s, List<E> result) {
        if (root.size > 0)
            root.findIntersecting(s, null, result);
    }

    public List<Area> findIntersections(Shape s) {
        List<Area> result = new ArrayList<Area>();
        if (root.size > 0)
            root.findIntersections(s, null, result);
        return result;
    }

    public void findIntersections(Shape s, List<Area> result) {
        if (root.size > 0)
            root.findIntersections(s, null, result);
    }

    public List<E> findContainers(double x, double y) {
        List<E> result = new ArrayList<E>();
        if (root.size > 0)
            root.findContainers(x, y, result);
        return result;
    }

    public List<E> findContained(Shape s) {
        List<E> result = new ArrayList<E>();
        if (root.size > 0)
            root.findContained(new Area(s), result);
        return result;
    }

    public void gatherAll(List<E> result) {
        if (root.size > 0)
            root.gatherAll(result);
    }

    public int size() {
        return root.size;
    }

    public Rectangle2D getBounds() {
        BoundingRectangle bounds = root.bounds;
        if (bounds == null)
            return new Rectangle2D.Double();
        return new Rectangle2D.Double(bounds.x, bounds.y, bounds.w, bounds.h);
    }

    public int getMaxDepth() {
        if (root.size == 0)
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

        private int size;
        private final BoundingRectangle bounds = new BoundingRectangle();

        // Array of IndexElement
        private final Object[] cache = new Object[cacheSize];
        // Array of IndexNode
        private final Object[] childNodes = new Object[4];

        void add(IndexElement e) {
            // Housekeeping tasks.
            size++;

            // Update the bounds of this node as necessary.
            bounds.add(e.bounds);

            // Do the real add.
            addImpl(e);
        }

        void checkBounds() {
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null && !bounds.contains(((IndexElement) cache[i]).bounds))
                    throw new RuntimeException("bounds failure");
            }

            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null) {
                    IndexNode n = (IndexNode) childNodes[i];
                    n.checkBounds();
                    if (!bounds.contains(n.bounds))
                        throw new RuntimeException("bounds failure");
                }
            }
        }

        private void addImpl(IndexElement e) {
            // Check if this should be the one of the priority shapes.
            e = addOrReplaceInCache(e);
            if (e == null)
                return;

            // We need to add the shape to the child nodes. Decide which one it should go into.
            double midx = bounds.x + (((double) bounds.w) / 2);
            double midxs = e.bounds.x + (e.bounds.width / 2);
            double midy = bounds.y + (((double) bounds.h) / 2);
            double midys = e.bounds.y + (e.bounds.height / 2);

            if (midxs < midx) {
                if (midys < midy) {
                    if (childNodes[CHILD_TOP_LEFT] == null)
                        childNodes[CHILD_TOP_LEFT] = new IndexNode();
                    ((IndexNode) childNodes[CHILD_TOP_LEFT]).add(e);
                }
                else {
                    if (childNodes[CHILD_BOTTOM_LEFT] == null)
                        childNodes[CHILD_BOTTOM_LEFT] = new IndexNode();
                    ((IndexNode) childNodes[CHILD_BOTTOM_LEFT]).add(e);
                }
            }
            else {
                if (midys < midy) {
                    if (childNodes[CHILD_TOP_RIGHT] == null)
                        childNodes[CHILD_TOP_RIGHT] = new IndexNode();
                    ((IndexNode) childNodes[CHILD_TOP_RIGHT]).add(e);
                }
                else {
                    if (childNodes[CHILD_BOTTOM_RIGHT] == null)
                        childNodes[CHILD_BOTTOM_RIGHT] = new IndexNode();
                    ((IndexNode) childNodes[CHILD_BOTTOM_RIGHT]).add(e);
                }
            }
        }

        private IndexElement addOrReplaceInCache(IndexElement e) {
            // Look for empty slots in the list.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] == null) {
                    cache[i] = e;
                    return null;
                }
            }

            // See if there is a cached item that is smaller.
            int replaceIndex = -1;
            double replaceSize = Double.MAX_VALUE;
            for (int i = 0; i < cacheSize; i++) {
                if (replaceIndex == -1 || ((IndexElement) cache[i]).size < replaceSize) {
                    replaceIndex = i;
                    replaceSize = ((IndexElement) cache[i]).size;
                }
            }

            if (replaceSize < e.size) {
                // Found a shape that can be replaced.
                IndexElement temp = (IndexElement) cache[replaceIndex];
                cache[replaceIndex] = e;
                return temp;
            }

            return e;
        }

        void findIntersecting(Shape s, Area a, List<E> result) {
            // Check if this node intersects at all with s.
            if (!s.intersects(bounds.x, bounds.y, bounds.w, bounds.h))
                return;

            // Test the cache shapes.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null) {
                    if (!s.intersects(((IndexElement) cache[i]).bounds))
                        continue;

                    if (a == null)
                        a = new Area(s);

                    Area ea = new Area(((IndexElement) cache[i]).s);
                    ea.intersect(a);
                    if (!ea.isEmpty())
                        result.add(((IndexElement) cache[i]).s);
                }
            }

            // Test the children
            for (Object child : childNodes) {
                if (child != null)
                    ((IndexNode) child).findIntersecting(s, a, result);
            }
        }

        void findIntersections(Shape s, Area a, List<Area> result) {
            // Check if this node intersects at all with s.
            if (!s.intersects(bounds.x, bounds.y, bounds.w, bounds.h))
                return;

            // Test the cache shapes.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null) {
                    if (!s.intersects(((IndexElement) cache[i]).bounds))
                        continue;

                    if (a == null)
                        a = new Area(s);

                    Area ea = new Area(((IndexElement) cache[i]).s);
                    ea.intersect(a);
                    if (!ea.isEmpty())
                        result.add(ea);
                }
            }

            // Test the children
            for (Object child : childNodes) {
                if (child != null)
                    ((IndexNode) child).findIntersections(s, a, result);
            }
        }

        void findContainers(double x, double y, List<E> result) {
            // Check if this point is in the bounds at all.
            if (!bounds.contains(x, y))
                return;

            // Test the cache shapes.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null && ((IndexElement) cache[i]).bounds.contains(x, y)
                        && ((IndexElement) cache[i]).s.contains(x, y))
                    result.add(((IndexElement) cache[i]).s);
            }

            // Test the children
            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null)
                    ((IndexNode) childNodes[i]).findContainers(x, y, result);
            }
        }

        void findContained(Area s, List<E> result) {
            // Check if this node intersects at all with s.
            if (!s.intersects(bounds.x, bounds.y, bounds.w, bounds.h))
                return;

            // Test the cache shapes.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null) {
                    Area a = new Area(((IndexElement) cache[i]).s);
                    a.subtract(s);
                    if (a.isEmpty())
                        result.add(((IndexElement) cache[i]).s);
                }
            }

            // Test the children
            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null)
                    ((IndexNode) childNodes[i]).findContained(s, result);
            }
        }

        boolean remove(Shape s, Rectangle2D.Double oldBounds) {
            // Check if this node intersects at all with s.
            if (!bounds.contains(oldBounds))
                return false;

            // Test the cache shapes.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null && s == ((IndexElement) cache[i]).s) {
                    // Remove the shape from the cache. If the child nodes are not null, find the largest entry
                    // among them and promote it up.
                    IndexElement replacement = null;
                    int childIndex = -1;
                    for (int j = 0; j < childNodes.length; j++) {
                        if (childNodes[j] != null) {
                            IndexElement bigger = ((IndexNode) childNodes[j]).getLargerThan(replacement);
                            if (bigger != null) {
                                replacement = bigger;
                                childIndex = j;
                            }
                        }
                    }

                    // Remove the replacement from the child node.
                    cache[i] = replacement;
                    if (replacement != null) {
                        if (((IndexNode) childNodes[childIndex]).size == 1)
                            childNodes[childIndex] = null;
                        else
                            ((IndexNode) childNodes[childIndex]).remove(replacement.s, replacement.bounds);
                    }

                    // We may need to update the bounds. Only worry about it if one or more of the edges of the
                    // shape match the edges of the bounds.
                    // Rectangle2D sBounds = s.getBounds2D();
                    // if (sBounds.getX() == bounds.x || sBounds.getY() == bounds.y ||
                    // sBounds.getX() + sBounds.getWidth() == bounds.x + bounds.width ||
                    // sBounds.getY() + sBounds.getHeight() == bounds.y + bounds.height) {
                    recalculateBounds();
                    // }

                    size--;
                    return true;
                }
            }

            // If the shape wasn't found in the cache, check the child nodes.
            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null && ((IndexNode) childNodes[i]).remove(s, oldBounds)) {
                    if (((IndexNode) childNodes[i]).size == 0)
                        childNodes[i] = null;
                    size--;
                    recalculateBounds();
                    return true;
                }
            }
            return false;
        }

        IndexElement modify(E s, Rectangle2D.Double oldBounds) {
            if (!bounds.contains(oldBounds))
                return null;

            // Check for the element in the cache.
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null && s == ((IndexElement) cache[i]).s) {
                    IndexElement e = (IndexElement) cache[i];
                    e.update();

                    // Found it in the cache. If the new bounds still fit in this node's bounds, just update the
                    // index element object.
                    if (bounds.contains(e.bounds))
                        recalculateBounds();
                    else
                        // Otherwise remove the element and return true.
                        remove(s, oldBounds);

                    return e;
                }
            }

            // Check for the element in the child nodes
            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] == null)
                    continue;

                IndexNode child = (IndexNode) childNodes[i];
                IndexElement e = child.modify(s, oldBounds);
                if (e == null)
                    continue;

                if (child.bounds != null && child.bounds.contains(e.bounds))
                    // The child kept the node. Just recalculate bounds
                    recalculateBounds();
                else {
                    // The child node removed the element.
                    if (child.size == 0)
                        childNodes[i] = null;

                    // If it fits into the bounds here then add it.
                    if (bounds.contains(e.bounds))
                        addImpl(e);
                    else {
                        // Otherwise return the element after we update some stuff
                        size--;
                        recalculateBounds();
                    }
                }

                return e;
            }

            return null;
        }

        private IndexElement getLargerThan(IndexElement e) {
            IndexElement maxElement = null;

            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null) {
                    if ((maxElement == null || ((IndexElement) cache[i]).size > maxElement.size)
                            && (e == null || ((IndexElement) cache[i]).size > e.size))
                        maxElement = (IndexElement) cache[i];
                }
            }

            return maxElement;
        }

        private void recalculateBounds() {
            bounds.reset();
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null)
                    bounds.add(((IndexElement) cache[i]).bounds);
            }

            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null)
                    bounds.add(((IndexNode) childNodes[i]).bounds);
            }
        }

        @SuppressWarnings("unchecked")
        private void gatherAll(List<E> list) {
            for (int i = 0; i < cacheSize; i++) {
                if (cache[i] != null)
                    list.add((E) cache[i]);
            }
            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null)
                    ((IndexNode) childNodes[i]).gatherAll(list);
            }
        }

        int getMaxDepth() {
            int max = 0;
            for (int i = 0; i < childNodes.length; i++) {
                if (childNodes[i] != null) {
                    int childDepth = ((IndexNode) childNodes[i]).getMaxDepth();
                    if (max < childDepth)
                        max = childDepth;
                }
            }
            return max + 1;
        }

        @Override
        public String toString() {
            String s = "Node(size=" + size + ", bounds=" + bounds;
            if (childNodes[CHILD_TOP_LEFT] != null)
                s += ", topLeft=" + childNodes[CHILD_TOP_LEFT];
            if (childNodes[CHILD_BOTTOM_LEFT] != null)
                s += ", bottomLeft=" + childNodes[CHILD_BOTTOM_LEFT];
            if (childNodes[CHILD_TOP_RIGHT] != null)
                s += ", topRight=" + childNodes[CHILD_TOP_RIGHT];
            if (childNodes[CHILD_BOTTOM_RIGHT] != null)
                s += ", bottomRight=" + childNodes[CHILD_BOTTOM_RIGHT];
            return s + ")";
        }

        class NodeIterator {
            private final NodeIterator parent;
            private int cacheIndex = -1;
            private int nodeIndex = -1;

            NodeIterator(NodeIterator parent) {
                this.parent = parent;
            }

            E next() {
                return ((IndexElement) cache[cacheIndex]).s;
            }

            NodeIterator updateNext() {
                while (++cacheIndex < cacheSize) {
                    if (cache[cacheIndex] != null)
                        return this;
                }

                while (++nodeIndex < childNodes.length) {
                    if (childNodes[nodeIndex] != null)
                        return ((IndexNode) childNodes[nodeIndex]).new NodeIterator(this).updateNext();
                }

                if (parent == null)
                    return null;
                return parent.updateNext();
            }
        }
    }

    private class IndexElement {
        E s;
        Rectangle2D.Double bounds;
        double size;

        public IndexElement(E s) {
            this.s = s;
            update();
        }

        public void update() {
            Rectangle2D sbounds = s.getBounds2D();
            bounds = new Rectangle2D.Double(sbounds.getX(), sbounds.getY(), sbounds.getWidth(), sbounds.getHeight());
            size = bounds.getWidth() * bounds.getHeight();
        }
    }

    static class IndexIterator<E extends Shape> implements Iterator<E> {
        private ShapeIndex<E>.IndexNode.NodeIterator node;

        IndexIterator(ShapeIndex<E>.IndexNode root) {
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

    // public static void main(String[] args) throws Exception {
    //        
    //        
    // Ellipse2D e1 = new Ellipse2D.Double(0,0,10,10);
    // Ellipse2D e2 = new Ellipse2D.Double(7,7,10,10);
    //        
    // System.out.println(e1.getBounds2D().intersects(e2.getBounds2D()));
    // System.out.println(e1.intersects(e2.getBounds2D()));
    //        
    // Area a = new Area(e1);
    // a.intersect(new Area(e2));
    // System.out.println(!a.isEmpty());
    // }

    public static void main(String[] args) throws Exception {
        // SpatialIndex2D<Rectangle2D> si = new SpatialIndex2D<Rectangle2D>();
        // si.put(new Rectangle2D.Double(0,0,4,4));
        // si.put(new Rectangle2D.Double(5,0,4,4));
        // si.put(new Rectangle2D.Double(0,5,4,4));
        // si.put(new Rectangle2D.Double(5,5,4,4));
        // System.out.println(si);
        //        
        // Rectangle2D r = new Rectangle2D.Double(1,1,2,2);
        // si.put(r);
        // System.out.println(si);
        // r.setRect(6,6,2,2);
        // si.put(r);
        // System.out.println(si);
        // r.setRect(1,6,2,2);
        // si.put(r);
        // System.out.println(si);
        // r.setRect(6,1,2,2);
        // si.put(r);
        // System.out.println(si);

        List<Rectangle2D> testData = generateData(50000);
        List<Rectangle2D> testQueries = generateData(1000);
        Map<Rectangle2D, List<Rectangle2D>> queryResults = new HashMap<Rectangle2D, List<Rectangle2D>>();

        for (Rectangle2D query : testQueries) {
            List<Rectangle2D> result = new ArrayList<Rectangle2D>();
            for (Rectangle2D item : testData) {
                if (query.intersects(item))
                    result.add(item);
            }
        }

        // Spatial index analysis
        ShapeIndex<Rectangle2D> spatialIndex = new ShapeIndex<Rectangle2D>();
        for (Rectangle2D item : testData)
            spatialIndex.put(item);
        System.out.println(spatialIndex.getMaxDepth());

        // Modify the rectangles randomly
        Random random = new Random();
        for (int i = 0; i < testData.size() * 10; i++) {
            int index = random.nextInt(testData.size());
            Rectangle2D r = testData.get(index);

            int aspect = random.nextInt(4);
            int adj = random.nextInt(1000) - 500;
            if (aspect == 0)
                r.setRect(r.getX() + adj, r.getY(), r.getWidth(), r.getHeight());
            else if (aspect == 1)
                r.setRect(r.getX(), r.getY() + adj, r.getWidth(), r.getHeight());
            else if (aspect == 2) {
                r.setRect(r.getX(), r.getY(), r.getWidth() + adj, r.getHeight());
                if (r.getWidth() < 1)
                    r.setRect(r.getX(), r.getY(), 1, r.getHeight());
            }
            else if (aspect == 3) {
                r.setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight() + adj);
                if (r.getHeight() < 1)
                    r.setRect(r.getX(), r.getY(), r.getWidth(), 1);
            }

            spatialIndex.put(r);
        }

        System.out.println(spatialIndex.getMaxDepth());

        // Sequential analysis.
        for (Rectangle2D query : testQueries) {
            List<Rectangle2D> result = new ArrayList<Rectangle2D>();
            queryResults.put(query, result);
            for (Rectangle2D item : testData) {
                if (query.intersects(item))
                    result.add(item);
            }
        }

        Map<Integer, Integer> resultMap = new TreeMap<Integer, Integer>();
        for (List<Rectangle2D> query : queryResults.values()) {
            Integer resultCount = query.size();
            Integer instances = resultMap.get(resultCount);
            if (instances == null)
                instances = 0;
            instances++;
            resultMap.put(resultCount, instances);
        }

        for (Integer resultCount : resultMap.keySet())
            System.out.println("Result count=" + resultCount + ", instances=" + resultMap.get(resultCount));

        for (Rectangle2D query : queryResults.keySet()) {
            List<Rectangle2D> seqResult = queryResults.get(query);
            List<Rectangle2D> spaResult = spatialIndex.findIntersecting(query);

            if (seqResult.size() != spaResult.size())
                System.out.println("Error in query " + query + ", result sizes are different");
            else {
                for (Shape s : spaResult)
                    seqResult.remove(s);
                if (seqResult.size() != 0)
                    System.out.println("Error in query " + query + ", result contents are different");
            }
        }

        System.out.println("Finished intersection test");

        queryResults.clear();
        for (Rectangle2D query : testQueries) {
            List<Rectangle2D> result = new ArrayList<Rectangle2D>();
            queryResults.put(query, result);
            for (Rectangle2D item : testData) {
                if (query.contains(item))
                    result.add(item);
            }
        }

        resultMap = new TreeMap<Integer, Integer>();
        for (List<Rectangle2D> query : queryResults.values()) {
            Integer resultCount = query.size();
            Integer instances = resultMap.get(resultCount);
            if (instances == null)
                instances = 0;
            instances++;
            resultMap.put(resultCount, instances);
        }

        for (Integer resultCount : resultMap.keySet())
            System.out.println("Result count=" + resultCount + ", instances=" + resultMap.get(resultCount));

        // Spatial index analysis
        spatialIndex = new ShapeIndex<Rectangle2D>();
        for (Rectangle2D item : testData)
            spatialIndex.put(item);

        for (Rectangle2D query : queryResults.keySet()) {
            List<Rectangle2D> seqResult = queryResults.get(query);
            List<Rectangle2D> spaResult = spatialIndex.findContained(query);

            if (seqResult.size() != spaResult.size())
                System.out.println("Error in query " + query + ", result sizes are different");
            else {
                for (Shape s : spaResult)
                    seqResult.remove(s);
                if (seqResult.size() != 0)
                    System.out.println("Error in query " + query + ", result contents are different");
            }
        }

        System.out.println("Finished contained test");

        int size = 0;
        while (spatialIndex.size() > 0) {
            size++;
            List<Rectangle2D> result = spatialIndex.findIntersecting(new Rectangle2D.Double(250 - size, 250 - size,
                    size + size, size + size));
            while (result.size() > 0)
                spatialIndex.remove(result.remove(0));
        }

        System.out.println("Finished removal test");

        // List<Rectangle2D> testData = generateData(500000);
        // List<Rectangle2D> testQueries = generateData(1000);
        //        
        // long start = System.currentTimeMillis();
        // SpatialIndex2D<Rectangle2D> spatialIndex = new SpatialIndex2D<Rectangle2D>();
        // for (Rectangle2D ts : testData)
        // spatialIndex.put(ts);
        // System.out.println("Done adding shapes to index: size="+ spatialIndex.size() +", time="+
        // (System.currentTimeMillis() - start));
        //        
        // start = System.currentTimeMillis();
        // for (int j=0; j<100; j++) {
        // for (Rectangle2D ts : testQueries)
        // spatialIndex.findIntersecting(ts);
        // }
        // System.out.println("Done searching: "+ (System.currentTimeMillis() - start));
        //        
        // //spatialIndex.root.dumpStats(0);
        //        
        // // start = System.currentTimeMillis();
        // // spatialIndex.root.rebalance();
        // // System.out.println("Done rebalancing: "+ (System.currentTimeMillis() - start));
        // //
        // // start = System.currentTimeMillis();
        // // for (int j=0; j<30; j++) {
        // // for (int i=1; i<10000; i++)
        // // spatialIndex.findIntersecting(new Rectangle(2000+i, 2000+i, 10, 10));
        // // }
        // // System.out.println("Done searching 1: "+ (System.currentTimeMillis() - start));
        // // start = System.currentTimeMillis();
        // // for (int j=0; j<30; j++) {
        // // for (int i=1; i<10000; i++)
        // // spatialIndex.findIntersecting(new Rectangle(2000+i, 2000+i, 40, 40));
        // // }
        // // System.out.println("Done searching 2: "+ (System.currentTimeMillis() - start));
        // // start = System.currentTimeMillis();
        // // for (int j=0; j<30; j++) {
        // // for (int i=1; i<10000; i++)
        // // spatialIndex.findIntersecting(new Rectangle(2000+i, 2000+i, 100, 100));
        // // }
        // // System.out.println("Done searching 3: "+ (System.currentTimeMillis() - start));
        //        
        // // spatialIndex.root.dumpStats(0);
        //        
        // // Delete some stuff
        // start = System.currentTimeMillis();
        // int size = 0;
        // while (spatialIndex.size() > 0) {
        // size++;
        // List<Rectangle2D> result = spatialIndex.findIntersecting(
        // new Rectangle2D.Double(250-size, 250-size, size+size, size+size));
        // while (result.size() > 0)
        // spatialIndex.remove(result.remove(0));
        // }
        // System.out.println("done deleting: "+ (System.currentTimeMillis() - start));
        //        
        //        
        // System.out.println();

    }

    static List<Rectangle2D> generateData(int count) throws Exception {
        Random random = new Random();
        List<Rectangle2D> data = new ArrayList<Rectangle2D>();
        for (int i = 0; i < count; i++) {
            double g = random.nextGaussian();
            g *= g;
            int w = (int) (g * 100) + 1;

            g = random.nextGaussian();
            g *= g;
            int h = (int) (g * 100) + 1;

            int x = random.nextInt(10000 - w);
            int y = random.nextInt(10000 - h);

            data.add(new Rectangle2D.Double(x, y, w, h));
        }
        return data;
    }
}
