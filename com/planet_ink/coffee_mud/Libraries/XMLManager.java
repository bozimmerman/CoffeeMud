package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2001-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "XMLManager";
	}

	protected final static String HEX_DIGITS="0123456789ABCDEF";
	
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
	protected List<XMLTag>contents;
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
		contents=new XVector<XMLTag>();
		for(int i=0;i<State.values().length;i++)
		{
			beginDex[i]=-1;
			endDex[i]=-1;
		}

		try
		{
			illegalTags=CMLib.coffeeFilter().getTagTable().keySet();
		}
		catch(final Exception e)
		{
			illegalTags=new HashSet<String>();
		}
	}

	/**
	 *
	 * @author Bo Zimmerman
	 *
	 */
	private static class XMLpiece implements Cloneable, XMLTag
	{
		protected String				tag			= "";
		protected String				value		= "";
		protected List<XMLTag>			contents	= new XVector<XMLTag>();
		protected Map<String, String>	parms		= new XHashtable<String, String>();
		protected XMLpiece				parent		= null;
		protected int					outerStart	= -1;
		protected int					innerStart	= -1;
		protected int					innerEnd	= -1;
		protected int					outerEnd	= -1;

		public XMLpiece()
		{
			
		}
		
		public XMLpiece(String tag, String value)
		{
			this.tag=tag.toUpperCase().trim();
			this.value=value;
		}
		
		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#copyOf()
		 */
		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#copyOf()
		 */
		@Override
		public XMLTag copyOf()
		{
			try
			{
				final XMLpiece piece2=(XMLpiece)this.clone();
				piece2.contents =new XVector<XMLTag>(contents());
				piece2.parms = new XHashtable<String,String>(parms());
				return piece2;
			}
			catch(final Exception e)
			{
				return this;
			}
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#addContent(com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag)
		 */
		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#addContent(com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag)
		 */
		@Override
		public void addContent(XMLTag x)
		{
			if (x == null) return;
			if (contents() == null) 
				contents = new XVector<XMLTag>();
			if(x instanceof XMLpiece)
				((XMLpiece)x).parent = this;
			contents().add(x);
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#toString()
		 */
		@Override
		public String toString()
		{
			final StringBuilder str=new StringBuilder("");
			str.append("<").append(tag());
			for(final String parm : parms().keySet())
				str.append(" ").append(parm).append("=\"").append(parms().get(parm)).append("\"");
			str.append(">").append(value()).append("</").append(tag()).append(">");
			return str.toString();
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#tag()
		 */
		@Override
		public String tag()
		{
			return tag;
		}
		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#value()
		 */
		@Override
		public String value()
		{
			return value;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#contents()
		 */
		@Override
		public List<XMLTag> contents()
		{
			return contents;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#parms()
		 */
		@Override
		public Map<String,String> parms()
		{
			return parms;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#parent()
		 */
		@Override
		public XMLpiece parent()
		{
			return parent;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#outerStartIndex()
		 */
		@Override
		public int outerStartIndex()
		{
			return outerStart;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#innerStartIndex()
		 */
		@Override
		public int innerStartIndex()
		{
			return innerStart;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#innerEndIndex()
		 */
		@Override
		public int innerEndIndex()
		{
			return innerEnd;
		}

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#outerEndIndex()
		 */
		@Override
		public int outerEndIndex()
		{
			return outerEnd;
		}
		
		@Override
		public String getParmValue(String Tag)
		{
			if((parms != null)&&(Tag != null))
				return parms.get(Tag.toUpperCase().trim());
			return null;
		}
		
		@Override
		public XMLTag getPieceFromPieces(String tag)
		{
			if(contents==null)
				return null;
			for(int v=0;v<contents.size();v++)
			{
				if(contents.get(v).tag().equalsIgnoreCase(tag))
					return contents.get(v);
			}
			return null;
		}
		
		@Override
		public double getDoubleFromPieces(String tag, double defVal)
		{
			final XMLTag x=getPieceFromPieces(tag);
			if((x!=null)&&(x.value()!=null))
				return s_double(x.value());
			return defVal;
		}

		@Override
		public boolean getBoolFromPieces(String tag, boolean defVal)
		{
			final String val=getValFromPieces(tag);
			if((val==null)||(val.length()==0))
				return false;
			if(val.toUpperCase().trim().startsWith("T"))
				return true;
			return false;
		}

		@Override
		public int getIntFromPieces(String tag, int defVal)
		{
			final XMLTag x=getPieceFromPieces(tag);
			if((x!=null)&&(x.value()!=null))
				return s_int(x.value());
			return defVal;
		}

		@Override
		public short getShortFromPieces(String tag, short defVal)
		{
			final XMLTag x=getPieceFromPieces(tag);
			if((x!=null)&&(x.value()!=null))
				return s_short(x.value());
			return defVal;
		}

		@Override
		public long getLongFromPieces(String tag, long defVal)
		{
			final XMLTag x=getPieceFromPieces(tag);
			if((x!=null)&&(x.value()!=null))
				return s_long(x.value());
			return defVal;
		}
		
		@Override
		public String getValFromPieces(String tag)
		{
			return getValFromPieces(tag, "");
		}

		@Override
		public String getValFromPieces(String tag, String defVal)
		{
			final XMLTag x=getPieceFromPieces(tag);
			if((x!=null)&&(x.value()!=null))
				return x.value();
			return defVal;
		}
		
		@Override
		public List<XMLTag> getContentsFromPieces(String tag)
		{
			final XMLTag x=getPieceFromPieces(tag);
			if(x!=null)
				return x.contents();
			return null;
		}

		@Override
		public boolean isTagInPieces(String tag)
		{
			if(contents!=null)
			{
				for(int v=0;v<contents.size();v++)
				{
					if(contents.get(v).tag().equalsIgnoreCase(tag))
						return true;
				}
			}
			return false;
		}

		@Override
		public List<XMLTag> getPiecesFromPieces(String tag)
		{
			if(contents==null)
				return null;
			final List<XMLTag> pieces = new ArrayList<XMLTag>();
			for(int v=0;v<contents.size();v++)
			{
				if(contents.get(v).tag().equalsIgnoreCase(tag))
					pieces.add(contents.get(v));
			}
			return pieces;
		}

		@Override
		public boolean getBoolFromPieces(String tag)
		{
			final String val=getValFromPieces(tag);
			if((val==null)||(val.length()==0))
				return false;
			if(val.toUpperCase().trim().startsWith("T"))
				return true;
			return false;
		}

		@Override
		public int getIntFromPieces(String tag)
		{
			return s_int(getValFromPieces(tag));
		}

		@Override
		public short getShortFromPieces(String tag)
		{
			return s_short(getValFromPieces(tag));
		}

		@Override
		public long getLongFromPieces(String tag)
		{
			return s_long(getValFromPieces(tag));
		}

		@Override
		public double getDoubleFromPieces(String tag)
		{
			return s_double(getValFromPieces(tag));
		}
	}

	@Override
	public XMLTag createNewTag(String key, String value)
	{
		return new XMLpiece(key, value);
	}
	
	@Override
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

	@Override
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

	@Override
	public String restoreAngleBrackets(String s)
	{
		if(s==null)
			return null;
		final StringBuffer buf=new StringBuffer(s);
		int loop=0;
		while(loop<buf.length())
		{
			switch(buf.charAt(loop))
			{
			case '&':
				if(loop<buf.length()-3)
				{
					switch(buf.charAt(loop+1))
					{
					case 'l':
						if(buf.substring(loop+1,loop+4).equalsIgnoreCase("lt;"))
							buf.replace(loop,loop+4,"<");
						break;
					case 'g':
						if(buf.substring(loop+1,loop+4).equalsIgnoreCase("gt;"))
							buf.replace(loop,loop+4,">");
						break;
					case 'q':
						if(buf.substring(loop+1,loop+6).equalsIgnoreCase("quot;"))
							buf.replace(loop,loop+6,"\"");
						break;
					case 'a':
						if(buf.substring(loop+1,loop+6).equalsIgnoreCase("amp;"))
							buf.replace(loop,loop+5,"&");
						else
						if(buf.substring(loop+1,loop+6).equalsIgnoreCase("apos;"))
							buf.replace(loop,loop+6,"'");
						break;
					}
				}
				break;
			case '%':
				if(loop<buf.length()-2)
				{
					final int dig1=HEX_DIGITS.indexOf(buf.charAt(loop+1));
					final int dig2=HEX_DIGITS.indexOf(buf.charAt(loop+2));
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
	 * Usage: dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param DOUBLE String to convert
	 * @return double Double value of the string
	 */
	protected static double s_double(String DOUBLE)
	{
		double sdouble=0;
		try{ sdouble=Double.parseDouble(DOUBLE); }
		catch(final Exception e){ return 0;}
		return sdouble;
	}

	/**
	 * Returns the integer value of a string without crashing
 	 *
	 * Usage: int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	protected static int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
		catch(final java.lang.NumberFormatException e){ return 0;}
		return sint;
	}

	/**
	 * Returns the short value of a string without crashing
 	 *
	 * Usage: int num=s_short(CMD.substring(14));
	 * @param SHORT Short value of string
	 * @return short Short value of the string
	 */
	protected static short s_short(String SHORT)
	{
		short sint=0;
		try{ sint=Short.parseShort(SHORT); }
		catch(final java.lang.NumberFormatException e){ return 0;}
		return sint;
	}

	/**
	 * Returns the long value of a string without crashing
 	 *
	 * Usage: int num=s_long(CMD.substring(14));
	 * @param LONG Long value of string
	 * @return long Long value of the string
	 */
	protected static  long s_long(String LONG)
	{
		long slong=0;
		try{ slong=Long.parseLong(LONG); }
		catch(final java.lang.NumberFormatException e){ return 0;}
		return slong;
	}

	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
  	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	@Override
	public String convertXMLtoTag(String TName, String Data)
	{
		if((Data==null)||(Data.length()==0))
			return "<"+TName+" />";
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
  	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	@Override
	public String convertXMLtoTag(String TName, int Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
  	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	@Override
	public String convertXMLtoTag(String TName, short Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
  	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	@Override
	public String convertXMLtoTag(String TName, boolean Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
  	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data to embed
	 * @return String Information corresponding to the tname
	 */
	@Override
	public String convertXMLtoTag(String TName, long Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}

	/**
	 * Return the contents of an XML tag, given the tag to search for
	 *
  	 * Usage: String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	@Override
	public String returnXMLBlock(String Blob, String Tag)
	{
		int foundb=Blob.indexOf("<"+Tag+">");
		if(foundb<0)
			foundb=Blob.indexOf("<"+Tag+" ");
		if(foundb<0)
			foundb=Blob.indexOf("<"+Tag+"/");
		if(foundb<0)
			return "";

		int founde=Blob.indexOf("/"+Tag+">",foundb)-1;
		if(founde<0)
			founde=Blob.indexOf("/"+Tag+" ",foundb)-1;
		if(founde<0)
		{
			founde=Blob.indexOf('>',foundb);
			if((founde>0)&&(Blob.charAt(founde-1)!='/'))
				founde=-1;
		}
		if(founde<0)
			return "";

		Blob=Blob.substring(foundb,founde).trim();
		return Blob;
	}

	@Override
	public String getValFromPieces(List<XMLTag> V, String tag)
	{
		return getValFromPieces(V, tag, "");
	}

	@Override
	public String getValFromPieces(List<XMLTag> V, String tag, String defVal)
	{
		final XMLTag x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value()!=null))
			return x.value();
		return defVal;
	}

	@Override
	public List<XMLTag> getContentsFromPieces(List<XMLTag> V, String tag)
	{
		final XMLTag x=getPieceFromPieces(V,tag);
		if(x!=null)
			return x.contents();
		return null;
	}

	@Override
	public XMLTag getPieceFromPieces(List<XMLTag> V, String tag)
	{
		if(V==null)
			return null;
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).tag().equalsIgnoreCase(tag))
				return V.get(v);
		}
		return null;
	}

	@Override
	public boolean isTagInPieces(List<XMLTag> V, String tag)
	{
		if(V!=null)
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).tag().equalsIgnoreCase(tag))
				return true;
		}
		return false;
	}

	@Override
	public List<XMLTag> getPiecesFromPieces(List<XMLTag> V, String tag)
	{
		if(V==null)
			return null;
		final List<XMLTag> pieces = new ArrayList<XMLTag>();
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).tag().equalsIgnoreCase(tag))
				pieces.add(V.get(v));
		}
		return pieces;
	}

	@Override
	public boolean getBoolFromPieces(List<XMLTag> V, String tag)
	{
		final String val=getValFromPieces(V,tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}

	@Override
	public int getIntFromPieces(List<XMLTag> V, String tag)
	{
		return s_int(getValFromPieces(V,tag));
	}

	@Override
	public short getShortFromPieces(List<XMLTag> V, String tag)
	{
		return s_short(getValFromPieces(V,tag));
	}

	@Override
	public long getLongFromPieces(List<XMLTag> V, String tag)
	{
		return s_long(getValFromPieces(V,tag));
	}

	@Override
	public double getDoubleFromPieces(List<XMLTag> V, String tag)
	{
		return s_double(getValFromPieces(V,tag));
	}

	@Override
	public boolean getBoolFromPieces(List<XMLTag> V, String tag, boolean defVal)
	{
		final String val=getValFromPieces(V,tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}

	@Override
	public int getIntFromPieces(List<XMLTag> V, String tag, int defVal)
	{
		final XMLTag x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value()!=null))
			return s_int(x.value());
		return defVal;
	}

	@Override
	public short getShortFromPieces(List<XMLTag> V, String tag, short defVal)
	{
		final XMLTag x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value()!=null))
			return s_short(x.value());
		return defVal;
	}

	@Override
	public long getLongFromPieces(List<XMLTag> V, String tag, long defVal)
	{
		final XMLTag x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value()!=null))
			return s_long(x.value());
		return defVal;
	}

	@Override
	public double getDoubleFromPieces(List<XMLTag> V, String tag, double defVal)
	{
		final XMLTag x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value()!=null))
			return s_double(x.value());
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
		if((piece!=null)&&(piece.outerEndIndex()<0))
		{
			if(piece.parent()!=null)
				piece.parent().contents().remove(piece);
			else
				contents.remove(piece);
			final XMLTag childPiece=piece;
			piece=piece.parent();
			Log.warnOut("XMLManager","Abandoned tag "+childPiece.tag()+((piece!=null)?" of parent "+piece.tag():""));
		}
		changeTagState(newState);
	}

	protected void handleTagBounds()
	{
		int x=0;
		for (final String[] bounds : IGNORE_TAG_BOUNDS)
		{
			final String boundStart=bounds[0];
			if(bufDex <= (buf.length()-boundStart.length()))
			{
				for(x=0;x<boundStart.length();x++)
				{
					if(buf.charAt(bufDex+x)!=boundStart.charAt(x))
						break;
				}
				if(x>=boundStart.length())
				{
					int comDex=bufDex+boundStart.length();
					final String boundEnd=bounds[1];
					while(comDex <= (buf.length()-boundEnd.length()))
					{
						if(buf.charAt(comDex)==boundEnd.charAt(0))
						{
							for(x=1;x<boundEnd.length();x++)
							{
								if(buf.charAt(comDex+x)!=boundEnd.charAt(x))
									break;
							}
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
		final XMLpiece newPiece=new XMLpiece();
		newPiece.outerStart = beginDex[State.BEFORETAG.ordinal()];
		newPiece. tag = buf.substring(beginDex[State.INTAG.ordinal()],endOfTagName).toUpperCase().trim();
		if(piece!=null)
			piece.contents().add(newPiece);
		else
			contents.add(newPiece);
		newPiece.parent = piece;
		piece=newPiece;
	}

	protected void doneWithPiece(int outerEnd)
	{
		if(piece!=null)
		{
			piece.outerEnd = outerEnd;
			piece=piece.parent();
		}
	}

	protected void closePiece(int outerEnd)
	{
		final String closeTag=buf.substring(beginDex[State.INCLOSETAG.ordinal()],endDex[State.INCLOSETAG.ordinal()]).toUpperCase().trim();
		XMLpiece closePiece=piece;
		while((closePiece!=null)&&(!closePiece.tag().equalsIgnoreCase(closeTag)))
			closePiece=closePiece.parent();
		if(closePiece!=null)
		{
			if(closePiece.innerStartIndex()>=0)
				closePiece.value = buf.substring(closePiece.innerStartIndex(),beginDex[State.BEFORETAG.ordinal()]-1);
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
				piece.innerStart = bufDex+1;
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
			piece.innerStart = bufDex;
			abandonTagState(State.BEFORETAG);
			return;
		case '>':
			changeTagState(State.START);
			piece.innerStart = bufDex;
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
			piece.innerStart = bufDex;
			abandonTagState(State.BEFORETAG);
			return;
		case '>':
			changeTagState(State.START);
			piece.innerStart = bufDex;
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
			final String value="";
			final String parmName=buf.substring(beginDex[State.INATTRIB.ordinal()],endOfAttrib);
			if((parmName.length()>35)||(parmName.length()==0))
				Log.warnOut("XMLManager","Suspicious attribute '"+parmName+"' for tag "+piece.tag());
			piece.parms().put(parmName, value);
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
			piece.innerStart = bufDex;
			abandonTagState(State.BEFORETAG);
			return;
		case '>':
			endEmptyAttrib(bufDex);
			changeTagState(State.START);
			piece.innerStart = bufDex;
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
			piece.innerStart = bufDex;
			abandonTagState(State.BEFORETAG);
			return;
		case '>':
			endEmptyAttrib(endDex[State.INATTRIB.ordinal()]);
			changeTagState(State.START);
			piece.innerStart =bufDex;
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
			final String parmName=buf.substring(beginDex[State.INATTRIB.ordinal()], endDex[State.INATTRIB.ordinal()]).toUpperCase().trim();
			final String value=buf.substring(beginDex[state.ordinal()],endOfValue).trim();
			if((parmName.length()>35)||(parmName.length()==0))
				Log.warnOut("XMLManager","Suspicious attribute '"+parmName+"' for tag "+piece.tag());
			piece.parms().put(parmName, value);
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
			piece.innerStart = bufDex;
			abandonTagState(State.BEFORETAG);
			return;
		case '>':
			assignAttrib(bufDex);
			changeTagState(State.START);
			piece.innerStart = bufDex;
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
			piece.innerStart = bufDex;
			abandonTagState(State.BEFORETAG);
			return;
		case '>':
			assignAttrib(bufDex);
			changeTagState(State.START);
			piece.innerStart = bufDex;
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

	protected XMLTag parseXML()
	{
		while(bufDex<buf.length())
		{
			switch(state)
			{
			case START:
				startState(buf.charAt(bufDex));
				break;
			case BEFORETAG:
				beforeTag(buf.charAt(bufDex));
				break;
			case INTAG:
				inTag(buf.charAt(bufDex));
				break;
			case BEGINTAGSELFEND:
				beginTagSelfEnd(buf.charAt(bufDex));
				break;
			case BEFORECLOSETAG:
				beforeCloseTag(buf.charAt(bufDex));
				break;
			case INCLOSETAG:
				inCloseTag(buf.charAt(bufDex));
				break;
			case AFTERCLOSETAG:
				afterCloseTag(buf.charAt(bufDex));
				break;
			case BEFOREATTRIB:
				beforeAttrib(buf.charAt(bufDex));
				break;
			case INATTRIB:
				inAttrib(buf.charAt(bufDex));
				break;
			case INPOSTATTRIB:
				inPostAttrib(buf.charAt(bufDex));
				break;
			case BEFOREATTRIBVALUE:
				beforeAttribValue(buf.charAt(bufDex));
				break;
			case INATTRIBVALUE:
				inAttribValue(buf.charAt(bufDex));
				break;
			case INQUOTEDATTRIBVALUE:
				inQuotedAttribValue(buf.charAt(bufDex));
				break;
			}
		}
		while((piece!=null)&&(piece.parent()!=null))
			piece=piece.parent();
		return piece;
	}

	@Override
	public List<XMLTag> parseAllXML(String buf)
	{
		return parseAllXML(new StringBuffer(buf));
	}

	@Override
	public List<XMLTag> parseAllXML(StringBuffer buf)
	{
		final XMLManager manager=new XMLManager(buf, 0);
		manager.parseXML();
		return manager.contents;
	}

	@Override
	public String returnXMLValue(String Blob)
	{
		int start=0;

		try
		{
			while((start<Blob.length())&&(Blob.charAt(start)!='>'))
				start++;
			if((start>=Blob.length())||(Blob.charAt(start-1)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		}
		catch (final Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}

	@Override
	public String returnXMLValue(String Blob, String Tag)
	{
		int start=0;
		Blob=returnXMLBlock(Blob,Tag);
		try
		{
			while((start<Blob.length())&&(Blob.charAt(start)!='>'))
				start++;
			if((start>=Blob.length())||(Blob.charAt(start)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		}
		catch (final Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}

	@Override
	public boolean returnXMLBoolean(String Blob, String Tag)
	{
		final String val=returnXMLValue(Blob,Tag);
		if((val==null)||(val.length()==0))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}

	@Override
	public String getXMLList(List<String> V)
	{
		final StringBuffer str=new StringBuffer("");
		if(V!=null)
		for(final String s : V)
		{
			if(s!=null)
			{
				if(s.trim().length()==0)
					str.append("<X />");
				else
					str.append("<X>"+parseOutAngleBrackets(s)+"</X>");
			}
		}
		return str.toString();
	}

	@Override
	public List<String> parseXMLList(String numberedList)
	{
		final List<XMLLibrary.XMLTag> xml=parseAllXML(numberedList);
		final Vector<String> V=new Vector<String>();
		for(int v=0;v<xml.size();v++)
			V.addElement(this.restoreAngleBrackets(xml.get(v).value()));
		return V;
	}
	
	/**
	 * Converts a pojo field to a xml value.
	 * @param type the class type
	 * @param val the value
	 * @return the xml value
	 */
	protected String fromPOJOFieldtoXML(Class<?> type, Object val)
	{
		final StringBuilder str=new StringBuilder("");
		if(type.isArray())
		{
			final int length = Array.getLength(val);
			for (int i=0; i<length; i++) 
			{
				Object e = Array.get(val, i);
				str.append("<VALUE>");
				str.append(fromPOJOFieldtoXML(type.getComponentType(),e));
				str.append("</VALUE>");
			}
		}
		else
		if(type == String.class)
			str.append(parseOutAngleBrackets(val.toString()));
		else
		if(type.isPrimitive())
			str.append(val.toString());
		else
		if((type == Float.class)||(type==Integer.class)||(type==Double.class)||(type==Boolean.class)
		 ||(type==Long.class)||(type==Short.class)||(type==Byte.class))
			str.append(val.toString());
		else
			str.append(fromPOJOtoXML(val));
		return str.toString();
	}

	/**
	 * Converts a pojo object to a XML document.
	 * @param o the object to convert
	 * @return the XML document
	 */
	@Override
	public String fromPOJOtoXML(Object o)
	{
		StringBuilder str=new StringBuilder("");
		final Field[] fields = o.getClass().getDeclaredFields();
		for(final Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				if(field.isAccessible())
				{
					str.append("<").append(field.getName());
					final Object obj = field.get(o);
					if(obj == null)
						str.append(" ISNULL=TRUE />");
					else
					{
						str.append(">");
						str.append(fromPOJOFieldtoXML(field.getType(),field.get(o)));
					}
					str.append("</").append(field.getName()).append(">");
				}
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
		}
		return str.toString();
	}

	/**
	 * Converts a JSON document to a XML object.
	 * @param XML the XML document
	 * @param o the object to convert
	 */
	@Override
	public void fromXMLtoPOJO(String XML, Object o)
	{
		fromXMLtoPOJO(this.parseAllXML(XML),o);
	}

	/**
	 * Converts a xml object to a pojo object.
	 * @param xmlObj the json object
	 * @param o the object to convert
	 */
	@Override
	public void fromXMLtoPOJO(List<XMLTag> xmlObj, Object o)
	{
		final Field[] fields = o.getClass().getDeclaredFields();
		for(final Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				XMLTag valTag = getPieceFromPieces(xmlObj, field.getName());
				if(field.isAccessible() && (valTag!=null))
				{
					if(valTag.parms().containsKey("ISNULL")
					&&(valTag.parms().get("ISNULL").equalsIgnoreCase("TRUE")))
						field.set(o, null);
					else
					if(field.getType().isArray())
					{
						List<XMLTag> objs = valTag.contents();
						final Object tgt;
						final Class<?> cType = field.getType().getComponentType();
						tgt = Array.newInstance(cType, objs.size());
						for(int i=0;i<objs.size();i++)
						{
							final XMLTag vTag=objs.get(i);
							if(!vTag.tag().equalsIgnoreCase("VALUE"))
								continue;
							if(cType == Float.class)
								Array.set(tgt, i, Float.valueOf(Double.valueOf(vTag.value()).floatValue()));
							else
							if(cType == Double.class)
								Array.set(tgt, i, Double.valueOf(vTag.value()));
							else
							if(cType == Long.class)
								Array.set(tgt, i, Long.valueOf(vTag.value()));
							else
							if(cType == Integer.class)
								Array.set(tgt, i, Integer.valueOf(Long.valueOf(vTag.value()).intValue()));
							else
							if(cType == Short.class)
								Array.set(tgt, i, Short.valueOf(Long.valueOf(vTag.value()).shortValue()));
							else
							if(cType == Byte.class)
								Array.set(tgt, i, Byte.valueOf(Long.valueOf(vTag.value()).byteValue()));
							else
							if(cType == Boolean.class)
								Array.set(tgt, i, Boolean.valueOf(vTag.value()));
							else
							if(cType.isPrimitive())
							{
								if(cType == boolean.class)
									Array.setBoolean(tgt, i, Boolean.valueOf(vTag.value()).booleanValue());
								else
								if(cType == int.class)
									Array.setInt(tgt, i, Long.valueOf(vTag.value()).intValue());
								else
								if(cType == short.class)
									Array.setShort(tgt, i, Long.valueOf(vTag.value()).shortValue());
								else
								if(cType == byte.class)
									Array.setByte(tgt, i, Long.valueOf(vTag.value()).byteValue());
								else
								if(cType == long.class)
									Array.setLong(tgt, i, Long.valueOf(vTag.value()).longValue());
								else
								if(cType == float.class)
									Array.setFloat(tgt, i, Double.valueOf(vTag.value()).floatValue());
								else
								if(cType == double.class)
									Array.setDouble(tgt, i, Double.valueOf(vTag.value()).doubleValue());
							}
							else
							{
								Object newObj = cType.newInstance();
								fromXMLtoPOJO(vTag.contents(), newObj);
								Array.set(tgt, i, newObj);
							}
						}
						field.set(o, tgt);
					}
					else
					if(field.getType() == String.class)
						field.set(o, valTag.value());
					else
					if(field.getType().isPrimitive())
					{
						final Class<?> cType=field.getType();
						if(cType == boolean.class)
							field.setBoolean(o, Boolean.valueOf(valTag.value()).booleanValue());
						else
						if(cType == int.class)
							field.setInt(o, Long.valueOf(valTag.value()).intValue());
						else
						if(cType == short.class)
							field.setShort(o, Long.valueOf(valTag.value()).shortValue());
						else
						if(cType == byte.class)
							field.setByte(o, Long.valueOf(valTag.value()).byteValue());
						else
						if(cType == long.class)
							field.setLong(o, Long.valueOf(valTag.value()).longValue());
						else
						if(cType == float.class)
							field.setFloat(o, Double.valueOf(valTag.value()).floatValue());
						else
						if(cType == double.class)
							field.setDouble(o, Double.valueOf(valTag.value()).doubleValue());
					}
					else
					if(field.getType() == Float.class)
						field.set(o, Float.valueOf(Double.valueOf(valTag.value()).floatValue()));
					else
					if(field.getType() == Double.class)
						field.set(o, Double.valueOf(valTag.value()));
					else
					if(field.getType() == Long.class)
						field.set(o, Long.valueOf(valTag.value()));
					else
					if(field.getType() == Integer.class)
						field.set(o, Integer.valueOf(Long.valueOf(valTag.value()).intValue()));
					else
					if(field.getType() == Short.class)
						field.set(o, Short.valueOf(Long.valueOf(valTag.value()).shortValue()));
					else
					if(field.getType() == Byte.class)
						field.set(o, Byte.valueOf(Long.valueOf(valTag.value()).byteValue()));
					else
					if(field.getType() == Boolean.class)
						field.set(o, Boolean.valueOf(valTag.value()));
					else
					{
						Object newObj = field.getType().newInstance();
						fromXMLtoPOJO(valTag.contents(), newObj);
						field.set(o, newObj);
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(e.getMessage(),e);
			}
			catch (IllegalAccessException e)
			{
				throw new IllegalArgumentException(e.getMessage(),e);
			}
			catch (InstantiationException e)
			{
				throw new IllegalArgumentException(e.getMessage(),e);
			}
		}
	}
}
