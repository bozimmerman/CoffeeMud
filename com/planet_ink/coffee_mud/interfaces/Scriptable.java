package com.planet_ink.coffee_mud.interfaces;
import com.planet_ink.coffee_mud.utils.*;

public abstract class Scriptable
{
	protected static String getScr(String which, int num)
	{
		String[] scripts=Scripts.load(which);
		if(scripts==null) scripts=new String[1];
		if((num>=0)&&(num<scripts.length))
			return scripts[num];
		return "";
	}
	protected static String getScr(String which, int num, String replaceX)
	{
		String msg=getScr(which,num);
		if(msg.length()>0)
			msg=Util.replaceAll(msg,"@x1",replaceX);
		return msg;
	}
	protected static String getScr(String which, int num, String replaceX, String replaceX2)
	{
		String msg=getScr(which,num);
		if(msg.length()>0)
		{
			msg=Util.replaceAll(msg,"@x1",replaceX);
			msg=Util.replaceAll(msg,"@x2",replaceX2);
		}
		return msg;
	}
	protected static String getScr(String which, int num, 
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
