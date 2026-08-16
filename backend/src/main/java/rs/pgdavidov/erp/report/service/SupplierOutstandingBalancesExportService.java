package rs.pgdavidov.erp.report.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
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
import rs.pgdavidov.erp.company.dto.CompanyLogoResponse;
import rs.pgdavidov.erp.company.dto.CompanyProfileResponse;
import rs.pgdavidov.erp.company.service.CompanyProfileService;
import rs.pgdavidov.erp.report.dto.SupplierOutstandingBalanceResponse;
import rs.pgdavidov.erp.report.dto.SupplierOutstandingBalancesExport;
import rs.pgdavidov.erp.report.dto.SupplierOutstandingBalancesResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SupplierOutstandingBalancesExportService {

    private static final String SHEET_NAME =
            "Otvorene obaveze";

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final SupplierOutstandingBalancesService
            supplierOutstandingBalancesService;

    private final CompanyProfileService
            companyProfileService;

    public SupplierOutstandingBalancesExport export(
            LocalDate periodFrom,
            LocalDate periodTo,
            boolean onlyOutstanding
    ) {
        SupplierOutstandingBalancesResponse balances =
                supplierOutstandingBalancesService
                        .getBalances(
                                periodFrom,
                                periodTo,
                                onlyOutstanding
                        );

        CompanyProfileResponse companyProfile =
                companyProfileService
                        .getProfile()
                        .orElse(null);

        CompanyLogoResponse companyLogo =
                companyProfileService
                        .getLogo()
                        .orElse(null);

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            Sheet sheet =
                    workbook.createSheet(
                            SHEET_NAME
                    );

            Styles styles =
                    createStyles(
                            workbook
                    );

            createHeader(
                    workbook,
                    sheet,
                    balances,
                    companyProfile,
                    companyLogo,
                    onlyOutstanding,
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
                            balances.suppliers(),
                            styles
                    );

            int totalsRowIndex =
                    Math.max(
                            lastDataRow + 2,
                            tableHeaderRowIndex + 2
                    );

            createTotals(
                    sheet,
                    totalsRowIndex,
                    balances.suppliers(),
                    styles
            );

            configureSheet(
                    sheet,
                    tableHeaderRowIndex,
                    lastDataRow
            );

            workbook.write(
                    outputStream
            );

            return new SupplierOutstandingBalancesExport(
                    createFilename(
                            balances,
                            onlyOutstanding
                    ),
                    outputStream.toByteArray()
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Supplier outstanding balances Excel export could not be generated.",
                    exception
            );
        }
    }

    private void createHeader(
            Workbook workbook,
            Sheet sheet,
            SupplierOutstandingBalancesResponse balances,
            CompanyProfileResponse companyProfile,
            CompanyLogoResponse companyLogo,
            boolean onlyOutstanding,
            Styles styles
    ) {
        Row titleRow =
                sheet.createRow(0);

        titleRow.setHeightInPoints(
                32
        );

        Cell titleCell =
                titleRow.createCell(0);

        titleCell.setCellValue(
                "PREGLED OBAVEZA PREMA DOBAVLJAČIMA"
        );

        titleCell.setCellStyle(
                styles.title()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        6
                )
        );

        for (
                int rowIndex = 2;
                rowIndex <= 10;
                rowIndex++
        ) {
            Row row =
                    sheet.getRow(
                            rowIndex
                    );

            if (row == null) {
                row =
                        sheet.createRow(
                                rowIndex
                        );
            }

            row.setHeightInPoints(
                    22
            );
        }

        if (
                companyLogo != null
                        && companyLogo.content() != null
                        && companyLogo.content().length > 0
        ) {
            addCompanyLogo(
                    workbook,
                    sheet,
                    companyLogo
            );
        }

        if (companyProfile != null) {
            createCompanyName(
                    sheet,
                    companyProfile,
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    3,
                    "PIB: ",
                    companyProfile.pib(),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    4,
                    "Matični broj: ",
                    companyProfile.registrationNumber(),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    5,
                    "",
                    companyProfile.address(),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    6,
                    "",
                    formatCity(
                            companyProfile.postalCode(),
                            companyProfile.city()
                    ),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    7,
                    "Telefon: ",
                    companyProfile.phone(),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    8,
                    "Email: ",
                    companyProfile.email(),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    9,
                    "Banka: ",
                    companyProfile.bankName(),
                    styles
            );

            createCompanyInfoRow(
                    sheet,
                    10,
                    "Račun: ",
                    companyProfile.bankAccountNumber(),
                    styles
            );
        }

        createHeaderValueRow(
                sheet,
                2,
                "Period:",
                formatDate(
                        balances.periodFrom()
                )
                        + " - "
                        + formatDate(
                        balances.periodTo()
                ),
                styles
        );

        createHeaderValueRow(
                sheet,
                3,
                "Prikaz:",
                onlyOutstanding
                        ? "Samo otvorene obaveze"
                        : "Svi dobavljači",
                styles
        );

        createHeaderValueRow(
                sheet,
                4,
                "Dobavljača:",
                String.valueOf(
                        balances.suppliers().size()
                ),
                styles
        );
    }

    private void createCompanyName(
            Sheet sheet,
            CompanyProfileResponse companyProfile,
            Styles styles
    ) {
        Row row =
                sheet.getRow(2);

        if (row == null) {
            row =
                    sheet.createRow(2);
        }

        row.setHeightInPoints(
                22
        );

        Cell companyCell =
                row.createCell(2);

        companyCell.setCellValue(
                resolveText(
                        companyProfile.name()
                )
        );

        companyCell.setCellStyle(
                styles.companyTitle()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        2,
                        2,
                        2,
                        3
                )
        );
    }

    private void createCompanyInfoRow(
            Sheet sheet,
            int rowIndex,
            String label,
            String value,
            Styles styles
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return;
        }

        Row row =
                sheet.getRow(
                        rowIndex
                );

        if (row == null) {
            row =
                    sheet.createRow(
                            rowIndex
                    );
        }

        Cell cell =
                row.createCell(2);

        cell.setCellValue(
                label + value
        );

        cell.setCellStyle(
                styles.value()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        2,
                        3
                )
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
                sheet.getRow(
                        rowIndex
                );

        if (row == null) {
            row =
                    sheet.createRow(
                            rowIndex
                    );
        }

        Cell labelCell =
                row.createCell(4);

        labelCell.setCellValue(
                label
        );

        labelCell.setCellStyle(
                styles.label()
        );

        Cell valueCell =
                row.createCell(5);

        valueCell.setCellValue(
                value
        );

        valueCell.setCellStyle(
                styles.value()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        5,
                        6
                )
        );
    }

    private void addCompanyLogo(
            Workbook workbook,
            Sheet sheet,
            CompanyLogoResponse logo
    ) {
        int pictureType =
                resolvePictureType(
                        logo.filename()
                );

        int pictureIndex =
                workbook.addPicture(
                        logo.content(),
                        pictureType
                );

        CreationHelper creationHelper =
                workbook.getCreationHelper();

        Drawing<?> drawing =
                sheet.createDrawingPatriarch();

        ClientAnchor anchor =
                creationHelper.createClientAnchor();

        anchor.setCol1(0);
        anchor.setRow1(2);
        anchor.setCol2(2);
        anchor.setRow2(9);

        drawing.createPicture(
                anchor,
                pictureIndex
        );
    }

    private int resolvePictureType(
            String filename
    ) {
        if (
                filename != null
                        && filename
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .endsWith(".png")
        ) {
            return Workbook.PICTURE_TYPE_PNG;
        }

        return Workbook.PICTURE_TYPE_JPEG;
    }

    private void createTableHeader(
            Sheet sheet,
            int rowIndex,
            Styles styles
    ) {
        Row row =
                sheet.createRow(
                        rowIndex
                );

        row.setHeightInPoints(
                24
        );

        String[] headers = {
                "Dobavljač",
                "Šifra",
                "PIB",
                "Početno stanje",
                "Fakturisano",
                "Plaćeno",
                "Saldo"
        };

        for (
                int column = 0;
                column < headers.length;
                column++
        ) {
            Cell cell =
                    row.createCell(
                            column
                    );

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
            List<SupplierOutstandingBalanceResponse> balances,
            Styles styles
    ) {
        int rowIndex =
                firstRowIndex;

        for (
                SupplierOutstandingBalanceResponse balance
                : balances
        ) {
            Row row =
                    sheet.createRow(
                            rowIndex
                    );

            row.setHeightInPoints(
                    21
            );

            createTextCell(
                    row,
                    0,
                    balance.supplierName(),
                    styles.tableText()
            );

            createTextCell(
                    row,
                    1,
                    balance.supplierCode(),
                    styles.tableText()
            );

            createTextCell(
                    row,
                    2,
                    balance.pib(),
                    styles.tableText()
            );

            createAmountCell(
                    row,
                    3,
                    balance.openingBalance(),
                    styles.amount()
            );

            createAmountCell(
                    row,
                    4,
                    balance.totalInvoiced(),
                    styles.amount()
            );

            createAmountCell(
                    row,
                    5,
                    balance.totalPaid(),
                    styles.amount()
            );

            createAmountCell(
                    row,
                    6,
                    balance.closingBalance(),
                    styles.balance()
            );

            rowIndex++;
        }

        return rowIndex - 1;
    }

    private void createTotals(
            Sheet sheet,
            int rowIndex,
            List<SupplierOutstandingBalanceResponse> balances,
            Styles styles
    ) {
        BigDecimal openingBalance =
                sum(
                        balances,
                        AmountType.OPENING
                );

        BigDecimal totalInvoiced =
                sum(
                        balances,
                        AmountType.INVOICED
                );

        BigDecimal totalPaid =
                sum(
                        balances,
                        AmountType.PAID
                );

        BigDecimal closingBalance =
                sum(
                        balances,
                        AmountType.CLOSING
                );

        Row row =
                sheet.createRow(
                        rowIndex
                );

        row.setHeightInPoints(
                24
        );

        Cell labelCell =
                row.createCell(0);

        labelCell.setCellValue(
                "UKUPNO"
        );

        labelCell.setCellStyle(
                styles.totalLabel()
        );

        for (
                int column = 1;
                column <= 2;
                column++
        ) {
            Cell fillCell =
                    row.createCell(
                            column
                    );

            fillCell.setCellStyle(
                    styles.totalLabel()
            );
        }

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        0,
                        2
                )
        );

        createAmountCell(
                row,
                3,
                openingBalance,
                styles.totalAmount()
        );

        createAmountCell(
                row,
                4,
                totalInvoiced,
                styles.totalAmount()
        );

        createAmountCell(
                row,
                5,
                totalPaid,
                styles.totalAmount()
        );

        createAmountCell(
                row,
                6,
                closingBalance,
                styles.totalBalance()
        );
    }

    private BigDecimal sum(
            List<SupplierOutstandingBalanceResponse> balances,
            AmountType amountType
    ) {
        return balances
                .stream()
                .map(balance ->
                        switch (amountType) {
                            case OPENING ->
                                    balance.openingBalance();
                            case INVOICED ->
                                    balance.totalInvoiced();
                            case PAID ->
                                    balance.totalPaid();
                            case CLOSING ->
                                    balance.closingBalance();
                        }
                )
                .filter(value ->
                        value != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private void configureSheet(
            Sheet sheet,
            int tableHeaderRowIndex,
            int lastDataRow
    ) {
        sheet.setColumnWidth(
                0,
                28 * 256
        );

        sheet.setColumnWidth(
                1,
                16 * 256
        );

        sheet.setColumnWidth(
                2,
                16 * 256
        );

        sheet.setColumnWidth(
                3,
                19 * 256
        );

        sheet.setColumnWidth(
                4,
                19 * 256
        );

        sheet.setColumnWidth(
                5,
                19 * 256
        );

        sheet.setColumnWidth(
                6,
                19 * 256
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

        sheet.setAutobreaks(
                true
        );

        sheet.setFitToPage(
                true
        );

        PrintSetup printSetup =
                sheet.getPrintSetup();

        printSetup.setLandscape(
                true
        );

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

        sheet.setHorizontallyCenter(
                true
        );
    }

    private void createTextCell(
            Row row,
            int column,
            String value,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(
                        column
                );

        cell.setCellValue(
                resolveText(
                        value
                )
        );

        cell.setCellStyle(
                style
        );
    }

    private void createAmountCell(
            Row row,
            int column,
            BigDecimal value,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(
                        column
                );

        if (value != null) {
            cell.setCellValue(
                    value.doubleValue()
            );
        }

        cell.setCellStyle(
                style
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

        titleFont.setBold(
                true
        );

        titleFont.setFontHeightInPoints(
                (short) 18
        );

        title.setFont(
                titleFont
        );

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

        companyFont.setBold(
                true
        );

        companyFont.setFontHeightInPoints(
                (short) 14
        );

        companyTitle.setFont(
                companyFont
        );

        CellStyle label =
                workbook.createCellStyle();

        Font labelFont =
                workbook.createFont();

        labelFont.setBold(
                true
        );

        label.setFont(
                labelFont
        );

        CellStyle value =
                workbook.createCellStyle();

        CellStyle tableHeader =
                workbook.createCellStyle();

        Font tableHeaderFont =
                workbook.createFont();

        tableHeaderFont.setBold(
                true
        );

        tableHeaderFont.setColor(
                IndexedColors.WHITE.getIndex()
        );

        tableHeader.setFont(
                tableHeaderFont
        );

        tableHeader.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
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

        applyBorders(
                tableHeader
        );

        CellStyle tableText =
                workbook.createCellStyle();

        tableText.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        applyBorders(
                tableText
        );

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

        applyBorders(
                amount
        );

        CellStyle balance =
                workbook.createCellStyle();

        Font balanceFont =
                workbook.createFont();

        balanceFont.setBold(
                true
        );

        balance.setFont(
                balanceFont
        );

        balance.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.00"
                )
        );

        balance.setAlignment(
                HorizontalAlignment.RIGHT
        );

        applyBorders(
                balance
        );

        CellStyle totalLabel =
                workbook.createCellStyle();

        Font totalLabelFont =
                workbook.createFont();

        totalLabelFont.setBold(
                true
        );

        totalLabel.setFont(
                totalLabelFont
        );

        totalLabel.setAlignment(
                HorizontalAlignment.RIGHT
        );

        totalLabel.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        totalLabel.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        totalLabel.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        applyBorders(
                totalLabel
        );

        CellStyle totalAmount =
                workbook.createCellStyle();

        Font totalAmountFont =
                workbook.createFont();

        totalAmountFont.setBold(
                true
        );

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

        totalAmount.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        totalAmount.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        totalAmount.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        applyBorders(
                totalAmount
        );

        CellStyle totalBalance =
                workbook.createCellStyle();

        totalBalance.cloneStyleFrom(
                totalAmount
        );

        Font totalBalanceFont =
                workbook.createFont();

        totalBalanceFont.setBold(
                true
        );

        totalBalanceFont.setFontHeightInPoints(
                (short) 12
        );

        totalBalance.setFont(
                totalBalanceFont
        );

        return new Styles(
                title,
                companyTitle,
                label,
                value,
                tableHeader,
                tableText,
                amount,
                balance,
                totalLabel,
                totalAmount,
                totalBalance
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
            SupplierOutstandingBalancesResponse balances,
            boolean onlyOutstanding
    ) {
        String prefix =
                onlyOutstanding
                        ? "otvorene-obaveze"
                        : "obaveze-dobavljaci";

        return prefix
                + "-"
                + FILE_DATE_FORMAT.format(
                balances.periodFrom()
        )
                + "-"
                + FILE_DATE_FORMAT.format(
                balances.periodTo()
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
                .format(
                        date
                );
    }

    private String formatCity(
            String postalCode,
            String city
    ) {
        String postal =
                postalCode == null
                        ? ""
                        : postalCode.trim();

        String cityName =
                city == null
                        ? ""
                        : city.trim();

        return (
                postal
                        + " "
                        + cityName
        ).trim();
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

    private enum AmountType {
        OPENING,
        INVOICED,
        PAID,
        CLOSING
    }

    private record Styles(

            CellStyle title,

            CellStyle companyTitle,

            CellStyle label,

            CellStyle value,

            CellStyle tableHeader,

            CellStyle tableText,

            CellStyle amount,

            CellStyle balance,

            CellStyle totalLabel,

            CellStyle totalAmount,

            CellStyle totalBalance

    ) {
    }
}