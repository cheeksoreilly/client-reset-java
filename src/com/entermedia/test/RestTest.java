package com.entermedia.test;


import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class RestTest extends BaseTestCase
{
	/**
	 * listing the available catalogs on the server for this user. A catalog ID is in the form of
	 * media/catalogs/documents - Application ID / "catalogs" / Catalog Name
	 * @throws Exception
	 */
	public void testListCatalogs() throws Exception
	{
		GetMethod method = new GetMethod( getServerUrl() + getDefaultApplicationId() + "/services/rest/listcatalogs.xml");
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); //Make sure auto login works
		Element root = getXml(method.getResponseBodyAsStream());
		assertTrue(root.elements().size() > 0);
	}

	/**
	 * List the child categories of a catalog. A category tree can be seen on the left side under the "Assets" link of the application
	 * @throws Exception
	 */
	public void testListCategories() throws Exception
	{
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/listcategories.xml");
		method.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		method.addParameter(new NameValuePair("parentcategoryid", "index"));
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		assertTrue(root.elements().size() > 0);
	}

	/**
	 * Search for assets. Each asset will contain metadata and URL's to the data.
	 * Search results will be limited to only assets that this user has "viewasset" permisions for.
	 * Results will only include a single page of results. Additional pages of results can be retrieved by called getpage.xml
	 * The results will have a hitssessionid that is used to find results
	 * The colums of data that are returned can be changed by editing the asset/restsearchresults view from the Catalog settings area
	 * @throws Exception
	 */
	public void testSearch() throws Exception
	{
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetsearch.xml");
		method.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		method.addParameter(new NameValuePair("field", "fileformat"));
		method.addParameter(new NameValuePair("operation", "exact"));
		method.addParameter(new NameValuePair("fileformat.value", "jpeg"));	
		method.addParameter(new NameValuePair("hitsperpage", "1"));
		method.addParameter(new NameValuePair("sortby", "fileformat"));
		
		
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		Element hits = (Element)root.elements().get(0);
		Element hit = (Element)hits.elements().get(0);
		assertEquals( "jpeg", hit.attributeValue("fileformat").toLowerCase());
		String sessionid = hits.attributeValue("sessionid");
		
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/getpage.xml");
		method.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		method.addParameter(new NameValuePair("hitssessionid", sessionid));
		method.addParameter(new NameValuePair("page", "2"));
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 

		root = getXml(method.getResponseBodyAsStream());
		hits = (Element)root.elements().get(0);
		assertTrue(hits.elements().size() > 0);
		hit = (Element)hits.elements().get(0);
		assertNotNull(hit);
		String page = hits.attributeValue("page");
		assertEquals("2", page);
	}

	/**
	 * Get more information about a single asset
	 * The data that is returned can be changed by editing the asset/restassetdetails view from the Catalog settings area
	 * @throws Exception
	 */
	public void testAssetDetails() throws Exception
	{
		PostMethod method = new PostMethod( getServerUrl() + getDefaultApplicationId() + "/services/rest/assetdetails.xml");
		method.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		method.addParameter(new NameValuePair("id", "105"));
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		assertTrue(root.elements().size() > 0);
		Element hit = getElementById(root, "id");
		assertNotNull(hit);
		assertEquals("105", hit.getTextTrim());
	}

	/**
	 * Edit a single asset metadata. More than one field can be edited at once by adding multiple "field" parameters
	 * @throws Exception
	 */
	public void testSaveAsset() throws Exception
	{
		PostMethod details = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetdetails.xml");
		details.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		details.addParameter(new NameValuePair("id", "105"));		
		int statusCode = getClient().executeMethod(details);
		assertEquals(200, statusCode); 
		Element root = getXml(details.getResponseBodyAsStream());
		String name = getElementById(root, "name").getText();
		details.releaseConnection();
		
		PostMethod save = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/saveassetdetails.xml");
		save.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		save.addParameter(new NameValuePair("id", "105"));
		save.addParameter(new NameValuePair("field", "name"));
		save.addParameter(new NameValuePair("name.value", "test"));		
		statusCode = getClient().executeMethod(save);
		assertEquals(200, statusCode); 
		root = getXml(save.getResponseBodyAsStream());
		save.releaseConnection();
		
		details = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetdetails.xml");
		details.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		details.addParameter(new NameValuePair("id", "105"));		
		statusCode = getClient().executeMethod(details);
		assertEquals(200, statusCode); 
		root = getXml(details.getResponseBodyAsStream());
		assertEquals(getElementById(root, "name").getText(), "test");
		details.releaseConnection();
		
		save = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/saveassetdetails.xml");
		save.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		save.addParameter(new NameValuePair("id", "105"));
		save.addParameter(new NameValuePair("field", "name"));
		save.addParameter(new NameValuePair("name.value", name));
		statusCode = getClient().executeMethod(save);
		assertEquals(200, statusCode); 
		save.releaseConnection();
		
		details = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetdetails.xml");
		details.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		details.addParameter(new NameValuePair("id", "105"));
		statusCode = getClient().executeMethod(details);
		assertEquals(200, statusCode); 
		root = getXml(details.getResponseBodyAsStream());
		assertEquals(getElementById(root,"name").getText(), name);
		details.releaseConnection();
	}
	/**
	 * Do a single file upload using MultiPart HTTP upload
	 * The asset that is created will be returned with an asset "id"
	 * @throws Exception
	 */
	public void testUpload() throws Exception
	{
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/upload.xml?catalogid=" + getDefaultCatalogId());

		File f = new File("etc/testasset.jpg"); 
		
		Part[] parts = { 
				new FilePart("file", f.getName(), f),
				new StringPart("sourcepath", "users/admin/"),
				new StringPart("field", "caption"),
				new StringPart("caption.value", "Testing")
				//new StringPart("libraries", "client1")  //This can be used to enforce security
		};
		
		method.setRequestEntity( new MultipartRequestEntity(parts, method.getParams()) ); 

		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		
		Element root = getXml(method.getResponseBodyAsStream());
		Element asset = (Element)root.elementIterator("asset").next();
		assertNotNull(asset.attributeValue("id"));
	}
	/**
	 * Search for any type of Data in the system. Types of data can be viewed from the catalog Settings area
	 * In this examples we return the uploadstatus options and all the users in the system
	 * @throws Exception
	 */
	public void testListSearch() throws Exception
	{
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/datasearch.xml");
		method.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		method.addParameter(new NameValuePair("searchtype", "fileformat"));
		method.addParameter(new NameValuePair("hitsperpage", "3"));
		
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		Element hits = (Element)root.elements().get(0);
		assertTrue(hits.elements().size() > 2);
		
		String sessionid = hits.attributeValue("sessionid");

		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/datagetpage.xml");
		method.addParameter(new NameValuePair("catalogid", getDefaultCatalogId()));
		method.addParameter(new NameValuePair("searchtype", "fileformat"));
		method.addParameter(new NameValuePair("hitssessionid", sessionid));
		method.addParameter(new NameValuePair("page", "2"));
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 

		
		
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/search.xml");
		method.addParameter(new NameValuePair("catalogid", "system"));
		method.addParameter(new NameValuePair("searchtype", "user"));		
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		root = getXml(method.getResponseBodyAsStream());
		assertTrue( root.elements().size() > 0);
		
			
	}
	
	/**
	 * A user uploads a file to the data/originals folder through FTP or any other way.
	 * This user runs this action to create an asset from the file, or multiple assets if it's a zip file
	 * 
	 * @throws Exception
	 */
	public void testCreateAndDelteAssetFromFile() throws Exception
	{
		// Upload the file
		String testname = "Test Asset " + System.currentTimeMillis();
		File f = new File("etc/testasset.jpg");
		String destUrl = "/WEB-INF/data/" + getDefaultCatalogId() + "/originals/test/";
		
		Part[] parts = { 
				new FilePart("file", testname + ".jpg", f),
				new StringPart("path", destUrl)
		};
		
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/settings/filemanager/upload/uploadfile-finish.html");
		method.setRequestEntity( new MultipartRequestEntity(parts, method.getParams()) );
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		
		String sourcepath = "test/" + testname + ".jpg";
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/importassets.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("sourcepath", sourcepath);
		method.addParameter("field", "caption");
		method.addParameter("caption.value", testname);
		
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		assertTrue(root.elements().size() > 0);
		
		String id = ((Element)root.elements().get(0)).attributeValue("id");
		assertNotNull(id);
		
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/deleteassets.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("assetid", id);
		statusCode = getClient().executeMethod(method);
		assertEquals("Response must be 200", 200, statusCode);
		
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetsearch.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("field", "id");
		method.addParameter("id.value", id);	
		method.addParameter("operation", "exact");
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode);
		
		root = getXml(method.getResponseBodyAsStream());
		Element hits = (Element)root.elements().get(0);
		assertEquals("Asset should not be found", 0, hits.elements().size());
	}
	public void testCreateAndDeleteZipAssetFromFile() throws Exception
	{
		// Upload the file
		String testname = "Test Assets " + System.currentTimeMillis();
		File f = new File("etc/testimages.zip");
		String destUrl = "/WEB-INF/data/" + getDefaultCatalogId() + "/originals/test/" + testname + ".zip";
		
		Part[] parts = { 
				new FilePart("file", testname + ".zip", f),
				new StringPart("path", destUrl)
		};
		
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/settings/filemanager/upload/uploadfile-finish.html");
		method.setRequestEntity( new MultipartRequestEntity(parts, method.getParams()) );
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		
		String sourcepath = "test/" + testname + ".zip";
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/importassets.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("sourcepath", sourcepath);
		method.addParameter("field", "caption");
		method.addParameter("unzip", "true");
		method.addParameter("caption.value", testname);
		
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		assertEquals(3, root.elements().size() );
		
		for (Iterator iterator = root.elementIterator(); iterator.hasNext();)
		{
			Element child = (Element) iterator.next();
			String id = child.attributeValue("id");
			assertNotNull(id);
			
			method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/deleteassets.xml");
			method.addParameter("catalogid", getDefaultCatalogId());
			method.addParameter("assetid", id);
			method.addParameter("deleteoriginal", "true");
			
			statusCode = getClient().executeMethod(method);
			assertEquals("Response must be 200", 200, statusCode);
			
			method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetsearch.xml");
			method.addParameter("catalogid", getDefaultCatalogId());
			method.addParameter("field", "id");
			method.addParameter("id.value", id);	
			method.addParameter("operation", "exact");
			statusCode = getClient().executeMethod(method);
			assertEquals(200, statusCode);
			
			root = getXml(method.getResponseBodyAsStream());
			Element hits = (Element)root.elements().get(0);
			assertEquals("Asset should not be found", 0, hits.elements().size());
		}
	}
	
	/**
	 * Do an OR search (search for more than one asset ID)
	 */
	public void testOrSearch() throws Exception
	{
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetsearch.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("field", "id");
		method.addParameter("operation", "exact");
		method.addParameter("id.value", "101");	
		method.addParameter("field", "id");
		method.addParameter("operation", "exact");
		method.addParameter("id.value", "102");	
		method.addParameter(new NameValuePair("sortby", "idUp"));
		
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode); 
		Element root = getXml(method.getResponseBodyAsStream());
		Element hits = (Element)root.elements().get(0);
		assertEquals(2, hits.elements().size());
		
		Element hit = (Element)hits.elements().get(0);
		String id = hit.attributeValue("id");
		assertTrue("101".equals(id) );
		hit = (Element)hits.elements().get(1);
		id = hit.attributeValue("id");
		assertTrue("102".equals(id));
	}
	
	/**
	 * Test clients join data
	 */
	public void testJoinData() throws Exception
	{
		// Add client 1 to an asset
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/savedata.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("searchtype", "assetclients");
		method.addParameter("field", "assetid");
		method.addParameter("assetid.value", "101");	
		method.addParameter("field", "clientid");
		method.addParameter("clientid.value", "1");	
		int statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode);

		Element droot = getXml(method.getResponseBodyAsStream());
		Element data = (Element)droot.element("data");
		assertNotNull( data.attributeValue("id") );
		
		
		// Reindex asset
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/saveassetdetails.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("id", "101");
		method.addParameter("field", "test");
		method.addParameter("test.value", "test");
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode);
		
		// Search assets with client 1
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/assetsearch.xml");
		method.addParameter("catalogid", getDefaultCatalogId());
		method.addParameter("field", "assetclients.clientid");
		method.addParameter("assetclients.clientid.value", "1");	
		method.addParameter("operation", "matches");
		statusCode = getClient().executeMethod(method);
		assertEquals(200, statusCode);
		
		// Should be more than zero
		Element root = getXml(method.getResponseBodyAsStream());
		Element hits = (Element)root.elements().get(0);
		
		// If the test fails here open up fields/asset.xml and
		// uncomment back in the assetclients.clientid property, at the bottom
		// sockassertTrue(hits.elements().size() > 0);
		
		boolean found = false;
		for(Object o: hits.elements("hit"))
		{
			Element hit = (Element)o;
			if("101".equals(hit.attributeValue("id")))
			{
				found = true;
				break;
			}
		}
		assertTrue(found);
		
	}
	
	public void testCreateCatalog() throws Exception
	{
		long time = System.currentTimeMillis();
		PostMethod method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/createcatalog.xml");
		method.addParameter("appfolder", "new" + time);	
		int statusCode = getClient().executeMethod(method);
		assertEquals("Response was not 200", 200, statusCode);
		
		Element root = getXml(method.getResponseBodyAsStream());
		Element cat = root.element("catalog");
		assertNotNull("There is not a catalog element in the response", cat);
		assertEquals("New catalog id is wrong", "media/catalogs/new"+time, cat.attributeValue("id"));
	}
	
	public void testRemoveAssetImages() throws Exception
	{
		PostMethod method = new PostMethod(getServerUrl() + getDefaultCatalogId() + "/downloads/preview/thumb/newassets/admin/101/thumb.jpg");
		int statusCode = getClient().executeMethod(method);
		assertEquals("Response must be 200", 200, statusCode);
		Header modified1 = method.getResponseHeader("Last-Modified");
		
		method = new PostMethod(getServerUrl() + getDefaultApplicationId() + "/services/rest/removeassetimages.xml");
		method.addParameter("assetid", "101");
		method.addParameter("catalogid", getDefaultCatalogId());
		statusCode = getClient().executeMethod(method);
		assertEquals("Response must be 200", 200, statusCode);
		
		Thread.sleep(1000);
		method = new PostMethod(getServerUrl() + getDefaultCatalogId() + "/downloads/preview/thumb/newassets/admin/101/thumb.jpg");
		statusCode = getClient().executeMethod(method);
		assertEquals("Response must be 200", 200, statusCode);
		Header modified2 = method.getResponseHeader("Last-Modified");
		
		assertFalse("modification time for thumbs must be different", modified1.getValue().equals(modified2.getValue()));
	}
	
}
