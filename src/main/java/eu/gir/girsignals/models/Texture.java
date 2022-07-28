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

    private List<Predicate<IExtendedBlockState>> hasBlockstates;

    private List<Predicate<IExtendedBlockState>> hasandisBlockstates;

    private Predicate<IExtendedBlockState> states;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public Predicate<IExtendedBlockState> getPredicates() {
        this.hasBlockstates = new ArrayList<>();
        final ModelStateBuilder state = new ModelStateBuilder();
        final Map<String, ModelStats> content = state
                .getfromJson("/assets/girsignals/modeldefinitions");
        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);
        content.forEach((filename, values) -> {
            final String name = filename.replace(".json", "");
            signals.forEach(signal -> {
                final String signalsystem = signal.getSignalTypeName();
                if (name.equalsIgnoreCase(signalsystem)) {
                    final List<IUnlistedProperty> properties = signal.getProperties();
                    final Map<String, Models> models = values.getModels();
                    models.forEach((modelname, model) -> {
                        model.getTexture().forEach((test1) -> {
                            properties.forEach(property -> {
                                final String prop = property.toString();
                                if (!test1.getHas().isEmpty()) {
                                    final String has0 = test1.getHas().get(0);
                                    if (prop.equalsIgnoreCase(has0)) {
                                        this.hasBlockstates.add(has(property));
                                    }
                                }
                                if (test1.getHas().size() >= 1) {
                                    final String has1 = test1.getHas().get(1);
                                    if (prop.equalsIgnoreCase(has1)) {
                                        this.hasBlockstates.add(has(property));
                                    }
                                }
                                if (test1.getHas().size() >= 2) {
                                    final String has2 = test1.getHas().get(2);
                                    if (prop.equalsIgnoreCase(has2)) {
                                        this.hasBlockstates.add(has(property));
                                    }
                                }
                                if (test1.getHasandis().size() >= 0) {
                                    final String hasandis0 = test1.getHasandis().get(0);
                                    if (prop.equalsIgnoreCase(hasandis0)) {
                                        this.hasBlockstates.add(has(property));
                                    }
                                }
                            });
                        });
                    });
                }
            });
        });
        for (int i = 0; i < hasBlockstates.size(); i++) {
            final Predicate<IExtendedBlockState> firststate = this.hasBlockstates.get(0);
            final Predicate<IExtendedBlockState> blockstate = hasBlockstates.get(i);
        }
        return states;
    }

}