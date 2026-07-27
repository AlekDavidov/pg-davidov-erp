package rs.pgdavidov.erp.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.pgdavidov.erp.transaction.dto.TransactionRequest;
import rs.pgdavidov.erp.transaction.dto.TransactionResponse;
import rs.pgdavidov.erp.transaction.dto.TransactionUpdateRequest;
import rs.pgdavidov.erp.transaction.service.TransactionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(
        name = "Transactions",
        description = "Transaction management"
)
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions")
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a transaction")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        transactionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}