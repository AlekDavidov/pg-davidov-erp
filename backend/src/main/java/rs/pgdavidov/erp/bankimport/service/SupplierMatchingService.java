package rs.pgdavidov.erp.bankimport.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.repository.SupplierRepository;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierMatchingService {

    public static final String STATUS_MATCHED =
            "MATCHED";

    public static final String STATUS_UNMATCHED =
            "UNMATCHED";

    public static final String STATUS_AMBIGUOUS =
            "AMBIGUOUS";

    private static final int MINIMUM_MATCH_LENGTH =
            4;

    private final SupplierRepository supplierRepository;

    public List<Supplier> findActiveSuppliers() {
        return supplierRepository
                .findAll()
                .stream()
                .filter(Supplier::isActive)
                .toList();
    }

    public SupplierMatchResult match(
            String counterparty,
            List<Supplier> suppliers
    ) {
        String normalizedCounterparty =
                normalize(counterparty);

        if (
                normalizedCounterparty.isBlank()
                        || suppliers == null
                        || suppliers.isEmpty()
        ) {
            return SupplierMatchResult.unmatched();
        }

        List<SupplierCandidate> candidates =
                suppliers
                        .stream()
                        .filter(Objects::nonNull)
                        .map(supplier ->
                                new SupplierCandidate(
                                        supplier,
                                        normalize(
                                                supplier.getName()
                                        )
                                )
                        )
                        .filter(candidate ->
                                !candidate
                                        .normalizedName()
                                        .isBlank()
                        )
                        .toList();

        Optional<SupplierCandidate> exactMatch =
                candidates
                        .stream()
                        .filter(candidate ->
                                normalizedCounterparty.equals(
                                        candidate.normalizedName()
                                )
                        )
                        .findFirst();

        if (exactMatch.isPresent()) {
            return SupplierMatchResult.matched(
                    exactMatch.get().supplier()
            );
        }

        List<SupplierCandidate> containmentMatches =
                candidates
                        .stream()
                        .filter(candidate ->
                                candidate
                                        .normalizedName()
                                        .length()
                                        >= MINIMUM_MATCH_LENGTH
                        )
                        .filter(candidate ->
                                containsCompleteName(
                                        normalizedCounterparty,
                                        candidate.normalizedName()
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                (
                                                        SupplierCandidate
                                                                candidate
                                                ) ->
                                                        candidate
                                                                .normalizedName()
                                                                .length()
                                        )
                                        .reversed()
                        )
                        .toList();

        if (containmentMatches.isEmpty()) {
            return SupplierMatchResult.unmatched();
        }

        SupplierCandidate strongestMatch =
                containmentMatches.getFirst();

        boolean ambiguous =
                containmentMatches
                        .stream()
                        .skip(1)
                        .anyMatch(candidate ->
                                candidate
                                        .normalizedName()
                                        .length()
                                        == strongestMatch
                                        .normalizedName()
                                        .length()
                        );

        if (ambiguous) {
            return SupplierMatchResult.ambiguous();
        }

        return SupplierMatchResult.matched(
                strongestMatch.supplier()
        );
    }

    private boolean containsCompleteName(
            String normalizedCounterparty,
            String normalizedSupplierName
    ) {
        String paddedCounterparty =
                " " + normalizedCounterparty + " ";

        String paddedSupplierName =
                " " + normalizedSupplierName + " ";

        return paddedCounterparty.contains(
                paddedSupplierName
        );
    }

    private String normalize(
            String value
    ) {
        if (value == null) {
            return "";
        }

        String withoutDiacritics =
                Normalizer
                        .normalize(
                                value,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}+",
                                ""
                        );

        return withoutDiacritics
                .toUpperCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll(
                        "[^A-Z0-9]+",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private record SupplierCandidate(

            Supplier supplier,

            String normalizedName

    ) {
    }

    public record SupplierMatchResult(

            Supplier supplier,

            String status

    ) {

        public static SupplierMatchResult matched(
                Supplier supplier
        ) {
            return new SupplierMatchResult(
                    supplier,
                    STATUS_MATCHED
            );
        }

        public static SupplierMatchResult unmatched() {
            return new SupplierMatchResult(
                    null,
                    STATUS_UNMATCHED
            );
        }

        public static SupplierMatchResult ambiguous() {
            return new SupplierMatchResult(
                    null,
                    STATUS_AMBIGUOUS
            );
        }

        public boolean isMatched() {
            return supplier != null
                    && STATUS_MATCHED.equals(status);
        }
    }
}