package rs.pgdavidov.erp.bankimport.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.exception.ParserDetectionException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BankStatementParserResolver {

    private final List<BankStatementParser> parsers;

    public BankStatementParser resolve(
            MultipartFile file
    ) {

        return parsers
                .stream()
                .filter(parser ->
                        parser.supports(file)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ParserDetectionException(
                                "Unsupported bank statement format."
                        )
                );
    }
}