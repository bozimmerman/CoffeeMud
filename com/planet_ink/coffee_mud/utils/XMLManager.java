package com.planet_ink.coffee_mud.utils;

import java.util.*;

public class XMLManager
{
	/**
	 * Returns the double value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param double String to convert
	 * @return double Double value of the string
	 */
	public static double s_double(String DOUBLE)
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
	private static int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
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
	private  static long s_long(String LONG)
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
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, String Data)
	{
		return "<"+TName+">"+Data+""+"</"+TName+">";
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, int Data)
	{
		return "<"+TName+">"+Data+""+"</"+TName+">";
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, boolean Data)
	{
		return "<"+TName+">"+Data+""+"</"+TName+">";
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, long Data)
	{
		return "<"+TName+">"+Data+""+"</"+TName+">";
	}
	
	/**
	 * Return the contents of an XML tag, given the tag to search for
	 * 
  	 * <br><br><b>Usage:</b> String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	public static String returnXMLBlock(String Blob, String Tag)
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
	
	public static class XMLpiece
	{
		public String tag="";
		public String value="";
		public Vector parms=null;
		public Vector contents=null;
		public int outerStart=-1;
		public int innerStart=-1;
		public int innerEnd=-1;
		public int outerEnd=-1;
		public void addContent(XMLpiece x)
		{
			if(x==null) return;
			if(contents==null) contents=new Vector();
			contents.addElement(x);
		}
	}
	
	public static String parseOutParms(String blk, Vector parmList)
	{
		blk=blk.trim();
		for(int x=0;x<blk.length();x++)
			if(Character.isWhitespace(blk.charAt(x)))
			{
				parmList.addElement(blk.substring(x).trim());
				return blk;
			}
		return blk;
	}
	
	
	public static String getValFromPieces(Vector V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return x.value;
		return "";
	}
	
	public static Vector getContentsFromPieces(Vector V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.contents!=null))
			return x.contents;
		return new Vector();
	}
	
	public static Vector getRealContentsFromPieces(Vector V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if(x!=null)	return x.contents;
		return null;
	}
	
	public static XMLpiece getPieceFromPieces(Vector V, String tag)
	{
		if(V==null) return null;
		for(int v=0;v<V.size();v++)
			if(((XMLpiece)V.elementAt(v)).tag.equalsIgnoreCase(tag))
				return (XMLpiece)V.elementAt(v);
		return null;
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public static boolean getBoolFromPieces(Vector V, String tag)
	{
		String val=getValFromPieces(V,tag);
		if((val==null)||((val!=null)&&(val.length()==0)))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return int Information from XML block
	 */
	public static int getIntFromPieces(Vector V, String tag)
	{
		return s_int(getValFromPieces(V,tag));
	}
	
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return long Information from XML block
	 */
	public static long getLongFromPieces(Vector V, String tag)
	{
		return s_long(getValFromPieces(V,tag));
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return double Information from XML block
	 */
	public static double getDoubleFromPieces(Vector V, String tag)
	{
		return s_double(getValFromPieces(V,tag));
	}
	
	public static XMLpiece nextXML(StringBuffer buf, XMLpiece parent, int start)
	{
		start=buf.indexOf("<",start);
		if(start<0) return null;
		int end=buf.indexOf(">",start);
		if(end<0) return null;
		while(buf.substring(start+1,end).trim().length()==0)
		{
			start=buf.indexOf("<",end);
			if(start<0) return null;
			end=buf.indexOf(">",start);
			if(end<0) return null;
		}
		int nextStart=buf.indexOf("<",start+1);
		while((nextStart>=0)&&(nextStart<end))
		{
			start=nextStart;
			nextStart=buf.indexOf("<",start+1);
		}
		Vector parmList=new Vector();
		String tag=parseOutParms(buf.substring(start+1,end).trim(),parmList).toUpperCase().trim();
		if(!tag.startsWith("/"))
		{
			XMLpiece piece=new XMLpiece();
			piece.parms=parmList;
			if(tag.endsWith("/"))
			{
				piece.tag=tag.substring(0,tag.length()-1);
				piece.value="";
				piece.contents=new Vector();
				piece.outerStart=start;
				piece.outerEnd=end;
			}
			else
			{
				piece.tag=tag;
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
		else
		{
			tag=tag.substring(1);
			if((parent!=null)&&(tag.equals(parent.tag)))
			{
				parent.value=buf.substring(parent.innerStart,start);
				parent.innerEnd=start;
				parent.outerEnd=end;
				return parent;
			}
		}
		return null;
	}
	
	
	public static Vector parseAllXML(String buf)
	{ return parseAllXML(new StringBuffer(buf));}
		
	public static Vector parseAllXML(StringBuffer buf)
	{
		Vector V=new Vector();
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
	
	
	/**
	 * Return the data value within the first XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow);
	 * @param Blob String to searh
	 * @return String Information from first XML block
	 */
	public static String returnXMLValue(String Blob)
	{
		int start=0;
		
		try{
			while((start<Blob.length())&&(Blob.charAt(start)!='>')) start++;
			if((start>=Blob.length())||(Blob.charAt(start-1)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		} catch (Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}
	
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public static String returnXMLValue(String Blob, String Tag)
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
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public static boolean returnXMLBoolean(String Blob, String Tag)
	{
		String val=returnXMLValue(Blob,Tag);
		if((val==null)||((val!=null)&&(val.length()==0)))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}


	/**
	 * Return a parameter value within an XML tag
	 * <TAG Parameter="VALUE">
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=ReturnXMLParm(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Parameter value
	 */
	public static String returnXMLParm(String Blob, String Tag)
	{
		int foundb=Blob.indexOf(Tag+"=");
		if(foundb<0)foundb=Blob.indexOf(Tag+" =");
		if(foundb<0)return"";
		try{ while(Blob.charAt(foundb)!='\"') foundb++;
		} catch(Throwable t){return "";}
		foundb++;
		int founde=Blob.indexOf('\"',foundb);
		if(founde<foundb)return"";
		return Blob.substring(foundb,founde);
	}
}
