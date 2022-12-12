package com.troblecodings.signals.parser.interm;

public class IntermidiateOr extends IntermidiateNode {

    public IntermidiateOr() {
        super(null, EvaluationLevel.LEVEL3);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean combine(final IntermidiateNode first, final IntermidiateNode next) {
        if (first.predicate == null || next.predicate == null)
            return false;
        this.predicate = first.predicate.or(next.predicate);
        return true;
    }

    @Override
    public String toString() {
        return "IntermidiateOr";
    }
}
