package com.entermedia.test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.dom4j.Element;


public class LoadTest extends BaseTestCase
{
	List<Part> parts;
	File f = new File("etc/loadtestasset.jpg"); 
	long jobid = new Date().getTime();
	int jobtype = 1;
	int sequence = 1;
	long sent = 0;
	int failed = 0;
	ThreadLocal perthread = new ThreadLocal();
	long startjob = System.currentTimeMillis();
	
	public void testUploading() throws Exception
	{
		Runnable test = new Runnable()
		{
			public void run() {
				execute();
			}
		};
				
		ThreadPoolExecutor runner = new ThreadPoolExecutor(4, 4, 10L, TimeUnit.MINUTES, 
				new LinkedBlockingQueue<Runnable>(),  
				new ThreadPoolExecutor.CallerRunsPolicy());

		for (int i = 0; i < 10; i++)
		{
			runner.execute(test);
		}
		runner.shutdown();
		runner.awaitTermination(30L, TimeUnit.MINUTES);
		log("completed: sent :  " + sent + " failed:" + failed );
		assertTrue(failed == 0);
	}
	
	protected void execute() 
	{
		try
		{ 
			long id = jobid++;
			PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/upload.xml?catalogid=" + getDefaultCatalogId());

			String fullid = String.valueOf(id);
			StringBuffer sourcepath = new StringBuffer();
			for (int i = 0; i < fullid.length(); i++)
			{
				if( i > 0 && i % 3 == 0)
				{
					sourcepath.append("/");
				}
				sourcepath.append(fullid.charAt(i));
			}
			sourcepath.append("/" + jobtype + "_" + sequence );
			
			List<Part> customparts = new ArrayList<Part>();
			customparts.add( new FilePart("file", id + f.getName(), f) );
			
			customparts.add( new StringPart("sourcepath", sourcepath.toString()) );
			customparts.addAll( getParts() );
			
			Part[] all = customparts.toArray(new Part[customparts.size()]);
			method.setRequestEntity( new MultipartRequestEntity(all, method.getParams()) ); 

			try
			{
				long start = System.currentTimeMillis();
				int statusCode = getClient().executeMethod(method);
				assertEquals(200, statusCode); 
				Element root = getXml(method.getResponseBodyAsStream());
				//log( root.asXML() );
				Element asset = (Element)root.elementIterator("asset").next();
				assertNotNull(asset.attributeValue("id"));
				sent++;
				long end = System.currentTimeMillis();
				long diff = (end - startjob);
				if( diff > 1000 )
				{
					BigDecimal timesofar =  new BigDecimal(diff).divide( new BigDecimal(60000L),4,RoundingMode.HALF_UP );
					BigDecimal assetspermin = new BigDecimal(sent).divide(timesofar,4,RoundingMode.HALF_UP);
					log(sent + " returned in " + (end-start) + " milliseconds " + assetspermin + " assets per minute" );
				}
				
			}
			finally
			{
				method.releaseConnection();				
			}
		}
		catch ( Throwable ex )
		{
			failed++;
			//ex.printStackTrace();
			
		}
	}
	protected void log(String inLog)
	{
		System.out.println(inLog);
		
	}
	public synchronized HttpClient getClient() throws Exception
	{
		HttpClient client = (HttpClient)perthread.get();
		if( client == null )
		{
			client = createClient();
			perthread.set(client);
		}
		return client;
	}

	public synchronized List<Part> getParts() throws Exception
	{
		if( parts == null )
		{
			Element data = getXml(new FileInputStream(new File("etc/data.xml") ) );
			List rows = data.elements();
			parts = new ArrayList<Part>();
			for (Iterator iterator = data.elementIterator(); iterator.hasNext();)
			{
				Element element = (Element) iterator.next();
				String field = element.attributeValue("name");
				if( field == null || field.length() == 0 )
				{
					throw new IllegalArgumentException();
				}
				parts.add( new StringPart("field", field) );
				String value = element.getTextTrim();
				if( value == null || value.length() == 0 )
				{
					throw new IllegalArgumentException();
				}
				parts.add( new StringPart(field + ".value", value) );
			}
		}
		return parts;
	}
	
	
}
