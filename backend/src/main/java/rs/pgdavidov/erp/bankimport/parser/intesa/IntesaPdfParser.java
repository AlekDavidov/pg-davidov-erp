package rs.pgdavidov.erp.bankimport.parser.intesa;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rs.pgdavidov.erp.bankimport.exception.BankStatementParseException;
import rs.pgdavidov.erp.bankimport.model.ParsedBankStatement;
import rs.pgdavidov.erp.bankimport.model.ParsedBankTransaction;
import rs.pgdavidov.erp.bankimport.parser.BankStatementParser;
import rs.pgdavidov.erp.bankimport.parser.pdf.PdfWordExtractor;
import rs.pgdavidov.erp.bankimport.parser.pdf.PdfWordExtractor.PdfWord;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IntesaPdfParser
        implements BankStatementParser {

    private static final String BANK_CODE =
            "INTESA";

    private static final String BANK_NAME =
            "Banca Intesa";

    private static final String DEFAULT_CURRENCY =
            "RSD";

    private static final Pattern DATE_PATTERN =
            Pattern.compile(
                    "^\\d{2}\\.\\d{2}\\.\\d{4}$"
            );

    private static final Pattern HEADER_PATTERN =
            Pattern.compile(
                    "broj\\s*([0-9]{10,})"
                            + "\\s*za\\s*mesec\\s*"
                            + "([\\p{L}]+)\\s*/\\s*(\\d{4})",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE
            );

    private static final Pattern OPENING_BALANCE_PATTERN =
            Pattern.compile(
                    "Početno\\s*stanje\\s*:\\s*"
                            + "RSD\\s*"
                            + "(-?[0-9.]+,[0-9]{2})",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE
            );

    private static final Pattern SERBIAN_AMOUNT_PATTERN =
            Pattern.compile(
                    "^-?(?:\\d{1,3}(?:\\.\\d{3})*|\\d+),\\d{2}$"
            );

    private static final float ENTRY_DATE_FROM =
            30F;

    private static final float ENTRY_DATE_TO =
            85F;

    private static final float EXECUTION_DATE_FROM =
            130F;

    private static final float EXECUTION_DATE_TO =
            180F;

    private static final float DESCRIPTION_FROM =
            180F;

    private static final float DESCRIPTION_TO =
            274F;

    private static final float DEBIT_FROM =
            274F;

    private static final float DEBIT_TO =
            316F;

    private static final float CREDIT_FROM =
            316F;

    private static final float CREDIT_TO =
            370F;

    private static final float BALANCE_FROM =
            375F;

    private static final float BALANCE_TO =
            430F;

    private static final float COUNTERPARTY_FROM =
            425F;

    private static final float COUNTERPARTY_TO =
            505F;

    private static final float COUNTERPARTY_ACCOUNT_FROM =
            505F;

    private static final float COUNTERPARTY_ACCOUNT_TO =
            600F;

    private static final float REFERENCE_FROM =
            765F;

    private static final float REFERENCE_TO =
            835F;

    private static final float ENTRY_START_OFFSET =
            2F;

    private static final float PAGE_BOTTOM_MARGIN =
            35F;

    private static final float AMOUNT_CONTINUATION_HEIGHT =
            14F;

    @Override
    public boolean supports(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
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

            String normalized =
                    normalizeText(headerText)
                            .toLowerCase(Locale.ROOT);

            boolean intesaDocument =
                    normalized.contains(
                            "bancaintesa.rs"
                    )
                            || normalized.contains(
                            "banca intesa"
                    );

            boolean rsdStatement =
                    OPENING_BALANCE_PATTERN
                            .matcher(headerText)
                            .find();

            return intesaDocument
                    && rsdStatement;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public String getParserName() {
        return "Banca Intesa RSD PDF parser";
    }

    @Override
    public ParsedBankStatement parse(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            throw new BankStatementParseException(
                    "Banca Intesa izvod nije prosleđen."
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
                            words,
                            metadata
                    );

            validateTransactions(
                    transactions,
                    metadata.openingBalance()
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
                    metadata.openingBalance(),
                    null,
                    null,
                    null,
                    transactions
            );
        } catch (BankStatementParseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BankStatementParseException(
                    "Banca Intesa RSD izvod nije mogao da bude parsiran.",
                    exception
            );
        }
    }

    private StatementMetadata extractMetadata(
            PDDocument document
    ) throws IOException {
        String headerText =
                extractHeaderText(document);

        Matcher headerMatcher =
                HEADER_PATTERN.matcher(
                        headerText
                );

        if (!headerMatcher.find()) {
            throw new BankStatementParseException(
                    "Broj računa ili mesec Banca Intesa izvoda nisu pronađeni."
            );
        }

        String accountNumber =
                normalizeAccountNumber(
                        headerMatcher.group(1)
                );

        int month =
                resolveMonth(
                        headerMatcher.group(2)
                );

        int year =
                Integer.parseInt(
                        headerMatcher.group(3)
                );

        YearMonth yearMonth =
                YearMonth.of(
                        year,
                        month
                );

        LocalDate periodFrom =
                yearMonth.atDay(1);

        LocalDate periodTo =
                yearMonth.atEndOfMonth();

        String statementNumber =
                "%04d%02d".formatted(
                        year,
                        month
                );

        String statementId =
                "%s-%s-%s".formatted(
                        BANK_CODE,
                        accountNumber,
                        statementNumber
                );

        Matcher openingBalanceMatcher =
                OPENING_BALANCE_PATTERN.matcher(
                        headerText
                );

        if (!openingBalanceMatcher.find()) {
            throw new BankStatementParseException(
                    "Početno RSD stanje Banca Intesa izvoda nije pronađeno."
            );
        }

        BigDecimal openingBalance =
                parseAmount(
                        openingBalanceMatcher.group(1)
                );

        return new StatementMetadata(
                statementNumber,
                accountNumber,
                periodFrom,
                periodTo,
                statementId,
                openingBalance
        );
    }

    private String extractHeaderText(
            PDDocument document
    ) throws IOException {
        PDFTextStripper textStripper =
                new PDFTextStripper();

        textStripper.setSortByPosition(true);
        textStripper.setStartPage(1);
        textStripper.setEndPage(1);

        return textStripper.getText(document);
    }

    private List<ParsedBankTransaction> parseTransactions(
            PDDocument document,
            List<PdfWord> words,
            StatementMetadata metadata
    ) {
        List<ParsedBankTransaction> transactions =
                new ArrayList<>();

        int entryNumber = 1;

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

            List<PdfWord> anchors =
                    findEntryAnchors(
                            pageWords
                    );

            for (
                    int index = 0;
                    index < anchors.size();
                    index++
            ) {
                PdfWord anchor =
                        anchors.get(index);

                float start =
                        anchor.top()
                                - ENTRY_START_OFFSET;

                float end =
                        index + 1 < anchors.size()
                                ? anchors
                                .get(index + 1)
                                .top()
                                - ENTRY_START_OFFSET
                                : resolveLastEntryEnd(
                                pageWords,
                                anchor,
                                pageHeight
                        );

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
                                entryNumber,
                                anchor,
                                band,
                                currentPageNumber,
                                metadata
                        );

                transactions.add(
                        transaction
                );

                entryNumber++;
            }
        }

        return transactions;
    }

    private List<PdfWord> findEntryAnchors(
            List<PdfWord> pageWords
    ) {
        return pageWords
                .stream()
                .filter(word ->
                        word.x0() >= ENTRY_DATE_FROM
                                && word.x0() < ENTRY_DATE_TO
                )
                .filter(word ->
                        DATE_PATTERN
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
    }

    private float resolveLastEntryEnd(
            List<PdfWord> pageWords,
            PdfWord anchor,
            float pageHeight
    ) {
        float defaultEnd =
                pageHeight
                        - PAGE_BOTTOM_MARGIN;

        return pageWords
                .stream()
                .filter(word ->
                        word.top() > anchor.top()
                )
                .filter(word -> {
                    String normalized =
                            normalizeText(
                                    word.text()
                            )
                                    .toUpperCase(
                                            Locale.ROOT
                                    );

                    return normalized.startsWith(
                            "DOZVOLJENO"
                    );
                })
                .map(PdfWord::top)
                .min(Float::compare)
                .map(top ->
                        Math.min(
                                defaultEnd,
                                top - ENTRY_START_OFFSET
                        )
                )
                .orElse(
                        defaultEnd
                );
    }

    private ParsedBankTransaction parseEntry(
            int entryNumber,
            PdfWord anchor,
            List<PdfWord> band,
            int pageNumber,
            StatementMetadata metadata
    ) {
        LocalDate transactionDate =
                parseDate(
                        anchor.text(),
                        "datum knjiženja stavke "
                                + entryNumber
                );

        LocalDate executionDate =
                extractDateFromColumn(
                        band,
                        anchor.top(),
                        EXECUTION_DATE_FROM,
                        EXECUTION_DATE_TO
                );

        BigDecimal debit =
                extractAmountFromColumn(
                        band,
                        anchor.top(),
                        DEBIT_FROM,
                        DEBIT_TO
                );

        BigDecimal credit =
                extractAmountFromColumn(
                        band,
                        anchor.top(),
                        CREDIT_FROM,
                        CREDIT_TO
                );

        BigDecimal balance =
                extractAmountFromColumn(
                        band,
                        anchor.top(),
                        BALANCE_FROM,
                        BALANCE_TO
                );

        String description =
                extractTextFromColumn(
                        band,
                        DESCRIPTION_FROM,
                        DESCRIPTION_TO,
                        true
                );

        String counterparty =
                extractTextFromColumn(
                        band,
                        COUNTERPARTY_FROM,
                        COUNTERPARTY_TO,
                        true
                );

        String counterpartyAccount =
                extractTextFromColumn(
                        band,
                        COUNTERPARTY_ACCOUNT_FROM,
                        COUNTERPARTY_ACCOUNT_TO,
                        false
                );

        String reference =
                extractTextFromColumn(
                        band,
                        REFERENCE_FROM,
                        REFERENCE_TO,
                        false
                );

        if (
                counterparty == null
                        || counterparty.isBlank()
        ) {
            counterparty =
                    resolveFallbackCounterparty(
                            description
                    );
        }

        if (
                transactionDate.isBefore(
                        metadata.periodFrom()
                )
                        || transactionDate.isAfter(
                        metadata.periodTo()
                )
        ) {
            throw new BankStatementParseException(
                    "Datum knjiženja Banca Intesa stavke "
                            + entryNumber
                            + " nije u periodu izvoda: "
                            + transactionDate
                            + "."
            );
        }

        return new ParsedBankTransaction(
                entryNumber,
                transactionDate,
                executionDate,
                debit,
                credit,
                balance,
                DEFAULT_CURRENCY,
                counterparty,
                counterpartyAccount,
                description,
                reference,
                null,
                reference,
                pageNumber
        );
    }

    private LocalDate extractDateFromColumn(
            List<PdfWord> band,
            float anchorTop,
            float fromX,
            float toX
    ) {
        return band
                .stream()
                .filter(word ->
                        word.x0() >= fromX
                                && word.x0() < toX
                )
                .filter(word ->
                        Math.abs(
                                word.top()
                                        - anchorTop
                        ) <= 3F
                )
                .map(PdfWord::text)
                .filter(text ->
                        DATE_PATTERN
                                .matcher(text)
                                .matches()
                )
                .findFirst()
                .map(text ->
                        parseDate(
                                text,
                                "datum prijema naloga"
                        )
                )
                .orElse(null);
    }

    private BigDecimal extractAmountFromColumn(
            List<PdfWord> band,
            float anchorTop,
            float fromX,
            float toX
    ) {
        List<PdfWord> candidates =
                band
                        .stream()
                        .filter(word ->
                                word.x0() >= fromX
                                        && word.x0() < toX
                        )
                        .filter(word ->
                                word.top()
                                        >= anchorTop - 3F
                                        && word.top()
                                        <= anchorTop
                                        + AMOUNT_CONTINUATION_HEIGHT
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                PdfWord::top
                                        )
                                        .thenComparing(
                                                PdfWord::x0
                                        )
                        )
                        .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        StringBuilder value =
                new StringBuilder();

        for (PdfWord candidate : candidates) {
            String text =
                    normalizeText(
                            candidate.text()
                    )
                            .replace(" ", "");

            if (text.isBlank()) {
                continue;
            }

            value.append(text);

            String current =
                    value.toString();

            if (
                    SERBIAN_AMOUNT_PATTERN
                            .matcher(current)
                            .matches()
            ) {
                return parseAmount(
                        current
                );
            }
        }

        return null;
    }

    private String extractTextFromColumn(
            List<PdfWord> band,
            float fromX,
            float toX,
            boolean useSpaces
    ) {
        List<PdfWord> words =
                band
                        .stream()
                        .filter(word ->
                                word.x0() >= fromX
                                        && word.x0() < toX
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                PdfWord::top
                                        )
                                        .thenComparing(
                                                PdfWord::x0
                                        )
                        )
                        .toList();

        if (words.isEmpty()) {
            return null;
        }

        String separator =
                useSpaces
                        ? " "
                        : "";

        String value =
                words
                        .stream()
                        .map(PdfWord::text)
                        .map(this::normalizeText)
                        .filter(text ->
                                !text.isBlank()
                        )
                        .reduce(
                                "",
                                (left, right) ->
                                        left.isBlank()
                                                ? right
                                                : left
                                                + separator
                                                + right
                        );

        value =
                normalizeText(
                        value
                );

        return value.isBlank()
                ? null
                : value;
    }

    private String resolveFallbackCounterparty(
            String description
    ) {
        if (
                description == null
                        || description.isBlank()
        ) {
            return null;
        }

        String normalized =
                description
                        .toLowerCase(
                                Locale.forLanguageTag(
                                        "sr"
                                )
                        );

        if (
                normalized.contains(
                        "naknada"
                )
                        || normalized.contains(
                        "kamate"
                )
                        || normalized.contains(
                        "kamat"
                )
                        || normalized.contains(
                        "kredit"
                )
                        || normalized.contains(
                        "kartic"
                )
        ) {
            return BANK_NAME;
        }

        return null;
    }

    private void validateTransactions(
            List<ParsedBankTransaction> transactions,
            BigDecimal openingBalance
    ) {
        if (transactions.isEmpty()) {
            throw new BankStatementParseException(
                    "Banca Intesa izvod ne sadrži prepoznate transakcije."
            );
        }

        BigDecimal runningBalance =
                openingBalance;

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

            if (hasCredit == hasDebit) {
                throw new BankStatementParseException(
                        "Nije jednoznačno utvrđena uplata ili isplata "
                                + "za Banca Intesa stavku "
                                + transaction.entryNumber()
                                + "."
                );
            }

            if (transaction.balance() == null) {
                throw new BankStatementParseException(
                        "Stanje nije pronađeno za Banca Intesa stavku "
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
                        "Opis nije pronađen za Banca Intesa stavku "
                                + transaction.entryNumber()
                                + "."
                );
            }

            BigDecimal debit =
                    transaction.debit() != null
                            ? transaction.debit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    transaction.credit() != null
                            ? transaction.credit()
                            : BigDecimal.ZERO;

            BigDecimal expectedBalance =
                    runningBalance
                            .subtract(debit)
                            .add(credit);

            if (
                    expectedBalance.compareTo(
                            transaction.balance()
                    ) != 0
            ) {
                throw new BankStatementParseException(
                        "Kontrola stanja nije prošla za Banca Intesa stavku "
                                + transaction.entryNumber()
                                + ". Očekivano stanje: "
                                + expectedBalance
                                + ", pronađeno stanje: "
                                + transaction.balance()
                                + "."
                );
            }

            runningBalance =
                    transaction.balance();
        }
    }

    private LocalDate parseDate(
            String value,
            String fieldName
    ) {
        try {
            String[] parts =
                    value.split("\\.");

            if (parts.length != 3) {
                throw new IllegalArgumentException();
            }

            return LocalDate.of(
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[0])
            );
        } catch (Exception exception) {
            throw new BankStatementParseException(
                    "Neispravan "
                            + fieldName
                            + ": "
                            + value,
                    exception
            );
        }
    }

    private BigDecimal parseAmount(
            String value
    ) {
        try {
            return new BigDecimal(
                    value
                            .replace(".", "")
                            .replace(",", ".")
            );
        } catch (NumberFormatException exception) {
            throw new BankStatementParseException(
                    "Iznos iz Banca Intesa izvoda nije ispravan: "
                            + value,
                    exception
            );
        }
    }

    private int resolveMonth(
            String monthName
    ) {
        String normalized =
                normalizeText(
                        monthName
                )
                        .toLowerCase(
                                Locale.forLanguageTag(
                                        "sr"
                                )
                        );

        return switch (normalized) {
            case "januar" -> 1;
            case "februar" -> 2;
            case "mart" -> 3;
            case "april" -> 4;
            case "maj" -> 5;
            case "jun" -> 6;
            case "jul" -> 7;
            case "avgust" -> 8;
            case "septembar" -> 9;
            case "oktobar" -> 10;
            case "novembar" -> 11;
            case "decembar" -> 12;
            default ->
                    throw new BankStatementParseException(
                            "Nepoznat mesec u Banca Intesa izvodu: "
                                    + monthName
                                    + "."
                    );
        };
    }

    private String normalizeAccountNumber(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value
                .replaceAll(
                        "\\D",
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

            String statementId,

            BigDecimal openingBalance

    ) {
    }
}