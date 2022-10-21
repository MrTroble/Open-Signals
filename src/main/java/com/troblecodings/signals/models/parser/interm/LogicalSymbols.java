package com.troblecodings.signals.models.parser.interm;

import java.util.Arrays;
import java.util.function.Supplier;

public enum LogicalSymbols {

    AND("&&", IntermidiateAnd::new), OR("||", IntermidiateOr::new),
    NEGATE("!", IntermidiateNegate::new);

    public final String symbol;
    public final Supplier<IntermidiateNode> builder;

    private LogicalSymbols(final String symbol, final Supplier<IntermidiateNode> builder) {
        this.symbol = symbol;
        this.builder = builder;
    }

    public static LogicalSymbols find(final String name) {
        return Arrays.stream(LogicalSymbols.values()).filter(symbol -> symbol.symbol.equals(name))
                .findAny().orElse(null);
    }
}
