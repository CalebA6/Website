package net.caleba.nonstatic_pages;

import net.caleba.Request;
import net.caleba.Response;

public interface NonstaticPage {
	public boolean checkAddress(String address);
	public Response newThread(Request request);
}
