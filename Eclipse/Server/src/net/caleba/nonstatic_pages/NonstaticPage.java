package net.caleba.nonstatic_pages;

import java.net.Socket;

public interface NonstaticPage {
	public boolean checkAddress(String address);
	public byte[] newThread(String method, String page, String httpVersion, Socket connection, String user);
}
