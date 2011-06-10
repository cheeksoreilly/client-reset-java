package com.entermedia.commands;

public class SearchAssetsCommand extends BaseCommand
{
	public Object run()
	{
		String catalog = getConfig().getProperty("catalogid", "media/catalogs/photo");
		String search = getConfig().getProperty("search", "id matches *");
		String[] pairs = search.split("[,;&]");
		String[] fields = new String[pairs.length];
		String[] values = new String[pairs.length];
		String[] operations = new String[pairs.length];
		for(int i = 0; i < pairs.length; i++)
		{
			String[] pair = pairs[i].split(" ");
			fields[i] = pair[0];
			operations[i] = pair[1];
			values[i] = pair[2];
		}
		return getRestClient().searchAssets(catalog, fields, values, operations);
	}
}
