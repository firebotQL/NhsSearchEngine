package jobs;

import domain.ScrapperAndIndexerConstants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IndexingService;

import java.io.IOException;
import java.util.Date;

public class IndexerJob implements Job {
    private Logger logger = LoggerFactory.getLogger(IndexerJob.class);

    private final IndexingService indexingService;

    public IndexerJob() throws IOException {
        indexingService = new IndexingService(ScrapperAndIndexerConstants.INDEXING_PATH);
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.warn("Indexer Job has been executed! Timestamp is " + new Date());
        try {
            indexingService.index(ScrapperAndIndexerConstants.CACHE_PATH);
        } catch (IOException e) {
            throw new JobExecutionException(e);
        }
        logger.warn("Indexer Job has been finished! Timestamp is " + new Date());
    }
}
