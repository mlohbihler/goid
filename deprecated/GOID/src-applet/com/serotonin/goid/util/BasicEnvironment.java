package com.serotonin.goid.util;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import com.serotonin.goid.util2d.ShapeIndex;

public class BasicEnvironment implements Environment {
    private final ShapeIndex<Shape> index = new ShapeIndex<Shape>();
    private final List<Renderable> renderables = new ArrayList<Renderable>();
    private final List<TurnListener> turnListeners = new ArrayList<TurnListener>();

    private final List<TurnListener> toBeAdded = new ArrayList<TurnListener>();
    private final List<TurnListener> toBeRemoved = new ArrayList<TurnListener>();

    public void add(Object o) {
        if (o instanceof Shape)
            index.put((Shape) o);
        if (o instanceof Renderable)
            renderables.add((Renderable) o);
        if (o instanceof TurnListener)
            toBeAdded.add((TurnListener) o);
    }

    public void remove(Object o) {
        if (o instanceof Shape)
            index.remove((Shape) o);
        if (o instanceof Renderable)
            renderables.remove(o);
        if (o instanceof TurnListener)
            toBeRemoved.add((TurnListener) o);
    }

    public void next(long turn) {
        turnListeners.addAll(toBeAdded);
        toBeAdded.clear();

        for (int i = 0; i < turnListeners.size(); i++)
            turnListeners.get(i).next(turn);

        turnListeners.removeAll(toBeRemoved);
        toBeRemoved.clear();
    }

    public ShapeIndex<Shape> getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return index.toString();
    }

    public void render(Graphics2D g) {
        for (int i = 0; i < renderables.size(); i++)
            renderables.get(i).render(g);
    }
}
