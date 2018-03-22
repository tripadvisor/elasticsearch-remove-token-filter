package elasticsearch.remove;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;

import java.io.IOException;
import java.io.StringReader;

import static org.elasticsearch.test.ESTestCase.createTestAnalysis;

// TODO: add more tests for other languages

public class RemoveTokenFilterTest extends ESTokenStreamTestCase {

    public void testRemoveTokenFilter() throws IOException
    {
        Settings settings = Settings.builder()
                .put("index.analysis.filter.my_token_removal_filter.type", "token_removal")
                .build();

        ESTestCase.TestAnalysis analysis = createTestAnalysis(new Index("test", "_na_"), settings, new TokenRemovalPlugin());
        TokenFilterFactory filter = analysis.tokenFilter.get("my_token_removal_filter");
        Tokenizer tokenizer = new WhitespaceTokenizer();
        tokenizer.setReader(new StringReader("next to"));
        TokenStream tokenStream = filter.create(tokenizer);
        BaseTokenStreamTestCase.assertTokenStreamContents(tokenStream, new String[] { "next" }, new int[]{0}, new int[]{4}, new int[]{1});

        tokenizer.setReader(new StringReader("hotels next to the eiffel tower"));
        tokenStream = filter.create(tokenizer);
        // Make sure ever increment each token by 1 even when we remove multiple tokens adjacent to each other
        // normal stop filter fails at this assert - ES version 5.4
        BaseTokenStreamTestCase.assertTokenStreamContents(tokenStream, new String[] { "hotels", "next", "eiffel", "tower" }, new int[]{0,7,12,19}, new int[]{6,11,18,24}, new int[]{1, 1, 1, 1});

    }

    // This is a unit test for lucene's build-in stop filter, it is here only to illustrate its difference
    // comparing to token_removal filter
    public void testStopFilter() throws IOException
    {
        Settings settings = Settings.builder()
                .put("index.analysis.filter.my_stop_filter.type", "stop")
                .build();

        ESTestCase.TestAnalysis analysis = createTestAnalysis(new Index("test", "_na_"), settings);
        TokenFilterFactory filter = analysis.tokenFilter.get("my_stop_filter");
        Tokenizer tokenizer = new WhitespaceTokenizer();

        tokenizer.setReader(new StringReader("hotels next to the eiffel tower"));
        TokenStream tokenStream = filter.create(tokenizer);
        // Notice how "eiffel" token is 3 positions behind after "next"
        BaseTokenStreamTestCase.assertTokenStreamContents(tokenStream, new String[] { "hotels", "next", "eiffel", "tower" }, new int[]{0,7,19,26}, new int[]{6,11,25,31}, new int[]{1, 1, 3, 1});

    }

    public void testFrenchRemoveTokenFilter() throws IOException
    {
        Settings settings = Settings.builder()
                .put("index.analysis.filter.my_token_removal_filter.type", "token_removal")
                .put("index.analysis.filter.my_token_removal_filter.stopwords", "_french_")
                .build();

        ESTestCase.TestAnalysis analysis = createTestAnalysis(new Index("test", "_na_"), settings, new TokenRemovalPlugin());
        TokenFilterFactory filter = analysis.tokenFilter.get("my_token_removal_filter");
        Tokenizer tokenizer = new WhitespaceTokenizer();

        tokenizer.setReader(new StringReader("hötels près de la tour eiffel"));
        TokenStream tokenStream = filter.create(tokenizer);
        // Make sure ever increment each token by 1 even when we remove multiple tokens adjacent to each other
        // normal stop filter fails at this assert - ES version 5.4
        BaseTokenStreamTestCase.assertTokenStreamContents(tokenStream, new String[] { "hötels", "près", "tour", "eiffel" }, new int[]{0,7,12,17}, new int[]{6,11,16,23}, new int[]{1, 1, 1, 1});
    }
}
