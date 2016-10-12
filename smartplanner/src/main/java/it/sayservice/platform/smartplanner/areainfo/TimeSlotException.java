package it.sayservice.platform.smartplanner.areainfo;

public class TimeSlotException extends Exception {

	private static final long serialVersionUID = -437092178299024861L;

	public TimeSlotException() {
		super();
	}

	public TimeSlotException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TimeSlotException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeSlotException(String message) {
		super(message);
	}

	public TimeSlotException(Throwable cause) {
		super(cause);
	}

}
