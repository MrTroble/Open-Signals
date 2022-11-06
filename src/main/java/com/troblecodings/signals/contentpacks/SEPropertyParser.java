package com.troblecodings.signals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.SEProperty.SEAutoNameProp;
import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.utils.JsonEnum;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;

public class SEPropertyParser {

    private String name;
    private String defaultEnum;
    private Object defaultState;
    private String changeableStage;
    private boolean autoname = false;
    private String dependencies;
    private transient Object type;
    private transient Object parent;

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public SEProperty createSEProperty(final FunctionParsingInfo info) {

        if (defaultState instanceof Boolean) {
            type = (Boolean) type;
            defaultState = (Boolean) defaultState;
            parent = PropertyBool.create(name);
        } else {
            type = (String) type;
            defaultState = (String) defaultState;
            parent = (JsonEnum) parent;

            if (defaultEnum != null && !defaultEnum.isEmpty()) {
                parent = JsonEnum.PROPERTIES.get(defaultEnum);
                if (parent == null)
                    SignalsMain.getLogger()
                            .error("The given defaultEnum '" + defaultEnum + "' doesn't exists!");
            }
        }

        try {
            Enum.valueOf(ChangeableStage.class, changeableStage);
        } catch (final IllegalArgumentException e) {
            SignalsMain.getLogger()
                    .error("The given Changeable Stage is not permitted! You can use 'APISTAGE, "
                            + "GUISTAGE, APISTAGE_NONE_CONFIG' or 'AUTOMATICSTAGE!' Your stage was "
                            + changeableStage + ".");
        }

        Predicate predicate;
        if (dependencies == null || dependencies.isEmpty()) {
            predicate = t -> true;
        } else {
            predicate = LogicParser.predicate(dependencies, info);
        }

        if (autoname)
            return new SEAutoNameProp(name, (IProperty) parent, (Comparable) defaultState,
                    Enum.valueOf(ChangeableStage.class, changeableStage),
                    (Predicate<Map<SEProperty<?>, Object>>) predicate, type);

        return new SEProperty(name, (IProperty) parent, (Comparable) defaultState,
                Enum.valueOf(ChangeableStage.class, changeableStage),
                (Predicate<Map<SEProperty<?>, Object>>) predicate, type);
    }
}