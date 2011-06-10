package com.entermedia.commands;

public class AssetDetailsCommand extends BaseCommand
{
	public Object run()
	{
		String catalog = getConfig().getProperty("catalogid", "media/catalogs/photo");
		String id = getConfig().getProperty("id");
		return getRestClient().getAssetDetails(catalog, id);
	}
}
