package elasticsearch.remove;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public class RemoveTokenFilter extends TokenFilter {

    private final CharArraySet stopWords;
    private boolean skippedPositions;
    private int currentStartOffset;


    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    public RemoveTokenFilter(TokenStream in, CharArraySet stopWords)
    {
        super(in);
        this.stopWords = stopWords;
    }

    private boolean accept() {
        return !stopWords.contains(termAtt.buffer(), 0, termAtt.length());
    }

    // TODO: get start and end offsets right!!!
    @Override
    public final boolean incrementToken() throws IOException {
        skippedPositions = false;

        while (input.incrementToken())
        {
            if (accept()) {
                int endOffset = currentStartOffset + termAtt.length();

                if (skippedPositions)
                {
                    posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement());
                    offsetAtt.setOffset(currentStartOffset, endOffset);
                }
                else
                {
                    offsetAtt.setOffset(currentStartOffset, endOffset);
                }

                currentStartOffset = endOffset + 1;
                return true;
            }
            skippedPositions = true;
        }

        // reached EOS -- return false
        return false;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        skippedPositions = false;
        currentStartOffset = 0;
    }

    @Override
    public void end() throws IOException {
        super.end();
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement());
    }
}
