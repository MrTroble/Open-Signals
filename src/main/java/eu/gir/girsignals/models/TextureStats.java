package eu.gir.girsignals.models;

import java.util.List;
import java.util.Map;

public class TextureStats {

    private boolean autoBlockstate;
    private String blockstate;
    private Map<String, String> retexture;
    private List<String> extentions;

    public boolean isautoBlockstate() {
        return autoBlockstate;
    }

    public String getBlockstate() {
        return blockstate;
    }

    public Map<String, String> getRetextures() {
        return retexture;
    }

    public List<String> getExtentions() {
        return extentions;
    }
}
