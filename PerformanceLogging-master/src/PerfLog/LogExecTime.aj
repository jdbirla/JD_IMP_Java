package PerfLog;

import java.time.Duration;
import java.time.Instant;

public aspect LogExecTime {

	pointcut hasAnnotation(LogAnnot logAnnot) : @annotation(logAnnot);
	pointcut methodExecution() : execution(* *(..));
 
   
   void around(LogAnnot logAnnot) : methodExecution() && hasAnnotation(logAnnot) {
	    long FACTOR 		= 1024; 
   		String methodName 	= (new Throwable().getStackTrace())[0].getMethodName();
   		long usedMemStart 	= (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/FACTOR;
        Instant start 		= Instant.now();
        proceed(logAnnot);
        Instant end 		= Instant.now();
        long executionTime 	= Duration.between(start, end).toMillis();
        long usedMemEnd 	= (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/FACTOR;
        System.out.printf(" %s | executed in %dms | UsedMem_Start(KB) : %d End: % d | DiffMemUsedKB: %d  \n", methodName, executionTime, usedMemStart, usedMemEnd, (usedMemEnd - usedMemStart) );
       
   }
 
 
   
    
      
}