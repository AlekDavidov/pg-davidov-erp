package rs.pgdavidov.erp.bankimport.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.dto.BankImportPreviewResponse;
import rs.pgdavidov.erp.bankimport.model.ParsedBankStatement;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParser;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParserResolver;

@Service
@RequiredArgsConstructor
public class BankImportService {

    private final BankStatementParserResolver parserResolver;

    public BankImportPreviewResponse preview(
            MultipartFile file
    ) {
        BankStatementParser parser =
                parserResolver.resolve(file);

        ParsedBankStatement statement =
                parser.parse(file);

        return new BankImportPreviewResponse(
                parser.getParserName(),
                statement.bankCode(),
                statement.bankName(),
                statement.statementId(),
                statement.statementNumber(),
                statement.accountNumber(),
                statement.periodFrom(),
                statement.periodTo(),
                statement.currencyCode(),
                statement.transactions().size(),
                statement.transactions()
        );
    }
}