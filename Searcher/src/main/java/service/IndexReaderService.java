package service;

import common.Page;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IndexReaderService {
    private Directory indexDirectory;

    public IndexReaderService(String indexDirectoryPath) throws IOException {
        indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
    }

    public List<Page> queryIndex(String queryStr) throws ParseException, IOException {
        int hitsPerPage = 10;

        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        Query query = new QueryParser("content", standardAnalyzer).parse(queryStr);

        List<Page> foundPages = new ArrayList<Page>();
        IndexReader reader = DirectoryReader.open(indexDirectory);
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);

            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;


            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                String url = d.get("url");
                String title = d.get("title");
                String content = d.get("content").substring(0, 100);    // just returning 100 chars
                Page page = new Page();
                page.setUrl(url);
                page.setTitle(title);
                page.setContent(content);
                foundPages.add(page);
            }
        } finally {
            reader.close();
        }

        return foundPages;
    }
}
