package eu.gir.girsignals.models;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ModelExtention {

    private Map<String, String> extention;

    public ImmutableMap<String, String> getExtention() {
        return ImmutableMap.copyOf(extention);
    }
}
