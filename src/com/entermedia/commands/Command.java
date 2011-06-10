package com.entermedia.commands;

import java.util.Properties;

public interface Command
{
	public void setup(Properties inProperties);
	public Object run();
}
