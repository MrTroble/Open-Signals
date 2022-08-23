package eu.gir.girsignals.models;

import java.util.Map;

public class Models {

    private Map<String, TextureStats> textures;
    private float x;
    private float y;
    private float z;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Map<String, TextureStats> getTexture() {
        return textures;
    }
}
