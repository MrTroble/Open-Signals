package eu.gir.girsignals.models.parser.interm;

public class IntermidiateAnd extends IntermidiateNode {

    public IntermidiateAnd() {
        super(null, EvaluationLevel.LEVEL2);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean combine(final IntermidiateNode first, final IntermidiateNode next) {
        if (first.predicate == null || next.predicate == null)
            return false;
        this.predicate = first.predicate.and(next.predicate);
        return true;
    }

    @Override
    public String toString() {
        return "IntermidiateAnd";
    }
}
