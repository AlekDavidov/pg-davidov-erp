package rs.pgdavidov.erp.invoice.mapper;

import org.springframework.stereotype.Component;
import rs.pgdavidov.erp.invoice.dto.InvoiceRequest;
import rs.pgdavidov.erp.invoice.dto.InvoiceResponse;
import rs.pgdavidov.erp.invoice.entity.Invoice;
import rs.pgdavidov.erp.invoice.model.InvoiceStatus;
import rs.pgdavidov.erp.supplier.entity.Supplier;

import java.math.BigDecimal;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(
            Invoice invoice,
            BigDecimal paidAmount,
            BigDecimal remainingAmount,
            InvoiceStatus status
    ) {
        InvoiceResponse response = new InvoiceResponse();

        response.setId(invoice.getId());
        response.setInvoiceCode(invoice.getInvoiceCode());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setSupplierId(invoice.getSupplier().getId());
        response.setSupplierName(invoice.getSupplier().getName());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setDueDate(invoice.getDueDate());
        response.setAmount(invoice.getAmount());
        response.setPaidAmount(paidAmount);
        response.setRemainingAmount(remainingAmount);
        response.setStatus(status);
        response.setCurrencyCode(invoice.getCurrencyCode());
        response.setNotes(invoice.getNotes());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());

        return response;
    }

    public Invoice toEntity(
            InvoiceRequest request,
            Supplier supplier
    ) {
        Invoice invoice = new Invoice();

        invoice.setInvoiceCode(request.getInvoiceCode());
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setSupplier(supplier);
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setAmount(request.getAmount());
        invoice.setCurrencyCode(request.getCurrencyCode());
        invoice.setNotes(request.getNotes());

        return invoice;
    }

    public void updateEntity(
            Invoice invoice,
            InvoiceRequest request,
            Supplier supplier
    ) {
        invoice.setInvoiceCode(request.getInvoiceCode());
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setSupplier(supplier);
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setAmount(request.getAmount());
        invoice.setCurrencyCode(request.getCurrencyCode());
        invoice.setNotes(request.getNotes());
    }
}