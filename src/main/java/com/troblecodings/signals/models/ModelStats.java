package com.troblecodings.signals.models;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;

public class ModelStats {

    protected Map<String, String> textures;
    protected Map<String, Models> models;

    public ImmutableMap<String, Models> getModels() {

        if (models == null)
            models = new HashMap<>();

        return ImmutableMap.copyOf(models);
    }

    public static Map<String, Object> getfromJson(final String directory) {

        final Gson gson = new Gson();

        final Map<String, Object> content = new HashMap<>();

        OpenSignalsMain.contentPacks.getFiles(directory).forEach(entry -> {
            final String filename = entry.getKey();
            final String file = entry.getValue();
            if (!filename.endsWith("extention.json")) {

                final ModelStats json = gson.fromJson(file, ModelStats.class);
                content.put(filename, json);

            } else {
                final ModelExtention json = gson.fromJson(file, ModelExtention.class);
                content.put(filename, json);
            }
        });
        return content;
    }

    public Map<String, String> createRetexture(final Map<String, String> textureNames) {

        if (textures == null || textures.isEmpty()) {
            return textureNames;
        }

        final Map<String, String> retexturemap = new HashMap<>();

        if (textureNames != null) {
            for (final Map.Entry<String, String> entry : textureNames.entrySet()) {

                final String key = entry.getKey();
                final String textureVariable = entry.getValue();
                final String texture = textures.get(textureVariable);

                if (texture == null) {
                    retexturemap.put(key, textureVariable);
                } else {
                    retexturemap.put(key, texture);
                }
            }
        }
        return retexturemap;
    }
}