package net.caleba;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AccountServices {
	
	private static final String COOKIE_HEADER = "Cookie";
	
	private static Map<String, Session> sessions = Collections.synchronizedSortedMap(new TreeMap<>());
	private static File accountsFolder = new File(Default.getMainDirectory().getParent() + "/accounts");
	private Request request;
	
	public AccountServices(Request request) {
		this.request = request;
	}
	
	public boolean isLoggedIn() {
		try {
			getSessionCookie(request.getHeader(COOKIE_HEADER));
			return true;
		} catch(NoSuchHeaderException|NotLoggedInException e) {
			return false;
		}
	}
	
	public String getUsername() throws NotLoggedInException {
		try {
			String sessionName = getSessionCookie(request.getHeader(COOKIE_HEADER));
			Session session = sessions.get(sessionName);
			return session.getUsername();
		} catch(NoSuchHeaderException e) {
			throw new NotLoggedInException();
		}
	}
	
	// TODO: didn't split of the header name
	private String getSessionCookie(String cookieString) throws NotLoggedInException {
		String[] cookies = cookieString.split(";");
		for(String cookie: cookies) {
			String[] valuePair = cookie.split("=");
			if(valuePair[0] == "session") {
				return valuePair[1];
			}
		}
		throw new NotLoggedInException();
	}
	
	public boolean hasResponse() {
		String[] path = request.getPath();
		String method = request.getMethod();
		
		if(path[0].equals("account")) {
			switch(path[1]) {
				case "login.html": 
					return method.equals("GET");
			}
		}
		
		return false;
	}
	
	public byte[] getResponse() throws NoResponseException {
		String[] path = request.getPath();
		String method = request.getMethod();
		
		if(method.equals("POST") && path[0].equals("account")) {
			switch(path[1]) {
				
			}
		}
		
		throw new NoResponseException();
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
