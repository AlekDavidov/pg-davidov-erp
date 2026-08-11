package rs.pgdavidov.erp.supplier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.pgdavidov.erp.supplier.entity.Supplier;
import rs.pgdavidov.erp.supplier.entity.SupplierAlias;
import rs.pgdavidov.erp.supplier.repository.SupplierAliasRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierAliasService {

    private final SupplierAliasRepository
            supplierAliasRepository;

    private final SupplierAliasNormalizer
            supplierAliasNormalizer;

    @Transactional
    public void rememberAlias(
            String rawAlias,
            Supplier supplier
    ) {
        if (
                supplier == null
                        || rawAlias == null
                        || rawAlias.isBlank()
        ) {
            return;
        }

        String normalizedAlias =
                supplierAliasNormalizer.normalize(
                        rawAlias
                );

        if (normalizedAlias.isBlank()) {
            return;
        }

        SupplierAlias existingAlias =
                supplierAliasRepository
                        .findByNormalizedAliasAndActiveTrue(
                                normalizedAlias
                        )
                        .orElse(null);

        if (existingAlias != null) {
            return;
        }

        if (
                supplierAliasRepository
                        .existsByNormalizedAlias(
                                normalizedAlias
                        )
        ) {
            return;
        }

        SupplierAlias alias =
                new SupplierAlias();

        alias.setSupplier(
                supplier
        );

        alias.setAlias(
                rawAlias.trim()
        );

        alias.setNormalizedAlias(
                normalizedAlias
        );

        alias.setActive(
                true
        );

        supplierAliasRepository
                .saveAndFlush(
                        alias
                );
    }
}