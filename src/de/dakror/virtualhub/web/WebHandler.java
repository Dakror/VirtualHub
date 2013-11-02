package de.dakror.virtualhub.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class WebHandler implements HttpHandler
{
	@Override
	public void handle(HttpExchange e) throws IOException
	{
		String file = e.getRequestURI().toString();
		
		if (file.endsWith("/")) file += "index.html";
		
		e.getResponseHeaders().add("Content-type", "text/html");
		
		File f = new File(Web.dir, "htdocs" + file);
		
		if (!f.exists()) f = new File(Web.dir, "htdocs/404.html");
		
		String response = Assistant.getFileContent(f);
		
		@SuppressWarnings("unchecked")
		List<String> keys = (List<String>) Collections.list(Web.properties.propertyNames());
		for (String key : keys)
		{
			response = response.replace("%" + key + "%", Web.properties.getProperty(key));
		}
		
		
		e.sendResponseHeaders(200, response.length());
		
		OutputStream os = e.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
