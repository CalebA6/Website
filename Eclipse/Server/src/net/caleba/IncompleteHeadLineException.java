package net.caleba;

public class IncompleteHeadLineException extends Exception {

	private static final long serialVersionUID = 672912106194721639L;
	private boolean timeout;
	
	public IncompleteHeadLineException(boolean timeout) {
		this.timeout = timeout;
	}
	
	public boolean isTimeout() {
		return timeout;
	}

}
