package rs.pgdavidov.erp.bankimport.parser.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PdfWordExtractor
        extends PDFTextStripper {

    private static final float SAME_LINE_TOLERANCE =
            2.5F;

    private static final float WORD_GAP_MULTIPLIER =
            0.55F;

    private final List<PdfWord> words =
            new ArrayList<>();

    private final List<TextPosition> currentWordCharacters =
            new ArrayList<>();

    private TextPosition previousCharacter;

    public PdfWordExtractor() throws IOException {
        setSortByPosition(true);
    }

    public List<PdfWord> extract(
            PDDocument document
    ) throws IOException {
        words.clear();
        currentWordCharacters.clear();
        previousCharacter = null;

        setStartPage(1);
        setEndPage(document.getNumberOfPages());

        getText(document);

        flushCurrentWord();

        return words
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
    }

    @Override
    protected void processTextPosition(
            TextPosition textPosition
    ) {
        String unicode =
                textPosition.getUnicode();

        if (unicode == null || unicode.isBlank()) {
            flushCurrentWord();
            previousCharacter = null;

            return;
        }

        if (shouldStartNewWord(textPosition)) {
            flushCurrentWord();
        }

        currentWordCharacters.add(
                textPosition
        );

        previousCharacter =
                textPosition;
    }

    @Override
    protected void writeLineSeparator()
            throws IOException {
        flushCurrentWord();
        previousCharacter = null;

        super.writeLineSeparator();
    }

    @Override
    protected void writeWordSeparator()
            throws IOException {
        flushCurrentWord();
        previousCharacter = null;

        super.writeWordSeparator();
    }

    @Override
    protected void endPage(
            org.apache.pdfbox.pdmodel.PDPage page
    ) throws IOException {
        flushCurrentWord();
        previousCharacter = null;

        super.endPage(page);
    }

    private boolean shouldStartNewWord(
            TextPosition currentCharacter
    ) {
        if (previousCharacter == null) {
            return false;
        }

        float previousTop =
                getTop(previousCharacter);

        float currentTop =
                getTop(currentCharacter);

        boolean differentLine =
                Math.abs(
                        previousTop -
                                currentTop
                ) > SAME_LINE_TOLERANCE;

        if (differentLine) {
            return true;
        }

        float previousRight =
                previousCharacter.getXDirAdj()
                        + previousCharacter.getWidthDirAdj();

        float horizontalGap =
                currentCharacter.getXDirAdj()
                        - previousRight;

        float averageCharacterWidth =
                Math.max(
                        previousCharacter.getWidthDirAdj(),
                        currentCharacter.getWidthDirAdj()
                );

        float allowedGap =
                averageCharacterWidth
                        * WORD_GAP_MULTIPLIER;

        return horizontalGap > allowedGap;
    }

    private void flushCurrentWord() {
        if (currentWordCharacters.isEmpty()) {
            return;
        }

        StringBuilder text =
                new StringBuilder();

        float x0 =
                Float.MAX_VALUE;

        float x1 =
                Float.MIN_VALUE;

        float top =
                Float.MAX_VALUE;

        float bottom =
                Float.MIN_VALUE;

        for (TextPosition character
                : currentWordCharacters) {
            text.append(
                    character.getUnicode()
            );

            float characterX0 =
                    character.getXDirAdj();

            float characterX1 =
                    characterX0
                            + character.getWidthDirAdj();

            float characterTop =
                    getTop(character);

            float characterBottom =
                    characterTop
                            + character.getHeightDir();

            x0 = Math.min(
                    x0,
                    characterX0
            );

            x1 = Math.max(
                    x1,
                    characterX1
            );

            top = Math.min(
                    top,
                    characterTop
            );

            bottom = Math.max(
                    bottom,
                    characterBottom
            );
        }

        String normalizedText =
                text.toString().trim();

        if (!normalizedText.isEmpty()) {
            words.add(
                    new PdfWord(
                            getCurrentPageNo(),
                            normalizedText,
                            x0,
                            x1,
                            top,
                            bottom
                    )
            );
        }

        currentWordCharacters.clear();
    }

    private float getTop(
            TextPosition textPosition
    ) {
        return textPosition.getYDirAdj()
                - textPosition.getHeightDir();
    }

    public record PdfWord(

            int pageNumber,

            String text,

            float x0,

            float x1,

            float top,

            float bottom

    ) {
    }
}