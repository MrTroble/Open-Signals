package eu.gir.girsignals.contentpacks;

import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;
import eu.gir.girsignals.models.parser.ValuePack;
import net.minecraft.util.IStringSerializable;

public class SEPropertyParser {

    private String name = "";
    private String defaultValue = "";
    private String changeableStage = "";
    private boolean autoname = false;
    private String dependencies = "";

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public SEProperty createSEProperty(final FunctionParsingInfo info) {

        final Class[] classes = {
                ValuePack.class
        };

        final String[] strings = {
                defaultValue
        };

        final Object[] obj = {
                info.getParameter(classes, strings)
        };

        return SEProperty.of(name, (Enum & IStringSerializable) obj[1],
                Enum.valueOf(ChangeableStage.class, changeableStage), autoname,
                (Predicate<Set<Entry<SEProperty<?>, Object>>>) LogicParser.predicate(dependencies,
                        info));
    }
}