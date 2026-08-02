package rs.pgdavidov.erp.bankimport.exception;

public class ParserDetectionException
        extends RuntimeException {

    public ParserDetectionException(
            String message
    ) {
        super(message);
    }
}