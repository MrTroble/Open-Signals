package com.troblecodings.signals.core;

import java.util.Map;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.enums.ChangedState;
import com.troblecodings.signals.handler.SignalStateInfo;

public interface SignalStateListener {

    public void update(final SignalStateInfo info, final Map<SEProperty, String> changedProperties,
            final ChangedState changedState);

    default SignalStateListener andThen(final SignalStateListener otherTask) {
        return (info, properties, state) -> {
            this.update(info, properties, state);
            otherTask.update(info, properties, state);
        };
    }
}