package eu.gir.girsignals.models.parser;

public class LogicalParserException extends RuntimeException {

	private static final long serialVersionUID = -2087199269266746700L;

	public LogicalParserException(String message) {
		super(message);
	}
	
	public LogicalParserException(Throwable th) {
		super(th);
	}
	
}
