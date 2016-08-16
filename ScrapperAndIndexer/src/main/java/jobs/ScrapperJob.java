package jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ScrappingService;

import java.util.Date;

public class ScrapperJob implements Job {
    private Logger logger = LoggerFactory.getLogger(ScrapperJob.class);

    private final ScrappingService scrappingService;

    public ScrapperJob() {
        scrappingService = new ScrappingService();
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.warn("Scrapper Job has been executed! Timestamp is " + new Date());
        long startTime = System.currentTimeMillis();
        try {
            scrappingService.scrape();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
        long endTime = System.currentTimeMillis();
        double duration = (double)(endTime - startTime) / 1000;
        String warnMsg = "Scrapper Job has finished! Timestamp is %s Took: %.2f .s";
        logger.warn(String.format(warnMsg, new Date(), duration));
    }
}
