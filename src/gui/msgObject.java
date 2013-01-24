package gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class msgObject implements Serializable 
{
	private String command;
	private ArrayList<Object> parameters;
	
	public msgObject()
	{
		command=null;
		parameters=new ArrayList<Object>();
	}
	
	public void setCommand(String newMessage)
	{
		command=newMessage;
	}
	
	public void addParameters(Object newObject)
	{
		parameters.add(newObject);
	}
	
	public String getCommand()
	{
		return command;
	}
	
	public Object popObject()
	{
		return parameters.remove(0);
	}
	
	public Object getObject()
	{
		return parameters.get(0);
	}

	public List<Object> getParameters()
	{
		return parameters;
	}
	
	
}
