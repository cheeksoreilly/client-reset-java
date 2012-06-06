package com.entermedia.test;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BaseTestCase extends TestCase
{
	HttpClient fieldClient;

	protected String getServerUrl()
	{
		//return "http://demo.entermediasoftware.com/";
		return "http://localhost:8080/";
	}
	
	protected String getDefaultApplicationId()
	{
		return "media";
	}
	
	protected String getDefaultCatalogId()
	{
		return "media/catalogs/public";
	}
	/**
	 * The web services API require a client to log first. 
	 * The login is the same as one used within the EnterMedia usermanager
	 * There are two Cookies that need to be passed in on subsequent requests
	 * 1. JSESSIONID - This is used by resin or similar Java container. Enables short term sessions on the server
	 * 2. entermedia.key - This allows the user to be auto-logged in. Useful for long term connections. 
	 * 	  If the web server is restarted then clients don't need to log in again
	 * @throws Exception
	 */
	public HttpClient getClient() throws Exception
	{
		
		if (fieldClient == null)
		{
			synchronized (this)
			{
				if (fieldClient == null)
				{
					fieldClient = createClient();
				}
			}

		}
		return fieldClient;
	}

	protected HttpClient createClient() throws IOException, HttpException
	{
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/login.xml");
		method.addParameter(new NameValuePair("accountname", "admin"));
		method.addParameter(new NameValuePair("password", "admin"));

		int statusCode = client.executeMethod(method);
		assertEquals(200, statusCode);

		Element root = getXml(method.getResponseBodyAsStream());

		assertNotNull(root);
		String ok = root.attributeValue("stat");
		assertEquals("ok", ok);
		return client;
	}
	protected Element getXml(InputStream inXmlReader)
	{
		try
		{
			SAXReader reader = new SAXReader();
			reader.setEncoding("UTF-8");
			Document document = reader.read(new InputStreamReader(inXmlReader,"UTF-8"));
			Element root = document.getRootElement();
			return root;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	protected Element getElementById(Element inElement, String inId)
	{
		if(inElement == null || inId == null)
		{
			return null;
		}
		
		for(Iterator i = inElement.elementIterator(); i.hasNext();)
		{
			Element e = (Element) i.next();
			if(inId.equals(e.attributeValue("id")))
			{
				return e;
			}
		}
		
		return null;
	}

}
