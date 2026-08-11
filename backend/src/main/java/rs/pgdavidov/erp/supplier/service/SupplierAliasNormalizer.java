package rs.pgdavidov.erp.supplier.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class SupplierAliasNormalizer {

    public String normalize(
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
}