package Const;

/**
 * 
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 11:10:39 AM
 * 
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200614          	|Improvement:20200614:Chandan	v1.0.5							|
*  						|Changed banner and added one enum for messages.				|	
*                       |																|
* **************************************************************************************/
public enum Msg {
	SRV_STARTED(				"... Server Started ..."),
	SRV_WAITING_CLIENT(			"... Waiting for client!..."),	
	SRV_CLIENT_CONNECTED(		"... New Client Connected!..."),
	SRV_CLIENT_DISCONNECTED(	"... The Client Closed Socket!..."),
	ERR_SERVER_CREATE_PORT(		"Error in server create! PortCoversion error."),
	ERR_SERVER_CREATE_IO(		"Error in server create! IO failed."),
	ERR_PARSE_QRY(				"Error in parse query!"),
	ERR_INVALID_ACTION_IN_URL(	"Action passed in URL is invalid. Expected Format=> ?action=submit|status|wait4endThenStatus"),
	ERR_HANDLECLINETMSG_UE(	"Unexpected error while processing client message."), //v1.0.5
	URL_ACTION_KEY("action"),	
	URL_BATCHNAME_KEY("bn"),
	URL_BIZDATE_KEY("bd"),
	URL_UID_KEY("uid"),
	URL_TIMEOUT_KEY("tout"),
	OVER_SIGNAL("Bye");
	
	
	private String msg;
	private Msg(String s){ msg = s;}
	public String getMsgStr(){return msg;}
	
	public static String getBanner(){
		StringBuilder sb = new StringBuilder(1000);
		sb.append(" ******************************************************************************************* \r\n");
		sb.append(" ***																						    \r\n");
		sb.append(" ***					IG SOCKET SERVICE STARTED v1.0.5									    \r\n");
		sb.append(" ***																						    \r\n");
		sb.append(" ******************************************************************************************* \r\n");
		return sb.toString();
	}
}
