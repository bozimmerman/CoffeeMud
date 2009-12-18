package com.planet_ink.coffee_mud.core.intermud.packets;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
@SuppressWarnings("unchecked")
public class LPCData {
    static public Object getLPCData(String str) throws I3Exception {
        return getLPCData(str, false);
    }

    static public Object getLPCData(String str, boolean flag) throws I3Exception {
        Vector data = new Vector(2);

        data.addElement(null);
        data.addElement("");
        if( str == null ) {
            if( ! flag ) {
                return "";
            }
            return data;
        }
        str = str.trim();
        if( str.length() < 1 ) {
            if( !flag ) {
                return "";
            }
            return data;
        }
        else if( str.length() == 1 ) {
            try {
                int x = Integer.parseInt(str);

                if( !flag ) {
                    return Integer.valueOf(x);
                }
                data.setElementAt(Integer.valueOf(x), 0);
                return data;
            }
            catch( NumberFormatException e ) {
                throw new I3Exception("Invalid LPC Data in string: " + str);
            }
        }
        if( str.charAt(0) == '(' ) {
            switch(str.charAt(1)) {
                case '{':
                {
                    Vector v = new Vector();

                    str = str.substring(2, str.length());
                    while( str.charAt(0) != '}' ) {
                        Vector tmp = (Vector)getLPCData(str, true);

                        v.addElement(tmp.elementAt(0));
                        str = ((String)tmp.elementAt(1)).trim();
                        if( str.length() < 1 || (str.charAt(0) != ',' && str.charAt(0) != '}') ) {
                            throw new I3Exception("Invalid LPC Data in string: " + str);
                        }
                        else if( str.charAt(0) == ',' ) {
                            str = str.substring(1, str.length());
                            str = str.trim();
                        }
                    }
                    if( str.charAt(1) != ')' ) {
                        str = str.substring(2, str.length());
                        str = str.trim();
                        if( str.charAt(0) != ')' ) {
                            throw new I3Exception("Illegal array terminator.");
                        }
                        data.setElementAt(str.substring(1, str.length()), 1);
                    }
                    else {
                        data.setElementAt(str.substring(2,str.length()), 1);
                    }
                    if( !flag ) return v;
                    data.setElementAt(v, 0);
                    return data;
                }

                case '[':
                {
                    Hashtable h = new Hashtable();

                    str = str.substring(2, str.length());
                    while( str.charAt(0) != ']' ) {
                        Vector tmp = (Vector)getLPCData(str, true);
                        Object key, value;

                        str = (String)tmp.elementAt(1);
                        str = str.trim();
                        if( str.charAt(0) != ':' ) {
                            throw new I3Exception("Invalid mapping format1: " + str);
                        }
                        str = str.substring(1, str.length());
                        key = tmp.elementAt(0);
                        tmp = (Vector)getLPCData(str, true);
                        value = tmp.elementAt(0);
                        str = (String)tmp.elementAt(1);
                        h.put(key, value);
                        str = str.trim();
                        if( str.charAt(0) != ',' && str.charAt(0) != ']' ) {
                            throw new I3Exception("Invalid mapping format2: " + str);
                        }
                        else if( str.charAt(0) != ']' ) {
                            str = str.substring(1, str.length());
                            str = str.trim();
                        }
                    }
                    if( str.charAt(1) != ')' ) {
                        str = str.substring(2, str.length()).trim();
                        if( str.charAt(0) != ')' ) {
                            throw new I3Exception("Invalid mapping format3: " + str);
                        }
                        data.setElementAt(str.substring(1, str.length()).trim(), 1);
                    }
                    else data.setElementAt(str.substring(2, str.length()).trim(), 1);
                    if( !flag ) {
                        return h;
                    }
                    data.setElementAt(h, 0);
                    return data;
                }

                default:
                throw new I3Exception("Invalid LPC Data in string: " + str);
            }
        }
        else if( str.charAt(0) == '"' ) {
        	int x=1;
        	StringBuffer in=new StringBuffer("");
        	char c='\0';
        	while(x<str.length())
        	{
        		c=str.charAt(x);
	        	switch(str.charAt(x))
	        	{
	        	case '\\':
	        		if((x+1)<str.length())
	        		{
	        			in.append(str.charAt(x+1));
	        			x++;
	        		}
	        		else
	        			in.append(c);
	        		x++;
	        		break;
	        	case '"':
	                if( !flag ) return in.toString();
	        		data.setElementAt(in.toString(),0);
	        		data.setElementAt(str.substring(x+1),1);
	        		return data;
	        	default:
	        		in.append(c);
	        		x++;
	        		break;
	        	}
        	}
            if( !flag ) return in.toString();
    		data.setElementAt(in.toString(),0);
    		data.setElementAt("",1);
            return data;
        }
        else if( Character.isDigit(str.charAt(0)) || str.charAt(0) == '-' ) {
            String tmp;
            int x;

            if( str.length() > 1 && str.startsWith("0x" ) ) {
                tmp = "0x";
                str = str.substring(2, str.length());
            }
            else if( str.length() > 1 && str.startsWith("-") ) {
                tmp = "-";
                str = str.substring(1, str.length());
            }
            else {
                tmp = "";
            }
            while( !str.equals("") && (Character.isDigit(str.charAt(0))) ) {
                tmp += str.charAt(0);
              //  tmp += str.substring(0, 1);
                if( str.length() > 1 ) {
                    str = str.substring(1, str.length());
                }
                else {
                    str = "";
                }
            }
            try {
                x = Integer.parseInt(tmp);
            }
            catch( NumberFormatException e ) {
                throw new I3Exception("Invalid number format: " + tmp);
            }
            if( !flag ) {
                return Integer.valueOf(x);
            }
            data.setElementAt(Integer.valueOf(x), 0);
            data.setElementAt(str, 1);
            return data;
        }
        throw new I3Exception("Gobbledygook in string.");
    }
}
