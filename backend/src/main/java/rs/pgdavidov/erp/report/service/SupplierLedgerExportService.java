package rs.pgdavidov.erp.report.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import rs.pgdavidov.erp.report.dto.SupplierLedgerEntryResponse;
import rs.pgdavidov.erp.report.dto.SupplierLedgerExport;
import rs.pgdavidov.erp.report.dto.SupplierLedgerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierLedgerExportService {

    private static final String SHEET_NAME =
            "Kartica dobavljača";

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final SupplierLedgerService supplierLedgerService;

    public SupplierLedgerExport export(
            UUID supplierId,
            LocalDate periodFrom,
            LocalDate periodTo
    ) {
        SupplierLedgerResponse ledger =
                supplierLedgerService.getLedger(
                        supplierId,
                        periodFrom,
                        periodTo
                );

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            Sheet sheet =
                    workbook.createSheet(SHEET_NAME);

            Styles styles =
                    createStyles(workbook);

            createHeader(
                    sheet,
                    ledger,
                    styles
            );

            createSummary(
                    sheet,
                    ledger,
                    styles
            );

            int tableHeaderRowIndex = 12;

            createTableHeader(
                    sheet,
                    tableHeaderRowIndex,
                    styles
            );

            int lastDataRow =
                    createEntries(
                            sheet,
                            tableHeaderRowIndex + 1,
                            ledger,
                            styles
                    );

            int totalsRow =
                    Math.max(
                            lastDataRow + 2,
                            tableHeaderRowIndex + 2
                    );

            createTotals(
                    sheet,
                    totalsRow,
                    ledger,
                    styles
            );

            configureSheet(
                    sheet,
                    tableHeaderRowIndex,
                    lastDataRow
            );

            workbook.write(outputStream);

            return new SupplierLedgerExport(
                    createFilename(ledger),
                    outputStream.toByteArray()
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Supplier ledger Excel export could not be generated.",
                    exception
            );
        }
    }

    private void createHeader(
            Sheet sheet,
            SupplierLedgerResponse ledger,
            Styles styles
    ) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(32);

        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("KARTICA DOBAVLJAČA");
        titleCell.setCellStyle(styles.title());

        sheet.addMergedRegion(
                new CellRangeAddress(0, 0, 0, 6)
        );

        Row companyRow = sheet.createRow(2);
        companyRow.setHeightInPoints(22);

        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue("PG Davidov");
        companyCell.setCellStyle(styles.companyTitle());

        sheet.addMergedRegion(
                new CellRangeAddress(2, 2, 0, 2)
        );

        createHeaderValueRow(
                sheet,
                2,
                "Dobavljač:",
                ledger.supplierName(),
                styles
        );

        createHeaderValueRow(
                sheet,
                3,
                "Šifra:",
                ledger.supplierCode(),
                styles
        );

        createHeaderValueRow(
                sheet,
                4,
                "PIB:",
                resolveText(ledger.pib()),
                styles
        );

        createHeaderValueRow(
                sheet,
                5,
                "Period:",
                formatDate(ledger.periodFrom())
                        + " - "
                        + formatDate(ledger.periodTo()),
                styles
        );
    }

    private void createHeaderValueRow(
            Sheet sheet,
            int rowIndex,
            String label,
            String value,
            Styles styles
    ) {
        Row row =
                sheet.getRow(rowIndex);

        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell labelCell = row.createCell(3);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.label());

        Cell valueCell = row.createCell(4);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(styles.value());

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        4,
                        6
                )
        );
    }

    private void createSummary(
            Sheet sheet,
            SupplierLedgerResponse ledger,
            Styles styles
    ) {
        Row labelRow = sheet.createRow(8);
        Row valueRow = sheet.createRow(9);

        labelRow.setHeightInPoints(22);
        valueRow.setHeightInPoints(26);

        createSummaryBlock(
                sheet,
                labelRow,
                valueRow,
                0,
                1,
                "Početno stanje",
                ledger.openingBalance(),
                styles
        );

        createSummaryBlock(
                sheet,
                labelRow,
                valueRow,
                2,
                3,
                "Fakturisano",
                ledger.totalInvoiced(),
                styles
        );

        createSummaryBlock(
                sheet,
                labelRow,
                valueRow,
                4,
                5,
                "Plaćeno",
                ledger.totalPaid(),
                styles
        );

        createSummaryBlock(
                sheet,
                labelRow,
                valueRow,
                6,
                6,
                "Završni saldo",
                ledger.closingBalance(),
                styles
        );
    }

    private void createSummaryBlock(
            Sheet sheet,
            Row labelRow,
            Row valueRow,
            int startColumn,
            int endColumn,
            String label,
            BigDecimal amount,
            Styles styles
    ) {
        Cell labelCell =
                labelRow.createCell(startColumn);

        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.summaryLabel());

        Cell valueCell =
                valueRow.createCell(startColumn);

        setAmount(valueCell, amount);
        valueCell.setCellStyle(styles.summaryAmount());

        for (
                int column = startColumn + 1;
                column <= endColumn;
                column++
        ) {
            Cell labelFill =
                    labelRow.createCell(column);

            labelFill.setCellStyle(
                    styles.summaryLabel()
            );

            Cell valueFill =
                    valueRow.createCell(column);

            valueFill.setCellStyle(
                    styles.summaryAmount()
            );
        }

        if (endColumn > startColumn) {
            sheet.addMergedRegion(
                    new CellRangeAddress(
                            labelRow.getRowNum(),
                            labelRow.getRowNum(),
                            startColumn,
                            endColumn
                    )
            );

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            valueRow.getRowNum(),
                            valueRow.getRowNum(),
                            startColumn,
                            endColumn
                    )
            );
        }
    }

    private void createTableHeader(
            Sheet sheet,
            int rowIndex,
            Styles styles
    ) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(24);

        String[] headers = {
                "Datum",
                "Broj fakture",
                "Br. izvoda",
                "Referenca transakcije",
                "Plaćeno",
                "Iznos fakture",
                "Saldo"
        };

        for (
                int column = 0;
                column < headers.length;
                column++
        ) {
            Cell cell = row.createCell(column);

            cell.setCellValue(
                    headers[column]
            );

            cell.setCellStyle(
                    styles.tableHeader()
            );
        }
    }

    private int createEntries(
            Sheet sheet,
            int firstRowIndex,
            SupplierLedgerResponse ledger,
            Styles styles
    ) {
        int rowIndex = firstRowIndex;

        for (
                SupplierLedgerEntryResponse entry
                : ledger.entries()
        ) {
            Row row = sheet.createRow(rowIndex);
            row.setHeightInPoints(21);

            createDateCell(
                    row,
                    0,
                    entry.date(),
                    styles.date()
            );

            createTextCell(
                    row,
                    1,
                    entry.invoiceNumber(),
                    styles.tableText()
            );

            createTextCell(
                    row,
                    2,
                    entry.statementCode(),
                    styles.tableText()
            );

            createTextCell(
                    row,
                    3,
                    entry.transactionReference(),
                    styles.tableText()
            );

            createAmountCell(
                    row,
                    4,
                    entry.paidAmount(),
                    styles.amount()
            );

            createAmountCell(
                    row,
                    5,
                    entry.invoiceAmount(),
                    styles.amount()
            );

            createAmountCell(
                    row,
                    6,
                    entry.balance(),
                    styles.balance()
            );

            rowIndex++;
        }

        return rowIndex - 1;
    }

    private void createTotals(
            Sheet sheet,
            int rowIndex,
            SupplierLedgerResponse ledger,
            Styles styles
    ) {
        createTotalRow(
                sheet,
                rowIndex,
                "UKUPNO FAKTURISANO",
                ledger.totalInvoiced(),
                styles
        );

        createTotalRow(
                sheet,
                rowIndex + 1,
                "UKUPNO PLAĆENO",
                ledger.totalPaid(),
                styles
        );

        createTotalRow(
                sheet,
                rowIndex + 2,
                "ZAVRŠNI SALDO",
                ledger.closingBalance(),
                styles
        );
    }

    private void createTotalRow(
            Sheet sheet,
            int rowIndex,
            String label,
            BigDecimal amount,
            Styles styles
    ) {
        Row row = sheet.createRow(rowIndex);

        Cell labelCell = row.createCell(4);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.totalLabel());

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        4,
                        5
                )
        );

        Cell amountCell = row.createCell(6);

        setAmount(
                amountCell,
                amount
        );

        amountCell.setCellStyle(
                styles.totalAmount()
        );
    }

    private void configureSheet(
            Sheet sheet,
            int tableHeaderRowIndex,
            int lastDataRow
    ) {
        sheet.setColumnWidth(
                0,
                14 * 256
        );

        sheet.setColumnWidth(
                1,
                21 * 256
        );

        sheet.setColumnWidth(
                2,
                23 * 256
        );

        sheet.setColumnWidth(
                3,
                30 * 256
        );

        sheet.setColumnWidth(
                4,
                17 * 256
        );

        sheet.setColumnWidth(
                5,
                18 * 256
        );

        sheet.setColumnWidth(
                6,
                18 * 256
        );

        sheet.createFreezePane(
                0,
                tableHeaderRowIndex + 1
        );

        if (
                lastDataRow >=
                        tableHeaderRowIndex + 1
        ) {
            sheet.setAutoFilter(
                    new CellRangeAddress(
                            tableHeaderRowIndex,
                            lastDataRow,
                            0,
                            6
                    )
            );
        }

        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);

        PrintSetup printSetup =
                sheet.getPrintSetup();

        printSetup.setLandscape(true);

        printSetup.setPaperSize(
                PrintSetup.A4_PAPERSIZE
        );

        printSetup.setFitWidth(
                (short) 1
        );

        printSetup.setFitHeight(
                (short) 0
        );

        sheet.setMargin(
                Sheet.LeftMargin,
                0.3
        );

        sheet.setMargin(
                Sheet.RightMargin,
                0.3
        );

        sheet.setMargin(
                Sheet.TopMargin,
                0.5
        );

        sheet.setMargin(
                Sheet.BottomMargin,
                0.5
        );

        sheet.setHorizontallyCenter(true);
    }

    private void createDateCell(
            Row row,
            int column,
            LocalDate value,
            CellStyle style
    ) {
        Cell cell = row.createCell(column);

        if (value != null) {
            cell.setCellValue(value);
        }

        cell.setCellStyle(style);
    }

    private void createTextCell(
            Row row,
            int column,
            String value,
            CellStyle style
    ) {
        Cell cell = row.createCell(column);

        cell.setCellValue(
                resolveText(value)
        );

        cell.setCellStyle(style);
    }

    private void createAmountCell(
            Row row,
            int column,
            BigDecimal value,
            CellStyle style
    ) {
        Cell cell = row.createCell(column);

        if (value != null) {
            setAmount(cell, value);
        }

        cell.setCellStyle(style);
    }

    private void setAmount(
            Cell cell,
            BigDecimal amount
    ) {
        if (amount == null) {
            return;
        }

        cell.setCellValue(
                amount.doubleValue()
        );
    }

    private Styles createStyles(
            Workbook workbook
    ) {
        DataFormat dataFormat =
                workbook.createDataFormat();

        CellStyle title =
                workbook.createCellStyle();

        Font titleFont =
                workbook.createFont();

        titleFont.setBold(true);
        titleFont.setFontHeightInPoints(
                (short) 18
        );

        title.setFont(titleFont);

        title.setAlignment(
                HorizontalAlignment.CENTER
        );

        title.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        CellStyle companyTitle =
                workbook.createCellStyle();

        Font companyFont =
                workbook.createFont();

        companyFont.setBold(true);

        companyFont.setFontHeightInPoints(
                (short) 14
        );

        companyTitle.setFont(companyFont);

        CellStyle label =
                workbook.createCellStyle();

        Font labelFont =
                workbook.createFont();

        labelFont.setBold(true);

        label.setFont(labelFont);

        CellStyle value =
                workbook.createCellStyle();

        CellStyle summaryLabel =
                workbook.createCellStyle();

        Font summaryLabelFont =
                workbook.createFont();

        summaryLabelFont.setBold(true);

        summaryLabel.setFont(
                summaryLabelFont
        );

        summaryLabel.setAlignment(
                HorizontalAlignment.CENTER
        );

        summaryLabel.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        summaryLabel.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT
                        .getIndex()
        );

        summaryLabel.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        applyBorders(summaryLabel);

        CellStyle summaryAmount =
                workbook.createCellStyle();

        Font summaryAmountFont =
                workbook.createFont();

        summaryAmountFont.setBold(true);

        summaryAmountFont.setFontHeightInPoints(
                (short) 12
        );

        summaryAmount.setFont(
                summaryAmountFont
        );

        summaryAmount.setAlignment(
                HorizontalAlignment.CENTER
        );

        summaryAmount.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        summaryAmount.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.00"
                )
        );

        applyBorders(summaryAmount);

        CellStyle tableHeader =
                workbook.createCellStyle();

        Font tableHeaderFont =
                workbook.createFont();

        tableHeaderFont.setBold(true);

        tableHeaderFont.setColor(
                IndexedColors.WHITE.getIndex()
        );

        tableHeader.setFont(
                tableHeaderFont
        );

        tableHeader.setFillForegroundColor(
                IndexedColors.DARK_BLUE
                        .getIndex()
        );

        tableHeader.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        tableHeader.setAlignment(
                HorizontalAlignment.CENTER
        );

        tableHeader.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        applyBorders(tableHeader);

        CellStyle date =
                workbook.createCellStyle();

        date.setDataFormat(
                dataFormat.getFormat(
                        "dd.MM.yyyy"
                )
        );

        applyBorders(date);

        CellStyle tableText =
                workbook.createCellStyle();

        tableText.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        applyBorders(tableText);

        CellStyle amount =
                workbook.createCellStyle();

        amount.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.00"
                )
        );

        amount.setAlignment(
                HorizontalAlignment.RIGHT
        );

        applyBorders(amount);

        CellStyle balance =
                workbook.createCellStyle();

        Font balanceFont =
                workbook.createFont();

        balanceFont.setBold(true);

        balance.setFont(balanceFont);

        balance.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.00"
                )
        );

        balance.setAlignment(
                HorizontalAlignment.RIGHT
        );

        applyBorders(balance);

        CellStyle totalLabel =
                workbook.createCellStyle();

        Font totalLabelFont =
                workbook.createFont();

        totalLabelFont.setBold(true);

        totalLabel.setFont(
                totalLabelFont
        );

        totalLabel.setAlignment(
                HorizontalAlignment.RIGHT
        );

        CellStyle totalAmount =
                workbook.createCellStyle();

        Font totalAmountFont =
                workbook.createFont();

        totalAmountFont.setBold(true);

        totalAmount.setFont(
                totalAmountFont
        );

        totalAmount.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.00"
                )
        );

        totalAmount.setAlignment(
                HorizontalAlignment.RIGHT
        );

        return new Styles(
                title,
                companyTitle,
                label,
                value,
                summaryLabel,
                summaryAmount,
                tableHeader,
                date,
                tableText,
                amount,
                balance,
                totalLabel,
                totalAmount
        );
    }

    private void applyBorders(
            CellStyle style
    ) {
        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );
    }

    private String createFilename(
            SupplierLedgerResponse ledger
    ) {
        String supplierName =
                ledger.supplierName()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replaceAll(
                                "[^a-z0-9čćžšđ]+",
                                "-"
                        )
                        .replaceAll(
                                "^-|-$",
                                ""
                        );

        if (supplierName.isBlank()) {
            supplierName =
                    ledger.supplierCode()
                            .toLowerCase(
                                    Locale.ROOT
                            );
        }

        return "kartica-dobavljaca-"
                + supplierName
                + "-"
                + FILE_DATE_FORMAT.format(
                ledger.periodFrom()
        )
                + "-"
                + FILE_DATE_FORMAT.format(
                ledger.periodTo()
        )
                + ".xlsx";
    }

    private String formatDate(
            LocalDate date
    ) {
        if (date == null) {
            return "";
        }

        return DISPLAY_DATE_FORMAT
                .format(date);
    }

    private String resolveText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return "—";
        }

        return value;
    }

    private record Styles(

            CellStyle title,

            CellStyle companyTitle,

            CellStyle label,

            CellStyle value,

            CellStyle summaryLabel,

            CellStyle summaryAmount,

            CellStyle tableHeader,

            CellStyle date,

            CellStyle tableText,

            CellStyle amount,

            CellStyle balance,

            CellStyle totalLabel,

            CellStyle totalAmount

    ) {
    }
}