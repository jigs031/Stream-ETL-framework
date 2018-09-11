package jn.util;
public abstract class EventProcessor {
		
	/**
	 * Method tranforms the input data.
	 * @param message: Input Kafka Message
	 * @return enriched message
	 */
	public abstract String process(String message);
	
	/**
	 * Method returns true if enriched message is Error Message 
	 * @param message : Enriched/Failed Message
	 * @return returns true if error message else false
	 */
	public boolean isErrorMessage(String message) {
		return false;
	}
		
}
