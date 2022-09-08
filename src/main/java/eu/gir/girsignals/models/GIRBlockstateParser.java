package eu.gir.girsignals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.DefaultName;
import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class GIRBlockstateParser {

    private static final List<String> OPERATIONS = new ArrayList<>();

    public static Predicate<IExtendedBlockState> getBlockstatefromString(final String blockstate,
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
            final Object obj = checkBlockstates(strblockstate, sigprops, filename, modelname);

            if (obj instanceof String) {

                OPERATIONS.add((String) obj);

            } else if (obj instanceof Predicate<?>) {

                blockstates.add((Predicate<IExtendedBlockState>) obj);
            }
        });

        return blockstates;
    }

    @SuppressWarnings({
            "rawtypes"
    })
    private static Object checkBlockstates(final String strblockstate,
            final List<IUnlistedProperty> sigprops, final String filename, final String modelname) {

        boolean negate = false;

        if (strblockstate.startsWith("!")) {

            negate = true;
        }

        if (strblockstate.equals("&&") || strblockstate.equals("||")) {

            return strblockstate;
        }
        /*
         * final boolean has = strblockstate.startsWith("has") ||
         * strblockstate.startsWith("!has");
         * 
         * if (has && !(strblockstate.startsWith("hasand") ||
         * strblockstate.startsWith("!hasand"))) {
         * 
         * final String hasstate = strblockstate.replace("has", "").replace("(", "")
         * .replace(")", "").replace("!", "");
         * 
         * for (int i = 0; i < sigprops.size(); i++) { if (hasstate.equalsIgnoreCase(
         * sigprops.get(i).toString().replace("SEP[", "").replace("]", ""))) {
         * 
         * if (negate) {
         * 
         * return has(sigprops.get(i)).negate();
         * 
         * } else {
         * 
         * return has(sigprops.get(i)); } } } }
         * 
         * if (strblockstate.startsWith("hasandis") ||
         * strblockstate.startsWith("!hasandis")) {
         * 
         * final String hasandisstate = strblockstate.replace("hasandis",
         * "").replace("(", "") .replace(")", "").replace("!", "");
         * 
         * for (int i = 0; i < sigprops.size(); i++) {
         * 
         * if (hasandisstate.equalsIgnoreCase(
         * sigprops.get(i).toString().replace("SEP[", "").replace("]", ""))) {
         * 
         * if (negate) {
         * 
         * System.out.println("The Blockstate of " + modelname + " is negated!"); return
         * hasAndIs(sigprops.get(i)).negate();
         * 
         * } else if (!hasandisstate.equals("CUSTOMNAME") && !negate) {
         * System.out.println("The Blockstate of " + modelname + " is not negated!");
         * return hasAndIs(sigprops.get(i));
         * 
         * } else if (hasandisstate.equalsIgnoreCase("CUSTOMNAME")) {
         * 
         * return hasAndIs(Signal.CUSTOMNAME); } } } }
         * 
         * if (strblockstate.startsWith("hasandisnot") ||
         * strblockstate.startsWith("!hasandisnot"))
         * 
         * if (strblockstate.startsWith("with") || strblockstate.startsWith("!with")) {
         * 
         * final Map<String, String> withstates = new HashMap<>();
         * 
         * final String withstate = strblockstate.replace("with", "").replace(" ", "")
         * .replace(",", " ").replace("(", "").replace(")", "").replace("!", "");
         * 
         * String buffer = "";
         * 
         * for (int i = 0; i < withstate.length(); i++) {
         * 
         * final StringBuilder builder = new StringBuilder();
         * 
         * String str = builder.append(withstate.charAt(i)).toString();
         * 
         * if (str.equals(" ")) {
         * 
         * withstates.put(buffer, withstate.replace(" ", "").replaceAll(buffer, ""));
         * 
         * buffer = ""; str = "";
         * 
         * } else { buffer += str; str = ""; } }
         * 
         * for (Map.Entry<String, String> entry : withstates.entrySet()) { final String
         * key = entry.getKey(); final String val = entry.getValue();
         * 
         * for (int j = 0; j < sigprops.size(); j++) {
         * 
         * final IUnlistedProperty prop = sigprops.get(j); if (key.equalsIgnoreCase(
         * sigprops.get(j).toString().replace("SEP[", "").replace("]", ""))) {
         * 
         * final SEProperty seprop = (SEProperty) prop;
         * 
         * final Enum defaultvalue = (Enum) seprop.getDefault();
         * 
         * return with(prop, obj -> obj
         * .equals(Enum.valueOf(defaultvalue.getDeclaringClass(), val))); } } } }
         */
        return null;
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

    private static class GIRFunctions {

        public static <T extends Enum<?>> Predicate<IExtendedBlockState> has(
                final IUnlistedProperty<T> property) {
            return ebs -> ebs.getValue(property) != null;
        }

        public static <T extends DefaultName<?>> Predicate<IExtendedBlockState> with(
                final IUnlistedProperty<T> property, final Predicate<T> t) {
            return bs -> {
                final T test = bs.getValue(property);
                return test != null && t.test(test);
            };
        }

        public static Predicate<IExtendedBlockState> hasAndIs(
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

    }
}