package net.caleba.nonstatic_pages;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.caleba.Connection;
import net.caleba.Default;
import net.caleba.EasyByteArray;
import net.caleba.Session;

public class Account implements NonstaticPage {

	public static final String LOCATION = "account";
	
	private static Map<String, Session> sessions = Collections.synchronizedSortedMap(new TreeMap<>());
	
	private File accountsFolder = new File(Default.getMainDirectory().getParent() + "/accounts");
	
	public boolean checkAddress(String address) {
		if(address.equals(LOCATION)) return true;
		if(address.length() <= LOCATION.length()) return false;
		if(address.substring(0, LOCATION.length()+1).equals(LOCATION + "/")) return true;
		return false;
	}

	public byte[] newThread(String method, String page, String httpVersion, Socket connection, String user) {
		try {
			char[] newLineArray = {13, 10};
			String newLine = new String(newLineArray);
			String[] path = page.split("/");
			
			// Handles Already Logged in User
			if(user != null) {
				
				// Reads Arguments from Socket
				InputStream conInput = connection.getInputStream();
				StringBuilder requestBuilder = new StringBuilder();
				long start = System.nanoTime();
				while(true) {
					int in = conInput.read();
					if(in > -1) {
						requestBuilder.append((char)in);
					}
					int length = requestBuilder.length();
					try{
						if(requestBuilder.charAt(length-1) == 10 && requestBuilder.charAt(length-2) == 13 && requestBuilder.charAt(length-3) == 10 && requestBuilder.charAt(length-4) == 13) {
							break;
						}
					} catch(StringIndexOutOfBoundsException e) {
						
					}
					if(System.nanoTime() - start > 3000000000L) {
						System.err.println("Timed out.");
						break;
					}
				}
				String request = method + " " + page + " " + httpVersion + "\r\n" + requestBuilder.toString();
				
				if(request.substring(0, 4).equals("GET ")) {
					if(path.length == 1) return Connection.respondWithFile("account/accountSuperPage.html", user); else 
						return Connection.respondWithFile("account/account.html", user);
				} else if(request.substring(0, 5).equals("POST ")) {                   // Changing Password
					System.out.println("Password Change Attempt: " + user);
					int lineZero = 0;
					String[] data = request.split(newLine);
					try {
						while(!data[lineZero].equals("")) {
							++lineZero;
						}
					} catch(ArrayIndexOutOfBoundsException e) {
						return ("HTTP/1.1 500 NoData" + newLine + newLine + "Failure").getBytes();
					}
					if(checkPassword(data[lineZero+1], user.split("/")[0])) {
						File account = new File(accountsFolder.getPath() + "/" + user);
						EasyByteArray fileData = new EasyByteArray((int)account.length());
						BufferedInputStream input = new BufferedInputStream(new FileInputStream(account));
						for(int i=0; i<account.length(); ++i) {
							fileData.add((byte)input.read());
						}
						input.close();
						String newHash = hash(data[lineZero+2]);
						for(int i=0; i<8; ++i) {
							fileData.set(i, (byte)newHash.charAt(i));
						}
						OutputStream output = new FileOutputStream(account);
						output.write(fileData.toArray());
						output.close();
						System.out.println("Password Changed: " + user);
						return (("HTTP/1.1 200 OK" + newLine + newLine + "success").getBytes());
					} else {
						return (("HTTP/1.1 200 OK" + newLine + newLine + "failure").getBytes());
					}
				} else {
					return ("HTTP/1.1 405 USE POST OR GET" + newLine + newLine + "<html><head><title>ERROR</title></head>"
							+ "<body>Method not allowed. <br><a href=\"" + Default.getAddress() + "\">&#8592;Home</a></body></html>").getBytes();
				}
			}
			
			// Handles Not Logged in
			if(path.length == 1) return Connection.respondWithFile("account", user); else {
				if(path[1].equals("main.html")) {
					if(user == null) return Connection.respondWithFile("account/main.html", null);
				}
				else {
					if(accountExists(path[1])) {
						// Handles login
						if(method.equals("POST")) {
							if(checkPassword(path[2], path[1])) {
								System.out.println("Login: " + path[1]);
								return ("HTTP/1.1 200 Login" + newLine + newLine + "success" + newLine + new Session(sessions).getID()).getBytes();
							} else {
								System.out.println("Invalid Password Login Attempt");
								return (("HTTP/1.1 200 Login" + newLine + newLine + "login failure").getBytes());
							}
						} else {
							// Handles session connection
							Session session = sessions.get(path[2]);
							if(session == null) {
								return Connection.respondWithFile("account/relogin.html", null); // If session does not exist
							}
							int accountCharacters = path[0].length() + path[1].length() + path[2].length() + 2;
							StringBuilder subRequest = new StringBuilder();
							subRequest.append(method);
							subRequest.append(" ");
							String subPage = page.substring(accountCharacters);
							if(subPage.length() < 1 || subPage.charAt(0) != '/') {
								subRequest.append('/');
							}
							subRequest.append(subPage);
							subRequest.append(" ");
							subRequest.append(httpVersion);
							return Connection.getResponse(subRequest.toString(), connection, path[1] + "/" + session.replace());
						}
					} else {
						return (("HTTP/1.1 200 OK" + newLine + newLine + "login failure").getBytes());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ("HTTP/1.1 500 EndOfCode\r\n\r\n<html><head><title>ERROR</title></head>"
				+ "<body>Something went wrong. <br><a href=\"" + Default.getAddress() + "\">&#8592;Home</a></body></html>").getBytes();
	}
	
	public static boolean accountExists(String username) {
		File accountsFolder = new File(Default.getMainDirectory().getParent() + "/accounts");
		String[] accounts = accountsFolder.list();
		for(String potential: accounts) {
			if(potential.equals(username)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean validUsername(String username) {
		for(int i=0; i<username.length(); ++i) {
			char c = username.charAt(i);
			if(0 <= c && c <= 31) {
				return false;
			}
			if(c == 60 || c == 62) {
				return false;
			}
			if(127 <= c && c <= 159) {
				return false;
			}
			if(0x2028 <= c && c <= 0x2029) {
				return false;
			}
			if(0xE0001 == c || 0xE0065 == c || 0xE006E == c || 0xE002D == c || 0xE0075 == c || 0xE0073 == c) {
				return false;
			}
			if(0xFFF9 <= c && c <= 0xFFFB) {
				return false;
			}
			if(0x061C == c || 0x200E <= c && c <= 0x200F || 0x202A <= c && c <= 0x202E || 0x2066 <= c && c <= 0x2069) {
				return false;
			}
		}
		return true;
	}
	
	private boolean checkPassword(String password, String user) throws IOException {
		File account = new File(accountsFolder.getPath() + "/" + user);
		String hash = "";
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(account));
		for(int i=0; i<8; ++i) {
			hash += (char)in.read();
		}
		in.close();
		return hash(password).equals(hash);
	}
	
	private String hash(String input) {
		List<Integer> primes = new ArrayList<>();
		primes.add(2);
		for(int i=0; i<input.length()-1; i++) {
			int start = primes.get(i) + 1;
			while(true) {
				boolean prime = true;
				for(int j=2; j<start; j++) {
					if(start%j == 0) {
						prime = false;
						break;
					}
				}
				if(prime) {
					primes.add(start);
					break;
				} else {
					start++;
				}
			}
		}
		String doubInput = input + input;
		int numPrimes = primes.size();
		int[] doubPrimes = new int[numPrimes*2];
		for(int i=0; i<numPrimes*2; i++) {
			doubPrimes[i] = primes.get(i%numPrimes);
		}
		List<Integer> sums = new ArrayList<>();
		for(int i=0; i<input.length(); i++) {
			int sum = 0;
			for(int j=i; j<input.length()+i; j++) {
				sum += doubInput.charAt(j) * doubPrimes[j-i];
			}
			sums.add(sum);
		}
		while(sums.size() < 8) {
			int sum = 0;
			for(int i=sums.size()-1; i>=0; i--) {
				sum += sums.get(i);
			}
			sums.add(sum);
		}
		int[] hash = new int[8];
		int loopMax = sums.size() / 8;
		int loopOver = sums.size() % 8;
		for(int i=0; i<8; i++) {
			hash[i] = sums.get(i);
			int loops = loopMax;
			if(i < loopOver) {
				loops++;
			}
			for(int j=1; j<loops; j++) {
				hash[i] = sums.get(j*8 + i);
			}
			hash[i] %= 256;
		}
		StringBuilder output = new StringBuilder();
		for(int i=0; i<8; i++) {
			output.append((char)hash[i]);
		}
		return output.toString();
	}
	
}
