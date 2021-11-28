package cronhandler.app;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;


/**
 * 
 * @author xpierre
 *
 */
public class InCronJob {

	// ALL final atribute
	private final Path path; // required

	private Thread dirWatcherThread;

	private Class<? extends Job> MODIFY = JobsBlank.class;
	private Class<? extends Job> DELETED = JobsBlank.class;
	private Class<? extends Job> CREATE = JobsBlank.class;
	private Class<? extends Job> RENAME = JobsBlank.class;

	private InCronJob(Builder builder) {
		this.path = builder.path;
		this.MODIFY = builder.MODIFY;
		this.CREATE = builder.CREATE;
		this.DELETED = builder.DELETED;
		this.RENAME = builder.RENAME;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static Builder builderof(String path) {

		return new Builder(path);
	}

	public Path getPath() {
		return path;
	}

	public void Execute() {

		DirectoryWatcher dirWatcher = new DirectoryWatcher(this.path, this.CREATE, this.DELETED, this.MODIFY,
				this.RENAME);
		Thread dirWatcherThread = new Thread(dirWatcher);
		this.dirWatcherThread = dirWatcherThread;
		this.dirWatcherThread.start();
	}

	public void Shutdown() {
		this.dirWatcherThread.interrupt();
	}

	public static class Builder {

		private final Path path; // required

		private Class<? extends Job> MODIFY = JobsBlank.class;
		private Class<? extends Job> DELETED = JobsBlank.class;
		private Class<? extends Job> CREATE = JobsBlank.class;
		private Class<? extends Job> RENAME = JobsBlank.class;

		public Builder(String path) {
			this.path = FileSystems.getDefault().getPath(path);
		}

		public Builder setMODIFY(Class<? extends Job> MODIFY) {
			this.MODIFY = MODIFY;
			return this;
		}

		public Builder setDELETED(Class<? extends Job> DELETED) {
			this.DELETED = DELETED;
			return this;
		}

		public Builder setCREATE(Class<? extends Job> CREATE) {
			this.CREATE = CREATE;
			return this;
		}

		public Builder setRENAME(Class<? extends Job> RENAME) {
			this.RENAME = RENAME;
			return this;
		}

		public InCronJob build() {
			return new InCronJob(this);

		}
	}

	// Simple class to watch directory events.
	class DirectoryWatcher implements Runnable {

		private Path path;
		public Class<? extends Job> MODIFY;
		public Class<? extends Job> DELETED;
		public Class<? extends Job> CREATE;
		public Class<? extends Job> RENAME;

		public DirectoryWatcher(Path path, Class<? extends Job> CREATE, Class<? extends Job> DELETED,
				Class<? extends Job> MODIFY, Class<? extends Job> RENAME) {
			this.path = path;
			this.CREATE = CREATE;
			this.DELETED = DELETED;
			this.MODIFY = MODIFY;
			this.RENAME = RENAME;
		}

		// print the events and the affected file
		private void printEvent(WatchEvent<?> event) throws SchedulerException, IOException {
			Kind<?> kind = event.kind();

			JobDetail job = JobBuilder.newJob().build();

			if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
				Path pathCreated = (Path) event.context();
				// boolean exist = Files.exists(pathCreated);

				job = JobBuilder.newJob(this.CREATE).usingJobData("myParamRelativePath", this.path.toString())
						.usingJobData("myNameFile", pathCreated.toString()).build();

			} else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
				Path pathDeleted = (Path) event.context();
				System.out.println("Entry deleted: " + pathDeleted);

				job = JobBuilder.newJob(this.DELETED).usingJobData("myParamRelativePath", this.path.toString())
						.usingJobData("myNameFile", pathDeleted.toString()).build();
			} else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
				Path pathModified = (Path) event.context();
				System.out.println("Entry modified: " + pathModified);

				job = JobBuilder.newJob(this.MODIFY).usingJobData("myParamRelativePath", this.path.toString())
						.usingJobData("myNameFile", pathModified.toString()).build();
			}

			Trigger t1 = TriggerBuilder.newTrigger().startNow().build();
			Scheduler sc = StdSchedulerFactory.getDefaultScheduler();
			sc.start();
			sc.scheduleJob(job, t1);
		}

		@Override
		public void run() {
			try {
				WatchService watchService = path.getFileSystem().newWatchService();
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE);

				// loop forever to watch directory
				while (true) {
					WatchKey watchKey;
					watchKey = watchService.take(); // this call is blocking until events are present

					// poll for file system events on the WatchKey
					for (final WatchEvent<?> event : watchKey.pollEvents()) {
						printEvent(event);
					}

					// if the watched directed gets deleted, get out of run method
					if (!watchKey.reset()) {
						System.out.println("No longer valid");
						watchKey.cancel();
						watchService.close();
						break;
					}
				}

			} catch (InterruptedException ex) {
				System.out.println("interrupted. Goodbye");
				return;
			} catch (IOException ex) {
				ex.printStackTrace(); 
				return;
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class JobsBlank implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			// DO NOTHING 

		}
	}

}
