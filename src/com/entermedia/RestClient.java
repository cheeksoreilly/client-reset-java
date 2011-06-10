package com.entermedia;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class RestClient
{
	protected String fieldServerUrl = "http://demo.entermediasoftware.com/";
	protected String fieldUserName = "admin";
	protected String fieldPassword = "admin";
	protected String fieldRestPath = "/services/rest";
	protected String fieldDefaultAppId = "media";
	protected HttpClient fieldClient;
	
	private SAXReader reader = new SAXReader();
	
	/**
	 * The web services API require a client to log first. 
	 * The login is the same as one used within the EnterMedia usermanager
	 * There are two Cookies that need to be passed in on subsequent requests
	 * 1. JSESSIONID - This is used by resin or similar Java container. Enables short term sessions on the server
	 * 2. entermedia.key - This allows the user to be auto-logged in. Useful for long term connections. 
	 * 	  If the web server is restarted then clients don't need to log in again
	 */
	public HttpClient getClient()
	{
		if (fieldClient == null)
		{
			fieldClient = new HttpClient();
			PostMethod method = new PostMethod(getServerUrl() + getDefaultAppId() + getRestPath() + "/login.xml");
			method.addParameter("accountname", getUserName());
			method.addParameter("password", getPassword());
			execute(method);
		}
		return fieldClient;
	}

	/**
	 * Lists the catalogs in an EnterMedia application.
	 * 
	 * @param inApplicationId the application id.
	 * @return a map with catalog ids as keys and catalog names as values.
	 */
	public Map<String, String> listCatalogs(String inApplicationId)
	{
		String url = getServerUrl() + inApplicationId + getRestPath() + "/listcatalogs.xml";
		GetMethod method = new GetMethod(url);
		Element root = execute(method);
		Map<String, String> result = new HashMap<String, String>();
		for( Object o: root.elements("catalog"))
		{
			Element e = (Element)o;
			result.put(e.attributeValue("id"), e.getTextTrim());
		}
		return result;
	}

	/**
	 * List the child categories of a catalog. A category tree can be seen on the left side under the "Assets" link of the application
	 * 
	 * @return a map with asset ids as keys and the asset properties as values.
	 */
	public Map<String, String> listCategories(String inCatalogId, String inParentCategory)
	{
		String appid = extractApplicationId(inCatalogId);
		String url = getServerUrl() + appid + getRestPath() + "/listcategories.xml";
		if( inParentCategory == null )
		{
			inParentCategory = "index";
		}
		PostMethod method = new PostMethod(url);
		method.addParameter("catalogid", inCatalogId);
		method.addParameter("parentcategoryid", inParentCategory);
		Element root = execute(method);
		Map<String, String> result = new HashMap<String, String>();
		for(Object o: root.elements("category"))
		{
			Element e = (Element)o;
			result.put(e.attributeValue("id"), e.getText());
		}
		return result;
	}

	/**
	 * Searches for assets in a catalog.
	 * @param inCatalogId the catalog id to search in.
	 * @param inFields an array of fields to search for. null to get all the assets in the catalog.
	 * @param inValues the values for the fields to search for. Must have the same length as the fields array.
	 * @param inOperations the search operation for each search field (matches, exact, graterthan, etc.) Must have the same length as the fields array.
	 * @return a map with asset ids as keys and asset properties as values.
	 */
	public Map<String, Properties> searchAssets(String inCatalogId, String[] inFields, String[] inValues, String[] inOperations)
	{
		String appid = extractApplicationId(inCatalogId);
		if(inFields == null)
		{
			inFields = new String[] {"id"};
			inValues = new String[] {"*"};
			inOperations = new String[] {"matches"};
		}
		//http://localhost:8080/entermedia/services/rest/assetsearch.xml?catalogid=entermedia/browse/testcatalog&field=fileformat&operation=exact&fileformat.value=pdf
		String url = getServerUrl() + appid + getRestPath() + "/assetsearch.xml";
		PostMethod method = new PostMethod(url);
		method.addParameter("catalogid", inCatalogId);
		for( int i = 0; i < inFields.length; i++ )
		{
			method.addParameter("field", inFields[i]);
			method.addParameter("operation", inOperations[i]);
			method.addParameter(inFields[i] + ".value", inValues[i]);	
		}
		
		Element root = execute(method);
		Element hits = (Element)root.elements().get(0);
		int pages = Integer.parseInt(hits.attributeValue("pages"));
		String sessionid = hits.attributeValue("sessionid");
		
		Map<String, Properties> result = new HashMap<String, Properties>();
		addHits(hits, result);
		
		url = getServerUrl() + appid + getRestPath() + "/getpage.xml";
		for( int i = 2; i <= pages; i++ )
		{
			method = new PostMethod(url);
			method.addParameter("catalogid", inCatalogId);
			method.addParameter("hitssessionid", sessionid);
			method.addParameter("page", String.valueOf(i));
			root = execute(method);
			hits = (Element)root.elements().get(0);
			addHits(hits, result);
		}
		
		return result;
	}

	protected void addHits(Element inHits, Map<String, Properties> inResults)
	{
		for(Object o: inHits.elements("hit"))
		{
			Element e = (Element)o;
			String id = e.attributeValue("id");
			Properties props = new Properties();
			for(Object o2: e.attributes())
			{
				Attribute a = (Attribute)o2;
				props.setProperty(a.getName(), a.getValue());
			}
			inResults.put(id, props);
		}
	}
	
	/**
	 * Gets an asset's details.
	 * @param inCatalogId the asset's catalog id.
	 * @param inAssetId the asset's id.
	 * @return the asset properties.
	 */
	public Properties getAssetDetails(String inCatalogId, String inAssetId)
	{
		String appid = extractApplicationId(inCatalogId);
		String url = getServerUrl() + appid + getRestPath() + "/assetdetails.xml";
		PostMethod method = new PostMethod(url);
		method.addParameter("catalogid", inCatalogId);
		method.addParameter("id", inAssetId);
		Element root = execute(method);
		Properties result = new Properties();
		for(Object o: root.elements("property"))
		{
			Element e = (Element)o;
			result.setProperty(e.attributeValue("id"), e.getTextTrim());
		}
		return result;
	}

	/**
	 * Edit a single asset metadata.
	 * @throws Exception
	 */
	public Properties saveAsset(String inCatalogId, String inAssetId, String[] inFields, String[] inValues)
	{
		if(inFields == null || inFields.length == 0 || inValues == null || inValues.length != inFields.length)
		{
			return null;
		}
		
		String app = extractApplicationId(inCatalogId);
		String url = getServerUrl() + app + getRestPath() + "/saveassetdetails.xml";
		PostMethod save = new PostMethod(url);
		save.addParameter("catalogid", inCatalogId);
		save.addParameter("id", inAssetId);
		Properties result = new Properties();
		for(int i = 0; i < inFields.length; i++)
		{
			String key = inFields[i];
			String value = inValues[i];
			save.addParameter("field", key);
			save.addParameter(key + ".value", value);
			result.setProperty(key, value);
		}
		execute(save);
		return result;		
	}
	
	/**
	 * Do a single file upload using MultiPart HTTP upload
	 * @param inCatalogId the catalog id to upload to.
	 * @param inSourcePath the base source path for the new assets.
	 * @param inFile the file to upload.
	 * @return a map with new asset ids as keys and source paths as values. 
	 */
	public Map<String, String> upload(String inCatalogId, String inSourcePath, File inFile)
	{
		String app = extractApplicationId(inCatalogId);
		String url = getServerUrl() + app + getRestPath() + "/upload.xml?catalogid=" + inCatalogId;
		PostMethod method = new PostMethod(url);

		try
		{
			Part[] parts = { 
					new FilePart("file", inFile.getName(), inFile),
					new StringPart("sourcepath", "users/admin/")
			}; 
			
			method.setRequestEntity( new MultipartRequestEntity(parts, method.getParams()) ); 
	
			Element root = execute(method);
			Map<String, String> result = new HashMap<String, String>();
			for(Object o: root.elements("asset"))
			{
				Element asset = (Element)o;
				result.put(asset.attributeValue("id"), asset.attributeValue("sourcepath"));
			}
			return result;
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	/**
	 * Search for any type of Data in the system. Types of data can be viewed from the catalog Settings area.
	 * @param inCatalogId the catalog id to search in.
	 * @param inListId 
	 */
	public Map<String, String> listSearch(String inCatalogId, String inListId)
	{
		String app = extractApplicationId(inCatalogId);
		String url = getServerUrl() + app + getRestPath() + "/search.xml";
		PostMethod method = new PostMethod(url);
		method.addParameter("catalogid", inCatalogId);
		method.addParameter("searchtype", inListId);		
		Element root = execute(method);
		Map<String, String> result = new HashMap<String, String>();
		for(Object o: root.elements("hit"))
		{
			Element e = (Element)o;
			result.put(e.attributeValue("id"), e.attributeValue("name"));
		}
		return result;
	}

	/**
	 * Creates a new catalog.
	 * @param inApplicationId the Id of the application to create the catalog into.
	 * @param inCatalogName the new catalog's name.
	 * @return the new catalog's Id.
	 */
	public String createCatalog(String inApplicationId, String inCatalogName)
	{
		String url = getServerUrl() + inApplicationId + getRestPath() + "/createcatalog.xml";
		PostMethod method = new PostMethod(url);
		method.addParameter("appfolder", inCatalogName);
		Element root = execute(method);
		return root.element("catalog").attributeValue("id");
	}

	protected Element execute( HttpMethod inMethod )
	{
		try
		{
			int status = getClient().executeMethod(inMethod);
			if(status != 200)
			{
				throw new Exception("Request failed: status code " + status);
			}
			Element result = reader.read(inMethod.getResponseBodyAsStream()).getRootElement();
			return result;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	public String extractApplicationId(String inCatalogId)
	{
		return inCatalogId.substring(0, inCatalogId.indexOf("/"));
	}
	
	public String getServerUrl()
	{
		return fieldServerUrl;
	}

	public void setServerUrl(String serverUrl)
	{
		fieldServerUrl = serverUrl;
	}

	public String getUserName()
	{
		return fieldUserName;
	}

	public void setUserName(String userName)
	{
		fieldUserName = userName;
	}

	public String getPassword()
	{
		return fieldPassword;
	}

	public void setPassword(String password)
	{
		fieldPassword = password;
	}

	public String getRestPath()
	{
		return fieldRestPath;
	}

	public void setRestPath(String restPath)
	{
		fieldRestPath = restPath;
	}

	public String getDefaultAppId()
	{
		return fieldDefaultAppId;
	}

	public void setDefaultAppId(String defaultAppId)
	{
		fieldDefaultAppId = defaultAppId;
	}
}
