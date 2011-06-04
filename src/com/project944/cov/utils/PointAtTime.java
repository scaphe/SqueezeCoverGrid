package com.project944.cov.utils;

public class PointAtTime {
    public final int x;
    public final int y;
    public final long time;

    public PointAtTime(int x, int y) {
        this.x = x;
        this.y = y;
        this.time = System.nanoTime();
    }
}
