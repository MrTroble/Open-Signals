package eu.gir.girsignals.models;

import java.util.Map;

public class Models extends ModelStats {
    
    private Map<String, Map<String, Texture>> texture;
    private int x;
    private int y;
    private int z;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    
    public Map<String, Map<String, Texture>> getTexture() {
        return texture;
    }
}
