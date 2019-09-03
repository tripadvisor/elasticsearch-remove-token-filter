package elasticsearch.tokenremove;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;

public class RemoveTokenFilterFactory extends AbstractTokenFilterFactory {

    private final CharArraySet stopWords;
    private final boolean ignoreCase;

    public RemoveTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.ignoreCase = settings.getAsBoolean("ignore_case", false);
        this.stopWords = Analysis.parseStopWords(env, settings, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET, ignoreCase);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new RemoveTokenFilter(tokenStream, stopWords);
    }
}
