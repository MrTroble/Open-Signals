package com.troblecodings.signals.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.util.ResourceLocation;

public class MapWrapper implements Map<ResourceLocation, IUnbakedModel> {

    private final Map<ResourceLocation, IUnbakedModel> map;
    private final Set<String> whitelist;

    public MapWrapper(final Map<ResourceLocation, IUnbakedModel> map, final Set<String> whitelist) {
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
    public IUnbakedModel get(final Object key) {
        return this.map.get(key);
    }

    @Override
    public IUnbakedModel put(final ResourceLocation key, final IUnbakedModel value) {
        if (!key.getNamespace().equalsIgnoreCase(OpenSignalsMain.MODID)
                || !whitelist.contains(key.getPath()))
            return this.map.put(key, value);
        return value;
    }

    public IUnbakedModel putNormal(final ResourceLocation key, final IUnbakedModel value) {
        return this.map.put(key, value);
    }

    @Override
    public IUnbakedModel remove(final Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(final Map<? extends ResourceLocation, ? extends IUnbakedModel> m) {
        final HashMap<? extends ResourceLocation, ? extends IUnbakedModel> map = new HashMap<>(m);
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
    public Collection<IUnbakedModel> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<ResourceLocation, IUnbakedModel>> entrySet() {
        return this.map.entrySet();
    }
}