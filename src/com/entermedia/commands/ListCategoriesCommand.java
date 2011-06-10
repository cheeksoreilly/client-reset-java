package com.entermedia.commands;

import java.util.Map;

public class ListCategoriesCommand extends BaseCommand
{
	public Object run()
	{
		String catalog = getConfig().getProperty("catalogid", "media/catalogs/photo");
		String parent = getConfig().getProperty("categoryid", "index");
		Map<String, String> categories = getRestClient().listCategories(catalog, parent);
		return categories;
	}
}
