package org.devside.logviewer;

public class SequenceGenerator {

    private static final SequenceGenerator instance = new SequenceGenerator();

    private int count;

    private SequenceGenerator() {
        count = 0;
    }

    public static SequenceGenerator getInstance() {
        return instance;
    }

    public int next() {
        return count ++;
    }
}
