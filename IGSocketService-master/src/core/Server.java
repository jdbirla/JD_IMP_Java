package core;

import Const.Msg;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.io.*; 

/**
 * The Socket Server to just listen the input request and do simple task.
 * This program was built using socket, because the services, which is meant for simple task should not be bulky.
 * Had tried to integrate with the SpringBoot jar but that SpringBoot jar itself was more than 130MB. 
 * This jar will be less than that, and easy to deploy. Also, the jar conflicts which I had faced 
 * while using SpringBoot are also time consuming to solve and that can creep in again.
 * While running the Server, following parameters be provided:<br>
 * <b> JVM Parameters <b> -Doracle.jdbc.autoCommitSpecCompliant=false -DQuipoz.CSCGroupAsiaWeb.XMLPath=<Path of QuipozCfg.xml> -Ddbconfig=<Path of database.properties>
 * <b> Command line parameter </b> The port on which Server should listen.
 * @author Chandan K Pratihast
 * @version V:1.0.5
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200614          	|Improvement:20200614:Chandan	v1.0.5							|
*  						|ServerHelper made as static									|	
*                       |																|
* **************************************************************************************/
public class Server {
	private Socket socket 			= null;
	private ServerSocket srvSocket	= null;
	private int noOfThreads 		= 3;
	
	Executor e = Executors.newFixedThreadPool(noOfThreads, (Runnable r) -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});
	
	/**
	 * The server for socket communication.
	 * @param port
	 * @throws IOException
	 */
	public Server(int port) throws IOException{
		srvSocket	= new ServerSocket(port);
//		System.out.println(Msg.SRV_STARTED.getMsgStr()+" port "+port);
		System.out.println(Msg.getBanner());
		while(true){ // The server will repeat listening on the port.
			System.out.println(Msg.SRV_WAITING_CLIENT.getMsgStr());
			socket = srvSocket.accept();
			System.out.println(Msg.SRV_CLIENT_CONNECTED.getMsgStr());
			// Working the Server job in ASynchronous way. This internally calls:serverHelper.serveRequest(socket);
			ServerHelper.asyncServeRequest(socket, e);
		}
	}
	
	/**
	 * The main method to start the server. 
	 * @param args: The first parameter is the port number. 
	 */
	public static void main(String[] args) {
			
		try {
			Server srv = new Server(Integer.parseInt(args[0]));
		} catch (NumberFormatException e) {
			System.out.println(Msg.ERR_SERVER_CREATE_PORT.getMsgStr());
//			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(Msg.ERR_SERVER_CREATE_IO.getMsgStr());
//			e.printStackTrace();
		}
		
	}
	
	
}
