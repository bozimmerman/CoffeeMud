package com.planet_ink.coffee_mud.interfaces;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public abstract class Scriptable
{
	public static String getScr(String which, String num)
	{
		ResourceBundle scripts=Scripts.load(which);
		if(scripts==null) return "";
		if(scripts.getString(num)!=null)
			return scripts.getString(num);
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

