package com.troblecodings.signals.models;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ModelExtention {

    private Map<String, String> extention;

    public ImmutableMap<String, String> getExtention() {

        if (extention == null)
            extention = new HashMap<>();

        return ImmutableMap.copyOf(extention);
    }
}
