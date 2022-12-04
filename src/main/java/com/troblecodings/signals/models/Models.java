package com.troblecodings.signals.models;

import java.util.List;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;

public class Models {

    private List<TextureStats> textures;
    private float x = 0;
    private float y = 0;
    private float z = 0;

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
