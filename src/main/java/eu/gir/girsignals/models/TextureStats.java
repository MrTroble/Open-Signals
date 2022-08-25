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

    public List<String> getHasandis() {
        return hasandis;
    }

    public List<String> getHasandisnot() {
        return hasandisnot;
    }

    public Map<String, String> getRetextures() {
        return retexture;
    }

    private static <T extends Enum<?>> Predicate<IExtendedBlockState> has(
            final IUnlistedProperty<T> property) {
        return ebs -> ebs.getValue(property) != null;
    }

    private static <T extends Enum<?>> Predicate<IExtendedBlockState> hasNot(
            final IUnlistedProperty<T> property) {
        return ebs -> ebs.getValue(property) == null;
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

    private static Predicate<IExtendedBlockState> hasAndIsNot(
            final IUnlistedProperty<Boolean> property) {
        return ebs -> {
            final Boolean bool = ebs.getValue(property);
            return bool != null && !bool.booleanValue();
        };
    }

    private static List<Predicate<IExtendedBlockState>> hasBlockstates;
    private static List<Predicate<IExtendedBlockState>> hasandisBlockstates;
    private static List<Predicate<IExtendedBlockState>> hasnotBlockstates;
    private static List<Predicate<IExtendedBlockState>> hasandisnotBlockstates;
    private static List<Predicate<IExtendedBlockState>> withBlockstates;
    private static Predicate<IExtendedBlockState> state = null;

    @SuppressWarnings({
            "rawtypes", "unchecked", "unlikely-arg-type"
    })
    public static Predicate<IExtendedBlockState> getPredicates(final String filename,
            final TextureStats texture) {
        hasBlockstates = new ArrayList<>();
        hasandisBlockstates = new ArrayList<>();
        hasnotBlockstates = new ArrayList<>();
        hasandisnotBlockstates = new ArrayList<>();
        withBlockstates = new ArrayList<>();
        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);
        signals.forEach(signal -> {
            if (filename.replace(".json", "").replace("signal", "")
                    .equalsIgnoreCase(signal.getSignalTypeName().replace("signal", ""))) {
                final List<IUnlistedProperty> properties = signal.getProperties();
                for (int i = 0; i < texture.getHas().size(); i++) {
                    if (properties != null) {
                        for (int j = 0; j < properties.size(); j++) {
                            if (texture.getHas().get(i).replace("[", "").replace("]", "")
                                    .equalsIgnoreCase(properties.get(j).toString()
                                            .replace("SEP[", "").replace("]", ""))) {
                                final Predicate<IExtendedBlockState> state = has(properties.get(j));
                                hasBlockstates.add(state);
                            }
                        }
                    }
                }
                for (int i = 0; i < texture.getHasandis().size(); i++) {
                    if (properties != null) {
                        for (int j = 0; j < properties.size(); j++) {
                            if (texture.getHasandis().get(i).replace("[", "").replace("]", "")
                                    .equalsIgnoreCase(properties.get(j).toString()
                                            .replace("SEP[", "").replace("]", ""))) {
                                final Predicate<IExtendedBlockState> state = hasAndIs(
                                        properties.get(j));
                                hasandisBlockstates.add(state);
                            }
                        }

                    }
                    if (texture.getHasandis().get(i).equalsIgnoreCase("CUSTOMNAME"))
                        hasandisBlockstates.add(hasAndIs(Signal.CUSTOMNAME));
                }
                for (int i = 0; i < texture.getHasnot().size(); i++) {
                    if (properties != null) {
                        for (int j = 0; j < properties.size(); j++) {
                            if (texture.getHasnot().get(i).replace("[", "").replace("]", "")
                                    .equalsIgnoreCase(properties.get(j).toString()
                                            .replace("SEP[", "").replace("]", ""))) {
                                final Predicate<IExtendedBlockState> state = hasNot(
                                        properties.get(j));
                                hasnotBlockstates.add(state);
                            }
                        }
                    }
                }
                for (int i = 0; i < texture.getHasandisnot().size(); i++) {
                    if (properties != null) {
                        for (int j = 0; j < properties.size(); j++) {
                            if (texture.getHasandisnot().get(i).replace("[", "").replace("]", "")
                                    .equalsIgnoreCase(properties.get(j).toString()
                                            .replace("SEP[", "").replace("]", ""))) {
                                final Predicate<IExtendedBlockState> state = hasAndIsNot(
                                        properties.get(j));
                                hasandisnotBlockstates.add(state);
                            }
                        }
                    }
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
                                final Enum signalenum = Enum
                                        .valueOf(defaultvalue.getDeclaringClass(), val);
                                withBlockstates.add(with(prop, obj -> obj.equals(signalenum)));
                            }
                        }
                    }
                }
                if (statesNullChecker(state, blockStateBuilder(hasBlockstates)) != null)
                    state = statesNullChecker(state, blockStateBuilder(hasBlockstates));
                if (statesNullChecker(state, blockStateBuilder(hasandisBlockstates)) != null)
                    state = statesNullChecker(state, blockStateBuilder(hasandisBlockstates));
                if (statesNullChecker(state, blockStateBuilder(hasnotBlockstates)) != null)
                    state = statesNullChecker(state, blockStateBuilder(hasnotBlockstates));
                if (statesNullChecker(state, blockStateBuilder(hasandisBlockstates)) != null)
                    state = statesNullChecker(state, blockStateBuilder(hasandisnotBlockstates));
                if (statesNullChecker(state, blockStateBuilder(withBlockstates)) != null)
                    state = statesNullChecker(state, blockStateBuilder(withBlockstates));
            }
        });
        return state;
    }

    private static Predicate<IExtendedBlockState> blockstate = null;

    private static Predicate<IExtendedBlockState> blockStateBuilder(
            final List<Predicate<IExtendedBlockState>> statelist) {
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
        else
            return state1.and(state2);

    }

}
