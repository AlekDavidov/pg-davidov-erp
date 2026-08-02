package rs.pgdavidov.erp.bankimport.parser;

import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.model.ParsedBankStatement;

public interface BankStatementParser {

    boolean supports(
            MultipartFile file
    );

    String getParserName();

    ParsedBankStatement parse(
            MultipartFile file
    );
}