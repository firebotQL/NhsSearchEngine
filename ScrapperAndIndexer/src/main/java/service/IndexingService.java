package service;

import com.google.gson.Gson;
import domain.Page;
import filter.JsonFileFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

public class IndexingService {
    private IndexWriter writer;
    private Directory indexDirectory;
    private JsonFileFilter jsonFileFilter;

    public IndexingService(String indexDirectoryPath) throws IOException {
        // this directory will contain the indexes
        indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        // create the indexer
        IndexWriterConfig config = new IndexWriterConfig(standardAnalyzer);
        writer = new IndexWriter(indexDirectory, config);

        jsonFileFilter = new JsonFileFilter();
    }

    public void index(String dataDirectory) throws IOException {
        try {
            createRecursiveIndex(dataDirectory);
        } finally {
            writer.close();
        }
    }

    private void createRecursiveIndex(String dataDirectory) throws IOException {
        File[] files = new File(dataDirectory).listFiles();
        for (File file : files) {
            if (file.isFile() &&
                    !file.isHidden() &&
                    file.exists() &&
                    file.canRead() &&
                    jsonFileFilter.accept(file)) {
                indexFile(file);
            } else if (file.isDirectory()) {
                createRecursiveIndex(file.getAbsolutePath());
            }
        }
    }

    private void indexFile(File file) throws IOException {
        Document document = buildDocument(file);
        writer.addDocument(document);
    }

    private Document buildDocument(File file) throws FileNotFoundException {
        Document document = new Document();
        // index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));
        Page page = new Gson().fromJson(br, Page.class);

        Field contentField = new TextField("content", page.getContent(), Field.Store.YES );
        Field titleField = new StringField("title", page.getTitle(), Field.Store.YES );
        Field url = new StringField("url", page.getUrl(), Field.Store.YES );
        document.add(contentField);
        document.add(titleField);
        document.add(url);
        return document;
    }
}
