package eu.gir.girsignals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class Texture extends Models {

    private List<String> has;
    private List<String> hasnot;
    private Map<String, String> with;
    private List<String> hasandis;
    private List<String> hasandisnot;

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
    
    private Predicate<IExtendedBlockState> states;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public Predicate<IExtendedBlockState> getPredicates() {
        final ModelStateBuilder state = new ModelStateBuilder();
        final Map<String, ModelStats> content = state
                .getfromJson("/assets/girsignals/modeldefinitions");
        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);
        content.forEach((filename, values) -> {
            final String name = filename.replace(".json", "");
            //System.out.println(name);
            signals.forEach(signal -> {
                final String signalsystem = signal.getSignalTypeName();
                //System.out.println(signalsystem);
                if (name.equalsIgnoreCase(signalsystem)) {
                    final List<IUnlistedProperty> properties = signal.getProperties();
                    properties.forEach(property -> {
                        for (int i = 0; i < getHas().toArray().length; i++) {
                            try {
                                final String has = getHas().get(i);
                                final String prop = property.toString();
                                if (prop.equalsIgnoreCase(has)) {
                                    this.states = GIRCustomModelLoader
                                            .has(property);
                                    //System.out.println(states);
                                    if (getHas().get(1) != null) {
                                        
                                    }
                                }
                            } catch (IndexOutOfBoundsException | NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        });
        return states;
    }
}