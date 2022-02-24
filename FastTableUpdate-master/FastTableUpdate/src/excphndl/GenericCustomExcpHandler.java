package excphndl;

import java.io.PrintWriter;
import java.io.StringWriter;

import consts.ConfigConsts;

public class GenericCustomExcpHandler extends Exception {
	Exception e = null;
	static StringBuffer msgBuffer = new StringBuffer(500);
	static StringWriter sw = new StringWriter();
	static PrintWriter pw = new PrintWriter(sw);
	
	public GenericCustomExcpHandler(Exception e, String msg){
		this.e = e;
		cleanAndSet(msg);
	}
	
	public void setCustomMsg(String customMsg) {
		cleanAndSet(customMsg);
	}
	
	public String getMsg(){
		
		msgBuffer.append(e.getMessage()).append(ConfigConsts.NEXT_LINE);
		e.printStackTrace(pw);
		msgBuffer.append(sw.toString()).append(ConfigConsts.NEXT_LINE);
		sw.flush();
		pw.flush();
		
		return msgBuffer.toString();
	}
	
	private void cleanAndSet(String m){
		msgBuffer.delete(0, msgBuffer.length());msgBuffer.append(m).append(ConfigConsts.NEXT_LINE);
	}
}
