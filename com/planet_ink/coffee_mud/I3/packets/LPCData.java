package com.planet_ink.coffee_mud.system.I3.packets;


import java.util.Hashtable;
import java.util.Vector;

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
            else {
                return data;
            }
        }
        str = str.trim();
        if( str.length() < 1 ) {
            if( !flag ) {
                return "";
            }
            else {
                return data;
            }
        }
        else if( str.length() == 1 ) {
            try {
                int x = Integer.parseInt(str);

                if( !flag ) {
                    return new Integer(x);
                }
                else {
                    data.setElementAt(new Integer(x), 0);
                    return data;
                }
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
                        else {
                            data.setElementAt(str.substring(1, str.length()), 1);
                        }
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
                            throw new I3Exception("Invalid mapping format: " + str);
                        }
                        else {
                            str = str.substring(1, str.length());
                        }
                        key = tmp.elementAt(0);
                        tmp = (Vector)getLPCData(str, true);
                        value = tmp.elementAt(0);
                        str = (String)tmp.elementAt(1);
                        h.put(key, value);
                        str = str.trim();
                        if( str.charAt(0) != ',' && str.charAt(0) != ']' ) {
                            throw new I3Exception("Invalid mapping format: " + str);
                        }
                        else if( str.charAt(0) != ']' ) {
                            str = str.substring(1, str.length());
                            str = str.trim();
                        }
                    }
                    if( str.charAt(1) != ')' ) {
                        str = str.substring(2, str.length()).trim();
                        if( str.charAt(0) != ')' ) {
                            throw new I3Exception("Invalid mapping format: " + str);
                        }
                        else data.setElementAt(str.substring(1, str.length()).trim(), 1);
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
            String tmp = "";
            char prior = '\0';
            char next = str.charAt(1);

            while( next != '"' || (next == '"' && str.charAt(0) == '\\') ) {
                if( next == '"' && str.charAt(0) == '\\' && prior == '\\') {
                    break;
                }
                if( next != '\\' || str.charAt(0) == '\\') {
                    tmp += next;
                }
                prior = str.charAt(0);
                str = str.substring(1, str.length());
                next = str.charAt(1);
            }
            if( !flag ) {
                return tmp;
            }
            if( str.length() > 2 ) {
                str = str.substring(2, str.length()).trim();
            }
            data.setElementAt(tmp, 0);
            data.setElementAt(str, 1);
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
                return new Integer(x);
            }
            data.setElementAt(new Integer(x), 0);
            data.setElementAt(str, 1);
            return data;
        }
        throw new I3Exception("Gobbledygook in string.");
    }
}