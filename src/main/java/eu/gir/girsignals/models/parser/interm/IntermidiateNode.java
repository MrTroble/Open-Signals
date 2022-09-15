package eu.gir.girsignals.models.parser.interm;

import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class IntermidiateNode {

    protected Predicate predicate;
    private final EvaluationLevel level;

    public IntermidiateNode(final Predicate predicate, final EvaluationLevel level) {
        super();
        this.predicate = predicate;
        this.level = level;
    }

    public boolean next(final IntermidiateNode node) {
        return false;
    }

    public boolean combine(final IntermidiateNode first, final IntermidiateNode next) {
        return false;
    }

    public IntermidiateNode getFinished() {
        return new IntermidiateNode(predicate, EvaluationLevel.PRELEVEL);
    }

    public EvaluationLevel getLevel() {
        return level;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public String toString() {
        return "IntermidiateNode [predicate=" + predicate + ", level=" + level + "]";
    }
}