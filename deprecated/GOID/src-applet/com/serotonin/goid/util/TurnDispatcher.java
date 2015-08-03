package com.serotonin.goid.util;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.util.StringUtils;

public class TurnDispatcher {
    private final int MICROS_PER_TURN = 10000;

    private final List<TurnListener> listeners = new ArrayList<TurnListener>();
    private final List<TurnListener> listenersToAdd = new ArrayList<TurnListener>();
    private final List<TurnListener> listenersToRemove = new ArrayList<TurnListener>();
    private long turn;

    public void addListener(TurnListener listener) {
        listenersToAdd.add(listener);
    }

    public void removeListener(TurnListener listener) {
        listenersToRemove.remove(listener);
    }

    public void next() {
        if (!listenersToAdd.isEmpty()) {
            listeners.addAll(listenersToAdd);
            listenersToAdd.clear();
        }

        for (TurnListener l : listeners)
            l.next(turn);
        turn++;

        if (!listenersToRemove.isEmpty()) {
            listeners.removeAll(listenersToRemove);
            listenersToRemove.clear();
        }
    }

    // public List<TurnListener> getListeners() {
    // return listeners;
    // }

    public long getTurn() {
        return turn;
    }

    public String getTime() {
        long time = turn * MICROS_PER_TURN / 1000;

        String millis = StringUtils.pad(Long.toString(time % 1000), '0', 3);
        time /= 1000;

        String sec = StringUtils.pad(Long.toString(time % 60), '0', 2);
        time /= 60;

        return "" + Long.toString(time) + ":" + sec + "." + millis;
    }

    public void execute(long turns) {
        while (turns > 0) {
            next();
            turns--;
        }
    }
}
