package com.planet_ink.coffee_mud.utils;

import java.util.*;

public class XMLManager
{

	/**
	 * Returns the integer value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	private  static int s_int(String INT)
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
	
	private static int findEndingTag(String buf, String tag)
	{
		if((buf==null)||(buf.length()==0)) return -1;
		
		int x=buf.indexOf("</"+tag+">");
		if(x>=0) return x+("</"+tag+">").length();
		
		int newPos=0;
		while(newPos<buf.length())
		{
			int check=buf.indexOf(tag,newPos);
			if(check<0) return -1;
			
			if(check<2)
				newPos+=2;
			else
			if(check==(buf.length()-1))
			   return -1;
			else
			if(((!Character.isWhitespace(buf.charAt(check+tag.length())))&&(buf.charAt(check+tag.length())!='>'))
			||((!Character.isWhitespace(buf.charAt(check-1)))&&(buf.charAt(check-1)!='/')))
			   newPos=check+1;
			else
			{
				boolean foundslash=false;
				for(x=check-1;x>=0;x--)
					if((buf.charAt(x)=='<')&&(foundslash))
					{
						check=x;
						break;
					}
					else
					if((buf.charAt(x)=='/')&&(!foundslash))
						foundslash=true;
					else
					if(!Character.isWhitespace(buf.charAt(x)))
					{
					   newPos=check+1;
					   break;
					}
				if((check==x)&&(x>=0)&&foundslash)
				{
					for(x=check+1;x<buf.length();x++)
						if(buf.charAt(x)=='>')
							return x+1;
						else
						if(buf.charAt(x)=='<')
						{
							newPos=check+1;
							break;
						}
				}
			}
			
		}
		return -1;
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
		return Util.s_double(getValFromPieces(V,tag));
	}
	
	
	private static int findCompetingTag(String buf, String tag)
	{
		if((buf==null)||(buf.length()==0)) return -1;
		
		int x=buf.indexOf("<"+tag+">");
		if(x>=0) return x;
		
		int newPos=0;
		while(newPos<buf.length())
		{
			int check=buf.indexOf(tag,newPos);
			if(check<0) return -1;
			
			if(check==0)
				newPos++;
			else
			if(check==(buf.length()-1))
			   return -1;
			else
			if(((!Character.isWhitespace(buf.charAt(check+tag.length())))&&(buf.charAt(check+tag.length())!='>'))
			||((!Character.isWhitespace(buf.charAt(check-1)))&&(buf.charAt(check-1)!='/')))
			   newPos=check+1;
			else
			{
				for(x=check-1;x>=0;x--)
					if(buf.charAt(x)=='<')
					{
						check=x;
						break;
					}
					else
					if(!Character.isWhitespace(buf.charAt(x)))
					{
					   newPos=check+1;
					   break;
					}
				if((check==x)&&(x>=0))
				{
					for(x=check+1;x<buf.length();x++)
						if(buf.charAt(x)=='>')
							return check;
						else
						if(buf.charAt(x)=='<')
						{
							newPos=check+1;
							break;
						}
				}
			}
		}
		return -1;
	}
	
	private static String completeBlock(String buf, String tag)
	{
		int possEnd=findEndingTag(buf.toUpperCase(),tag);
		if(possEnd<0) return buf;
		String possBlock=buf.substring(0,possEnd);
		
		int possMid=findCompetingTag(possBlock.toUpperCase(),tag);
		if(possMid<0) return possBlock;
		if(possMid>possEnd) return possBlock;
		
		String subBuf=buf.substring(possMid);
		int newPos=subBuf.indexOf(">");
		if(newPos<0) return possBlock;
		newPos+=possMid;
		String newBlock=completeBlock(buf.substring(newPos+1),tag);
		newPos+=newBlock.length();
		
		int newPossEnd=findEndingTag(buf.substring(newPos+1).toUpperCase(),tag);
		if(newPossEnd<0) return buf.substring(0,newPos);
		return buf.substring(0,newPos+newPossEnd+1);
	}
	
	public static Vector parseAllXML(String buf)
	{
		Vector xml=null;
		int position=0;
		while(position<buf.length())
		{
			int startTag=buf.indexOf("<",position);
			if(startTag<0) break;
			int endofTag=buf.indexOf(">",startTag);
			if(endofTag<0) break;
			endofTag++;
			position=endofTag;
			
			XMLpiece piece=new XMLpiece();
			
			Vector parmList=new Vector();
			String tag=parseOutParms(buf.substring(startTag+1,endofTag-1).trim(),parmList);
			piece.tag=tag.toUpperCase().trim();
			if(parmList.size()>0)
				piece.parms=parmList;
			
			if((!tag.endsWith("/"))&&(findEndingTag(buf.substring(endofTag).toUpperCase(),tag)<0))
				break;
			else
			{
				if(!tag.endsWith("/"))
				{
					String wholeBlock=completeBlock(buf.substring(endofTag),tag);
					position+=wholeBlock.length();
					int z=wholeBlock.lastIndexOf("<");
					if(z>=0) wholeBlock=wholeBlock.substring(0,z);
					piece.contents=parseAllXML(wholeBlock.trim());
					piece.value=wholeBlock.trim();
				}
				if(piece.contents==null)
					piece.contents=new Vector();
			}
			
			if(xml==null) xml=new Vector();
			xml.addElement(piece);
		}
		return xml;
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
