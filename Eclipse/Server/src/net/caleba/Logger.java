package net.caleba;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	
	public void logInfo(String info) {
		print(getTime() + " " + info, System.out);
	}
	
	public void logError(String error) {
		print(getTime() + " " + error, System.err);
	}
	
	public void logError(Exception error) {
		StringWriter traceStringWriter = new StringWriter();
		PrintWriter traceStringWriterWriter = new PrintWriter(traceStringWriter);
		error.printStackTrace(traceStringWriterWriter);
		traceStringWriterWriter.flush();
		traceStringWriterWriter.close();
		traceStringWriter.flush();
		print(getTime() + " " + traceStringWriter.toString().trim(), System.err);
	}
	
	private synchronized void print(String message, PrintStream stream) {
		stream.println(message);
	}
	
	private String getTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyLLLdd HH:mm:ss")).toUpperCase();
	}

}
