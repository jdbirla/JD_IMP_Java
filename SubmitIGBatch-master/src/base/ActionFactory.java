package base;

public class ActionFactory {
	/**
	 * The factory class to return the actual action class (singleton object) based on the action parameter passed by user.
	 * @param action <br>
	 * action=submit : to submit the batch<br>
     * action=status : to check status of previous submit.<br>
     * action=wait4EndThenStatus : wait in loop till it completes or abort or cancelled.<br>
	 * @return BaseAction object.
	 * @throws Exception : If no recognized action.
	 * @since Mar 2, 2020 10:30:47 AM
	 */
	public static BaseAction getAction(String action) throws Exception{
		BaseAction ba;
		
		Action a = Action.valueOf(action);
		switch(a){
			case submit:
				ba =  SubmitAction.getInstance();
				break;
			case status:
				ba = StatusChkAction.getInstance();
				break;
			case wait4EndThenStatus:
				ba =  WaitCompleteAction.getInstance();
				break;
			default:
				System.out.println("No Action");
				ba = null;
				throw new Exception("No Recognized Action Passed!");
		}
		return ba;
	}
}
