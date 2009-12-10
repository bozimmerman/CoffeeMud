package com.planet_ink.coffee_mud.Libraries;
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

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;

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
public class XMLManager extends StdLibrary implements XMLLibrary
{
    public String ID(){return "XMLManager";}
    
	public String parseOutAngleBrackets(String s)
	{
		int x=s.indexOf("<");
		while(x>=0)
		{
			s=s.substring(0,x)+"&lt;"+s.substring(x+1);
			x=s.indexOf("<");
		}
		x=s.indexOf(">");
		while(x>=0)
		{
			s=s.substring(0,x)+"&gt;"+s.substring(x+1);
			x=s.indexOf(">");
		}
		return s;
	}

	public String restoreAngleBrackets(String s)
	{
		if(s==null) return null;
		StringBuffer buf=new StringBuffer(s);
		int loop=0;
		while(loop<buf.length())
		{
			switch(buf.charAt(loop))
			{
			case '&':
				if(loop<buf.length()-3)
				{
					if(buf.substring(loop+1,loop+4).equalsIgnoreCase("lt;"))
						buf.replace(loop,loop+4,"<");
					else
					if(buf.substring(loop+1,loop+4).equalsIgnoreCase("gt;"))
						buf.replace(loop,loop+4,">");
				}
				break;
			case '%':
				if(loop<buf.length()-2)
				{
					int dig1=HEX_DIGITS.indexOf(buf.charAt(loop+1));
					int dig2=HEX_DIGITS.indexOf(buf.charAt(loop+2));
					if((dig1>=0)&&(dig2>=0))
					{
						buf.setCharAt(loop,(char)((dig1*16)+dig2));
						buf.deleteCharAt(loop+1);
						buf.deleteCharAt(loop+1);
					}
				}
				break;
			}
			++loop;
		}
		return buf.toString();
	}

	/**
	 * Returns the double value of a string without crashing
 	 *
	 * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param DOUBLE String to convert
	 * @return double Double value of the string
	 */
	public double s_double(String DOUBLE)
	{
		double sdouble=0;
		try{ sdouble=Double.parseDouble(DOUBLE); }
		catch(Exception e){ return 0;}
		return sdouble;
	}

	/**
	 * Returns the integer value of a string without crashing
 	 *
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	public int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
		catch(java.lang.NumberFormatException e){ return 0;}
		return sint;
	}

	/**
	 * Returns the short value of a string without crashing
 	 *
	 * <br><br><b>Usage:</b> int num=s_short(CMD.substring(14));
	 * @param SHORT Short value of string
	 * @return short Short value of the string
	 */
	public short s_short(String SHORT)
	{
		short sint=0;
		try{ sint=Short.parseShort(SHORT); }
		catch(java.lang.NumberFormatException e){ return 0;}
		return sint;
	}

	/**
	 * Returns the long value of a string without crashing
 	 *
	 * <br><br><b>Usage:</b> int num=s_long(CMD.substring(14));
	 * @param LONG Long value of string
	 * @return long Long value of the string
	 */
	public  long s_long(String LONG)
	{
		long slong=0;
		try{ slong=Long.parseLong(LONG); }
		catch(java.lang.NumberFormatException e){ return 0;}
		return slong;
	}

	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, String Data)
	{
	    if(Data.length()==0)
			return "<"+TName+" />";
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, int Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, short Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, boolean Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, long Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the contents of an XML tag, given the tag to search for
	 *
  	 * <br><br><b>Usage:</b> String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	public String returnXMLBlock(String Blob, String Tag)
	{
		int foundb=Blob.indexOf("<"+Tag+">");
		if(foundb<0) foundb=Blob.indexOf("<"+Tag+" ");
		if(foundb<0) foundb=Blob.indexOf("<"+Tag+"/");
		if(foundb<0) return "";

		int founde=Blob.indexOf("/"+Tag+">",foundb)-1;
		if(founde<0) founde=Blob.indexOf("/"+Tag+" ",foundb)-1;
		if(founde<0)
		{
			founde=Blob.indexOf(">",foundb);
			if((founde>0)&&(Blob.charAt(founde-1)!='/')) founde=-1;
		}
		if(founde<0) return "";

		Blob=Blob.substring(foundb,founde).trim();
		return Blob;
	}

	protected String parseOutParms(String blk, Hashtable parmList)
	{
		blk=blk.trim();
		for(int x=0;x<blk.length();x++)
			if(Character.isWhitespace(blk.charAt(x)))
			{
			    if(!blk.substring(x).trim().startsWith("/"))
			    {
                    parmList.putAll(parseParms(blk.substring(x).trim()));
			    	if(blk.endsWith("/"))
				    	return blk.substring(0,x).trim()+" /";
			    	return blk.substring(0,x).trim();
			    }
		        break;
			}
		return blk;
	}


	protected Hashtable parseParms(String Blob)
	{
		Hashtable H=new Hashtable();
		StringBuffer curVal=null;
		StringBuffer key=new StringBuffer("");
		boolean quoteMode=false;
		char c=' ';
		char[] cs=Blob.toCharArray();
		for(int i=0;i<cs.length;i++)
		{
			c=cs[i];
			switch(c)
			{
			case '\"':
				if((curVal!=null)&&(!quoteMode)&&(curVal.length()==0))
					quoteMode=true;
				else
				if((curVal!=null)&&(quoteMode))
				{
					if((curVal.length()==0)||(curVal.charAt(curVal.length()-1)!='\\'))
					{
						quoteMode=false;
						H.put(key.toString().toUpperCase().trim(),curVal.toString());
						key=new StringBuffer("");
						curVal=null;
					}
					else
						curVal.setCharAt(curVal.length()-1,c);
				}
				else
				if(curVal!=null)
					curVal.append(c);
				else
					key.append(c);
				break;
			case ' ':
			case '\t':
				if((curVal!=null)&&(curVal.length()>0)&&(!quoteMode))
				{
					quoteMode=false;
					H.put(key.toString().toUpperCase().trim(),curVal.toString());
					key=new StringBuffer("");
					curVal=null;
				}
				else
				if((curVal!=null)&&((curVal.length()>0)||(quoteMode)))
					curVal.append(c);
				break;
			case '=':
				if(curVal==null)
					curVal=new StringBuffer("");
				else
					curVal.append(c);
				break;
			default:
				if(curVal!=null)
					curVal.append(c);
				else
					key.append(c);
				break;
			}
		}
		if((curVal!=null)
		&&(curVal.length()>0)
		&&(key.length()>0)
		&&(!H.containsKey(key.toString().toUpperCase().trim())))
			H.put(key.toString().toUpperCase().trim(),curVal.toString());
		return H;
	}

	public String getValFromPieces(Vector<XMLpiece> V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return x.value;
		return "";
	}

	public Vector getContentsFromPieces(Vector<XMLpiece> V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.contents!=null))
			return x.contents;
		return new Vector();
	}

	public Vector<XMLpiece> getRealContentsFromPieces(Vector<XMLpiece> V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if(x!=null)	return x.contents;
		return null;
	}

	public XMLpiece getPieceFromPieces(Vector<XMLpiece> V, String tag)
	{
		if(V==null) return null;
		for(int v=0;v<V.size();v++)
			if(((XMLpiece)V.elementAt(v)).tag.equalsIgnoreCase(tag))
				return (XMLpiece)V.elementAt(v);
		return null;
	}

	public boolean getBoolFromPieces(Vector<XMLpiece> V, String tag)
	{
		String val=getValFromPieces(V,tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}

	public int getIntFromPieces(Vector<XMLpiece> V, String tag)
	{
		return s_int(getValFromPieces(V,tag));
	}

	public short getShortFromPieces(Vector<XMLpiece> V, String tag)
	{
		return s_short(getValFromPieces(V,tag));
	}

	public long getLongFromPieces(Vector<XMLpiece> V, String tag)
	{
		return s_long(getValFromPieces(V,tag));
	}

	public double getDoubleFromPieces(Vector<XMLpiece> V, String tag)
	{
		return s_double(getValFromPieces(V,tag));
	}

    protected boolean acceptableTag(StringBuffer str, int start, int end)
    {
        while(Character.isWhitespace(str.charAt(start)))
            start++;
        while(Character.isWhitespace(str.charAt(end)))
            end--;
        if((start>=end)
        ||(end>(start+250))
        ||((str.charAt(start)!='/')&&(!Character.isLetter(str.charAt(start)))))
            return false;
        if(start+1==end) return true;
        if(CMLib.coffeeFilter().getTagTable().containsKey(str.substring(start,end).toUpperCase()))
            return false;
        return true;
    }

    protected XMLpiece nextXML(StringBuffer buf, XMLpiece parent, int start)
    {
        int end=-1;
        start--;
        while((end<(start+1))||(!acceptableTag(buf,start+1,end)))
        {
            start=buf.indexOf("<",start+1);
            if(start<0) return null;
            end=buf.indexOf(">",start);
            if(end<=start) return null;
			if((buf.charAt(start+1)=='!')&&(buf.substring(start,start+4).equals("<!--")))
			{
				int commentEnd=buf.indexOf("-->",start+1);
				if(commentEnd<0) return null;
				end=-1;
				start=commentEnd;
				continue;
			}
            int nextStart=buf.indexOf("<",start+1);
            while((nextStart>=0)&&(nextStart<end))
            {
                start=nextStart;
                nextStart=buf.indexOf("<",start+1);
            }
        }
        Hashtable parmList = new Hashtable();
		String tag=parseOutParms(buf.substring(start+1,end).trim(),parmList).toUpperCase().trim();

		if(!tag.startsWith("/"))
		{
			XMLpiece piece=new XMLpiece();
			piece.parms=parmList;
			if(tag.endsWith("/"))
			{
				piece.tag=tag.substring(0,tag.length()-1).trim();
				piece.value="";
				piece.contents=new Vector();
				piece.outerStart=start;
				piece.outerEnd=end;
			}
			else
			{
				piece.tag=tag.trim();
				piece.outerStart=start;
				piece.innerStart=end+1;
				piece.contents=new Vector();
				XMLpiece next=null;
				while(next!=piece)
				{
					next=nextXML(buf,piece,end+1);
					if(next==null) // this was probably a faulty start tag
						return nextXML(buf,parent,end+1);
					else
					if(next!=piece)
					{
						end=next.outerEnd;
						piece.addContent(next);
					}
				}
			}
			return piece;
		}
		tag=tag.substring(1);
		if((parent!=null)&&(tag.equals(parent.tag)))
		{
			parent.value=buf.substring(parent.innerStart,start);
			parent.innerEnd=start;
			parent.outerEnd=end;
			return parent;
		}
		return null;
	}


	public Vector<XMLpiece> parseAllXML(String buf)
	{  
        return parseAllXML(new StringBuffer(buf));
    }

	public Vector<XMLpiece> parseAllXML(StringBuffer buf)
	{
		Vector<XMLpiece> V=new Vector();
		int end=-1;
		XMLpiece next=nextXML(buf,null,end+1);
		while(next!=null)
		{
			end=next.outerEnd;
			V.addElement(next);
			next=nextXML(buf,null,end+1);
		}
		return V;
	}

	public String returnXMLValue(String Blob)
	{
		int start=0;

		try{
			while((start<Blob.length())&&(Blob.charAt(start)!='>')) start++;
			if((start>=Blob.length())||(Blob.charAt(start-1)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		} catch (Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}


	public String returnXMLValue(String Blob, String Tag)
	{
		int start=0;
		Blob=returnXMLBlock(Blob,Tag);
		try{
			while((start<Blob.length())&&(Blob.charAt(start)!='>')) start++;
			if((start>=Blob.length())||(Blob.charAt(start)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		} catch (Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}

	public boolean returnXMLBoolean(String Blob, String Tag)
	{
		String val=returnXMLValue(Blob,Tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}

	public String getParmValue(Hashtable<String,String> parmSet, String Tag)
	{
        if((parmSet != null)&&(Tag != null))
            return (String)parmSet.get(Tag.toUpperCase().trim());
        return null;
	}

	public String getXMLList(Vector<?> V)
    {
        StringBuffer str=new StringBuffer("");
        String s=null;
        for(int v=0;v<V.size();v++)
        {
            s=V.elementAt(v).toString();
            if(s.trim().length()==0)
                str.append("<X />");
            else
                str.append("<X>"+parseOutAngleBrackets(s)+"</X>");
        }
        return str.toString();
    }
    
    public Vector<String> parseXMLList(String numberedList)
    {
        Vector<XMLpiece> xml=parseAllXML(numberedList);
        Vector<String> V=new Vector<String>();
        for(int v=0;v<xml.size();v++)
            V.addElement(this.restoreAngleBrackets(((XMLLibrary.XMLpiece)xml.elementAt(v)).value));
        return V;
    }
    

}
