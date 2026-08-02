package rs.pgdavidov.erp.bankimport.parser.pdf;

import rs.pgdavidov.erp.bankimport.parser.pdf.PdfWordExtractor.PdfWord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PdfLineGrouper {

    private static final float DEFAULT_TOLERANCE =
            2.5F;

    private PdfLineGrouper() {
    }

    public static List<PdfLine> group(
            List<PdfWord> words
    ) {
        return group(
                words,
                DEFAULT_TOLERANCE
        );
    }

    public static List<PdfLine> group(
            List<PdfWord> words,
            float tolerance
    ) {
        List<List<PdfWord>> groupedWords =
                new ArrayList<>();

        List<PdfWord> sortedWords =
                words
                        .stream()
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                PdfWord::pageNumber
                                        )
                                        .thenComparing(
                                                PdfWord::top
                                        )
                                        .thenComparing(
                                                PdfWord::x0
                                        )
                        )
                        .toList();

        for (PdfWord word : sortedWords) {
            List<PdfWord> matchingLine =
                    findMatchingLine(
                            groupedWords,
                            word,
                            tolerance
                    );

            if (matchingLine == null) {
                List<PdfWord> newLine =
                        new ArrayList<>();

                newLine.add(word);
                groupedWords.add(newLine);

                continue;
            }

            matchingLine.add(word);
        }

        return groupedWords
                .stream()
                .map(PdfLineGrouper::toPdfLine)
                .sorted(
                        Comparator
                                .comparingInt(
                                        PdfLine::pageNumber
                                )
                                .thenComparing(
                                        PdfLine::top
                                )
                                .thenComparing(
                                        PdfLine::x0
                                )
                )
                .toList();
    }

    private static List<PdfWord> findMatchingLine(
            List<List<PdfWord>> groupedWords,
            PdfWord word,
            float tolerance
    ) {
        for (List<PdfWord> line : groupedWords) {
            PdfWord firstWord =
                    line.getFirst();

            boolean samePage =
                    firstWord.pageNumber()
                            == word.pageNumber();

            boolean sameVerticalPosition =
                    Math.abs(
                            firstWord.top()
                                    - word.top()
                    ) <= tolerance;

            if (samePage
                    && sameVerticalPosition) {
                return line;
            }
        }

        return null;
    }

    private static PdfLine toPdfLine(
            List<PdfWord> words
    ) {
        List<PdfWord> sortedWords =
                words
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        PdfWord::x0
                                )
                        )
                        .toList();

        String text =
                sortedWords
                        .stream()
                        .map(PdfWord::text)
                        .reduce(
                                "",
                                (left, right) ->
                                        left.isBlank()
                                                ? right
                                                : left + " " + right
                        )
                        .trim();

        float x0 =
                sortedWords
                        .stream()
                        .map(PdfWord::x0)
                        .min(Float::compareTo)
                        .orElse(0F);

        float x1 =
                sortedWords
                        .stream()
                        .map(PdfWord::x1)
                        .max(Float::compareTo)
                        .orElse(0F);

        float top =
                sortedWords
                        .stream()
                        .map(PdfWord::top)
                        .min(Float::compareTo)
                        .orElse(0F);

        float bottom =
                sortedWords
                        .stream()
                        .map(PdfWord::bottom)
                        .max(Float::compareTo)
                        .orElse(0F);

        return new PdfLine(
                sortedWords.getFirst().pageNumber(),
                text,
                x0,
                x1,
                top,
                bottom,
                sortedWords
        );
    }

    public record PdfLine(

            int pageNumber,

            String text,

            float x0,

            float x1,

            float top,

            float bottom,

            List<PdfWord> words

    ) {
        public PdfLine {
            words = List.copyOf(words);
        }
    }
}