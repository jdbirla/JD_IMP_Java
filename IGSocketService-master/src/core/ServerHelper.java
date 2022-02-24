package core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.activation.ActivationDataFlavor;

import Const.Msg;
import base.ActionFactory;
import base.BaseAction;
import ext.ParseUrlQuery;

/**
 * The class which does the task of processing client request.
 * @author Chandan K Pratihast
 * 20200613: v1.0.5: Made the method of this class as static, so that it will be like utility class for the server.
 *
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200614          	|Improvement:20200614:Chandan	v1.0.5							|
*  						|Made the method of this class as static, so that it will be	|								|	
*                       |handle client message, added Throwable in catch.				|
* **************************************************************************************/
public class ServerHelper {
	
	/**
	 * This is asynchronous way to handle server processing
	 * @param s : The socket on which the client got connected.
	 * @param e : The passed in executor will be used.
	 * @return Future<Boolean>
	 */
	public static Future<Boolean> asyncServeRequest(Socket s, Executor e){ //20200613: v1.0.5: made it static
		return CompletableFuture.supplyAsync(()->{
			try {
				serveRequest(s);
				return Boolean.TRUE;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return Boolean.FALSE;
			}
		},e);
		
	}
		
	/**
	 * This method takes the message from the client. Then, it serves the client action and respond back.
	 * @param clientSocket : The socket on which the client got connected.
	 * @return true: if completed else false.
	 * @throws IOException : Some error happens on reading the socket.
	 */
	public static void serveRequest(Socket clientSocket) throws IOException{//20200613: v1.0.5: made it static
		try {
			PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in		= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String msgFromClient 	= in.readLine();
			System.out.println("Msg From Client: " + msgFromClient);
			String res = handleClientMsg(msgFromClient);
			outToClient.print(res);
			outToClient.flush();
			in.close();
			outToClient.close();
			clientSocket.close();
		} catch (SocketException se) {
			System.out.println(Msg.SRV_CLIENT_DISCONNECTED.getMsgStr());
		}
	}
	
	/**
	 * To build the http response string. It is used by serveRequest_And_SendHTTPResponse method.
	 * @param bodyMessage
	 * @return
	 */
	private static String buildHttpResponse(String bodyMessage){
		String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n Content-Type: text/html\r\n Content-Length: ";
		String OUTPUT_END_OF_HEADERS = "\r\n\r\n";
		String OUTPUT = "<html><head><title>Response</title></head><body><p>"+bodyMessage+"</p></body></html>";
		
		StringBuilder sb = new StringBuilder(1000);
		sb.append(OUTPUT_HEADERS);
		sb.append(OUTPUT.length());
		sb.append(OUTPUT_END_OF_HEADERS);
		sb.append(OUTPUT);
		return sb.toString();
	}
	
	/**
	 * The message received from client. It is expected to be in URL form.
	 * E.g: Expected URL string: "?action=status&bn=batchname&bizD=20190224&uid=jpacpr&tout=1".
	 * The quote is just to say it is string, in url this is not needed. On Mar 4, 2020, it is enhanced to handle the incoming request from http(browser as well).
	 * If the incoming request is HTTP, from browser then it will come like: GET /?action=submit&bn=G1AUTOALOC&bd=20200110&uid=JPANRY&tout=1 HTTP/1.1
	 * Hence, create the substring accordingly. HTTP request from Chromer Browser sends the request for Favicon, so better to use IE.
	 * @param s : The url string
	 * @return 	The result after handling the url according to the action in it. If the URL is not as expected it will return error message.
	 *  		If the incoming request was in http format then, the response will be created as valid http response and returned. If the incoming request is not http then
	 *  		it will just return plain string.
	 * @since Mar 6, 2020 10:25:33 PM
	 */
	private static String handleClientMsg(String s){
		String ret = "Not OK";
		if(null == s ){
			System.out.println("Unexpected request from client. Browser side call.");
			return "sorry request is not good." ;// Http request can sometime send null, as browser retry.
		}
		try {
			String msg = s;
			boolean isHttpRequest = s.contains("HTTP")?true:false;
			// If the incoming request is HTTP, from browser then it will come like: GET /?action=submit&bn=G1AUTOALOC&bd=20200110&uid=JPANRY&tout=1 HTTP/1.1
			// Hence, create the substring accordingly.
			int start 	= s.indexOf("?");int end 	= s.indexOf("HTTP");
			msg = isHttpRequest?s.substring(start, end).trim():s;
			HashMap<String, String> map = ParseUrlQuery.parseUrl(msg);
			String act 	= map.get(Msg.URL_ACTION_KEY.getMsgStr());
			String bn 	= map.get(Msg.URL_BATCHNAME_KEY.getMsgStr());
			String bd 	= map.get(Msg.URL_BIZDATE_KEY.getMsgStr()); 
			String uid 	= map.get(Msg.URL_UID_KEY.getMsgStr());
			String tout = map.get(Msg.URL_TIMEOUT_KEY.getMsgStr());
			
			BaseAction bAction 	= ActionFactory.getAction(act); 				//v1.0.5 rearranged.
			ret 				= bAction.execute(bn,bd,uid,tout); 				//	(String batchName, String bizDate, String userid, String tout)
			ret 				= isHttpRequest? buildHttpResponse(ret):ret; 	// 	If the incoming request was in http format then response shall also be correct http response.
		} catch (URISyntaxException e) {
			System.out.println(Msg.ERR_PARSE_QRY.getMsgStr());
			ret = Msg.ERR_PARSE_QRY.getMsgStr();
		} catch (Exception e) {
			System.out.println(Msg.ERR_INVALID_ACTION_IN_URL.getMsgStr()+e.getMessage());
//			e.printStackTrace();
			ret = Msg.ERR_INVALID_ACTION_IN_URL.getMsgStr()+e.getMessage();
		} catch (Throwable t) {//20200613: v1.0.5: to capture uncaught exception.
			System.out.println(Msg.ERR_HANDLECLINETMSG_UE.getMsgStr()+t.getMessage());
//			e.printStackTrace();
			ret = Msg.ERR_HANDLECLINETMSG_UE.getMsgStr();//v1.0.5: any uncaught exception shall not spill on console.
		}
		return ret;
	}
	
	/**
	 * For testing purpose, we can call the submitigbatch action directly without interfacing the socket. The VM parameters and command line parameters will be needed. 
	 * The VM Parameter is: -Doracle.jdbc.autoCommitSpecCompliant=false -DQuipoz.CSCGroupAsiaWeb.XMLPath=H:/ckp/tools/allJars/ig/Config/SIT01/QuipozCfg.xml -Ddbconfig=H:/ckp/tools/allJars/ig/Config/SIT01/database.properties
	 * @param args : args[0] like: "?action=status&bn=G1AUTOALOC&bd=20191209&uid=JPANRY&tout=1". The quotes shall be removed.
	 * @since Mar 2, 2020 11:02:19 PM
	 */
	public static void main(String[] args) {
//		String msg = sh.handleClientMsg("?action=status&bn=G1AUTOALOC&bd=20191209&uid=JPANRY&tout=1");
		String msg = ServerHelper.handleClientMsg(args[0]);
		System.out.println(msg);
	}
}
