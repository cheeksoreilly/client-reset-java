package com.entermedia.commands;

import java.util.Map;

public class ListCatalogsCommand extends BaseCommand
{

	public Object run()
	{
		String app = getConfig().getProperty("applicationid", "media");
		Map<String, String> catalogs = getRestClient().listCatalogs(app);
		return catalogs;
	}

}
