package eu.gir.girsignals.models.parser.interm;

public class IntermidiateNegate extends IntermidiateNode {

	public IntermidiateNegate() {
		super(null, EvaluationLevel.LEVEL1);
	}
	
	@Override
	public boolean next(final IntermidiateNode node) {
		if(node.predicate == null)
			return false;
		this.predicate = node.predicate.negate();
		return true;
	}
	
}
