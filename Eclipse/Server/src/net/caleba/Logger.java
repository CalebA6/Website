package net.caleba;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	
	private static int nextId = 0;
	private int id;
	private int log = 0;
	private InetAddress ipAddress;
	private int port;
	
	public Logger(InetAddress ipAddress, int port) {
		id = getNextId();
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	private synchronized int getNextId() {
		if(nextId < 0) {
			return nextId = 0;
		} else {
			return nextId++;
		}
	}
	
	public void logInfo(String info) {
		print(getPrefix() + info, System.out);
	}
	
	public void logError(String error) {
		print(getPrefix() + error, System.err);
	}
	
	public void logError(Exception error) {
		StringWriter traceStringWriter = new StringWriter();
		PrintWriter traceStringWriterWriter = new PrintWriter(traceStringWriter);
		error.printStackTrace(traceStringWriterWriter);
		traceStringWriterWriter.flush();
		traceStringWriterWriter.close();
		traceStringWriter.flush();
		print(getPrefix() + traceStringWriter.toString().trim(), System.err);
	}
	
	public static void logGeneralError(String error) {
		print(getTime() + " " + error, System.err);
	}
	
	private synchronized static void print(String message, PrintStream stream) {
		stream.println(message);
	}
	
	private String getPrefix() {
		boolean ipv6 = ipAddress.getAddress().length == 16;
		return id + " " + getLogNum() + " " + getTime() + " " + (ipv6 ? "[" : "") + ipAddress.getHostAddress() + (ipv6 ? "]" : "") + ":" + port + " ";
	}
	
	private synchronized int getLogNum() {
		return log++;
	}
	
	private static String getTime() {
		return OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyLLLdd HH:mm:ssxxx")).toUpperCase();
	}

}
