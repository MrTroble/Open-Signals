package com.troblecodings.signals.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.parser.interm.EvaluationLevel;
import com.troblecodings.signals.parser.interm.IntermidiateNode;
import com.troblecodings.signals.parser.interm.LogicalSymbols;

import net.minecraftforge.client.model.data.IModelData;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public final class LogicParser {

    public static final HashMap<String, MethodInfo> TRANSLATION_TABLE = new HashMap<>();
    public static final HashMap<String, MethodInfo> UNIVERSAL_TRANSLATION_TABLE = new HashMap<>();

    private LogicParser() {
    }

    static {
        TRANSLATION_TABLE.put("with", new MethodInfo(IModelData.class, "with",
                objects -> PredicateHolder.with((ValuePack) objects[0]), ValuePack.class));

        TRANSLATION_TABLE.put("has", new MethodInfo(IModelData.class, "has",
                objects -> PredicateHolder.has((SEProperty) objects[0]), SEProperty.class));

        TRANSLATION_TABLE.put("hasandis", new MethodInfo(IModelData.class, "hasandis",
                objects -> PredicateHolder.hasAndIs((SEProperty) objects[0]), SEProperty.class));

        TRANSLATION_TABLE.put("hasandisnot", new MethodInfo(IModelData.class, "hasandisnot",
                objects -> PredicateHolder.hasAndIsNot((SEProperty) objects[0]), SEProperty.class));

        TRANSLATION_TABLE.put("check", new MethodInfo(Map.class, "check",
                objects -> PredicateHolder.check((ValuePack) objects[0]), ValuePack.class));

        TRANSLATION_TABLE.put("config", new MethodInfo(Map.class, "config",
                objects -> PredicateHolder.config((ValuePack) objects[0]), ValuePack.class));

        TRANSLATION_TABLE.put("speed", new MethodInfo(Integer.class, "speed",
                objects -> PredicateHolder.speed((StringInteger) objects[0]), StringInteger.class));

        TRANSLATION_TABLE.forEach((name, info) -> UNIVERSAL_TRANSLATION_TABLE.put(name,
                new MethodInfo(Map.class, name, objects -> {
                    final Predicate original = info.blockState.apply(objects);
                    return (Predicate<Map>) inMap -> {
                        final Map<Class, Object> map = inMap;
                        final Object obj = map.get(info.getSubtype());
                        if (obj == null)
                            throw new IllegalArgumentException(
                                    String.format("No data for type=%s was passed to function=%s!",
                                            info.getSubtype().toString(), name));
                        return original.test(obj);
                    };
                }, info.parameter)));
    }

    public static Predicate nDegreeFunctionParser(final String name,
            final FunctionParsingInfo parser, final String... parameter) {
        final String[] arguments = parameter;
        final MethodInfo method = parser.getTable().get(name.toLowerCase());
        if (method == null)
            throw new LogicalParserException(
                    String.format("Syntax error function=%s does not exist permitted are:%n%s",
                            name, parser.getTable().keySet().toString()));
        final int length = method.parameter.length;
        if (arguments.length != length)
            throw new LogicalParserException(
                    String.format("Wrong argument count in function=%s, needed=%d, actual=%d", name,
                            length, arguments.length));
        return method.blockState.apply(parser.getParameter(method.parameter, arguments));
    }

    private static final int ERROR_CLAMP = 20;

    private static final String getSubstringError(final String input, final int index) {
        if (input.isEmpty())
            return input;
        final int start = index - ERROR_CLAMP;
        final int end = index + ERROR_CLAMP + Math.abs(Math.min(0, start));
        final int eStart = Math.max(start, 0);
        final String erroring = input.substring(eStart, Math.min(end, input.length()));
        final char[] nextLine = new char[erroring.length()];
        Arrays.fill(nextLine, ' ');
        nextLine[index - eStart - 1] = '^';
        return erroring + System.lineSeparator() + new String(nextLine);
    }

    public static IntermidiateLogic parse(final String input, final FunctionParsingInfo info) {
        final char[] array = input.toCharArray();
        final IntermidiateLogic logic = new IntermidiateLogic();
        final StringBuilder builder = new StringBuilder();
        String nextName = null;
        int i = 0;
        for (final char current : array) {
            i++;
            try {
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
            } catch (final Throwable e) {
                throw new LogicalParserException(
                        "Parserstate: " + System.lineSeparator() + getSubstringError(input, i), e);
            }
        }
        return logic;
    }

    public static Predicate predicate(final String input, final FunctionParsingInfo info) {
        return parse(input, info).pop().getPredicate();
    }
}
