package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class XMLManager extends StdLibrary implements XMLLibrary
{
	public String ID(){return "XMLManager";}
	
	protected final String[][] IGNORE_TAG_BOUNDS={{"!--","-->"},{"?","?>"},{"![CDATA[","]]>"}};
	protected enum State {	START, BEFORETAG, INTAG, BEFOREATTRIB, BEGINTAGSELFEND, BEFORECLOSETAG, INCLOSETAG, 
							AFTERCLOSETAG, INATTRIB, INPOSTATTRIB, BEFOREATTRIBVALUE, INATTRIBVALUE, 
							INQUOTEDATTRIBVALUE }
	protected int 			bufDex;
	protected XMLpiece		piece;
	protected State			state;
	protected int[]			beginDex;
	protected int[]			endDex;
	protected StringBuffer	buf;
	protected List<XMLpiece>contents;
	protected Set<String>	illegalTags;
	
	public XMLManager()
	{
		super();
	}
	private XMLManager(final StringBuffer buf, final int startDex)
	{
		bufDex=startDex;
		piece=null;
		state=State.START;
		this.buf=buf;
		beginDex = new int[State.values().length];
		endDex = new int[State.values().length];
		contents=new XVector<XMLpiece>();
		for(int i=0;i<State.values().length;i++)
		{
			beginDex[i]=-1;
			endDex[i]=-1;
		}
		
		try
		{
			illegalTags=CMLib.coffeeFilter().getTagTable().keySet();
		}
		catch(Exception e) 
		{ 
			illegalTags=new HashSet<String>();
		}
	}

	public String parseOutAngleBrackets(String s)
	{
		int x=s.indexOf('<');
		while(x>=0)
		{
			s=s.substring(0,x)+"&lt;"+s.substring(x+1);
			x=s.indexOf('<');
		}
		x=s.indexOf('>');
		while(x>=0)
		{
			s=s.substring(0,x)+"&gt;"+s.substring(x+1);
			x=s.indexOf('>');
		}
		return s;
	}

	public String parseOutAngleBracketsAndQuotes(String s)
	{
		int x=s.indexOf('<');
		while(x>=0)
		{
			s=s.substring(0,x)+"&lt;"+s.substring(x+1);
			x=s.indexOf('<');
		}
		x=s.indexOf('>');
		while(x>=0)
		{
			s=s.substring(0,x)+"&gt;"+s.substring(x+1);
			x=s.indexOf('>');
		}
		x=s.indexOf('\"');
		while(x>=0)
		{
			s=s.substring(0,x)+"&quot;"+s.substring(x+1);
			x=s.indexOf('\"');
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
					else
					if(buf.substring(loop+1,loop+6).equalsIgnoreCase("quot;"))
						buf.replace(loop,loop+6,"\"");
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
			founde=Blob.indexOf('>',foundb);
			if((founde>0)&&(Blob.charAt(founde-1)!='/')) founde=-1;
		}
		if(founde<0) return "";

		Blob=Blob.substring(foundb,founde).trim();
		return Blob;
	}

	public String getValFromPieces(List<XMLpiece> V, String tag)
	{
		return getValFromPieces(V, tag, "");
	}

	public String getValFromPieces(List<XMLpiece> V, String tag, String defVal)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return x.value;
		return defVal;
	}

	public List<XMLpiece> getContentsFromPieces(List<XMLpiece> V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if(x!=null)	return x.contents;
		return null;
	}

	public XMLpiece getPieceFromPieces(List<XMLpiece> V, String tag)
	{
		if(V==null) return null;
		for(int v=0;v<V.size();v++)
			if(V.get(v).tag.equalsIgnoreCase(tag))
				return V.get(v);
		return null;
	}
	
	public boolean isTagInPieces(List<XMLpiece> V, String tag)
	{
		if(V!=null) 
		for(int v=0;v<V.size();v++)
			if(V.get(v).tag.equalsIgnoreCase(tag))
				return true;
		return false;
	}

	public List<XMLpiece> getPiecesFromPieces(List<XMLpiece> V, String tag)
	{
		if(V==null) return null;
		List<XMLpiece> pieces = new ArrayList<XMLpiece>();
		for(int v=0;v<V.size();v++)
			if(V.get(v).tag.equalsIgnoreCase(tag))
				pieces.add(V.get(v));
		return pieces;
	}

	public boolean getBoolFromPieces(List<XMLpiece> V, String tag)
	{
		String val=getValFromPieces(V,tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}


	public int getIntFromPieces(List<XMLpiece> V, String tag)
	{
		return s_int(getValFromPieces(V,tag));
	}

	public short getShortFromPieces(List<XMLpiece> V, String tag)
	{
		return s_short(getValFromPieces(V,tag));
	}

	public long getLongFromPieces(List<XMLpiece> V, String tag)
	{
		return s_long(getValFromPieces(V,tag));
	}

	public double getDoubleFromPieces(List<XMLpiece> V, String tag)
	{
		return s_double(getValFromPieces(V,tag));
	}
	
	public boolean getBoolFromPieces(List<XMLpiece> V, String tag, boolean defVal)
	{
		String val=getValFromPieces(V,tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}


	public int getIntFromPieces(List<XMLpiece> V, String tag, int defVal)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return s_int(x.value);
		return defVal;
	}

	public short getShortFromPieces(List<XMLpiece> V, String tag, short defVal)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return s_short(x.value);
		return defVal;
	}

	public long getLongFromPieces(List<XMLpiece> V, String tag, long defVal)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return s_long(x.value);
		return defVal;
	}

	public double getDoubleFromPieces(List<XMLpiece> V, String tag, double defVal)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return s_double(x.value);
		return defVal;
	}
	
	protected void changeTagState(State newState)
	{
		this.endDex[state.ordinal()]=bufDex;
		state=newState;
		bufDex++;
		this.beginDex[state.ordinal()]=bufDex;
	}

	protected void changedTagState(State newState)
	{
		this.endDex[state.ordinal()]=bufDex-1;
		state=newState;
		this.beginDex[state.ordinal()]=bufDex;
		bufDex++;
	}

	protected void abandonTagState(State newState)
	{
		if((piece!=null)&&(piece.outerEnd<0))
		{
			if(piece.parent!=null)
				piece.parent.contents.remove(piece);
			else
				contents.remove(piece);
			XMLpiece childPiece=piece;
			piece=piece.parent;
			Log.warnOut("XMLManager","Abandoned tag "+childPiece.tag+((piece!=null)?" of parent "+piece.tag:""));
		}
		changeTagState(newState);
	}
	
	protected void handleTagBounds()
	{
		int x=0;
		for(int b=0;b<IGNORE_TAG_BOUNDS.length;b++)
		{
			final String[] bounds=IGNORE_TAG_BOUNDS[b];
			final String boundStart=bounds[0];
			if(bufDex <= (buf.length()-boundStart.length()))
			{
				for(x=0;x<boundStart.length();x++)
					if(buf.charAt(bufDex+x)!=boundStart.charAt(x))
						break;
				if(x>=boundStart.length())
				{
					int comDex=bufDex+boundStart.length();
					final String boundEnd=bounds[1];
					while(comDex <= (buf.length()-boundEnd.length()))
					{
						if(buf.charAt(comDex)==boundEnd.charAt(0))
						{
							for(x=1;x<boundEnd.length();x++)
								if(buf.charAt(comDex+x)!=boundEnd.charAt(x))
									break;
							if(x>=boundEnd.length())
							{
								buf.delete(bufDex-1, comDex+boundEnd.length());
								bufDex-=2;
								changeTagState(State.START);
								return;
							}
						}
						comDex++;
					}
					buf.delete(bufDex-1,buf.length());
					bufDex-=2;
					changeTagState(State.START);
				}
			}
		}
	}
	

	protected void startState(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '<':
		{
			changeTagState(State.BEFORETAG);
			handleTagBounds();
			return;
		}
		default: bufDex++; return;
		}
	}

	protected void beforeTag(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '<': changeTagState(State.BEFORETAG); return;
		case '/': changedTagState(State.BEFORECLOSETAG); return;
		default:
			if(Character.isLetter(c))
				changedTagState(State.INTAG);
			else
				changeTagState(State.START);
			return;
		}
	}

	protected boolean canStartPiece(int endOfTagName)
	{
		final String tagName=buf.substring(beginDex[State.INTAG.ordinal()],endOfTagName).toUpperCase().trim();
		if(illegalTags.contains(tagName))
			return false;
		return true;
	}
	
	protected void startPiece(int endOfTagName)
	{
		XMLpiece newPiece=new XMLpiece();
		newPiece.outerStart=beginDex[State.BEFORETAG.ordinal()];
		newPiece.tag=buf.substring(beginDex[State.INTAG.ordinal()],endOfTagName).toUpperCase().trim();
		if(piece!=null)
			piece.contents.add(newPiece);
		else
			contents.add(newPiece);
		newPiece.parent=piece;
		piece=newPiece;
	}
	
	protected void doneWithPiece(int outerEnd)
	{
		if(piece!=null)
		{
			piece.outerEnd=outerEnd;
			piece=piece.parent;
		}
	}
	
	protected void closePiece(int outerEnd)
	{
		String closeTag=buf.substring(beginDex[State.INCLOSETAG.ordinal()],endDex[State.INCLOSETAG.ordinal()]).toUpperCase().trim();
		XMLpiece closePiece=piece;
		while((closePiece!=null)&&(!closePiece.tag.equalsIgnoreCase(closeTag)))
			closePiece=closePiece.parent;
		if(closePiece!=null)
		{
			if(closePiece.innerStart>=0)
				closePiece.value=buf.substring(closePiece.innerStart,beginDex[State.BEFORETAG.ordinal()]-1);
			piece=closePiece;
			doneWithPiece(outerEnd);
		}
		else
			Log.errOut("XMLManager","Unable to close tag "+closeTag);
	}

	protected void inTag(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': 
			if(canStartPiece(bufDex))
			{
				startPiece(bufDex);
				changedTagState(State.BEFOREATTRIB);
			}
			else
				changeTagState(State.START);
			return;
		case '<': changeTagState(State.BEFORETAG); return;
		case '>': 
			if(canStartPiece(bufDex))
			{
				startPiece(bufDex);
				piece.innerStart=bufDex+1;
			}
			changeTagState(State.START);
			return;
		case '/':
			startPiece(bufDex);
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		default: bufDex++; return;
		}
	}

	protected void beginTagSelfEnd(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '/': bufDex++; break;
		case '<': abandonTagState(State.BEFORETAG); return;
		case '>': doneWithPiece(bufDex); changeTagState(State.START); return;
		default: changeTagState(State.BEFOREATTRIB); return;
		}
	}

	protected void beforeCloseTag(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '<': changeTagState(State.BEFORETAG); return;
		case '/': changeTagState(State.BEFORECLOSETAG); return;
		default:
			if(Character.isLetter(c))
				changedTagState(State.INCLOSETAG);
			else
				changeTagState(State.START);
			return;
		}
	}

	protected void inCloseTag(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': 
			changedTagState(State.AFTERCLOSETAG);
			return;
		case '<': changeTagState(State.BEFORETAG); return;
		case '>':
			changeTagState(State.START); 
			closePiece(bufDex-1);
			return;
		case '/': changedTagState(State.BEFORECLOSETAG); return;
		default: bufDex++; return;
		}
	}

	protected void afterCloseTag(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '<': 
			closePiece(bufDex);
			changeTagState(State.BEFORETAG); 
			return;
		case '>':
			closePiece(bufDex);
			changeTagState(State.START); 
			return;
		default: bufDex++; return;
		}
	}

	protected void beforeAttrib(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '<':
			piece.innerStart=bufDex;
			abandonTagState(State.BEFORETAG); 
			return;
		case '>':
			changeTagState(State.START); 
			piece.innerStart=bufDex;
			return;
		case '/':
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		default:
			changedTagState(State.INATTRIB); 
			return;
		}
	}
	
	protected void beAttrib(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': bufDex++; return;
		case '<':
			piece.innerStart=bufDex;
			abandonTagState(State.BEFORETAG); 
			return;
		case '>':
			changeTagState(State.START); 
			piece.innerStart=bufDex;
			return;
		case '/':
			endEmptyAttrib(bufDex);
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		default:
			changedTagState(State.INATTRIB); 
			return;
		}
	}
	
	protected void endEmptyAttrib(int endOfAttrib)
	{
		if(piece!=null)
		{
			String value="";
			String parmName=buf.substring(beginDex[State.INATTRIB.ordinal()],endOfAttrib);
			if((parmName.length()>15)||(parmName.length()==0))
				Log.warnOut("XMLManager","Suspicious attribute '"+parmName+"' for tag "+piece.tag);
			piece.parms.put(parmName, value);
		}
	}

	protected void inAttrib(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n':  changedTagState(State.INPOSTATTRIB); return;
		case '=':  changeTagState(State.BEFOREATTRIBVALUE); return;
		case '<':
			endEmptyAttrib(bufDex);
			piece.innerStart=bufDex;
			abandonTagState(State.BEFORETAG); 
			return;
		case '>':
			endEmptyAttrib(bufDex);
			changeTagState(State.START); 
			piece.innerStart=bufDex;
			return;
		case '/':
			endEmptyAttrib(bufDex);
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		default: bufDex++; return;
		}
	}

	protected void inPostAttrib(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n':  bufDex++; return;
		case '=':  changeTagState(State.BEFOREATTRIBVALUE); return;
		case '<':
			endEmptyAttrib(endDex[State.INATTRIB.ordinal()]);
			piece.innerStart=bufDex;
			abandonTagState(State.BEFORETAG); 
			return;
		case '>':
			endEmptyAttrib(endDex[State.INATTRIB.ordinal()]);
			changeTagState(State.START); 
			piece.innerStart=bufDex;
			return;
		case '/':
			endEmptyAttrib(endDex[State.INATTRIB.ordinal()]);
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		default: changedTagState(State.INATTRIB); return;
		}
	}
	
	protected void assignAttrib(int endOfValue)
	{
		if(piece!=null)
		{
			String parmName=buf.substring(beginDex[State.INATTRIB.ordinal()], endDex[State.INATTRIB.ordinal()]).toUpperCase().trim();
			String value=buf.substring(beginDex[state.ordinal()],endOfValue).trim();
			if((parmName.length()>15)||(parmName.length()==0))
				Log.warnOut("XMLManager","Suspicious attribute '"+parmName+"' for tag "+piece.tag);
			piece.parms.put(parmName, value);
		}

	}

	protected void beforeAttribValue(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n':  bufDex++; return;
		case '=':  bufDex++; return;
		case '<':
			assignAttrib(bufDex);
			piece.innerStart=bufDex;
			abandonTagState(State.BEFORETAG); 
			return;
		case '>':
			assignAttrib(bufDex);
			changeTagState(State.START); 
			piece.innerStart=bufDex;
			return;
		case '/':
			assignAttrib(bufDex);
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		case '"': changeTagState(State.INQUOTEDATTRIBVALUE); return; 
		default: changedTagState(State.INATTRIBVALUE); return;
		}
	}

	protected void inAttribValue(final char c)
	{
		switch(c)
		{
		case ' ': case '\t': case '\r': case '\n': 
			assignAttrib(bufDex); 
			changedTagState(State.BEFOREATTRIB); 
			return;
		case '<':
			assignAttrib(bufDex);
			piece.innerStart=bufDex;
			abandonTagState(State.BEFORETAG); 
			return;
		case '>':
			assignAttrib(bufDex);
			changeTagState(State.START); 
			piece.innerStart=bufDex;
			return;
		case '/':
			assignAttrib(bufDex);
			changedTagState(State.BEGINTAGSELFEND); 
			return;
		default: bufDex++; return;
		}
	}

	protected void inQuotedAttribValue(final char c)
	{
		switch(c)
		{
		case '"':
			assignAttrib(bufDex); 
			changeTagState(State.BEFOREATTRIB); 
			return;
		default: bufDex++; return;
		}
	}

	protected XMLpiece parseXML()
	{
		while(bufDex<buf.length())
		{
			switch(state)
			{
			case START:				startState(buf.charAt(bufDex)); break;
			case BEFORETAG:			beforeTag(buf.charAt(bufDex)); break;
			case INTAG:				inTag(buf.charAt(bufDex)); break;
			case BEGINTAGSELFEND:	beginTagSelfEnd(buf.charAt(bufDex)); break;
			case BEFORECLOSETAG:	beforeCloseTag(buf.charAt(bufDex)); break;
			case INCLOSETAG:		inCloseTag(buf.charAt(bufDex)); break;
			case AFTERCLOSETAG:		afterCloseTag(buf.charAt(bufDex)); break;
			case BEFOREATTRIB:		beforeAttrib(buf.charAt(bufDex)); break;
			case INATTRIB:			inAttrib(buf.charAt(bufDex)); break;
			case INPOSTATTRIB:		inPostAttrib(buf.charAt(bufDex)); break;
			case BEFOREATTRIBVALUE:	beforeAttribValue(buf.charAt(bufDex)); break;
			case INATTRIBVALUE:		inAttribValue(buf.charAt(bufDex)); break;
			case INQUOTEDATTRIBVALUE: inQuotedAttribValue(buf.charAt(bufDex)); break;
			}
		}
		while((piece!=null)&&(piece.parent!=null))
			piece=piece.parent;
		return piece;
	}
	
	public List<XMLpiece> parseAllXML(String buf)
	{  
		return parseAllXML(new StringBuffer(buf));
	}

	public List<XMLpiece> parseAllXML(StringBuffer buf)
	{
		XMLManager manager=new XMLManager(buf, 0);
		manager.parseXML();
		return manager.contents;
	}

	public String returnXMLValue(String Blob)
	{
		int start=0;

		try
		{
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
		try
		{
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

	public String getParmValue(Map<String, String> parmSet, String Tag)
	{
		if((parmSet != null)&&(Tag != null))
			return parmSet.get(Tag.toUpperCase().trim());
		return null;
	}

	public String getXMLList(List<String> V)
	{
		StringBuffer str=new StringBuffer("");
		if(V!=null)
		for(String s : V)
			if(s!=null)
			{
				if(s.trim().length()==0)
					str.append("<X />");
				else
					str.append("<X>"+parseOutAngleBrackets(s)+"</X>");
			}
		return str.toString();
	}
	
	public List<String> parseXMLList(String numberedList)
	{
		List<XMLLibrary.XMLpiece> xml=parseAllXML(numberedList);
		Vector<String> V=new Vector<String>();
		for(int v=0;v<xml.size();v++)
			V.addElement(this.restoreAngleBrackets(xml.get(v).value));
		return V;
	}
}
