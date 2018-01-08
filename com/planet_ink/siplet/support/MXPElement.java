package com.planet_ink.siplet.support;

import java.util.*;

/*
   Copyright 2008-2018 Bo Zimmerman

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
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MXPElement implements Cloneable
{
	public static final int	BIT_OPEN				= 1;
	public static final int	BIT_COMMAND				= 2;
	public static final int	BIT_NEEDTEXT			= 4;
	public static final int	BIT_SPECIAL				= 8;
	public static final int	BIT_HTML				= 16;
	public static final int	BIT_NOTSUPPORTED		= 32;
	public static final int	BIT_EATTEXT				= 64;
	public static final int	BIT_DISABLED			= 128;

	private String			name					= "";
	private String			definition				= "";
	private String			attributes				= "";
	private String			flag					= "";
	private String			unsupportedParms		= "";
	private int				bitmap					= 0;
	private Vector<String>	parsedAttributes		= null;
	private List<String>	userParms				= new Vector<String>();
	private boolean			basicElement			= true;
	
	private Hashtable<String,String>		attributeValues			= null;
	private Hashtable<String,String>		alternativeAttributes	= null;

	private int				bufInsert				= -1;

	public MXPElement(String newName, String theDefinition, String theAttributes, String theFlag, int theBitmap)
	{
		super();
		name = newName;
		definition = theDefinition;
		attributes = theAttributes;
		flag = theFlag;
		bitmap = theBitmap;
		if ((!isCommand()) && (theDefinition.toUpperCase().indexOf("&TEXT;") >= 0))
			bitmap = bitmap | BIT_NEEDTEXT;
	}

	public MXPElement(String newName, String theDefinition, String theAttributes, String theFlag, int theBitmap, String unsupported)
	{
		super();
		name = newName;
		definition = theDefinition;
		attributes = theAttributes;
		flag = theFlag;
		bitmap = theBitmap;
		if ((!isCommand()) && (theDefinition.toUpperCase().indexOf("&TEXT;") >= 0))
			bitmap = bitmap | BIT_NEEDTEXT;
		unsupportedParms = unsupported;
	}

	public MXPElement copyOf()
	{
		try
		{
			final MXPElement E = (MXPElement) this.clone();
			if (E.parsedAttributes != null)
				E.parsedAttributes = (Vector) E.parsedAttributes.clone();
			if (E.attributeValues != null)
				E.attributeValues = (Hashtable) E.attributeValues.clone();
			if (E.alternativeAttributes != null)
				E.alternativeAttributes = (Hashtable) E.alternativeAttributes.clone();
			if (E.userParms != null)
				E.userParms = new Vector<String>(E.userParms);
			return E;
		}
		catch (final Exception e)
		{
		}
		return this;
	}

	public String name()
	{
		return name;
	}

	public void setName(String newName)
	{
		name = newName;
	}

	public boolean isCommand()
	{
		return Util.bset(bitmap, BIT_COMMAND);
	}

	public boolean isOpen()
	{
		return Util.bset(bitmap, BIT_OPEN);
	}

	public boolean isHTML()
	{
		return Util.bset(bitmap, BIT_HTML);
	}

	public boolean isSpecialProcessor()
	{
		return Util.bset(bitmap, BIT_SPECIAL);
	}

	public boolean isDisabled()
	{
		return Util.bset(bitmap, BIT_DISABLED);
	}

	public boolean isTextEater()
	{
		return Util.bset(bitmap, BIT_EATTEXT);
	}

	public String getDefinition()
	{
		return definition;
	}

	public void setDefinition(String defi)
	{
		definition = defi;
	}

	public String getAttributes()
	{
		return attributes;
	}

	public boolean needsText()
	{
		return Util.bset(bitmap, BIT_NEEDTEXT);
	}

	public void setNotBasicElement()
	{
		basicElement = false;
	}

	public boolean isBasicElement()
	{
		return basicElement;
	}

	public boolean isGenerallySupported()
	{
		return !Util.bset(bitmap, BIT_NOTSUPPORTED);
	}

	public void setBitmap(int newBitmap)
	{
		bitmap = newBitmap;
	}

	public int getBitmap()
	{
		return bitmap;
	}

	public Vector<String> getUnsupportedParms()
	{
		if ((unsupportedParms == null) || (unsupportedParms.trim().length() == 0))
			return new Vector<String>();
		return Util.parseSpaces(unsupportedParms, true);
	}

	public void setAttributes(String newAttributes)
	{
		attributes = newAttributes;
		parsedAttributes = null;
		attributeValues = null;
		alternativeAttributes = null;
	}

	public String getAttributeValue(String tag)
	{
		getParsedAttributes();
		tag = tag.toUpperCase().trim();
		if (attributeValues.containsKey(tag))
			return attributeValues.get(tag).toString();
		return null;
	}

	public void setAttributeValue(String tag, String value)
	{
		getParsedAttributes();
		attributeValues.remove(tag);
		if (value != null)
			attributeValues.put(tag, value);
	}

	public synchronized Vector<String> getParsedAttributes()
	{
		if (parsedAttributes != null)
			return parsedAttributes;
		parsedAttributes = new Vector<String>();
		attributeValues = new Hashtable<String,String>();
		alternativeAttributes = new Hashtable<String,String>();
		final StringBuffer buf = new StringBuffer(attributes.trim());
		StringBuffer bit = new StringBuffer("");
		char quotes = '\0';
		int i = -1;
		char lastC = ' ';
		boolean firstEqual = false;
		while ((++i) < buf.length())
		{
			switch (buf.charAt(i))
			{
			case '=':
				if ((!firstEqual) && (bit.length() > 0))
				{
					final String tag = bit.toString().toUpperCase().trim();
					bit = new StringBuffer("");
					parsedAttributes.addElement(tag);
					attributeValues.put(tag, bit.toString());
				}
				else
					bit.append(buf.charAt(i));
				firstEqual = true;
				break;
			case '\n':
			case '\r':
			case ' ':
			case '\t':
				if (quotes == '\0')
				{
					if ((!firstEqual) && (bit.length() > 0))
						parsedAttributes.addElement(bit.toString().toUpperCase().trim());
					bit = new StringBuffer("");
					firstEqual = false;
				}
				else
					bit.append(buf.charAt(i));
				break;
			case '"':
			case '\'':
				if (lastC == '\\')
					bit.append(buf.charAt(i));
				else 
				if ((lastC == '=') || (quotes != '\0') || ((quotes == '\0') && ((lastC == ' ') || (lastC == '\t'))))
				{
					if ((quotes != '\0') && (quotes == buf.charAt(i)))
					{
						quotes = '\0';
						if ((!firstEqual) && (bit.length() > 0))
							parsedAttributes.addElement(bit.toString().toUpperCase().trim());
						bit = new StringBuffer("");
						firstEqual = false;
					}
					else
					{
						if (quotes != '\0')
							bit.append(buf.charAt(i));
						else
							quotes = buf.charAt(i);
					}
				}
				else
					bit.append(buf.charAt(i));
				break;
			default:
				bit.append(buf.charAt(i));
				break;
			}
			lastC = buf.charAt(i);
		}
		if ((!firstEqual) && (bit.length() > 0))
			parsedAttributes.addElement(bit.toString().toUpperCase().trim());
		for (int p = parsedAttributes.size() - 1; p >= 0; p--)
		{
			final String PA = parsedAttributes.elementAt(p);
			final String VAL = attributeValues.get(PA);
			if ((VAL != null) && (parsedAttributes.contains(VAL.toString())))
			{
				parsedAttributes.removeElementAt(p);
				attributeValues.remove(PA);
				alternativeAttributes.put(PA, VAL.toString());
			}
		}
		return parsedAttributes;
	}

	public String getFlag()
	{
		return flag;
	}

	public List<String> getUserParms()
	{
		return userParms;
	}

	public void saveSettings(int insertPoint, Vector<String> theUserParms)
	{
		bufInsert = insertPoint;
		userParms = theUserParms;
	}

	public int getBufInsert()
	{
		return bufInsert;
	}

	public void deleteAttribute(String name)
	{
		getParsedAttributes();
		attributeValues.remove(name.toUpperCase().trim());
	}

	public Vector<String> getCloseTags(String desc)
	{
		final StringBuffer buf = new StringBuffer(desc);
		final Vector<String> tags = new Vector<String>();
		StringBuffer bit = null;
		char quotes = '\0';
		int i = -1;
		char lastC = ' ';
		while ((++i) < buf.length())
		{
			switch (buf.charAt(i))
			{
			case '<':
				if (quotes != '\0')
					bit = null;
				else if (bit != null)
				{
					if (MXP.tagDebug)
					{
						System.out.println("/TAG/CLOSER2S=" + Util.toStringList(tags));
						System.out.flush();
					}
					return tags;
				}
				else
					bit = new StringBuffer("");
				break;
			case '>':
				if ((quotes == '\0') && (bit != null) && (bit.toString().trim().length() > 0))
					tags.add(bit.toString().toUpperCase().trim());
				bit = null;
				break;
			case ' ':
			case '\t':
				if ((quotes == '\0') && (bit != null) && (bit.toString().trim().length() > 0))
					tags.add(bit.toString().toUpperCase().trim());
				bit = null;
				break;
			case '"':
			case '\'':
				if (lastC == '\\')
					bit = null;
				else if ((quotes != '\0') && (quotes == buf.charAt(i)))
					quotes = '\0';
				else if (quotes == '\0')
					quotes = buf.charAt(i);
				bit = null;
				break;
			default:
				if ((bit != null) && (Character.isLetterOrDigit(buf.charAt(i))))
					bit.append(buf.charAt(i));
				else
					bit = null;
				break;
			}
			lastC = buf.charAt(i);
		}
		if (MXP.tagDebug)
		{
			System.out.println("/TAG/CLOSERS=" + Util.toStringList(tags));
			System.out.flush();
		}
		return tags;
	}

	public String getFoldedDefinition(String text)
	{
		final Vector<String> aV = getParsedAttributes();
		attributeValues.remove("TEXT");
		attributeValues.put("TEXT", text);
		if ((userParms != null) && (userParms.size() > 0))
		{
			int position = -1;
			String avParm = null;
			String userParm = null;
			for (int u = 0; u < userParms.size(); u++)
			{
				userParm = userParms.get(u).toUpperCase().trim();
				int xx = userParm.indexOf('=');
				if ((xx > 0) && (alternativeAttributes.containsKey(userParm.substring(0, xx).trim())))
				{
					final String newKey = alternativeAttributes.get(userParm.substring(0, xx).trim());
					final String uu = userParms.get(u);
					xx = uu.indexOf('=');
					userParms.set(u, newKey + uu.substring(xx));
					userParm = userParms.get(u).toUpperCase().trim();
				}
				boolean found = false;
				if (userParm != null)
				{
					for (int a = 0; a < aV.size(); a++)
					{
						avParm = aV.elementAt(a);
						if ((userParm.startsWith(avParm + "=")) || (avParm.equals(userParm)))
						{
							found = true;
							if (a > position)
								position = a;
							attributeValues.remove(avParm);
							if (avParm != null)
								attributeValues.put(avParm, (userParm.equals(avParm)) ? "" : userParms.get(u).trim().substring(avParm.length() + 1));
							break;
						}
					}
				}
				if ((!found) && (position < (aV.size() - 1)))
				{
					position++;
					avParm = aV.elementAt(position);
					attributeValues.remove(avParm);
					attributeValues.put(avParm, userParms.get(u).trim());
				}
			}
		}
		return definition;
	}
}
