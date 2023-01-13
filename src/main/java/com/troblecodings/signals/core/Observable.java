package com.troblecodings.signals.core;

public interface Observable {

    public void addListener(final Observer observer);

    public void removeListener(final Observer observer);

}