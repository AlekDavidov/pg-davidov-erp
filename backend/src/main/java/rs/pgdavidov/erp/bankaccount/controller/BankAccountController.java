package rs.pgdavidov.erp.bankaccount.controller;

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
import rs.pgdavidov.erp.bankaccount.dto.BankAccountRequest;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountResponse;
import rs.pgdavidov.erp.bankaccount.dto.BankAccountUpdateRequest;
import rs.pgdavidov.erp.bankaccount.service.BankAccountService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
@Tag(
        name = "Bank Accounts",
        description = "Bank account management"
)
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    @Operation(summary = "Get all bank accounts")
    public ResponseEntity<List<BankAccountResponse>> getAll() {
        return ResponseEntity.ok(bankAccountService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID")
    public ResponseEntity<BankAccountResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(bankAccountService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a bank account")
    public ResponseEntity<BankAccountResponse> create(
            @Valid @RequestBody BankAccountRequest request
    ) {
        BankAccountResponse response = bankAccountService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank account")
    public ResponseEntity<BankAccountResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody BankAccountUpdateRequest request
    ) {
        return ResponseEntity.ok(
                bankAccountService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        bankAccountService.delete(id);

        return ResponseEntity.noContent().build();
    }
}