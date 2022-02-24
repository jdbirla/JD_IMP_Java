package sample;

import PerfLog.LogAnnot;

public class WorkBean {
	
	@LogAnnot
	public void doWork() {
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(" Sample target method to record the time.");
	}

}
