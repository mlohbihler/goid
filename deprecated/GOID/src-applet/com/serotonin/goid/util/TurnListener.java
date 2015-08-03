package com.serotonin.goid.util;

public interface TurnListener {
    /**
     * Do all required calculations for a single turn.
     */
    void next(long turn);
}
