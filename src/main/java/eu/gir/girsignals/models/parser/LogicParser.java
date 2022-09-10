package eu.gir.girsignals.models.parser;

import java.util.HashMap;
import java.util.function.Predicate;

import net.minecraftforge.common.property.IExtendedBlockState;

public class LogicParser {

	private static final HashMap<String, MethodInfo> translationTable = new HashMap<>();

	static {

	}

	public static Predicate<IExtendedBlockState> nDegreeFunctionParser(final FunctionParsingInfo info) {
		String[] arguments = info.arguments;
		MethodInfo method = translationTable.get(info.name);
		final int length = method.parameter.length;
		if (arguments.length != length)
			throw new LogicalParserException(String.format("Wrong argument count in function=%s, needed=%d, actual=%d",
					info.name, length, arguments.length));
		return method.blockState.apply(info.getParameter(method.parameter, arguments));
	}

}
