package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class Models {

    protected List<TextureStats> textures;
    protected float x = 0;
    protected float y = 0;
    protected float z = 0;

    public float getX(final float xOffset) {
        return x + xOffset;
    }

    public float getY(final float yOffset) {
        return y + yOffset;
    }

    public float getZ(final float zOffset) {
        return z + zOffset;
    }

    public ImmutableList<TextureStats> getTexture() {

        if (textures == null)
            textures = new ArrayList<>();

        return ImmutableList.copyOf(textures);
    }
}
