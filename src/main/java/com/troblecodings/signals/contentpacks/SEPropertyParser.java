package com.troblecodings.signals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.SEProperty.SEAutoNameProp;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.utils.JsonEnum;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;

public class SEPropertyParser {

    private String name;
    private String enumClass;
    private Object defaultState;
    private String changeableStage;
    private final boolean autoname = false;
    private String dependencies;
    private transient IProperty<?> parent;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public SEProperty createSEProperty() {
        if (defaultState instanceof Boolean) {
            parent = PropertyBool.create(name);
        } else {
            parent = JsonEnum.PROPERTIES.get(enumClass.toLowerCase());
        }

        if (parent == null)
            throw new ContentPackException(String.format("Property[%s] not found!", name));

        ChangeableStage stage = ChangeableStage.APISTAGE;
        if (changeableStage != null) {
            stage = Enum.valueOf(ChangeableStage.class, changeableStage);
        }

        Predicate<Map<SEProperty<?>, Object>> predicate = t -> true;
        if (dependencies != null && !dependencies.isEmpty()) {
            predicate = LogicParser.predicate(dependencies, FunctionParsingInfo.DEFAULT_INFO);
        }

        if (autoname)
            return new SEAutoNameProp(name, parent, (Comparable) defaultState, stage, predicate);
        return new SEProperty(name, parent, (Comparable) defaultState, stage, predicate);
    }
}