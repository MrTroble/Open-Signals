package eu.gir.girsignals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import eu.gir.girsignals.ChangeableStage;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;
import eu.gir.girsignals.models.parser.ValuePack;
import net.minecraft.util.IStringSerializable;

public class SEPropertyParser {

    private final String name = "";
    private final String defaultValue = "";
    private final String changeableStage = "";
    private final boolean autoname = false;
    private final String dependencies = "";

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
                (Predicate<Map<SEProperty<?>, Object>>) LogicParser.predicate(dependencies, info));
    }
}