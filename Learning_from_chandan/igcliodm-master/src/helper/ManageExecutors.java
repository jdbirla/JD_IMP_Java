package helper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageExecutors {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ManageExecutors.class);

	public static AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
	private static int SUBMIT_COUNTER = 0; 
		
	private static int NO_OF_THREADS = 4;	public static void setNoOfThreads(int t) { 	NO_OF_THREADS = t;	}
	private static int NO_OF_TASKS = 10;	public static void setNoOfTasks(int t)	{	NO_OF_TASKS = t; 	}
	private static Future<?> [] SUBMITTED_TASKS_ARRAY = new Future<?>[NO_OF_TASKS]; public Future<?> [] getSubmittedTasks(){	return SUBMITTED_TASKS_ARRAY;	}
	
	static final ExecutorService es = Executors.newFixedThreadPool(NO_OF_THREADS);
	
	public static Future<?> submitTask(Callable<?> task){
		Future<?> f = es.submit(task);
		SUBMITTED_TASKS_ARRAY[SUBMIT_COUNTER]=f;
		LOGGER.debug("submitTask completed. SUBMIT_COUNTER={} future={}", SUBMIT_COUNTER,f.hashCode());
		SUBMIT_COUNTER++;
		return f;
	}
	
	public static void waitForCompletionOfSubmittedTasks() throws InterruptedException, ExecutionException{
		for (int i = 0; i<NO_OF_TASKS;i++){
			LOGGER.debug("waitForCompletionOfSubmittedTasks(). i={} future={} ",i, SUBMITTED_TASKS_ARRAY[i].hashCode());
			SUBMITTED_TASKS_ARRAY[i].get();
		}
	}
	
	public static void shutdown(){
		es.shutdown();
	}
	
	public static void reset(){
		THREAD_COUNTER.set(1);
		SUBMIT_COUNTER=0;
	}
}
