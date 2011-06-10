package com.entermedia.commands;

public class SaveAssetCommand extends BaseCommand
{
	public Object run()
	{
		String catalog = getConfig().getProperty("catalogid", "media/catalogs/photo");
		String list = getConfig().getProperty("values");
		String id = getConfig().getProperty("id");
		
		String[] pairs = list.split("[,;&]");
		String[] fields = new String[pairs.length];
		String[] values = new String[pairs.length];
		for(int i = 0; i < pairs.length; i++)
		{
			String[] pair = pairs[i].split("=");
			fields[i] = pair[0];
			values[i] = pair[1];
		}
		return getRestClient().saveAsset(catalog, id, fields, values);
	}
}
