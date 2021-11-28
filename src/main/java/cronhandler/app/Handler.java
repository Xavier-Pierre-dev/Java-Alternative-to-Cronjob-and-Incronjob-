package cronhandler.app;

import org.quartz.SchedulerException;
import cronhandler.app.jobs.*;




public class Handler {
	public static void main(String[] args) throws SchedulerException, InterruptedException {



		// methode(job, trigger);
		CronJob cronjob1 = new CronJob();
		cronjob1.execute(Jobs2.class, "0 0/1 * 1/1 * ? *");


		InCronJob myIncronjob1 = InCronJob.builderof("C:\\Users\\xpierre\\Desktop\\test")
				.setCREATE(Jobs3.class).build();
		myIncronjob1.Execute();


	}
}

