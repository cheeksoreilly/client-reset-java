package com.entermedia.commands;

import java.util.Properties;

public class CreateCatalogCommand extends BaseCommand
{
	public Object run()
	{
		String app = getConfig().getProperty("applicationid", "media");
		String catalogname = getConfig().getProperty("catalogname");
		String newid = getRestClient().createCatalog(app, catalogname);
		Properties result = new Properties();
		result.setProperty("catalogid", newid);
		return result;
	}
}
