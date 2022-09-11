package eu.gir.girsignals.models.parser;

import java.util.HashMap;
import java.util.function.Predicate;

import eu.gir.girsignals.models.parser.interm.EvaluationLevel;
import eu.gir.girsignals.models.parser.interm.IntermidiateNode;
import eu.gir.girsignals.models.parser.interm.LogicalSymbols;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public final class LogicParser {

    private static final HashMap<String, MethodInfo> TRANSLATION_TABLE = new HashMap<>();

    private LogicParser() {
    }

    static {
        TRANSLATION_TABLE.put("with", new MethodInfo("with",
                objects -> PredicateHolder.with((ValuePack) objects[0]), ValuePack.class));

        TRANSLATION_TABLE.put("has",
                new MethodInfo("has",
                        objects -> PredicateHolder.has((IUnlistedProperty) objects[0]),
                        IUnlistedProperty.class));

        TRANSLATION_TABLE.put("hasandis",
                new MethodInfo("hasandis",
                        objects -> PredicateHolder.hasAndIs((IUnlistedProperty) objects[0]),
                        IUnlistedProperty.class));

        TRANSLATION_TABLE.put("hasandisnot",
                new MethodInfo("hasandisnot",
                        objects -> PredicateHolder.hasAndIsNot((IUnlistedProperty) objects[0]),
                        IUnlistedProperty.class));
    }

    public static Predicate<IExtendedBlockState> nDegreeFunctionParser(final String name,
            final FunctionParsingInfo parser, final String... parameter) {
        final String[] arguments = parameter;
        final MethodInfo method = TRANSLATION_TABLE.get(name.toLowerCase());
        if (method == null)
            throw new LogicalParserException(
                    String.format("Syntax error function=%s does not exist permitted are:%n%s",
                            name, TRANSLATION_TABLE.keySet().toString()));
        final int length = method.parameter.length;
        if (arguments.length != length)
            throw new LogicalParserException(
                    String.format("Wrong argument count in function=%s, needed=%d, actual=%d", name,
                            length, arguments.length));
        return method.blockState.apply(parser.getParameter(method.parameter, arguments));
    }

    public static IntermidiateLogic parse(final String input, final FunctionParsingInfo info) {
        final char[] array = input.toCharArray();
        final IntermidiateLogic logic = new IntermidiateLogic();
        final StringBuilder builder = new StringBuilder();
        String nextName = null;
        for (final char current : array) {
            if (current == '(') {
                if (builder.length() <= 0) {
                    logic.push();
                } else {
                    nextName = builder.toString();
                    builder.setLength(0);
                }
                continue;
            }
            if (current == ')') {
                if (nextName == null) {
                    logic.pop();
                } else {
                    final String arguments = builder.toString();
                    logic.add(
                            new IntermidiateNode(
                                    nDegreeFunctionParser(nextName, info,
                                            arguments.isEmpty() ? new String[0]
                                                    : arguments.split(",")),
                                    EvaluationLevel.PRELEVEL));
                    builder.setLength(0);
                    nextName = null;
                }
                continue;
            }
            if (!Character.isWhitespace(current)) {
                builder.append(current);
                final LogicalSymbols symbol = LogicalSymbols.find(builder.toString());
                if (symbol != null) {
                    logic.add(symbol.builder.get());
                    builder.setLength(0);
                }
            }
        }
        return logic;
    }

    public static Predicate<IExtendedBlockState> predicate(final String input,
            final FunctionParsingInfo info) {
        return parse(input, info).pop().getPredicate();
    }
}
