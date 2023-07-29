package com.troblecodings.signals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.SEProperty.SEAutoNameProp;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;

public class SEPropertyParser {

    private String name;
    private String enumClass;
    private Object defaultState;
    private String changeableStage;
    private boolean autoname = true;
    private String dependencies;
    private int itemDamage = 1;
    private transient JsonEnum parent;

    @SuppressWarnings("unchecked")
    public SEProperty createSEProperty(final FunctionParsingInfo info) {
        if (defaultState instanceof Boolean) {
            parent = JsonEnum.BOOLEAN;
            enumClass = JsonEnum.BOOLEAN.getName();
            defaultState = String.valueOf(defaultState);
        } else {
            parent = JsonEnumHolder.PROPERTIES.get(enumClass.toLowerCase());
        }

        if (parent == null)
            throw new ContentPackException(String.format("Property[%s], with class %s not found!",
                    name, enumClass.toLowerCase()));

        ChangeableStage stage = ChangeableStage.APISTAGE;
        if (changeableStage != null && !changeableStage.isEmpty()) {
            stage = Enum.valueOf(ChangeableStage.class, changeableStage);
        }

        Predicate<Map<SEProperty, String>> predicate = t -> true;
        if (dependencies != null && !dependencies.isEmpty()) {
            predicate = LogicParser.predicate(dependencies, info);
        }

        if (autoname)
            return new SEAutoNameProp(name, parent, (String) defaultState, stage, predicate,
                    itemDamage);
        return new SEProperty(name, parent, (String) defaultState, stage, predicate, itemDamage);
    }
}