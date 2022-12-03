package eu.gir.girsignals.models;

import java.util.Map;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

public class ModelExtention {

    private Map<String, String> extention;

    public ImmutableMap<String, String> getExtention() {

        if (extention == null)
            extention = new HashMap<>();
                 
        return ImmutableMap.copyOf(extention);
    }
}
