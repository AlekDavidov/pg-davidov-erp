package rs.pgdavidov.erp.bankimport.exception;

public class BankStatementParseException
        extends RuntimeException {

    public BankStatementParseException(
            String message
    ) {
        super(message);
    }

    public BankStatementParseException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );
    }
}