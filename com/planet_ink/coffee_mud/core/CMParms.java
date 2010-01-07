package com.planet_ink.coffee_mud.core;
import java.util.*;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class CMParms
{
    private CMParms(){super();}
    private static CMParms inst=new CMParms();
    public static CMParms instance(){return inst;}

    public static String combine(Vector commands, int startAt, int endAt)
    {
        StringBuffer Combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
            Combined.append(commands.elementAt(commandIndex).toString()+" ");
        return Combined.toString().trim();
    }

    public static String combineWithQuotes(Vector commands, int startAt, int endAt)
    {
        StringBuffer Combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
        {
            String s=commands.elementAt(commandIndex).toString();
            if(s.indexOf(" ")>=0) s="\""+s+"\"";
            Combined.append(s+" ");
        }
        return Combined.toString().trim();
    }
    
    
	public static void sortVector(Vector V) {
        Vector V2=new Vector(new TreeSet(V));
        V.clear();
        V.addAll(V2);
        V.trimToSize();
    }

    public static String combineAfterIndexWithQuotes(Vector commands, String match)
    {
        StringBuffer Combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=0;commandIndex<0;commandIndex++)
        {
            String s=(String)commands.elementAt(commandIndex);
            if(s.indexOf(" ")>=0) s="\""+s+"\"";
            Combined.append(s+" ");
        }
        return Combined.toString().trim();
    }

    public static String combineWithQuotes(Vector commands, int startAt)
    {
        StringBuffer Combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
        {
            String s=commands.elementAt(commandIndex).toString();
            if(s.indexOf(" ")>=0) s="\""+s+"\"";
            Combined.append(s+" ");
        }
        return Combined.toString().trim();
    }

    public static String combineWithTabs(Vector commands, int startAt)
    {
        return combineWithX(commands,"\t",startAt);
    }

    public static String combineWithX(Vector commands, String X, int startAt)
    {
        StringBuffer Combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
        {
            String s=commands.elementAt(commandIndex).toString();
            Combined.append(s+X);
        }
        return Combined.toString().trim();
    }

    public static String combine(Vector<?> commands, int startAt)
    {
        StringBuffer combined=new StringBuffer("");
        if(commands!=null)
        for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
            combined.append(commands.elementAt(commandIndex).toString()+" ");
        return combined.toString().trim();
    }

    public static String combine(HashSet<?> flags, int startAt)
    {
        StringBuffer combined=new StringBuffer("");
        if(flags!=null)
        for(Iterator<?> i=flags.iterator();i.hasNext();)
            combined.append(i.next().toString()+" ");
        return combined.toString().trim();
    }

    public static Vector parse(String str)
    {   return parse(str,-1);   }

    public static Vector paramParse(String str)
    {
        Vector commands=parse(str);
        for(int i=0;i<commands.size();i++)
        {
            String s=(String)commands.elementAt(i);
            if(s.startsWith("=")&&(s.length()>1)&&(i>0))
            {
                String prev=(String)commands.elementAt(i-1);
                commands.setElementAt(prev+s,i-1);
                commands.removeElementAt(i);
                i--;
            }
            else
            if(s.endsWith("=")&&(s.length()>1)&&(i<(commands.size()-1)))
            {
                String next=(String)commands.elementAt(i+1);
                commands.setElementAt(s+next,i);
                commands.removeElementAt(i+1);
            }
            else
            if(s.equals("=")&&((i>0)&&(i<(commands.size()-1))))
            {
                String prev=(String)commands.elementAt(i-1);
                String next=(String)commands.elementAt(i+1);
                commands.setElementAt(prev+"="+next,i-1);
                commands.removeElementAt(i);
                commands.removeElementAt(i+1);
                i--;
            }
        }
        return commands;
    }

    public static Vector parse(String str, int upTo)
    {
        Vector commands=new Vector();
        if(str==null) return commands;
        str=str.trim();
        while(!str.equals(""))
        {
            int spaceIndex=str.indexOf(" ");
            int strIndex=str.indexOf("\"");
            String CMD="";
            if((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
            {
                int endStrIndex=str.indexOf("\"",strIndex+1);
                if(endStrIndex>strIndex)
                {
                    CMD=str.substring(strIndex+1,endStrIndex).trim();
                    str=str.substring(endStrIndex+1).trim();
                }
                else
                {
                    CMD=str.substring(strIndex+1).trim();
                    str="";
                }
            }
            else
            if(spaceIndex>=0)
            {
                CMD=str.substring(0,spaceIndex).trim();
                str=str.substring(spaceIndex+1).trim();
            }
            else
            {
                CMD=str.trim();
                str="";
            }
            if(!CMD.equals(""))
            {
                commands.addElement(CMD);
                if((upTo>=0)&&(commands.size()>=upTo))
                {
                    if(str.length()>0)
                        commands.addElement(str);
                    break;
                }

            }
        }
        return commands;
    }

    public static Vector parseCommas(String s, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf(",");
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+1).trim();
            if((!ignoreNulls)||(s2.length()>0))
                V.addElement(s2);
            x=s.indexOf(",");
        }
        if((!ignoreNulls)||(s.trim().length()>0))
            V.addElement(s.trim());
        return V;
    }

    public static Vector parseTabs(String s, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf("\t");
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+1).trim();
            if((!ignoreNulls)||(s2.length()>0))
                V.addElement(s2);
            x=s.indexOf("\t");
        }
        if((!ignoreNulls)||(s.trim().length()>0))
            V.addElement(s.trim());
        return V;
    }

    public static Vector parseAny(String s, String delimeter, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf(delimeter);
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+delimeter.length()).trim();
            if((!ignoreNulls)||(s2.length()>0))
                V.addElement(s2);
            x=s.indexOf(delimeter);
        }
        if((!ignoreNulls)||(s.trim().length()>0))
            V.addElement(s.trim());
        return V;
    }
    public static Vector parseAnyWords(String s, String delimeter, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        delimeter=delimeter.toUpperCase();
        int x=s.toUpperCase().indexOf(delimeter);
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+delimeter.length()).trim();
            if((!ignoreNulls)||(s2.length()>0))
                V.addElement(s2);
            x=s.indexOf(delimeter);
        }
        if((!ignoreNulls)||(s.trim().length()>0))
            V.addElement(s.trim());
        return V;
    }

    public static Vector parseSquiggles(String s)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf("~");
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+1).trim();
            V.addElement(s2);
            x=s.indexOf("~");
        }
        return V;
    }

    public static Vector parseSentences(String s)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf(".");
        while(x>=0)
        {
            String s2=s.substring(0,x+1);
            s=s.substring(x+1);
            V.addElement(s2);
            x=s.indexOf(".");
        }
        return V;
    }

    public static Vector parseSquiggleDelimited(String s, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf("~");
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+1).trim();
            if((s2.length()>0)||(!ignoreNulls))
                V.addElement(s2);
            x=s.indexOf("~");
        }
        if((s.length()>0)||(!ignoreNulls))
            V.addElement(s);
        return V;
    }

    public static Vector parseSemicolons(String s, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf(";");
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+1).trim();
            if((!ignoreNulls)||(s2.length()>0))
                V.addElement(s2);
            x=s.indexOf(";");
        }
        if((!ignoreNulls)||(s.trim().length()>0))
            V.addElement(s.trim());
        return V;
    }

    public static Vector parseSpaces(String s, boolean ignoreNulls)
    {
        Vector V=new Vector();
        if((s==null)||(s.length()==0)) return V;
        int x=s.indexOf(" ");
        while(x>=0)
        {
            String s2=s.substring(0,x).trim();
            s=s.substring(x+1).trim();
            if((!ignoreNulls)||(s2.length()>0))
                V.addElement(s2);
            x=s.indexOf(" ");
        }
        if((!ignoreNulls)||(s.trim().length()>0))
            V.addElement(s.trim());
        return V;
    }

    public static int numBits(String s)
    { return ((Integer)getBitWork(s,Integer.MAX_VALUE,2)).intValue(); }

    public static String cleanBit(String s)
    {
        if(s.length()==0) 
            return s;
        if((s.charAt(0)==' ')||(s.charAt(s.length()-1)==' '))
            s=s.trim();
        if(s.length()<2) 
            return s.replace('\'','`');
        if(s.charAt(0)=='\'')
        {
            if(s.charAt(s.length()-1)=='\'')
                return s.substring(1,s.length()-1).replace('\'','`');
            return s.substring(1).replace('\'','`');
        }
        if(s.charAt(0)=='`') {
            if(s.charAt(s.length()-1)=='`')
                return s.substring(1,s.length()-1).replace('\'','`');
            return s.substring(1).replace('\'','`');
        }
        return s.replace('\'','`');
    }
    public static String getCleanBit(String s, int which)
    { return cleanBit(getBit(s,which));}

    public static String getPastBitClean(String s, int which)
    { return cleanBit(getPastBit(s,which));}

    public static String getPastBit(String s, int which)
    { return (String)getBitWork(s,which,1); }

    public static String getBit(String s, int which)
    { return (String)getBitWork(s,which,0); }

    public static Object getBitWork(String s, int which, int op)
    {
        int currOne=0;
        int start=-1;
        char q=' ';
        char[] cs=s.toCharArray();
        for(int c=0;c<cs.length;c++)
            switch(start)
            {
            case -1:
                switch(cs[c])
                {
                case ' ': case '\t': case '\n': case '\r':
                    break;
                case '\'': case '`':
                    q=cs[c];
                    start=c;
                    break;
                default:
                    q=' ';
                    start=c;
                    break;
                }
                break;
            default:
                if(cs[c]==q)
                {
                    if((q!=' ')
                    &&(c<cs.length-1)
                    &&(!Character.isWhitespace(cs[c+1])))
                        break;
                    if(which==currOne)
                    {
                        switch(op)
                        {
                        case 0:
                            if(q==' ')
                                return new String(cs,start,c-start);
                            return new String(cs,start,c-start+1);
                        case 1:
                            return new String(cs,c+1,cs.length-c-1).trim();
                        }
                    }
                    currOne++;
                    start=-1;
                }
                break;
            }
        switch(op)
        {
        case 0:
            if(start<0)
                return "";
            return new String(cs,start,cs.length-start);
        case 1:
            return "";
        default:
            if(start<0)
                return Integer.valueOf(currOne);
            return Integer.valueOf(currOne+1);
        }
    }

    public static String getParmStr(String text, String key, String defaultVal)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())&&(text.charAt(x)!='='))
                {
                    if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
                        return defaultVal;
                    x++;
                }
                if(x<text.length())
                {
                    boolean endWithQuote=false;
                    while((x<text.length())&&(!Character.isLetterOrDigit(text.charAt(x))))
                    {
                        if(text.charAt(x)=='\"')
                        {
                            endWithQuote=true;
                            x++;
                            break;
                        }
                        x++;
                    }
                    if(x<text.length())
                    {
                        text=text.substring(x);
                        x=0;
                        while((x<text.length())
                            &&((Character.isLetterOrDigit(text.charAt(x)))
                            ||((endWithQuote)&&(text.charAt(x)!='\"'))))
                            x++;
                        return text.substring(0,x).trim();
                    }

                }
                x=-1;
            }
            else
                x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return defaultVal;
    }

    private static int[] makeIntArray(int x, int y){ int[] xy=new int[2]; xy[0]=x;xy[1]=y;return xy;}

    public static int[] getParmCompare(String text, String key, int value)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())
					&&(text.charAt(x)!='>')
					&&(text.charAt(x)!='<')
					&&(text.charAt(x)!='!')
					&&(text.charAt(x)!='='))
                    x++;
                if(x<text.length()-1)
                {
					char comp=text.charAt(x);
					boolean andEqual=(text.charAt(x)=='=');
					if(text.charAt(x+1)=='='){ x++; andEqual=true;}
					if(x<text.length()-1)
					{
						while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
							x++;
						if(x<text.length())
						{
							text=text.substring(x);
							x=0;
							while((x<text.length())&&(Character.isDigit(text.charAt(x))))
								x++;
							int found=CMath.s_int(text.substring(0,x));
							if(andEqual&&(found==value))
								return makeIntArray(comp,(comp=='!')?-1:1);
							switch(comp)
							{
								case '>': return makeIntArray(comp,(value>found)?1:-1);
								case '<': return makeIntArray(comp,(value<found)?1:-1);
								case '!': makeIntArray(comp,1);
							}
							return makeIntArray(comp,0);
						}
					}
                }
                x=-1;
            }
            else
                x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return makeIntArray('\0',0);
    }

    private static int strIndex(Vector V, String str, int start)
    {
        if(str.indexOf(' ')<0) return V.indexOf(str,start);
        Vector V2=CMParms.parse(str);
        if(V2.size()==0) return -1;
        int x=V.indexOf(V2.firstElement(),start);
        boolean found=false;
        while((x>=0)&&((x+V2.size())<=V.size())&&(!found))
        {
            found=true;
            for(int v2=1;v2<V2.size();v2++)
                if(!V.elementAt(x+v2).equals(V2.elementAt(v2)))
                { found=false; break;}
            if(!found) x=V.indexOf(V2.firstElement(),x+1);
        }
        if(found) return x;
        return -1;
    }
    private static int stringContains(Vector V, char combiner, StringBuffer buf, int lastIndex)
    {
        String str=buf.toString().trim();
        if(str.length()==0) return lastIndex;
        buf.setLength(0);
        switch(combiner)
        {
        case '&':
            lastIndex=strIndex(V,str,0);
            return lastIndex;
        case '|':
            if(lastIndex>=0) return lastIndex;
            return strIndex(V,str,0);
        case '>':
            if(lastIndex<0) return lastIndex;
            return strIndex(V,str,lastIndex<0?0:lastIndex+1);
        case '<':
        {
            if(lastIndex<0) return lastIndex;
            int newIndex=strIndex(V,str,0);
            if(newIndex<lastIndex) return newIndex;
            return -1;
        }
        }
        return -1;
    }
    private static int stringContains(Vector V, char[] str, int[] index, int depth)
    {
        StringBuffer buf=new StringBuffer("");
        int lastIndex=0;
        boolean quoteMode=false;
        char combiner='&';
        for(int i=index[0];i<str.length;i++)
        {
            switch(str[i])
            {
            case ')':
                if((depth>0)&&(!quoteMode))
                {
                    index[0]=i;
                    return stringContains(V,combiner,buf,lastIndex);
                }
                buf.append(str[i]);
                break;
            case ' ':
                buf.append(str[i]);
                break;
            case '&':
            case '|':
            case '>':
            case '<':
                if(quoteMode)
                    buf.append(str[i]);
                else
                {
                    lastIndex=stringContains(V,combiner,buf,lastIndex);
                    combiner=str[i];
                }
                break;
            case '(':
                if(!quoteMode)
                {
                    lastIndex=stringContains(V,combiner,buf,lastIndex);
                    index[0]=i+1;
                    int newIndex=stringContains(V,str,index,depth+1);
                    i=index[0];
                    switch(combiner)
                    {
                    case '&':
                        if((lastIndex<0)||(newIndex<0))
                            lastIndex=-1;
                        break;
                    case '|':
                        if(newIndex>=0)
                            lastIndex=newIndex;
                        break;
                    case '>':
                        if(newIndex<=lastIndex)
                            lastIndex=-1;
                        else
                            lastIndex=newIndex;
                        break;
                    case '<':
                        if((newIndex<0)||(newIndex>=lastIndex))
                            lastIndex=-1;
                        else
                            lastIndex=newIndex;
                        break;
                    }
                }
                else
                    buf.append(str[i]);
                break;
            case '\"':
                quoteMode=(!quoteMode);
                break;
            case '\\':
                if(i<str.length-1)
                {
                    buf.append(str[i+1]);
                    i++;
                }
                break;
            default:
                if(Character.isLetter(str[i]))
                    buf.append(Character.toLowerCase(str[i]));
                else
                    buf.append(str[i]);
                break;
            }
        }
        return stringContains(V,combiner,buf,lastIndex);
    }

    public static Hashtable parseEQParms(String str, String[] parmList)
    {
        Hashtable h=new Hashtable();
        int lastEQ=-1;
        String lastParm=null;
        for(int x=0;x<str.length();x++)
        {
            char c=Character.toUpperCase(str.charAt(x));
            if(Character.isLetter(c))
                for(int p=0;p<parmList.length;p++)
                    if((Character.toUpperCase(parmList[p].charAt(0)) == c)
                    &&((str.length()-x) >= parmList[p].length())
                    &&(str.substring(x,x+parmList[p].length()).equalsIgnoreCase(parmList[p])))
                    {
                        int chkX=x+parmList[p].length();
                        while((chkX<str.length())&&(Character.isWhitespace(str.charAt(chkX))))
                            chkX++;
                        if((chkX<str.length())&&(str.charAt(chkX)=='='))
                        {
                            chkX++;
                            if((lastParm!=null)&&(lastEQ>0))
                            {
                                String val=str.substring(lastEQ,x).trim();
                                if(val.startsWith("\"")&&(val.endsWith("\"")))
                                    val=val.substring(1,val.length()-1).trim();
                                h.put(lastParm,val);
                            }
                            lastParm=parmList[p];
                            x=chkX;
                            lastEQ=chkX;
                        }
                    }
        }
        if((lastParm!=null)&&(lastEQ>0))
        {
            String val=str.substring(lastEQ).trim();
            if(val.startsWith("\"")&&(val.endsWith("\"")))
                val=val.substring(1,val.length()-1).trim();
            h.put(lastParm,val);
        }
        return h;
    }

    public static Hashtable parseEQParms(String parms)
    {
        Hashtable h=new Hashtable();
        int state=0;
        int start=-1;
        String parmName=null;
        int lastPossibleStart=-1;
        boolean lastWasWhitespace=false;
        StringBuffer str=new StringBuffer(parms);
        for(int x=0;x<=str.length();x++)
        {
        	char c=(x==str.length())?'\n':str.charAt(x);
        	switch(state)
        	{
        	case 0:
	            if((c=='_')||(Character.isLetter(c)))
	            {
	            	start=x;
	            	state=1;
	            	parmName=null;
	            }
	            break;
        	case 1:
	            if(c=='=')
	            {
            		parmName=str.substring(start,x).toUpperCase().trim();
	            	state=2;
	            }
	            else
	            if(Character.isWhitespace(c))
	            {
            		parmName=str.substring(start,x).toUpperCase().trim();
            		start=x;
	            }
	            break;
        	case 2:
        		if((c=='\"')||(c=='\n'))
        		{
        			state=3;
        			start=x+1;
        			lastPossibleStart=start;
        		}
        		else
        		if(c=='=')
        		{ // do nothing, this is a do-over
        		}
        		else
        		if(!Character.isWhitespace(c))
        		{
        			lastWasWhitespace=false;
        			state=4;
        			start=x;
        			lastPossibleStart=start;
        		}
        		break;
        	case 3:
        		if(c=='\\')
        			str.deleteCharAt(x);
        		else
        		if(c=='\"')
        		{
        			state=0;
        			h.put(parmName,str.substring(start,x));
        			parmName=null;
        		}
        		break;
        	case 4:
        		if(c=='\\')
        			str.deleteCharAt(x);
        		else
        		if(c=='=')
        		{
        			String value=str.substring(start,x).trim();
        			if(value.length()==0)
        				state=2;
        			else
        			{
	        			h.put(parmName,str.substring(start,lastPossibleStart).trim());
	        			parmName=str.substring(lastPossibleStart,x).toUpperCase().trim();
    	            	state=2;
        			}
        		}
        		else
        		if(c=='\n')
        		{
        			state=0;
        			h.put(parmName,str.substring(start,x));
        			parmName=null;
        		}
        		else
        		if(Character.isWhitespace(c))
        			lastWasWhitespace=true;
        		else
        		if(lastWasWhitespace)
        		{
        			lastWasWhitespace=false;
        			lastPossibleStart=x;
        		}
        		break;
        	}
        }
        return h;
    }

    public static Hashtable parseEQParms(Vector parms, int start, int end)
    {
        Hashtable h=new Hashtable();
    	for(int x=0;x<parms.size();x++)
    		h.putAll(parseEQParms((String)parms.elementAt(x)));
        return h;
    }
    
    public static int stringContains(String str1, String str2)
    {
        StringBuffer buf1=new StringBuffer(str1.toLowerCase());
        for(int i=buf1.length()-1;i>=0;i--)
            if((buf1.charAt(i)!=' ')
            &&(buf1.charAt(i)!='\'')
            &&(buf1.charAt(i)!='\"')
            &&(buf1.charAt(i)!='`')
            &&(!Character.isLetterOrDigit(buf1.charAt(i))))
                buf1.setCharAt(i,' ');
        Vector V=CMParms.parse(buf1.toString());
        return stringContains(V,str2.toCharArray(),new int[]{0},0);
    }

    public static int getParmPlus(String text, String key)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
                {
                    if(text.charAt(x)=='=')
                        return 0;
                    x++;
                }
                if(x<text.length())
                {
                    char pm=text.charAt(x);
                    while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
                        x++;
                    if(x<text.length())
                    {
                        text=text.substring(x);
                        x=0;
                        while((x<text.length())&&(Character.isDigit(text.charAt(x))))
                            x++;
                        if(pm=='+')
                            return CMath.s_int(text.substring(0,x));
                        return -CMath.s_int(text.substring(0,x));
                    }
                }
                x=-1;
            }
            else
                x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return 0;
    }

    public static double getParmDoublePlus(String text, String key)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
                {
                    if(text.charAt(x)=='=')
                        return 0.0;
                    x++;
                }
                if(x<text.length())
                {
                    char pm=text.charAt(x);
                    while((x<text.length())
                    &&(!Character.isDigit(text.charAt(x)))
                    &&(text.charAt(x)!='.'))
                        x++;
                    if(x<text.length())
                    {
                        text=text.substring(x);
                        x=0;
                        while((x<text.length())
                        &&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
                            x++;
                        if(text.substring(0,x).indexOf(".")<0)
                        {
                            if(pm=='+')
                                return (double)CMath.s_int(text.substring(0,x));
                            return (double)(-CMath.s_int(text.substring(0,x)));
                        }
                        if(pm=='+')
                            return CMath.s_double(text.substring(0,x));
                        return -CMath.s_double(text.substring(0,x));
                    }
                }
                x=-1;
            }
            else
                x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return 0.0;
    }

    public static double getParmDouble(String text, String key, double defaultValue)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())&&(text.charAt(x)!='='))
                    x++;
                if(x<text.length())
                {
                    while((x<text.length())
                    &&(!Character.isDigit(text.charAt(x)))
                    &&(text.charAt(x)!='.'))
                        x++;
                    if(x<text.length())
                    {
                        text=text.substring(x);
                        x=0;
                        while((x<text.length())
                        &&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
                            x++;
                        if(text.substring(0,x).indexOf(".")<0)
                            return (double)CMath.s_long(text.substring(0,x));
                        return CMath.s_double(text.substring(0,x));
                    }
                }
                x=-1;
            }
            else
                x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return defaultValue;
    }


    public static int getParmInt(String text, String key, int defaultValue)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
                {
                    if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
                        return defaultValue;
                    x++;
                }
                if((x<text.length())&&(text.charAt(x)=='='))
                {
                    while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
                        x++;
                    if(x<text.length())
                    {
                        text=text.substring(x);
                        x=0;
                        while((x<text.length())&&(Character.isDigit(text.charAt(x))))
                            x++;
                        return CMath.s_int(text.substring(0,x));
                    }
                }
                x=-1;
            }
            else
                x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return defaultValue;
    }

    public static boolean getParmBool(String text, String key, boolean defaultValue)
    {
        int x=text.toUpperCase().indexOf(key.toUpperCase());
        while(x>=0)
        {
            if((x==0)||(!Character.isLetter(text.charAt(x-1))))
            {
                while((x<text.length())&&(text.charAt(x)!='='))
                    x++;
                if((x<text.length())&&(text.charAt(x)=='='))
                {
                	String s=text.substring(x+1).trim();
                	if(Character.toUpperCase(s.charAt(0))=='T') return true;
                	if(Character.toUpperCase(s.charAt(0))=='T') return false;
                }
            }
            x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
        }
        return defaultValue;
    }

    public static String[] toStringArray(Vector V)
    {
        if((V==null)||(V.size()==0)){
            String[] s=new String[0];
            return s;
        }
        String[] s=new String[V.size()];
        for(int v=0;v<V.size();v++)
            s[v]=V.elementAt(v).toString();
        return s;
    }

    public static String[] toStringArray(Object[] O)
    {
    	if(O==null) return new String[0];
        String[] s=new String[O.length];
        for(int o=0;o<O.length;o++)
        	s[o]=(O[o]!=null)?O[o].toString():"";
        return s;
    }

    public static long[] toLongArray(Vector V)
    {
        if((V==null)||(V.size()==0)){
            long[] s=new long[0];
            return s;
        }
        long[] s=new long[V.size()];
        for(int v=0;v<V.size();v++)
            s[v]=CMath.s_long(V.elementAt(v).toString());
        return s;
    }
    public static int[] toIntArray(Vector V)
    {
        if((V==null)||(V.size()==0)){
            int[] s=new int[0];
            return s;
        }
        int[] s=new int[V.size()];
        for(int v=0;v<V.size();v++)
            s[v]=CMath.s_int(V.elementAt(v).toString());
        return s;
    }

    public static String toSemicolonList(byte[] bytes)
    {
        StringBuffer str=new StringBuffer("");
        for(int b=0;b<bytes.length;b++)
            str.append(Byte.toString(bytes[b])+(b<(bytes.length-1)?";":""));
        return str.toString();
    }

    public static String toSemicolonList(String[] bytes)
    {
        StringBuffer str=new StringBuffer("");
        for(int b=0;b<bytes.length;b++)
            str.append(bytes[b]+(b<(bytes.length-1)?";":""));
        return str.toString();
    }

    public static String toSemicolonList(Object[] bytes)
    {
        StringBuffer str=new StringBuffer("");
        for(int b=0;b<bytes.length;b++)
            str.append(bytes[b]+(b<(bytes.length-1)?";":""));
        return str.toString();
    }

    public static String toSemicolonList(Enumeration bytes)
    {
        StringBuffer str=new StringBuffer("");
        Object o;
        for(;bytes.hasMoreElements();)
        {
            o=(Object)bytes.nextElement();
            str.append(o.toString()+(bytes.hasMoreElements()?";":""));
        }
        return str.toString();
    }
    
    public static String toSemicolonList(Vector bytes)
    {
        StringBuffer str=new StringBuffer("");
        for(int b=0;b<bytes.size();b++)
            str.append(bytes.elementAt(b)+(b<(bytes.size()-1)?";":""));
        return str.toString();
    }

    public static String toSafeSemicolonList(Vector list)
    {
        return toSafeSemicolonList(list.toArray());
    }
    
    public static String toSafeSemicolonList(Object[] list)
    {
        StringBuffer buf1=new StringBuffer("");
        StringBuffer s=null;
        for(int l=0;l<list.length;l++)
        {
            s=new StringBuffer(list[l].toString());
            for(int i=0;i<s.length();i++)
                switch(s.charAt(i))
                {
                case '\\':
                case ';':
                    s.insert(i,'\\');
                    i++;
                    break;
                }
            buf1.append(s.toString());
            if(l<list.length-1)
                buf1.append(';');
        }
        return buf1.toString();
    }

    public static Vector parseSafeSemicolonList(String list, boolean ignoreNulls)
    {
        StringBuffer buf1=new StringBuffer(list);
        int lastDex=0;
        Vector V=new Vector();
        for(int l=0;l<buf1.length();l++)
            switch(buf1.charAt(l))
            {
            case '\\':
                buf1.delete(l,l+1);
                break;
            case ';':
                if((!ignoreNulls)||(lastDex<l))
                    V.addElement(buf1.substring(lastDex,l));
                lastDex=l+1;
                break;
            }
        if((!ignoreNulls)||(lastDex<buf1.length()));
            V.addElement(buf1.substring(lastDex,buf1.length()));
        return V;
    }

    public static byte[] fromByteList(String str)
    {
        Vector V=CMParms.parseSemicolons(str,true);
        if(V.size()>0)
        {
            byte[] bytes=new byte[V.size()];
            for(int b=0;b<V.size();b++)
                bytes[b]=Byte.parseByte((String)V.elementAt(b));
            return bytes;
        }
        return new byte[0];
    }

    public static String[] toStringArray(HashSet V)
    {
        if((V==null)||(V.size()==0)){
            String[] s=new String[0];
            return s;
        }
        String[] s=new String[V.size()];
        int v=0;
        for(Iterator i=V.iterator();i.hasNext();)
            s[v++]=(i.next()).toString();
        return s;
    }

    public static String toStringList(String[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+V[v]);
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(Object[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+V[v]);
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(Enumeration e)
    {
        if(!e.hasMoreElements()) return "";
        StringBuffer s=new StringBuffer("");
        Object o=null;
        for(;e.hasMoreElements();)
        {
            o=e.nextElement();
            s.append(", "+o);
        }
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(long[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+V[v]);
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(boolean[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+V[v]);
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(byte[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+((int)V[v]));
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(char[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+((long)V[v]));
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(int[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+V[v]);
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(double[] V)
    {
        if((V==null)||(V.length==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.length;v++)
            s.append(", "+V[v]);
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }


    public static String toStringList(Vector V)
    {
        if((V==null)||(V.size()==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(int v=0;v<V.size();v++)
            s.append(", "+V.elementAt(v).toString());
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static String toStringList(HashSet V)
    {
        if((V==null)||(V.size()==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(Iterator i=V.iterator();i.hasNext();)
            s.append(", "+i.next().toString());
        if(s.length()==0) return "";
        return s.toString().substring(2);
    }

    public static boolean equalVectors(Vector V1, Vector V2)
    {
        if((V1==null)&&(V2==null)) return true;
        if((V1==null)||(V2==null)) return false;
        if(V1.size()!=V2.size()) return false;
        for(int v=0;v<V1.size();v++)
            if(!V1.elementAt(v).equals(V2.elementAt(v)))
                return false;
        return true;
    }

    public static Hashtable makeHashtable(String[][] O)
    {
    	Hashtable H =new Hashtable(O!=null?O.length:0);
    	if(O!=null)
    		for(int o=0;o<O.length;o++)
    			H.put(O[o][0].toUpperCase().trim(),O[o][1]);
    	return H;
    }
    public static Hashtable makeHashtable(Object[][] O)
    {
    	Hashtable H =new Hashtable(O!=null?O.length:0);
    	if(O!=null)
    		for(int o=0;o<O.length;o++)
    			H.put(O[o][0],O[o][1]);
    	return H;
    }
    public static Vector makeVector(Object[] O)
    {
        Vector V=new Vector(O!=null?O.length:0);
        if(O!=null)
        for(int s=0;s<O.length;s++)
            V.addElement(O[s]);
        return V;
    }
    public static Vector makeVector(Enumeration e)
    {
        Vector V=new Vector();
        if(e!=null)
        for(;e.hasMoreElements();)
            V.addElement(e.nextElement());
        return V;
    }
    public static Vector makeVector(String[] O)
    {
        Vector V=new Vector(O!=null?O.length:0);
        if(O!=null)
        for(int s=0;s<O.length;s++)
            V.addElement(O[s]);
        return V;
    }
    public static HashSet makeHashSet(Object[] O)
    {
        HashSet V=new HashSet();
        if(O!=null)
        for(int s=0;s<O.length;s++)
            V.add(O[s]);
        return V;
    }
    public static HashSet makeHashSet(String[] O)
    {
        HashSet V=new HashSet();
        if(O!=null)
        for(int s=0;s<O.length;s++)
            V.add(O[s]);
        return V;
    }
    public static HashSet makeHashSet(Vector O)
    {
        HashSet V=new HashSet();
        if(O!=null)
        for(int s=0;s<O.size();s++)
            V.add(O.elementAt(s));
        return V;
    }
    
    public static HashSet makeHashSet(Enumeration E)
    {
        HashSet V=new HashSet();
        if(E!=null)
        for(;E.hasMoreElements();)
            V.add(E.nextElement());
        return V;
    }
    
    public static Vector makeVector()
    { return new Vector();}
    public static Vector makeVector(Object O)
    { Vector V=new Vector(); V.addElement(O); return V;}
    public static Vector makeVector(Object O, Object O2)
    { Vector V=new Vector(); V.addElement(O); V.addElement(O2); return V;}
    public static Vector makeVector(Object O, Object O2, Object O3)
    { Vector V=new Vector(); V.addElement(O); V.addElement(O2); V.addElement(O3); return V;}
    public static Vector makeVector(Object O, Object O2, Object O3, Object O4)
    { Vector V=new Vector(); V.addElement(O); V.addElement(O2); V.addElement(O3); V.addElement(O4); return V;}

    public static HashSet makeHashSet(){return new HashSet();}
    public static HashSet makeHashSet(Object O)
    {HashSet H=new HashSet(); H.add(O); return H;}
    public static HashSet makeHashSet(Object O, Object O2)
    {HashSet H=new HashSet(); H.add(O); H.add(O2); return H;}
    public static HashSet makeHashSet(Object O, Object O2, Object O3)
    {HashSet H=new HashSet(); H.add(O); H.add(O2); H.add(O3); return H;}
    public static HashSet makeHashSet(Object O, Object O2, Object O3, Object O4)
    {HashSet H=new HashSet(); H.add(O); H.add(O2); H.add(O3); H.add(O4); return H;}

    public static String[] toStringArray(Hashtable V)
    {
        if((V==null)||(V.size()==0)){
            String[] s=new String[0];
            return s;
        }
        String[] s=new String[V.size()];
        int v=0;
        for(Enumeration e=V.keys();e.hasMoreElements();)
        {
            String KEY=(String)e.nextElement();
            s[v]=(String)V.get(KEY);
            v++;
        }
        return s;
    }

    public static String[] appendToArray(String[] front, String[] back)
    {
    	if(back==null) return front;
    	if(front==null) return back;
    	if(back.length==0) return front;
    	String[] newa = Arrays.copyOf(front, front.length + back.length);
    	for(int i=0;i<back.length;i++)
    		newa[newa.length-1-i]=back[back.length-1-i];
    	return newa;
    }
    
	public static void addToVector(Vector from, Vector to)
    {
        if(from!=null)
        for(int i=0;i<from.size();i++)
            to.addElement(from.elementAt(i));
    }
    
	public static void delFromVector(Vector del, Vector from)
    {
        if(del!=null)
        for(int i=0;i<del.size();i++)
            from.removeElement(del.elementAt(i));
    }

    public static boolean vectorOfStringContainsIgnoreCase(Vector V, String s)
    {
        for(int v=0;v<V.size();v++)
            if(s.equalsIgnoreCase((String)V.elementAt(v)))
                return true;
        return false;
    }

    public static String toStringList(Hashtable V)
    {
        if((V==null)||(V.size()==0)){
            return "";
        }
        StringBuffer s=new StringBuffer("");
        for(Enumeration e=V.keys();e.hasMoreElements();)
        {
            String KEY=(String)e.nextElement();
            s.append(KEY+"="+(V.get(KEY).toString())+"/");
        }
        return s.toString();
    }


    public static Vector copyVector(Vector V)
    {
        Vector V2=new Vector();
        for(int v=0;v<V.size();v++)
        {
            Object h=V.elementAt(v);
            if(h instanceof Vector)
                V2.addElement(copyVector((Vector)h));
            else
                V2.addElement(h);
        }
        return V2;
    }

    public static int indexOf(String[] supported, String expertise)
    {
        if(supported==null) return -1;
        if(expertise==null) return -1;
        for(int i=0;i<supported.length;i++)
            if(supported[i].equals(expertise))
                return i;
        return -1;
    }
    public static int indexOfIgnoreCase(Enumeration supported, String key)
    {
        if(supported==null) return -1;
        int index = -1;
        for(;supported.hasMoreElements();)
        {
            if(supported.nextElement().toString().equalsIgnoreCase(key))
                return index;
            index++;
        }
        return -1;
    }
    public static int indexOf(int[] supported, int x)
    {
        if(supported==null) return -1;
        for(int i=0;i<supported.length;i++)
            if(supported[i]==x)
                return i;
        return -1;
    }
    public static int indexOf(Enumeration supported, Object key)
    {
        if(supported==null) return -1;
        int index = -1;
        for(;supported.hasMoreElements();)
        {
            if(supported.nextElement().equals(key))
                return index;
            index++;
        }
        return -1;
    }
    public static int indexOfIgnoreCase(Iterator supported, String key)
    {
        if(supported==null) return -1;
        int index = -1;
        for(;supported.hasNext();)
        {
            if(supported.next().toString().equalsIgnoreCase(key))
                return index;
            index++;
        }
        return -1;
    }
    public static int indexOf(Iterator supported, Object key)
    {
        if(supported==null) return -1;
        int index = -1;
        for(;supported.hasNext();)
        {
            if(supported.next().equals(key))
                return index;
            index++;
        }
        return -1;
    }
    public static int indexOfIgnoreCase(String[] supported, String expertise)
    {
        if(supported==null) return -1;
        if(expertise==null) return -1;
        for(int i=0;i<supported.length;i++)
            if(supported[i].equalsIgnoreCase(expertise))
                return i;
        return -1;
    }
    
    public static boolean contains(String[] supported, String expertise)
    { return indexOf(supported,expertise)>=0;}
    public static boolean containsIgnoreCase(String[] supported, String expertise)
    { return indexOfIgnoreCase(supported,expertise)>=0;}

    public static int indexOf(Object[] supported, Object expertise)
    {
        if(supported==null) return -1;
        if(expertise==null) return -1;
        for(int i=0;i<supported.length;i++)
            if(supported[i].equals(expertise))
                return i;
        return -1;
    }
    public static boolean contains(Object[] supported, Object expertise)
    { return indexOf(supported,expertise)>=0;}
    public static boolean contains(int[] supported, int x)
    { return indexOf(supported,x)>=0;}

    public static int startsWith(String[] supported, String expertise)
    {
        if(supported==null) return 0;
        if(expertise==null) return -1;
        for(int i=0;i<supported.length;i++)
            if(supported[i].startsWith(expertise))
                return i;
        return -1;
    }

    public static int startsWithIgnoreCase(String[] supported, String expertise)
    {
        if(supported==null) return 0;
        if(expertise==null) return -1;
        for(int i=0;i<supported.length;i++)
            if(supported[i].toUpperCase().startsWith(expertise.toUpperCase()))
                return i;
        return -1;
    }

    public static boolean startsAnyWith(String[] supported, String expertise)
    {
    	return startsWith(supported,expertise)>=0;
    }

    public static boolean startsAnyWithIgnoreCase(String[] supported, String expertise)
    {
    	return startsWithIgnoreCase(supported,expertise)>=0;
    }

    public static Vector denumerate(Enumeration e)
    {
        Vector V=new Vector();
        for(;e.hasMoreElements();)
            V.addElement(e.nextElement());
        return V;
    }

	/** constant value representing an undefined/unimplemented miscText/parms format.*/
	public static final String FORMAT_UNDEFINED="{UNDEFINED}";
	/** constant value representing an always empty miscText/parms format.*/
	public static final String FORMAT_EMPTY="{EMPTY}";
}
