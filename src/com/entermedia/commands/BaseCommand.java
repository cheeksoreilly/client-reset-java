package com.entermedia.commands;

import java.util.Properties;

import com.entermedia.RestClient;

public abstract class BaseCommand implements Command
{
	protected Properties fieldConfig;
	protected RestClient fieldRestClient;
	
	public void setup(Properties inProperties)
	{
		setConfig(inProperties);
		RestClient client = getRestClient();
		client.setServerUrl(inProperties.getProperty("serverurl", "http://demo.entermediasoftware.com/"));
		client.setUserName(inProperties.getProperty("username", "admin"));
		client.setPassword(inProperties.getProperty("password", "admin"));
		client.setRestPath(inProperties.getProperty("restpath", "/services/rest"));
	}

	public RestClient getRestClient()
	{
		if (fieldRestClient == null)
		{
			fieldRestClient = new RestClient();
		}
		return fieldRestClient;
	}

	public Properties getConfig()
	{
		return fieldConfig;
	}

	public void setConfig(Properties config)
	{
		fieldConfig = config;
	}

}
