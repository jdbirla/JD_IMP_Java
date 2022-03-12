package core;

//A Java program for a Client 
import java.net.*;
import java.util.Scanner;
import java.io.*; 

/**
 * The sample client class to send url message to server. 
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 11:11:21 AM
 */
public class Client 
{ 
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
 
    public void startConnection(String ip, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in 	= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
 
    public String sendMessage(String msg) throws IOException {
        out.println(msg);out.flush();
        String resp = in.readLine();
        return resp;
    }
 
    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
    
    
    /**
     * A sample client to connect to the server.
     * Usage: java -cp <PathOf_IGSocketService.jar> core.Client <IP> <port> action=<status|submit|wait4EndThenStatus> bn=<BatchName> bd=<BizDate> uid=<UserId> tout=<multipleOn10Sec>
     * Basically the steps will be:
     * 1. client  = new Client(); <br>
     * 2. String msg = build the string in the format URL query. The start character has to be ?, and key=value separated by &. E.g: ?action=status&bn=G1AUTOALOC&bd=20250901&uid=BATCHUSER&tout=1 <br>
     * 3. send message by using client.sendMessage(msg). This returns the response as String. <br>
     * 4. stop the connection by calling client.stopConnection().<br>
     * action=submit : to submit the batch<br>
     * action=status : to check status of previous submit.<br>
     * action=wait4EndThenStatus : wait in loop till it completes or abort or cancelled.<br>
     * @param args
     */
	public static void main(String args[])  
	{ 
		String ip;
		int port = 0;
		
		if(args.length < 7){
			System.out.println("Usage Spec: java -cp <PathOf_IGSocketService.jar> core.Client <IP> <port> action=<status|submit|wait4EndThenStatus> bn=<BatchName> bd=<BizDate> uid=<UserId> tout=<multipleOn10Sec>");
			System.out.println("Usage Example: java -cp ./igsocketservice_1.0.3.jar core.Client 127.0.0.1 2020 action=status bn=G1AUTOALOC bd=20250901 uid=BATCHUSER tout=1");
		}else{
			try {
				Client client = new Client();
				ip = args[0];
				port = Integer.parseInt(args[1]);
				client.startConnection(ip, port);
				System.out.println("Client Sending Message!");
				StringBuilder sb = new StringBuilder(200);
				sb.append("?");
				for (int i = 2; i < 6; i++) {
					sb.append(args[i]);
					sb.append("&");
				}
				sb.append(args[6]);
				System.out.println(sb.toString());
//				String respFromServer =	client.sendMessage("?action=status&bn=G1AUTOALOC&bd=20250901&uid=BATCHUSER&tout=1");
				String respFromServer = client.sendMessage(sb.toString());
				System.out.println("Response From Server :" + respFromServer);
				client.stopConnection();
			} catch (Exception e) {
				System.out.println(
						"Looks like not able to connect to server. Please confirm the server running and on port: "
								+ port);
			}
		}
	    
	} 
} 

