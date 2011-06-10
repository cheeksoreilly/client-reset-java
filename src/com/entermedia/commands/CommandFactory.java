package com.entermedia.commands;

public class CommandFactory
{

	public static Command getCommand(String inPackage, String inName)
	{
		try
		{
			Class<?> c = Class.forName(inPackage + "." + inName);
			return (Command)c.newInstance();
		}
		catch( Exception e )
		{
			e.printStackTrace(System.out);
			return null;
		}
	}
	
	public static Command getCommand(String inName)
	{
		inName = inName.substring(0,1).toUpperCase() + inName.substring(1);
		inName = inName + "Command";
		return getCommand("com.entermedia.commands", inName);
	}
	
}
