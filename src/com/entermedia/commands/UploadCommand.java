package com.entermedia.commands;

import java.io.File;
import java.util.Map;

public class UploadCommand extends BaseCommand
{

	public Object run()
	{
		String catalog = getConfig().getProperty("catalogid", "media/catalogs/photo");
		String sourcepath = getConfig().getProperty("sourcepath", "users/" + getRestClient().getUserName());
		String filepath = getConfig().getProperty("file");
		File file = new File(filepath);
		if(!file.exists())
		{
			return null;
		}
		Map<String, String> catalogs = getRestClient().upload(catalog, sourcepath, file);
		return catalogs;
	}

}
