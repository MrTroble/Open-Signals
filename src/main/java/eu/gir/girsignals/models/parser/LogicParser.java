package eu.gir.girsignals.models.parser;

import java.util.HashMap;
import java.util.function.Predicate;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LogicParser {

	private static final HashMap<String, MethodInfo> translationTable = new HashMap<>();

	static {
		translationTable.put("with",
				new MethodInfo("with", objects -> PredicateHolder.with((ValuePack) objects[0]), ValuePack.class));

		translationTable.put("has", new MethodInfo("has",
				objects -> PredicateHolder.has((IUnlistedProperty) objects[0]), IUnlistedProperty.class));

		translationTable.put("hasandis", new MethodInfo("hasandis",
				objects -> PredicateHolder.hasAndIs((IUnlistedProperty) objects[0]), IUnlistedProperty.class));

		translationTable.put("hasandisnot", new MethodInfo("hasandisnot",
				objects -> PredicateHolder.hasAndIsNot((IUnlistedProperty) objects[0]), IUnlistedProperty.class));
	}

	public static Predicate<IExtendedBlockState> nDegreeFunctionParser(final String name,
			final FunctionParsingInfo parser, final String... parameter) {
		final String[] arguments = parameter;
		final MethodInfo method = translationTable.get(name);
		if (method == null)
			throw new LogicalParserException(String.format("Syntax error function=%s does not exist permitted are:%n%s",
					name, translationTable.keySet().toString()));
		final int length = method.parameter.length;
		if (arguments.length != length)
			throw new LogicalParserException(String.format("Wrong argument count in function=%s, needed=%d, actual=%d",
					name, length, arguments.length));
		return method.blockState.apply(parser.getParameter(method.parameter, arguments));
	}

}
