package com.planet_ink.coffee_mud.interfaces;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public abstract class Scriptable
{
	public static String getScr(String which, String num)
	{
		ResourceBundle scripts=Scripts.load(which);
		if(scripts==null) return "";
		if(scripts.getString(num)!=null)
			return (String)scripts.getString(num);
		return "";
	}
	public static String getScr(String which, String num, String replaceX)
	{
		String msg=getScr(which,num);
		if(msg.length()>0)
			msg=Util.replaceAll(msg,"@x1",replaceX);
		return msg;
	}
	public static String getScr(String which, String num, String replaceX, String replaceX2)
	{
		String msg=getScr(which,num);
		if(msg.length()>0)
		{
			msg=Util.replaceAll(msg,"@x1",replaceX);
			msg=Util.replaceAll(msg,"@x2",replaceX2);
		}
		return msg;
	}
	public static String getScr(String which, String num, 
								   String replaceX, 
								   String replaceX2, 
								   String replaceX3)
	{
		String msg=getScr(which,num);
		if(msg.length()>0)
		{
			msg=Util.replaceAll(msg,"@x1",replaceX);
			msg=Util.replaceAll(msg,"@x2",replaceX2);
			msg=Util.replaceAll(msg,"@x3",replaceX3);
		}
		return msg;
	}
}

