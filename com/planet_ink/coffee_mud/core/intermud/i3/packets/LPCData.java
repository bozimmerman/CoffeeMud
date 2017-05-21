package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class LPCData {
	static public Object getLPCData(String cmd) throws I3Exception {
		return getLPCData(cmd, false);
	}

	static public Object getLPCData(String cmd, boolean flag) throws I3Exception {
		final Vector data = new Vector(2);

		data.addElement(null);
		data.addElement("");
		if( cmd == null )
		{
			if( ! flag )
			{
				return "";
			}
			return data;
		}
		cmd = cmd.trim();
		if( cmd.length() < 1 )
		{
			if( !flag )
			{
				return "";
			}
			return data;
		}
		else if( cmd.length() == 1 )
		{
			try
			{
				final int x = Integer.parseInt(cmd);

				if( !flag )
				{
					return Integer.valueOf(x);
				}
				data.setElementAt(Integer.valueOf(x), 0);
				return data;
			}
			catch( final NumberFormatException e )
			{
				throw new I3Exception("Invalid LPC Data in string: " + cmd);
			}
		}
		if( cmd.charAt(0) == '(' )
		{
			switch(cmd.charAt(1))
			{
				case '{':
				{
					final Vector v = new Vector();

					cmd = cmd.substring(2, cmd.length());
					while( cmd.charAt(0) != '}' )
					{
						final Vector tmp = (Vector)getLPCData(cmd, true);

						v.addElement(tmp.elementAt(0));
						cmd = ((String)tmp.elementAt(1)).trim();
						if( cmd.length() < 1 || (cmd.charAt(0) != ',' && cmd.charAt(0) != '}') )
						{
							throw new I3Exception("Invalid LPC Data in string: " + cmd);
						}
						else if( cmd.charAt(0) == ',' )
						{
							cmd = cmd.substring(1, cmd.length());
							cmd = cmd.trim();
						}
					}
					if( cmd.charAt(1) != ')' )
					{
						cmd = cmd.substring(2, cmd.length());
						cmd = cmd.trim();
						if( cmd.charAt(0) != ')' )
						{
							throw new I3Exception("Illegal array terminator.");
						}
						data.setElementAt(cmd.substring(1, cmd.length()), 1);
					}
					else
					{
						data.setElementAt(cmd.substring(2,cmd.length()), 1);
					}
					if( !flag )
						return v;
					data.setElementAt(v, 0);
					return data;
				}

				case '[':
				{
					final Hashtable h = new Hashtable();

					cmd = cmd.substring(2, cmd.length());
					while( cmd.charAt(0) != ']' )
					{
						Vector tmp = (Vector)getLPCData(cmd, true);
						Object key, value;

						cmd = (String)tmp.elementAt(1);
						cmd = cmd.trim();
						if( cmd.charAt(0) != ':' )
						{
							throw new I3Exception("Invalid mapping format1: " + cmd);
						}
						cmd = cmd.substring(1, cmd.length());
						key = tmp.elementAt(0);
						tmp = (Vector)getLPCData(cmd, true);
						value = tmp.elementAt(0);
						cmd = (String)tmp.elementAt(1);
						h.put(key, value);
						cmd = cmd.trim();
						if( cmd.charAt(0) != ',' && cmd.charAt(0) != ']' )
						{
							throw new I3Exception("Invalid mapping format2: " + cmd);
						}
						else if( cmd.charAt(0) != ']' )
						{
							cmd = cmd.substring(1, cmd.length());
							cmd = cmd.trim();
						}
					}
					if( cmd.charAt(1) != ')' )
					{
						cmd = cmd.substring(2, cmd.length()).trim();
						if( cmd.charAt(0) != ')' )
						{
							throw new I3Exception("Invalid mapping format3: " + cmd);
						}
						data.setElementAt(cmd.substring(1, cmd.length()).trim(), 1);
					}
					else data.setElementAt(cmd.substring(2, cmd.length()).trim(), 1);
					if( !flag )
					{
						return h;
					}
					data.setElementAt(h, 0);
					return data;
				}

				default:
				throw new I3Exception("Invalid LPC Data in string: " + cmd);
			}
		}
		else if( cmd.charAt(0) == '"' )
		{
			int x=1;
			final StringBuffer in=new StringBuffer("");
			char c='\0';
			while(x<cmd.length())
			{
				c=cmd.charAt(x);
				switch(cmd.charAt(x))
				{
				case '\\':
					if((x+1)<cmd.length())
					{
						in.append(cmd.charAt(x+1));
						x++;
					}
					else
						in.append(c);
					x++;
					break;
				case '"':
					if( !flag )
						return in.toString();
					data.setElementAt(in.toString(),0);
					data.setElementAt(cmd.substring(x+1),1);
					return data;
				default:
					in.append(c);
					x++;
					break;
				}
			}
			if( !flag )
				return in.toString();
			data.setElementAt(in.toString(),0);
			data.setElementAt("",1);
			return data;
		}
		else if( Character.isDigit(cmd.charAt(0)) || cmd.charAt(0) == '-' )
		{
			String tmp;
			int x;
			if( cmd.length() > 1 && cmd.startsWith("0x" ) )
			{
				tmp = "0x";
				cmd = cmd.substring(2, cmd.length());
			}
			else if( cmd.length() > 1 && cmd.startsWith("-") )
			{
				tmp = "-";
				cmd = cmd.substring(1, cmd.length());
			}
			else
			{
				tmp = "";
			}
			while( !cmd.equals("") && (Character.isDigit(cmd.charAt(0))) )
			{
				tmp += cmd.charAt(0);
			  //  tmp += cmd.substring(0, 1);
				if( cmd.length() > 1 )
				{
					cmd = cmd.substring(1, cmd.length());
				}
				else
				{
					cmd = "";
				}
			}
			try
			{
				x = Integer.parseInt(tmp);
			}
			catch( final NumberFormatException e )
			{
				throw new I3Exception("Invalid number format: " + tmp);
			}
			if((cmd.length()>1)&&(cmd.charAt(0)=='.'))
			{
				cmd=cmd.substring(1);
				while((cmd.length()>0)&&(Character.isDigit(cmd.charAt(0))))
					cmd=cmd.substring(1);
			}
			if( !flag )
			{
				return Integer.valueOf(x);
			}

			data.setElementAt(Integer.valueOf(x), 0);
			data.setElementAt(cmd, 1);
			return data;
		}
		throw new I3Exception("Gobbledygook in string.");
	}
}
