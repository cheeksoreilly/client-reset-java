package com.entermedia.ui;

import java.util.Map;
import java.util.Properties;

import com.entermedia.commands.Command;
import com.entermedia.commands.CommandFactory;

/**
 * Simple Java client for EnterMedia's REST API.
 * 
 * @author Jorge Valencia <jvalencia@openedit.org>
 *
 */
public class ConsoleClient
{
	protected Properties fieldConfig;
	
	public static void main(String[] args)
	{
		ConsoleClient client = new ConsoleClient();
		client.parseParameters(args);
		client.run();
	}

	private void run()
	{
		Properties config = getConfig();
		String command = config.getProperty("command");
		Command c = CommandFactory.getCommand(command);
		if(c == null)
		{
			System.out.println("Could not create command.");
			return;
		}
		
		c.setup(config);
		Object result = c.run();
		
		if(result instanceof Properties)
		{
			printProperties((Properties) result, "");
		}
		else if(result instanceof Map<?, ?>)
		{
			Map<?,?> map = (Map<?, ?>) result;
			if(map.size() == 0)
			{
				System.out.println("0 results.");
				return;
			}
			Object first = map.values().iterator().next();
			if(first == null)
			{
				return;
			}
			if(first instanceof Properties)
			{
				printPropertiesMap((Map<String, Properties>)map);
			}
			else
			{
				printStringMap((Map<String, String>)map);
			}
		}
	}

	private void parseParameters(String[] args)
	{
		Properties config = new Properties();
		for( int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if(arg.startsWith("-"))
			{
				String key = args[i].substring(1);
				StringBuffer value = new StringBuffer();
				for(int j = i+1; j < args.length; j++)
				{
					if(args[j].startsWith("-"))
					{
						i= j - 1;
						break;
					}
					if(value.length() != 0)
					{
						value.append(" ");
					}
					value.append(args[j]);
				}
				config.setProperty(key, value.toString());
			}
		}
		setConfig(config);
	}
	
	protected void printStringMap(Map<String, String> inPrint)
	{
		for(String key: inPrint.keySet())
		{
			System.out.println(key + ": " + inPrint.get(key));
		}
		System.out.println();
		System.out.println(inPrint.size() + " results.");
	}

	protected void printPropertiesMap(Map<String, Properties> inPrint)
	{
		for(String key: inPrint.keySet())
		{
			System.out.println(key + ":");
			Properties props = inPrint.get(key);
			printProperties(props, "\t");
		}
		System.out.println();
		System.out.println(inPrint.size() + " results.");
	}

	protected void printProperties(Properties inProperties, String inPrefix)
	{
		for(Object prop: inProperties.keySet())
		{
			System.out.println(inPrefix + prop + "=" + inProperties.getProperty(prop.toString()));
		}
	}
	
	public Properties getConfig()
	{
		if (fieldConfig == null)
		{
			fieldConfig = new Properties();
		}
		return fieldConfig;
	}

	public void setConfig(Properties config)
	{
		fieldConfig = config;
	}

}
