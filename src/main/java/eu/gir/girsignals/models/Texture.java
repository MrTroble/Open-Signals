package eu.gir.girsignals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.DefaultName;
import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class Texture extends Models {

    private List<String> has;
    private List<String> hasnot;
    private Map<String, String> with;
    private List<String> hasandis;
    private List<String> hasandisnot;
    private Map<String, String> retexture;

    public List<String> getHas() {
        return has;
    }

    public List<String> getHasnot() {
        return hasnot;
    }

    public Map<String, String> getWtih() {
        return with;
    }

    public List<String> getHasandis() {
        return hasandis;
    }

    public List<String> getHasandisnot() {
        return hasandisnot;
    }

    public Map<String, String> getRetexture() {
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
    private static List<Predicate<IExtendedBlockState>> withBLockstates;
    private static Predicate<IExtendedBlockState> state;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public static Predicate<IExtendedBlockState> getPredicates(final String filename,
            final Texture texture) {
        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);
        signals.forEach(signal -> {
            if (filename.equalsIgnoreCase(signal.getSignalTypeName())) {
                final List<IUnlistedProperty> properties = signal.getProperties();
                properties.forEach(property -> {
                    final String prop = property.toString();
                    for (int i = 0; i < texture.getHas().size(); i++) {
                        if (texture.getHas().get(i).equalsIgnoreCase(prop)) {
                            hasBlockstates.add(has(property));
                        }
                    }
                    for (int i = 0; i < texture.getHasandis().size(); i++) {
                        if (texture.getHasandis().get(i).equalsIgnoreCase(prop)) {
                            hasandisBlockstates.add(hasAndIs(property));
                        }
                    }
                    for (int i = 0; i < texture.getHasnot().size(); i++) {
                        if (texture.getHasnot().get(i).equalsIgnoreCase(prop)) {
                            hasnotBlockstates.add(hasNot(property));
                        }
                    }
                    for (int i = 0; i < texture.getHasandisnot().size(); i++) {
                        if (texture.getHasandisnot().get(i).equalsIgnoreCase(prop)) {
                            hasandisnotBlockstates.add(hasAndIsNot(property));
                        }
                    }
                });
                state = statesNullChecker(state, blockStateBuilder(hasBlockstates));
                state = statesNullChecker(state, blockStateBuilder(hasandisBlockstates));
                state = statesNullChecker(state, blockStateBuilder(hasnotBlockstates));
                state = statesNullChecker(state, blockStateBuilder(hasandisnotBlockstates));
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
            Predicate<IExtendedBlockState> state1, final Predicate<IExtendedBlockState> state2) {
        if (state1 == null) {
            return state1 = state2;
        } else {
            return state1 = state1.and(state2);
        }
    }
}
