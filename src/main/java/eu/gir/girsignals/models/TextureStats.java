package eu.gir.girsignals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.DefaultName;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class TextureStats {

    private boolean autoBlockstate;
    private List<String> has;
    private List<String> hasnot;
    private Map<String, String> with;
    private List<String> hasandis;
    private List<String> hasandisnot;
    private Map<String, String> retexture;
    private List<String> extentions;

    public boolean isautoBlockstate() {
        return autoBlockstate;
    }

    public List<String> getHas() {
        return has;
    }

    public List<String> getHasnot() {
        return hasnot;
    }

    public Map<String, String> getWith() {
        return with;
    }

    public void addExtention(final Map<String, String> map) {
        with.forEach((signalpart, enumvalue) -> {
            if (enumvalue.equalsIgnoreCase("prop")) {

            }
        });
        map.forEach((key, val) -> {
            with.put(key, val);
        });
    }

    public List<String> getHasandis() {
        return hasandis;
    }

    public List<String> getHasandisnot() {
        return hasandisnot;
    }

    public Map<String, String> getRetextures() {
        return retexture;
    }

    public void addRetexture(final Map<String, String> map) {
        map.forEach((key, val) -> {
            retexture.put(key, val);
        });
    }

    public List<String> getExtentions() {
        return extentions;
    }

    private static <T extends Enum<?>> Predicate<IExtendedBlockState> has(
            final IUnlistedProperty<T> property) {
        return ebs -> ebs.getValue(property) != null;
    }

    private static <T extends DefaultName<?>> Predicate<IExtendedBlockState> with(
            final IUnlistedProperty<T> property, final Predicate<T> t) {
        return bs -> {
            final T test = bs.getValue(property);
            return test != null && t.test(test);
        };
    }

    private static Predicate<IExtendedBlockState> hasAndIs(
            final IUnlistedProperty<Boolean> property) {
        return ebs -> {
            final Boolean bool = ebs.getValue(property);
            return bool != null && bool.booleanValue();
        };
    }

    @SuppressWarnings({
            "rawtypes", "unchecked", "unlikely-arg-type"
    })
    public Predicate<IExtendedBlockState> getPredicates(final String filename,
            final TextureStats texture) {
        final List<Predicate<IExtendedBlockState>> hasBlockstates = new ArrayList<>();
        final List<Predicate<IExtendedBlockState>> hasandisBlockstates = new ArrayList<>();
        final List<Predicate<IExtendedBlockState>> withBlockstates = new ArrayList<>();
        Predicate<IExtendedBlockState> state = null;
        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);
        for (int k = 0; k < signals.size(); k++) {
            if (filename.replace(".json", "").replace("signal", "")
                    .equalsIgnoreCase(signals.get(k).getSignalTypeName().replace("signal", ""))) {
                final List<IUnlistedProperty> properties = signals.get(k).getProperties();
                for (int i = 0; i < texture.getHas().size(); i++) {
                    if (properties != null) {
                        for (int j = 0; j < properties.size(); j++) {
                            if (texture.getHas().get(i).replace("[", "").replace("]", "")
                                    .equalsIgnoreCase(properties.get(j).toString()
                                            .replace("SEP[", "").replace("]", "")))
                                hasBlockstates.add(has(properties.get(j)));
                        }
                    }
                }
                for (int i = 0; i < texture.getHasandis().size(); i++) {
                    if (properties != null) {
                        for (int j = 0; j < properties.size(); j++) {
                            if (texture.getHasandis().get(i).replace("[", "").replace("]", "")
                                    .equalsIgnoreCase(properties.get(j).toString()
                                            .replace("SEP[", "").replace("]", "")))
                                hasandisBlockstates.add(hasAndIs(properties.get(j)));
                        }
                    }
                    if (texture.getHasandis().get(i).equalsIgnoreCase("CUSTOMNAME"))
                        hasandisBlockstates.add(hasAndIs(Signal.CUSTOMNAME));
                }
                for (Map.Entry<String, String> entry : texture.getWith().entrySet()) {
                    final String key = entry.getKey();
                    final String val = entry.getValue();
                    if (properties != null) {
                        for (int i = 0; i < properties.size(); i++) {
                            final IUnlistedProperty prop = properties.get(i);
                            if (key.equalsIgnoreCase(properties.get(i).toString()
                                    .replace("SEP[", "").replace("]", ""))) {
                                final SEProperty seprop = (SEProperty) prop;
                                final Enum defaultvalue = (Enum) seprop.getDefault();
                                withBlockstates.add(with(prop, obj -> obj.equals(
                                        Enum.valueOf(defaultvalue.getDeclaringClass(), val))));
                            }
                        }
                    }
                }
            }
            state = statesNullChecker(state, blockStateBuilder(hasBlockstates));
            state = statesNullChecker(state, blockStateBuilder(hasandisBlockstates));
            state = statesNullChecker(state, blockStateBuilder(withBlockstates));
        }
        return state;
    }

    private static Predicate<IExtendedBlockState> blockStateBuilder(
            final List<Predicate<IExtendedBlockState>> statelist) {
        Predicate<IExtendedBlockState> blockstate = null;
        if (statelist != null) {
            if (statelist.size() > 0) {
                blockstate = statelist.get(0);
                for (int i = 1; i < statelist.size(); i++) {
                    blockstate = blockstate.and(statelist.get(i));
                }
            }
        }
        return blockstate;
    }

    private static Predicate<IExtendedBlockState> statesNullChecker(
            final Predicate<IExtendedBlockState> state1,
            final Predicate<IExtendedBlockState> state2) {
        if (state1 == null)
            return state2;
        else if (state1 != null && state2 != null)
            return state1.and(state2);
        else
            return state1;
    }
}
