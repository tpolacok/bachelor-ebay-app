package tomaspolacok.bachelor.application.exceptions;

public class TooManyItemsException extends Exception{
	public TooManyItemsException(String message) {
		super(message);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -3838193247871513025L;
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}

}
