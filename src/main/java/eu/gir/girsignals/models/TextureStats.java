package eu.gir.girsignals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals;
import eu.gir.girsignals.EnumSignals.DefaultName;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class TextureStats {

    private List<String> has;
    private List<String> hasnot;
    private Map<String, String> with;
    private List<String> hasandis;
    private List<String> hasandisnot;
    private String retextures;

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

    public String getRetextures() {
        return retextures;
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
            "rawtypes", "unchecked"
    })
    public static Predicate<IExtendedBlockState> getPredicates(final String filename,
            final TextureStats texture) {
        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);
        signals.forEach(signal -> {
            if (filename.replace(".json", "")
                    .equalsIgnoreCase(signal.getSignalTypeName().replace("signal", ""))) {
                final List<IUnlistedProperty> properties = signal.getProperties();
                for (int i = 0; i < texture.getHas().size(); i++) {
                    for (int j = 0; j < properties.size(); j++) {
                        if (texture.getHas().get(i).equalsIgnoreCase(properties.get(j).toString()))
                            hasBlockstates.add(has(properties.get(j)));

                    }
                }
                for (int i = 0; i < texture.getHasandis().size(); i++) {
                    for (int j = 0; j < properties.size(); j++) {
                        if (texture.getHasandis().get(i)
                                .equalsIgnoreCase(properties.get(j).toString()))
                            hasandisBlockstates.add(hasAndIs(properties.get(j)));

                    }
                    if (texture.getHasandis().get(i).equalsIgnoreCase("CUSTOMNAME"))
                        hasandisBlockstates.add(hasAndIs(Signal.CUSTOMNAME));
                }
                for (int i = 0; i < texture.getHasnot().size(); i++) {
                    for (int j = 0; j < properties.size(); j++) {
                        if (texture.getHasnot().get(i)
                                .equalsIgnoreCase(properties.get(j).toString()))
                            hasnotBlockstates.add(hasNot(properties.get(j)));

                    }
                }
                for (int i = 0; i < texture.getHasandisnot().size(); i++) {
                    for (int j = 0; j < properties.size(); j++) {
                        if (texture.getHasandisnot().get(i)
                                .equalsIgnoreCase(properties.get(j).toString()))
                            hasandisnotBlockstates.add(hasAndIsNot(properties.get(j)));

                    }
                }
                for (Map.Entry<String, String> entry : texture.getWith().entrySet()) {
                    final String key = entry.getKey();
                    final String val = entry.getValue();
                    for (int i = 0; i < properties.size(); i++) {
                        final IUnlistedProperty prop = properties.get(i);
                        if (key.equalsIgnoreCase(properties.get(i).toString())) {
                            final SEProperty seprop = (SEProperty) prop;
                            final Enum defaultvalue = (Enum) seprop.getDefault();
                            final Enum signalenum = Enum.valueOf(defaultvalue.getDeclaringClass(),
                                    val);
                            withBlockstates.add(with(prop, obj -> obj.equals(signalenum)));
                        }
                    }
                }
                state = statesNullChecker(state, blockStateBuilder(hasBlockstates));
                state = statesNullChecker(state, blockStateBuilder(hasandisBlockstates));
                state = statesNullChecker(state, blockStateBuilder(hasnotBlockstates));
                state = statesNullChecker(state, blockStateBuilder(hasandisnotBlockstates));
                state = statesNullChecker(state, blockStateBuilder(withBlockstates));
            }
        });
        return state;
    }

    private static Predicate<IExtendedBlockState> blockstate;

    private static Predicate<IExtendedBlockState> blockStateBuilder(
            final List<Predicate<IExtendedBlockState>> statelist) {
        if (statelist.size() >= 0) {
            blockstate = statelist.get(0);
            for (int i = 1; i < statelist.size(); i++) {
                blockstate = blockstate.and(statelist.get(i));
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
