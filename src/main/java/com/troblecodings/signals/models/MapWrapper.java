package com.troblecodings.signals.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class MapWrapper implements Map<ResourceLocation, UnbakedModel> {

    private final Map<ResourceLocation, UnbakedModel> map;
    private final Set<String> whitelist;

    public MapWrapper(final Map<ResourceLocation, UnbakedModel> map, final Set<String> whitelist) {
        this.map = map;
        this.whitelist = new HashSet<>(whitelist);
        this.whitelist.add("ghostblock");
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        final ResourceLocation loc = (ResourceLocation) key;
        if (loc.getNamespace().equalsIgnoreCase(OpenSignalsMain.MODID))
            return this.map.containsKey(key);
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public UnbakedModel get(final Object key) {
        return this.map.get(key);
    }

    @Override
    public UnbakedModel put(final ResourceLocation key, final UnbakedModel value) {
        if (!key.getNamespace().equalsIgnoreCase(OpenSignalsMain.MODID)
                || !whitelist.contains(key.getPath()))
            return this.map.put(key, value);
        return value;
    }

    public UnbakedModel putNormal(final ResourceLocation key, final UnbakedModel value) {
        return this.map.put(key, value);
    }

    @Override
    public UnbakedModel remove(final Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(final Map<? extends ResourceLocation, ? extends UnbakedModel> m) {
        final HashMap<? extends ResourceLocation, ? extends UnbakedModel> map = new HashMap<>(m);
        map.keySet().removeIf(loc -> loc.getNamespace().equalsIgnoreCase(OpenSignalsMain.MODID));
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<UnbakedModel> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<ResourceLocation, UnbakedModel>> entrySet() {
        return this.map.entrySet();
    }
}