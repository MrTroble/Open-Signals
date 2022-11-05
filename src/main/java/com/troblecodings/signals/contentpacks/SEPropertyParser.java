package com.troblecodings.signals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.SEProperty.SEAutoNameProp;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.utils.JsonEnum;

public class SEPropertyParser {

    private String name;
    private String defaultEnum;
    private String defaultState;
    private String changeableStage;
    private boolean autoname = false;
    private String dependencies;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public SEProperty createSEProperty(final FunctionParsingInfo info) {

        final JsonEnum json = JsonEnum.PROPERTIES.get(defaultEnum);
        if (json == null)
            SignalsMain.log.error("The given defaultEnum '" + defaultEnum + "' doesn't exists!");

        try {
            Enum.valueOf(ChangeableStage.class, changeableStage);
        } catch (final IllegalArgumentException e) {
            SignalsMain.log
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
            return new SEAutoNameProp(name, json, defaultState,
                    Enum.valueOf(ChangeableStage.class, changeableStage),
                    (Predicate<Map<SEProperty<?>, Object>>) predicate);

        return new SEProperty(name, json, defaultState,
                Enum.valueOf(ChangeableStage.class, changeableStage),
                (Predicate<Map<SEProperty<?>, Object>>) predicate);
    }
}