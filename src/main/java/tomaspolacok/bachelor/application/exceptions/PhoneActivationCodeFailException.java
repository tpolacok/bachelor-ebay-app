package tomaspolacok.bachelor.application.exceptions;

public class PhoneActivationCodeFailException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8100503283847741557L;
	public PhoneActivationCodeFailException(String message) {
		super(message);
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}

}
