package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class Sessions
{
	private static Vector all=new Vector();
	public static Session elementAt(int x)
	{
		return (Session)all.elementAt(x);
	}
	public static int size()
	{
		return all.size();
	}
	public static void addElement(Session S)
	{
		all.addElement(S);
	}
	public static void removeElementAt(int x)
	{
		all.removeElementAt(x);
	}
	public static void removeElement(Session S)
	{
		all.removeElement(S);
	}
}
