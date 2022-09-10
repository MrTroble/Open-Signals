package eu.gir.girsignals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class GIRBlockstateParser {

    private static final List<String> OPERATIONS = new ArrayList<>();

    public static Predicate<IExtendedBlockState> getPredicateFromString(final String blockstate,
            final String filename, final String modelname) {

        final List<String> strblockstates = new ArrayList<>();

        String buffer = "";

        for (int i = 0; i < blockstate.length(); i++) {
            final StringBuilder builder = new StringBuilder();
            String str = builder.append(blockstate.charAt(i)).toString();

            if (str.equals(" ")) {
                strblockstates.add(buffer);
                buffer = "";
                str = "";

            } else if (blockstate.length() - 1 == i) {
                buffer += str;
                strblockstates.add(buffer);
                buffer = "";
                str = "";

            } else {
                buffer += str;
                str = "";
            }
        }

        final List<Predicate<IExtendedBlockState>> blockstatelist = createPredicates(strblockstates,
                filename, modelname);

        return createBlockstates(blockstatelist, modelname);
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private static List<Predicate<IExtendedBlockState>> createPredicates(
            final List<String> strblockstates, final String filename, final String modelname) {

        final List<IUnlistedProperty> sigprops = new ArrayList<>();

        final List<Predicate<IExtendedBlockState>> blockstates = new ArrayList<>();

        final ArrayList<Signal> signals = new ArrayList<>(Signal.SIGNALLIST);

        signals.forEach(signal -> {
            if (filename.replace(".json", "").replace("signal", "")
                    .equalsIgnoreCase(signal.getSignalTypeName().replace("signal", ""))) {
                final List<IUnlistedProperty> properties = signal.getProperties();
                properties.forEach(prop -> sigprops.add(prop));
            }
        });

        strblockstates.forEach(strblockstate -> {
            final Object obj = null;

            if (obj instanceof String) {

                OPERATIONS.add((String) obj);

            } else if (obj instanceof Predicate<?>) {

                blockstates.add((Predicate<IExtendedBlockState>) obj);
            }
        });

        return blockstates;
    }

    private static Predicate<IExtendedBlockState> createBlockstates(
            final List<Predicate<IExtendedBlockState>> blockstates, final String modelname) {

        String commingOperation = null;

        if (OPERATIONS.size() > 0) {
            commingOperation = OPERATIONS.get(0);
        }

        Predicate<IExtendedBlockState> blockstate = null;

        if (blockstates.size() > 0) {
            blockstate = blockstates.get(0);
            for (int i = 1; i < blockstates.size(); i++) {

                if (commingOperation != null) {

                    if (commingOperation.equals("||")) {

                        blockstate = blockstate.or(blockstates.get(i));

                    } else {

                        blockstate = blockstate.and(blockstates.get(i));
                    }

                }

                if (OPERATIONS.get(i) != null) {

                    commingOperation = OPERATIONS.get(i);

                } else {

                    commingOperation = null;
                }
            }
        }
        System.out.println("Blockstate of " + modelname + " is: " + blockstate);
        return blockstate;
    }

}