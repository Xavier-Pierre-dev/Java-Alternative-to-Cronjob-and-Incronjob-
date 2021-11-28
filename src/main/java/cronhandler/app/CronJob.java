package cronhandler.app;



import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class CronJob {

	/**
	 * 
	 * @param <T>
	 * @param myJob  
	 * @param cronRules 
	 * @throws SchedulerException
	 */
	public void execute(Class<? extends Job> myJob, String cronRules) throws SchedulerException {

		JobDetail jobDetail = JobBuilder.newJob(myJob).build();

		// Simple trigger wich start directly when the program start
		Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cronRules)).build();

		Scheduler sc = StdSchedulerFactory.getDefaultScheduler();
		sc.start();
		sc.scheduleJob(jobDetail, trigger);


	}
	
	

}
