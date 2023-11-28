package com.troblecodings.signals.core;

import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;

@FunctionalInterface
public interface OSSupplier<T> {

    T get(final SignalState type);

}
