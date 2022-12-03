package eu.gir.girsignals.models.parser;

public class LogicalParserException extends RuntimeException {

    private static final long serialVersionUID = -2087199269266746700L;

    public LogicalParserException(final String message) {
        super(message);
    }

    public LogicalParserException(final Throwable th) {
        super(th);
    }

    public LogicalParserException(final String message, final Throwable th) {
        super(message, th);
    }

}
