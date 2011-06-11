package com.project944.cov.utils;

public interface MyProgressTracker extends MyLogger {

    void setMinMax(int min, int max);
    void setProgress(int value);
}
