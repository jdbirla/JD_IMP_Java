package odm;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helper.ManageExecutors;
import ig.IGLoader;


public class CallBw978 {
	private static final Logger LOGGER = LoggerFactory.getLogger(CallBw978.class);
	
	public static int CHUNK_SIZE =10, NOOFTASK=10, NOFTHREAD=10;
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		ManageExecutors.reset();
		ManageExecutors.setNoOfTasks(NOOFTASK);
		ManageExecutors.setNoOfThreads(NOFTHREAD);
		LOGGER.debug("main() submitting tasksCount={} executorThreadCount={}", NOOFTASK,NOFTHREAD);
		for (int i=0;i<NOOFTASK;i++){
			ODMTask ot = new ODMTask(); ot.init(args);
			ManageExecutors.submitTask(ot);
		}
		LOGGER.debug("main() going to wait for completion of submitted tasks tasksCount={} executorThreadCount={}", NOOFTASK,NOFTHREAD);
		ManageExecutors.waitForCompletionOfSubmittedTasks();
		ManageExecutors.shutdown();
		LOGGER.debug("main() completed, tasksCount={} executorThreadCount={}", NOOFTASK,NOFTHREAD);
		
	}

}

class ODMTask implements Callable<Integer>{
	private static final Logger LOGGER = LoggerFactory.getLogger(ODMTask.class);
	String[] args = null;
	
	Instant startTime = null, endTime=null;;
	
	public void init(String[] args){
		this.args = args;
		
	}
	
	@Override
	public Integer call() throws Exception {
		LOGGER.debug(" call() start: taskHashCode={}", this.hashCode());
		startTime = Instant.now();

		IGLoader.load(args);
		Bw978Customized myBatch = new Bw978Customized();
		myBatch.appVars = IGLoader.appVars;
		int threadCount = ManageExecutors.THREAD_COUNTER.getAndIncrement();
		myBatch.setThreadNo(threadCount).setChunkSize(CallBw978.CHUNK_SIZE);
		
		myBatch.initialise1000();
		myBatch.readFile2000();
		myBatch.update3000();
		
		endTime = Instant.now();
		LOGGER.debug(" call() end: taskHashCode={} threadCount={} timeTaken(ms)={}", this.hashCode(), threadCount, Duration.between(startTime, endTime).toMillis() );
		return threadCount;
	}
	
}
