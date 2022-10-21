package eu.gir.girsignals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import eu.gir.girsignals.ChangeableStage;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.JsonSEProperty;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.SEAutoNameProp;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;

public class SEPropertyParser {

    private String defaultEnum;
    private String defaultState;
    private String changeableStage;
    private boolean autoname = false;
    private String dependencies;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public SEProperty createSEProperty(final FunctionParsingInfo info) {

        final JsonSEProperty json = JsonSEProperty.PROPERTIES.get(defaultEnum);
        if (json == null)
            GirsignalsMain.log.error("The given defaultEnum '" + defaultEnum + "' doesn't exists!");

        try {
            Enum.valueOf(ChangeableStage.class, changeableStage);
        } catch (final IllegalArgumentException e) {
            GirsignalsMain.log
                    .error("The given Changeable Stage is not permitted! You can use 'APISTAGE, "
                            + "GUISTAGE, APISTAGE_NONE_CONFIG' or 'AUTOMATICSTAGE! Your stage was "
                            + changeableStage + ".");
        }

        Predicate predicate;
        if (dependencies == null || dependencies.isEmpty()) {
            predicate = t -> true;
        } else {
            predicate = LogicParser.predicate(dependencies, info);
        }

        if (autoname)
            return new SEAutoNameProp(json, defaultState,
                    Enum.valueOf(ChangeableStage.class, changeableStage),
                    (Predicate<Map<SEProperty<?>, Object>>) predicate);

        return new SEProperty(json, defaultState,
                Enum.valueOf(ChangeableStage.class, changeableStage),
                (Predicate<Map<SEProperty<?>, Object>>) predicate);
    }
}