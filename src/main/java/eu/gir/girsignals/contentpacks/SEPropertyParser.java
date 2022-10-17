package eu.gir.girsignals.contentpacks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;

public class SEPropertyParser<T> {

    private String name = "";
    private T defaultValue;
    private String changeableStage = "";
    private boolean autoname = false;
    private String dependencies = "";

    @SuppressWarnings("rawtypes")
    public SEProperty createSEProperty(final FunctionParsingInfo info) {
        return SEProperty.of(name, defaultValue,
                Enum.valueOf(ChangeableStage.class, changeableStage), autoname,
                LogicParser.predicate(dependencies, info));
    }
}
