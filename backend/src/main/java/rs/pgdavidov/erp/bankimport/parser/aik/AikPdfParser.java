package rs.pgdavidov.erp.bankimport.parser.aik;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.exception.BankStatementParseException;
import rs.pgdavidov.erp.bankimport.model.ParsedBankStatement;
import rs.pgdavidov.erp.bankimport.model.ParsedBankTransaction;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParser;
import rs.pgdavidov.erp.bankimport.parser.pdf.PdfLineGrouper;
import rs.pgdavidov.erp.bankimport.parser.pdf.PdfLineGrouper.PdfLine;
import rs.pgdavidov.erp.bankimport.parser.pdf.PdfWordExtractor;
import rs.pgdavidov.erp.bankimport.parser.pdf.PdfWordExtractor.PdfWord;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AikPdfParser
        implements BankStatementParser {

    private static final String BANK_CODE =
            "AIK";

    private static final String BANK_NAME =
            "AIK Banka";

    private static final String DEFAULT_CURRENCY =
            "RSD";

    private static final Pattern ENTRY_PATTERN =
            Pattern.compile(
                    "^(\\d{1,3})\\.$"
            );

    private static final Pattern SHORT_DATE_PATTERN =
            Pattern.compile(
                    "^\\d{2}\\.\\d{2}\\.\\d{2}$"
            );

    private static final Pattern SHORT_DATE_SEARCH_PATTERN =
            Pattern.compile(
                    "(\\d{2}\\.\\d{2}\\.\\d{2})"
            );

    private static final Pattern LONG_DATE_PATTERN =
            Pattern.compile(
                    "\\b(\\d{2}\\.\\d{2}\\.\\d{4})\\b"
            );

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile(
                    "^\\d{1,3}(?:,\\d{3})*\\.\\d{2}$"
                            + "|^\\d+\\.\\d{2}$"
            );

    private static final Pattern STATEMENT_NUMBER_PATTERN =
            Pattern.compile(
                    "IZVOD\\s+BROJ\\s+(\\d+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern PERIOD_PATTERN =
            Pattern.compile(
                    "Od:\\s*"
                            + "(\\d{2}\\.\\d{2}\\.\\d{4})"
                            + "\\s*-\\s*"
                            + "(\\d{2}\\.\\d{2}\\.\\d{4})",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "Račun:\\s*([0-9\\-\\s]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern COMPLAINT_REFERENCE_PATTERN =
            Pattern.compile(
                    "reklamaciju\\s*:\\s*([0-9-]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ORDER_PATTERN =
            Pattern.compile(
                    "^(Nalog|Kartica)\\s*:\\s*(.+)$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final DateTimeFormatter SHORT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yy",
                    Locale.forLanguageTag("sr")
            );

    private static final DateTimeFormatter LONG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy",
                    Locale.forLanguageTag("sr")
            );

    private static final float ENTRY_MAX_X =
            35F;

    private static final float HEADER_LINE_TOLERANCE =
            2.5F;

    private static final float INCOME_COLUMN_FROM =
            285F;

    private static final float INCOME_COLUMN_TO =
            400F;

    private static final float EXPENSE_COLUMN_FROM =
            400F;

    private static final float EXPENSE_COLUMN_TO =
            485F;

    private static final float BALANCE_COLUMN_FROM =
            485F;

    private static final float BALANCE_COLUMN_TO =
            570F;

    private static final float ORDER_COLUMN_FROM =
            105F;

    private static final float ORDER_COLUMN_TO =
            285F;

    private static final float ENTRY_START_OFFSET =
            2F;

    private static final float PAGE_BOTTOM_MARGIN =
            45F;

    @Override
    public boolean supports(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType =
                file.getContentType();

        String filename =
                file.getOriginalFilename();

        boolean pdfFile =
                "application/pdf".equalsIgnoreCase(
                        contentType
                )
                        || (
                        filename != null
                                && filename
                                .toLowerCase(Locale.ROOT)
                                .endsWith(".pdf")
                );

        if (!pdfFile) {
            return false;
        }

        try (
                PDDocument document =
                        Loader.loadPDF(
                                file.getBytes()
                        )
        ) {
            String headerText =
                    extractHeaderText(document);

            return headerText.contains(
                    "AikBank"
            )
                    || headerText
                    .toUpperCase(Locale.ROOT)
                    .contains("AIK BANK")
                    || headerText
                    .toUpperCase(Locale.ROOT)
                    .contains("AIKBANK");
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public String getParserName() {
        return "AIK PDF parser";
    }

    @Override
    public ParsedBankStatement parse(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new BankStatementParseException(
                    "AIK izvod nije prosleđen."
            );
        }

        try (
                PDDocument document =
                        Loader.loadPDF(
                                file.getBytes()
                        )
        ) {
            StatementMetadata metadata =
                    extractMetadata(document);

            PdfWordExtractor wordExtractor =
                    new PdfWordExtractor();

            List<PdfWord> words =
                    wordExtractor.extract(document);

            List<ParsedBankTransaction> transactions =
                    parseTransactions(
                            document,
                            words
                    );

            validateTransactions(
                    transactions
            );

            return new ParsedBankStatement(
                    BANK_CODE,
                    BANK_NAME,
                    metadata.statementId(),
                    metadata.statementNumber(),
                    metadata.accountNumber(),
                    metadata.periodFrom(),
                    metadata.periodTo(),
                    DEFAULT_CURRENCY,
                    transactions
            );
        } catch (BankStatementParseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BankStatementParseException(
                    "AIK izvod nije mogao da bude parsiran.",
                    exception
            );
        }
    }

    private StatementMetadata extractMetadata(
            PDDocument document
    ) throws IOException {
        String headerText =
                extractHeaderText(document);

        Matcher statementMatcher =
                STATEMENT_NUMBER_PATTERN.matcher(
                        headerText
                );

        if (!statementMatcher.find()) {
            throw new BankStatementParseException(
                    "Broj AIK izvoda nije pronađen."
            );
        }

        Matcher periodMatcher =
                PERIOD_PATTERN.matcher(
                        headerText
                );

        if (!periodMatcher.find()) {
            throw new BankStatementParseException(
                    "Period AIK izvoda nije pronađen."
            );
        }

        Matcher accountMatcher =
                ACCOUNT_PATTERN.matcher(
                        headerText
                );

        String statementNumber =
                statementMatcher.group(1);

        LocalDate periodFrom =
                parseLongDate(
                        periodMatcher.group(1),
                        "početni datum perioda"
                );

        LocalDate periodTo =
                parseLongDate(
                        periodMatcher.group(2),
                        "krajnji datum perioda"
                );

        String accountNumber =
                accountMatcher.find()
                        ? normalizeAccountNumber(
                        accountMatcher.group(1)
                )
                        : null;

        String statementId =
                "%s-%04d-%02d-%03d".formatted(
                        BANK_CODE,
                        periodFrom.getYear(),
                        periodFrom.getMonthValue(),
                        Integer.parseInt(
                                statementNumber
                        )
                );

        return new StatementMetadata(
                statementNumber,
                accountNumber,
                periodFrom,
                periodTo,
                statementId
        );
    }

    private String extractHeaderText(
            PDDocument document
    ) throws IOException {
        PDFTextStripper textStripper =
                new PDFTextStripper();

        textStripper.setSortByPosition(true);
        textStripper.setStartPage(1);
        textStripper.setEndPage(
                Math.min(
                        2,
                        document.getNumberOfPages()
                )
        );

        return textStripper.getText(document);
    }

    private List<ParsedBankTransaction> parseTransactions(
            PDDocument document,
            List<PdfWord> words
    ) {
        List<ParsedBankTransaction> transactions =
                new ArrayList<>();

        for (
                int pageNumber = 1;
                pageNumber <= document.getNumberOfPages();
                pageNumber++
        ) {
            final int currentPageNumber =
                    pageNumber;

            List<PdfWord> pageWords =
                    words
                            .stream()
                            .filter(word ->
                                    word.pageNumber()
                                            == currentPageNumber
                            )
                            .toList();

            if (pageWords.isEmpty()) {
                continue;
            }

            float pageHeight =
                    document
                            .getPage(
                                    currentPageNumber - 1
                            )
                            .getMediaBox()
                            .getHeight();

            transactions.addAll(
                    parsePage(
                            pageWords,
                            currentPageNumber,
                            pageHeight
                    )
            );
        }

        return transactions
                .stream()
                .sorted(
                        Comparator.comparing(
                                ParsedBankTransaction::entryNumber
                        )
                )
                .toList();
    }

    private List<ParsedBankTransaction> parsePage(
            List<PdfWord> pageWords,
            int pageNumber,
            float pageHeight
    ) {
        List<PdfWord> entryAnchors =
                pageWords
                        .stream()
                        .filter(word ->
                                word.x0() < ENTRY_MAX_X
                        )
                        .filter(word ->
                                ENTRY_PATTERN
                                        .matcher(
                                                word.text()
                                        )
                                        .matches()
                        )
                        .sorted(
                                Comparator.comparing(
                                        PdfWord::top
                                )
                        )
                        .toList();

        List<ParsedBankTransaction> transactions =
                new ArrayList<>();

        for (
                int index = 0;
                index < entryAnchors.size();
                index++
        ) {
            PdfWord anchor =
                    entryAnchors.get(index);

            float start =
                    anchor.top()
                            - ENTRY_START_OFFSET;

            float end =
                    index + 1 < entryAnchors.size()
                            ? entryAnchors
                            .get(index + 1)
                            .top()
                            - ENTRY_START_OFFSET
                            : pageHeight
                            - PAGE_BOTTOM_MARGIN;

            List<PdfWord> band =
                    pageWords
                            .stream()
                            .filter(word ->
                                    word.top() >= start
                                            && word.top() < end
                            )
                            .toList();

            if (band.isEmpty()) {
                continue;
            }

            ParsedBankTransaction transaction =
                    parseEntry(
                            anchor,
                            band,
                            pageNumber
                    );

            transactions.add(transaction);
        }

        return transactions;
    }

    private ParsedBankTransaction parseEntry(
            PdfWord anchor,
            List<PdfWord> band,
            int pageNumber
    ) {
        int entryNumber =
                Integer.parseInt(
                        anchor
                                .text()
                                .substring(
                                        0,
                                        anchor.text().length() - 1
                                )
                );

        List<PdfWord> headerWords =
                band
                        .stream()
                        .filter(word ->
                                Math.abs(
                                        word.top()
                                                - anchor.top()
                                ) <= HEADER_LINE_TOLERANCE
                        )
                        .sorted(
                                Comparator.comparing(
                                        PdfWord::x0
                                )
                        )
                        .toList();

        LocalDate transactionDate =
                extractTransactionDate(
                        headerWords,
                        band,
                        anchor.top(),
                        entryNumber
                );

        BigDecimal credit =
                extractAmountFromColumn(
                        headerWords,
                        INCOME_COLUMN_FROM,
                        INCOME_COLUMN_TO
                );

        BigDecimal debit =
                extractAmountFromColumn(
                        headerWords,
                        EXPENSE_COLUMN_FROM,
                        EXPENSE_COLUMN_TO
                );

        BigDecimal balance =
                extractAmountNearAnchor(
                        band,
                        anchor.top(),
                        BALANCE_COLUMN_FROM,
                        BALANCE_COLUMN_TO
                );

        OrderData orderData =
                extractOrderData(
                        headerWords
                );

        List<PdfLine> lines =
                PdfLineGrouper.group(
                        band
                );

        String complaintReference =
                null;

        LocalDate executionDate =
                null;

        List<String> contentLines =
                new ArrayList<>();

        for (PdfLine line : lines) {
            String text =
                    normalizeText(
                            line.text()
                    );

            if (text.isBlank()) {
                continue;
            }

            if (
                    Math.abs(
                            line.top()
                                    - anchor.top()
                    ) <= HEADER_LINE_TOLERANCE
            ) {
                continue;
            }

            String lowercaseText =
                    text.toLowerCase(
                            Locale.forLanguageTag("sr")
                    );

            if (
                    lowercaseText.startsWith(
                            "poziv za reklamaciju"
                    )
            ) {
                Matcher matcher =
                        COMPLAINT_REFERENCE_PATTERN
                                .matcher(text);

                if (matcher.find()) {
                    complaintReference =
                            matcher.group(1);
                }

                continue;
            }

            if (
                    lowercaseText.startsWith(
                            "datum prijema"
                    )
                            || lowercaseText.startsWith(
                            "datum obrade"
                    )
            ) {
                Matcher matcher =
                        LONG_DATE_PATTERN.matcher(
                                text
                        );

                if (matcher.find()) {
                    executionDate =
                            parseLongDate(
                                    matcher.group(1),
                                    "datum izvršenja stavke "
                                            + entryNumber
                            );
                }

                continue;
            }

            if (
                    lowercaseText.startsWith(
                            "iznos transakcije:"
                    )
            ) {
                continue;
            }

            if (
                    isNoiseLine(
                            lowercaseText,
                            line
                    )
            ) {
                continue;
            }

            contentLines.add(text);
        }

        CounterpartyAndDescription content =
                resolveContent(
                        contentLines
                );

        String reference =
                complaintReference != null
                        && !complaintReference.isBlank()
                        ? complaintReference
                        : orderData.orderReference();

        return new ParsedBankTransaction(
                entryNumber,
                transactionDate,
                executionDate,
                debit,
                credit,
                balance,
                DEFAULT_CURRENCY,
                content.counterparty(),
                null,
                content.description(),
                reference,
                orderData.orderType(),
                orderData.orderReference(),
                pageNumber
        );
    }

    private LocalDate extractTransactionDate(
            List<PdfWord> headerWords,
            List<PdfWord> band,
            float anchorTop,
            int entryNumber
    ) {
        Optional<String> dateText =
                headerWords
                        .stream()
                        .map(PdfWord::text)
                        .filter(text ->
                                SHORT_DATE_PATTERN
                                        .matcher(text)
                                        .matches()
                        )
                        .findFirst();

        if (dateText.isEmpty()) {
            dateText =
                    band
                            .stream()
                            .filter(word ->
                                    SHORT_DATE_PATTERN
                                            .matcher(
                                                    word.text()
                                            )
                                            .matches()
                            )
                            .min(
                                    Comparator.comparingDouble(
                                            word ->
                                                    Math.abs(
                                                            word.top()
                                                                    - anchorTop
                                                    )
                                    )
                            )
                            .map(PdfWord::text);
        }

        if (dateText.isEmpty()) {
            String reconstructedHeader =
                    band
                            .stream()
                            .filter(word ->
                                    Math.abs(
                                            word.top()
                                                    - anchorTop
                                    ) <= 8F
                            )
                            .filter(word ->
                                    word.x0() < 160F
                            )
                            .sorted(
                                    Comparator.comparing(
                                            PdfWord::x0
                                    )
                            )
                            .map(PdfWord::text)
                            .reduce(
                                    "",
                                    (left, right) ->
                                            left + right
                            );

            Matcher dateMatcher =
                    SHORT_DATE_SEARCH_PATTERN
                            .matcher(
                                    reconstructedHeader
                            );

            if (dateMatcher.find()) {
                dateText =
                        Optional.of(
                                dateMatcher.group(1)
                        );
            }
        }

        if (dateText.isEmpty()) {
            throw new BankStatementParseException(
                    "Datum nije pronađen za AIK stavku "
                            + entryNumber
                            + "."
            );
        }

        try {
            return LocalDate.parse(
                    dateText.get(),
                    SHORT_DATE_FORMATTER
            );
        } catch (DateTimeParseException exception) {
            throw new BankStatementParseException(
                    "Datum nije ispravan za AIK stavku "
                            + entryNumber
                            + ": "
                            + dateText.get(),
                    exception
            );
        }
    }

    private BigDecimal extractAmountFromColumn(
            List<PdfWord> headerWords,
            float fromX,
            float toX
    ) {
        return headerWords
                .stream()
                .filter(word ->
                        word.x0() >= fromX
                                && word.x0() < toX
                )
                .map(PdfWord::text)
                .filter(text ->
                        AMOUNT_PATTERN
                                .matcher(text)
                                .matches()
                )
                .findFirst()
                .map(this::parseAmount)
                .orElse(null);
    }

    private BigDecimal extractAmountNearAnchor(
            List<PdfWord> words,
            float anchorTop,
            float fromX,
            float toX
    ) {
        return words
                .stream()
                .filter(word ->
                        word.x0() >= fromX
                                && word.x0() < toX
                )
                .filter(word ->
                        Math.abs(
                                word.top() - anchorTop
                        ) <= 6F
                )
                .filter(word ->
                        AMOUNT_PATTERN
                                .matcher(word.text())
                                .matches()
                )
                .min(
                        Comparator.comparingDouble(
                                word -> Math.abs(
                                        word.top() - anchorTop
                                )
                        )
                )
                .map(PdfWord::text)
                .map(this::parseAmount)
                .orElse(null);
    }

    private OrderData extractOrderData(
            List<PdfWord> headerWords
    ) {
        String middleHeader =
                headerWords
                        .stream()
                        .filter(word ->
                                word.x0()
                                        >= ORDER_COLUMN_FROM
                                        && word.x0()
                                        < ORDER_COLUMN_TO
                        )
                        .map(PdfWord::text)
                        .reduce(
                                "",
                                (left, right) ->
                                        left.isBlank()
                                                ? right
                                                : left + " " + right
                        )
                        .trim();

        Matcher matcher =
                ORDER_PATTERN.matcher(
                        middleHeader
                );

        if (!matcher.matches()) {
            return new OrderData(
                    null,
                    null
            );
        }

        String orderType =
                normalizeText(
                        matcher.group(1)
                );

        String orderReference =
                matcher
                        .group(2)
                        .replaceAll(
                                "\\s+",
                                ""
                        );

        return new OrderData(
                orderType,
                orderReference
        );
    }

    private boolean isNoiseLine(
            String lowercaseText,
            PdfLine line
    ) {
        if (
                lowercaseText.startsWith(
                        "asseco see"
                )
                        || lowercaseText.startsWith(
                        "aikbank ad"
                )
                        || lowercaseText.startsWith(
                        "strana:"
                )
                        || lowercaseText.startsWith(
                        "štampano:"
                )
        ) {
            return true;
        }

        if (
                lowercaseText.contains(
                        "ukupno za izvod"
                )
                        || lowercaseText.startsWith(
                        "prethodno stanje"
                )
                        || lowercaseText.startsWith(
                        "novo stanje"
                )
                        || lowercaseText.startsWith(
                        "ukupno uplata"
                )
                        || lowercaseText.startsWith(
                        "ukupno isplata"
                )
                        || lowercaseText.startsWith(
                        "nerealizovani čekovi"
                )
        ) {
            return true;
        }

        if (
                lowercaseText.contains("web:")
                        || lowercaseText.contains(
                        "tel.:"
                )
                        || lowercaseText.contains(
                        "e-mail:"
                )
        ) {
            return true;
        }

        return line
                .words()
                .stream()
                .allMatch(word ->
                        AMOUNT_PATTERN
                                .matcher(
                                        word.text()
                                )
                                .matches()
                );
    }

    private CounterpartyAndDescription resolveContent(
            List<String> contentLines
    ) {
        if (contentLines.isEmpty()) {
            return new CounterpartyAndDescription(
                    null,
                    null
            );
        }

        String firstLine =
                contentLines.getFirst();

        String normalizedFirstLine =
                firstLine.toLowerCase(
                        Locale.forLanguageTag("sr")
                );

        boolean bankFee =
                normalizedFirstLine.startsWith(
                        "provizija banke"
                )
                        || normalizedFirstLine.startsWith(
                        "naknada za korišcenje"
                )
                        || normalizedFirstLine.startsWith(
                        "naknada banke"
                );

        if (bankFee) {
            return new CounterpartyAndDescription(
                    BANK_NAME,
                    String.join(
                            " ",
                            contentLines
                    )
            );
        }

        if (contentLines.size() == 1) {
            return new CounterpartyAndDescription(
                    firstLine,
                    firstLine
            );
        }

        String description =
                String.join(
                        " ",
                        contentLines.subList(
                                1,
                                contentLines.size()
                        )
                );

        return new CounterpartyAndDescription(
                firstLine,
                description
        );
    }

    private void validateTransactions(
            List<ParsedBankTransaction> transactions
    ) {
        if (transactions.isEmpty()) {
            throw new BankStatementParseException(
                    "AIK izvod ne sadrži prepoznate transakcije."
            );
        }

        for (
                ParsedBankTransaction transaction
                : transactions
        ) {
            boolean hasCredit =
                    transaction.credit() != null
                            && transaction
                            .credit()
                            .compareTo(
                                    BigDecimal.ZERO
                            ) > 0;

            boolean hasDebit =
                    transaction.debit() != null
                            && transaction
                            .debit()
                            .compareTo(
                                    BigDecimal.ZERO
                            ) > 0;

            if (transaction.balance() == null) {
                throw new BankStatementParseException(
                        "Stanje nije pronađeno za AIK stavku "
                                + transaction.entryNumber()
                                + "."
                );
            }

            if (hasCredit == hasDebit) {
                throw new BankStatementParseException(
                        "Nije jednoznačno utvrđena uplata ili isplata "
                                + "za AIK stavku "
                                + transaction.entryNumber()
                                + "."
                );
            }

            if (
                    transaction.description() == null
                            || transaction
                            .description()
                            .isBlank()
            ) {
                throw new BankStatementParseException(
                        "Opis nije pronađen za AIK stavku "
                                + transaction.entryNumber()
                                + "."
                );
            }
        }

        List<Integer> entryNumbers =
                transactions
                        .stream()
                        .map(
                                ParsedBankTransaction::entryNumber
                        )
                        .toList();

        int firstEntryNumber =
                entryNumbers.getFirst();

        for (
                int index = 0;
                index < entryNumbers.size();
                index++
        ) {
            int expectedEntryNumber =
                    firstEntryNumber + index;

            if (
                    entryNumbers.get(index)
                            != expectedEntryNumber
            ) {
                throw new BankStatementParseException(
                        "Redni brojevi AIK stavki nisu neprekidni: "
                                + entryNumbers
                );
            }
        }
    }

    private BigDecimal parseAmount(
            String text
    ) {
        try {
            return new BigDecimal(
                    text.replace(
                            ",",
                            ""
                    )
            );
        } catch (NumberFormatException exception) {
            throw new BankStatementParseException(
                    "Iznos iz AIK izvoda nije ispravan: "
                            + text,
                    exception
            );
        }
    }

    private LocalDate parseLongDate(
            String text,
            String fieldName
    ) {
        try {
            return LocalDate.parse(
                    text,
                    LONG_DATE_FORMATTER
            );
        } catch (DateTimeParseException exception) {
            throw new BankStatementParseException(
                    "Neispravan "
                            + fieldName
                            + ": "
                            + text,
                    exception
            );
        }
    }

    private String normalizeAccountNumber(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value
                .replaceAll(
                        "\\s+",
                        ""
                )
                .trim();
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private record StatementMetadata(

            String statementNumber,

            String accountNumber,

            LocalDate periodFrom,

            LocalDate periodTo,

            String statementId

    ) {
    }

    private record OrderData(

            String orderType,

            String orderReference

    ) {
    }

    private record CounterpartyAndDescription(

            String counterparty,

            String description

    ) {
    }
}