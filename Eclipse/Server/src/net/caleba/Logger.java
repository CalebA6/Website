package net.caleba;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	
	private static int nextId = 0;
	private int id;
	private int log = 0;
	
	public Logger() {
		id = getNextId();
	}
	
	private synchronized int getNextId() {
		if(nextId < 0) {
			return nextId = 0;
		} else {
			return nextId++;
		}
	}
	
	public void logInfo(String info) {
		print(id + " " + getLogNum() + " " + getTime() + " " + info, System.out);
	}
	
	public void logError(String error) {
		print(id + " " + getLogNum() + " " + getTime() + " " + error, System.err);
	}
	
	public void logError(Exception error) {
		StringWriter traceStringWriter = new StringWriter();
		PrintWriter traceStringWriterWriter = new PrintWriter(traceStringWriter);
		error.printStackTrace(traceStringWriterWriter);
		traceStringWriterWriter.flush();
		traceStringWriterWriter.close();
		traceStringWriter.flush();
		print(id + " " + getLogNum() + " " + getTime() + " " + traceStringWriter.toString().trim(), System.err);
	}
	
	private synchronized void print(String message, PrintStream stream) {
		stream.println(message);
	}
	
	private synchronized int getLogNum() {
		return log++;
	}
	
	private String getTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyLLLdd HH:mm:ss")).toUpperCase();
	}

}
