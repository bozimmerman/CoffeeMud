package com.planet_ink.coffee_mud.utils;


public class XMLManager
{

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
