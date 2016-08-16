import jobs.IndexerJob;
import jobs.ScrapperJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobChainingJobListener;

public class App {
    public static void main(String[] args) throws SchedulerException {
        JobDetail scrapperJobDetail = JobBuilder.newJob(ScrapperJob.class)
                                .withIdentity("Scrapper Job").build();
        JobDetail indexerJobDetail = JobBuilder.newJob(IndexerJob.class)
                .withIdentity("Indexer Job").storeDurably(true).build();

        Trigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("cronTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.repeatHourlyForever(3))
                .build();

        JobChainingJobListener jobChainingJobListener = new JobChainingJobListener("myChainListener");
        jobChainingJobListener.addJobChainLink(scrapperJobDetail.getKey(), indexerJobDetail.getKey());

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.scheduleJob(scrapperJobDetail, cronTrigger);
        scheduler.addJob(indexerJobDetail, true);
        scheduler.getListenerManager().addJobListener(jobChainingJobListener);
        scheduler.start();
    }
}
