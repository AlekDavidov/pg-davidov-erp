package rs.pgdavidov.erp.bankimport.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BankStatementRowIdGeneratorTest {

    private final BankStatementRowIdGenerator generator =
            new BankStatementRowIdGenerator();

    @Test
    void shouldGenerateSameIdForSameBankStatementRow() {
        UUID first =
                generator.generate(
                        "AIK",
                        "105-0000002677463-16",
                        "AIK-2026-05-005",
                        1
                );

        UUID second =
                generator.generate(
                        "AIK",
                        "105-0000002677463-16",
                        "AIK-2026-05-005",
                        1
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldGenerateDifferentIdForDifferentEntryNumber() {
        UUID first =
                generator.generate(
                        "AIK",
                        "105-0000002677463-16",
                        "AIK-2026-05-005",
                        1
                );

        UUID second =
                generator.generate(
                        "AIK",
                        "105-0000002677463-16",
                        "AIK-2026-05-005",
                        2
                );

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void shouldIgnoreCaseAndSurroundingWhitespace() {
        UUID first =
                generator.generate(
                        "AIK",
                        "105-0000002677463-16",
                        "AIK-2026-05-005",
                        1
                );

        UUID second =
                generator.generate(
                        " aik ",
                        " 105-0000002677463-16 ",
                        " aik-2026-05-005 ",
                        1
                );

        assertThat(first)
                .isEqualTo(second);
    }
}