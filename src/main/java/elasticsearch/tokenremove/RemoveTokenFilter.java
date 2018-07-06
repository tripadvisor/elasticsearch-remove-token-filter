package elasticsearch.tokenremove;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class RemoveTokenFilter extends TokenFilter {

    private final CharArraySet stopWords;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public RemoveTokenFilter(TokenStream in, CharArraySet stopWords)
    {
        super(in);
        this.stopWords = stopWords;
    }

    private boolean accept() {
        return !stopWords.contains(termAtt.buffer(), 0, termAtt.length());
    }

    @Override
    public final boolean incrementToken() throws IOException
    {

        while (input.incrementToken())
        {
            if (accept())
            {
                return true;
            }
        }

        // reached EOS -- return false
        return false;
    }
}
