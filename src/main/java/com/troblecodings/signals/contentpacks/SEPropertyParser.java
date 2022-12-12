package com.troblecodings.signals.contentpacks;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.SEProperty.SEAutoNameProp;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.utils.JsonEnum;

public class SEPropertyParser {

	private String name;
	private String enumClass;
	private Object defaultState;
	private String changeableStage;
	private final boolean autoname = false;
	private String dependencies;
	private transient JsonEnum parent;

	@SuppressWarnings("unchecked")
	public SEProperty createSEProperty(final FunctionParsingInfo info) {
		if (defaultState instanceof Boolean) {
			parent = JsonEnum.PROPERTIES.get(Boolean.toString((boolean) defaultState).toLowerCase());
		} else {
			parent = JsonEnum.PROPERTIES.get(enumClass.toLowerCase());
		}

		if (parent == null)
			throw new ContentPackException(
					String.format("Property[%s], with class %s not found!", name, enumClass.toLowerCase()));

		ChangeableStage stage = ChangeableStage.APISTAGE;
		if (changeableStage != null && !changeableStage.isEmpty()) {
			stage = Enum.valueOf(ChangeableStage.class, changeableStage);
		}

		Predicate<Map<SEProperty, Object>> predicate = t -> true;
		if (dependencies != null && !dependencies.isEmpty()) {
			predicate = LogicParser.predicate(dependencies, info);
		}

		if (autoname)
			return new SEAutoNameProp(name, parent, (String) defaultState, stage, predicate);
		return new SEProperty(name, parent, (String) defaultState, stage, predicate);
	}
}