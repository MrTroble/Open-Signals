package com.troblecodings.signals.core;

import com.troblecodings.signals.enums.ChangedState;

public interface NameStateListener {

    public void update(final StateInfo info, final String name, final ChangedState state);

}