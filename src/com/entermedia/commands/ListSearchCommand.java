package com.entermedia.commands;

import java.util.Map;

public class ListSearchCommand extends BaseCommand
{
	public Object run()
	{
		String catalog = getConfig().getProperty("catalogid", "media/catalogs/photo");
		String listid = getConfig().getProperty("searchtype");
		Map<String, String> categories = getRestClient().listSearch(catalog, listid);
		return categories;
	}
}
