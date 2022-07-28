package eu.gir.girsignals.models;

import java.util.List;

public class Models extends ModelStats {

    private List<Texture> texture;
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

    public List<Texture> getTexture() {
        return texture;
    }
}
