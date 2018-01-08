package com.planet_ink.siplet.support;

import java.applet.*;
import java.net.*;
import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class MXP
{
	public final static boolean	tagDebug			= false;
	public final static boolean	tagDebugLong		= false;
	public final static boolean	entityDebug			= false;

	private int					defaultMode			= 0;
	public static final int		MODE_LINE_OPEN		= 0;
	public static final int		MODE_LINE_SECURE	= 1;
	public static final int		MODE_LINE_LOCKED	= 2;
	public static final int		MODE_RESET			= 3;
	public static final int		MODE_TEMP_SECURE	= 4;
	public static final int		MODE_LOCK_OPEN		= 5;
	public static final int		MODE_LOCK_SECURE	= 6;
	public static final int		MODE_LOCK_LOCKED	= 7;
	public static final int		MODE_LINE_ROOMNAME	= 10;
	public static final int		MODE_LINE_ROOMDESC	= 11;
	public static final int		MODE_LINE_ROOMEXITS	= 12;
	public static final int		MODE_LINE_WELCOME	= 19;

	public String				lastForeground		= "WH";
	public String				lastBackground		= "WH";
	private boolean				eatTextUntilEOLN	= false;
	private boolean				eatNextEOLN			= false;
	private boolean				eatAllEOLN			= false;
	private int					mode				= 0;
	
	private final StringBuffer					responses		= new StringBuffer("");
	private final StringBuffer					jscriptBuffer	= new StringBuffer("");
	private final Hashtable<String, MXPElement>	elements		= new Hashtable<String,MXPElement>();
	private final Hashtable<Integer, MXPElement>tags			= new Hashtable<Integer, MXPElement>();
	private final Hashtable<String, MXPEntity>	entities		= new Hashtable<String, MXPEntity>();
	private final Vector<MXPElement>			openElements	= new Vector<MXPElement>();
	private final Vector<String[]>				gauges			= new Vector<String[]>();

	public MXP()
	{
		super();
		initMXP();
	}

	public void initMXP()
	{
		elements.clear();
		addElement(new MXPElement("B", "<B>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("BOLD", "<B>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("STRONG", "<B>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("U", "<U>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("UNDERLINE", "<U>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("I", "<I>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("ITALIC", "<I>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("S", "<S>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("STRIKEOUT", "<S>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("EM", "<I>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("H1", "<H1>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("H2", "<H2>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("H3", "<H3>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("H4", "<H4>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("H5", "<H5>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("H6", "<H6>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("HR", "<HR>", "", "", MXPElement.BIT_HTML | MXPElement.BIT_COMMAND));
		addElement(new MXPElement("SMALL", "<SMALL>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("TT", "<PRE>", "", "", MXPElement.BIT_HTML));
		addElement(new MXPElement("BR", "<BR>", "", "", MXPElement.BIT_HTML | MXPElement.BIT_COMMAND));
		addElement(new MXPElement("SBR", "&nbsp;", "", "", MXPElement.BIT_HTML | MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("P", "", "", "", MXPElement.BIT_HTML | MXPElement.BIT_SPECIAL)); // special
																									// done
		addElement(new MXPElement("C", "<FONT COLOR=&fore; BACK=&back;>", "FORE BACK", "", 0));
		addElement(new MXPElement("COLOR", "<FONT COLOR=&fore; BACK=&back;>", "FORE BACK", "", 0));
		addElement(new MXPElement("HIGH", "", "", "", MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("H", "", "", "", MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("FONT", "<FONT STYLE=\"color: &color;;background-color: &back;;font-family: &face;;font-size: &size;;\">", "FACE SIZE COLOR BACK STYLE", "", MXPElement.BIT_SPECIAL));
		addElement(new MXPElement("NOBR", "", "", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND)); // special
																											// done
		addElement(new MXPElement("A", "<A TARGET=ELSEWHERE STYLE=\"&lcc;\" ONMOUSEOVER=\"&onmouseover;\" ONCLICK=\"&onclick;\" HREF=\"&href;\" TITLE=\"&hint;\">",
				"HREF HINT EXPIRE TITLE=HINT STYLE ONMOUSEOUT ONMOUSEOVER ONCLICK", "", 0, "EXPIRE"));
		addElement(new MXPElement("SEND", "<A STYLE=\"&lcc;\" HREF=\"&href;\" ONMOUSEOUT=\"delayhidemenu();\" ONCLICK=\"&onclick;\" TITLE=\"&hint;\">", "HREF HINT PROMPT EXPIRE STYLE", "", MXPElement.BIT_SPECIAL,
				"EXPIRE")); // special done
		addElement(new MXPElement("EXPIRE", "", "NAME", "", MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("VERSION", "", "", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND)); // special
																											// done
		addElement(new MXPElement("SUPPORT", "", "", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND)); // special
																											// done
		addElement(new MXPElement("GAUGE", "", "ENTITY MAX CAPTION COLOR", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND));
		addElement(new MXPElement("STAT", "", "ENTITY MAX CAPTION", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("FRAME", "", "NAME ACTION TITLE INTERNAL ALIGN LEFT TOP WIDTH HEIGHT SCROLLING FLOATING", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("DEST", "", "NAME", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("DESTINATION", "", "NAME", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("RELOCATE", "", "URL PORT", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("USER", "", "", "", MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("PASSWORD", "", "", "", MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("IMAGE", "<IMG SRC=&url;&fname; HEIGHT=&h; WIDTH=&w; ALIGN=&align;>", "FNAME URL T H W HSPACE VSPACE ALIGN ISMAP", "", MXPElement.BIT_COMMAND, "HSPACE VSPACE ISMAP"));
		addElement(new MXPElement("IMG", "<IMG SRC=&src; HEIGHT=&height; WIDTH=&width; ALIGN=&align;>", "SRC HEIGHT=70 WIDTH=70 ALIGN", "", MXPElement.BIT_COMMAND));
		addElement(new MXPElement("FILTER", "", "SRC DEST NAME", "", MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("SCRIPT", "", "", "", MXPElement.BIT_COMMAND | MXPElement.BIT_NOTSUPPORTED));
		addElement(new MXPElement("ENTITY", "", "NAME VALUE DESC PRIVATE PUBLISH DELETE ADD", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND, "PRIVATE PUBLISH ADD")); // special
																																											// done
		addElement(new MXPElement("EN", "", "NAME VALUE DESC PRIVATE PUBLISH DELETE ADD", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND, "PRIVATE PUBLISH ADD")); // special
																																										// done
		addElement(new MXPElement("TAG", "", "INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND, "WINDOWNAME"));
		addElement(new MXPElement("VAR", "", "NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE", "", MXPElement.BIT_SPECIAL, "PRIVATE PUBLISH ADD REMOVE")); // special
																																						// done
		addElement(new MXPElement("V", "", "NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE", "", MXPElement.BIT_SPECIAL, "PRIVATE PUBLISH ADD REMOVE")); // special
																																						// done
		addElement(new MXPElement("ELEMENT", "", "NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND)); // special
																																							// done
		addElement(new MXPElement("EL", "", "NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND)); // special
																																						// done
		addElement(new MXPElement("ATTLIST", "", "NAME ATT", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND));
		addElement(new MXPElement("AT", "", "NAME ATT", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND));
		addElement(new MXPElement("SOUND", "!!SOUND(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)", "FNAME V=100 L=1 P=50 T U", "", MXPElement.BIT_COMMAND));
		addElement(new MXPElement("MUSIC", "!!MUSIC(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)", "FNAME V=100 L=1 P=50 T U", "", MXPElement.BIT_COMMAND));
		// -------------------------------------------------------------------------
		entities.clear();
		entities.put("nbsp", new MXPEntity("nbsp", "&nbsp;"));
		entities.put("lt", new MXPEntity("lt", "&lt;"));
		entities.put("gt", new MXPEntity("gt", "&gt;"));
		entities.put("quot", new MXPEntity("quot", "&quot;"));
		entities.put("amp", new MXPEntity("amp", "&amp;"));
	}

	public void addElement(MXPElement E)
	{
		elements.put(E.name(), E);
	}

	public String getAnyResponses()
	{
		synchronized (responses)
		{
			if (responses.length() == 0)
				return "";
			final String s = responses.toString();
			responses.setLength(0);
			return s;
		}
	}

	public String getAnyJScript()
	{
		synchronized (jscriptBuffer)
		{
			if (jscriptBuffer.length() == 0)
				return "";
			final String s = jscriptBuffer.toString();
			jscriptBuffer.setLength(0);
			return s;
		}
	}

	private int mode()
	{
		return mode;
	}

	private void setMode(int newMode)
	{
		mode = newMode;
	}

	private int setModeAndExecute(int newMode, StringBuffer buf, int i)
	{
		setMode(newMode);
		return executeMode(buf, i);
	}

	private int executeMode(StringBuffer buf, int i)
	{
		switch (mode())
		{
		case MODE_RESET:
			defaultMode = MODE_LINE_OPEN;
			mode = defaultMode;
			return closeAllTags(buf, i);
		case MODE_LOCK_OPEN:
		case MODE_LOCK_SECURE:
		case MODE_LOCK_LOCKED:
			defaultMode = mode;
			break;
		}
		return 0;
	}

	public int newlineDetected(StringBuffer buf, int i, boolean[] eatEOL)
	{
		if ((mode() == MXP.MODE_LINE_LOCKED) || (mode() == MXP.MODE_LOCK_LOCKED))
		{
			eatEOL[0] = false;
			return 0;
		}
		eatEOL[0] = eatNextEOLN;
		eatNextEOLN = eatAllEOLN;
		if (eatTextUntilEOLN)
		{
			eatTextUntilEOLN = false;
			eatEOL[0] = true;
		}
		switch (mode())
		{
		case MODE_LINE_OPEN:
		{
			final int ret = closeAllTags(buf, i);
			setModeAndExecute(defaultMode, buf, i);
			return ret;
		}
		case MODE_LINE_SECURE:
		case MODE_LINE_LOCKED:
		case MODE_TEMP_SECURE:
		{
			final int ret = closeAllTags(buf, i);
			setModeAndExecute(defaultMode, buf, i);
			return ret;
		}
		}
		return 0;
	}

	// does not close Secure tags -- they are never ever closed
	private int closeAllTags(StringBuffer buf, int i)
	{
		MXPElement E = null;
		for (int x = openElements.size() - 1; x >= 0; x--)
		{
			E = openElements.elementAt(x);
			if (E.isOpen())
			{
				final String close = closeTag(E);
				if (close.length() > 0)
					buf.insert(i, close + ">");
				openElements.removeElementAt(x);
			}
		}
		return 0;
	}

	public boolean isUIonHold()
	{
		if ((mode() == MXP.MODE_LINE_LOCKED) || (mode() == MXP.MODE_LOCK_LOCKED))
			return false;
		MXPElement E = null;
		for (int i = 0; i < openElements.size(); i++)
		{
			E = openElements.elementAt(i);
			if (E.needsText())
				return true;
		}
		return false;
	}

	private String closeTag(MXPElement E)
	{
		final Vector<String> endTags = E.getCloseTags(E.getDefinition());
		final StringBuffer newEnd = new StringBuffer("");
		for (int e = endTags.size() - 1; e >= 0; e--)
		{
			if (elements.containsKey((endTags.elementAt(e)).toUpperCase().trim()))
				newEnd.append("</" + endTags.elementAt(e).toUpperCase().trim());
		}
		return newEnd.toString();
	}

	public int escapeTranslate(String escapeString, StringBuffer buf, int i)
	{
		if (escapeString.endsWith("z") || escapeString.endsWith("Z"))
		{
			buf.delete(i, i + escapeString.length() + 2);
			final int code = Util.s0_int(escapeString.substring(0, escapeString.length() - 1));
			if (code < 20)
			{
				setModeAndExecute(code, buf, i);
				return -1;
			}
			else 
			if (code < 100)
			{
				final MXPElement replace = tags.get(Integer.valueOf(code));
				if ((replace != null) && (!replace.isDisabled()))
				{
					buf.insert(i, replace.getFoldedDefinition(""));
					if (replace.isTextEater())
						eatTextUntilEOLN = true;
				}
			}
			return -1;
		}
		return escapeString.length();
	}

	public boolean eatTextUntilNextEOLN()
	{
		return eatTextUntilEOLN;
	}

	private void processAnyEntities(StringBuffer buf, MXPElement currentElement)
	{
		int i = 0;
		while (i < buf.length())
		{
			switch (buf.charAt(i))
			{
			case '&':
			{
				final int x = processEntity(buf, i, currentElement, false);
				if (x == Integer.MAX_VALUE)
					return;
				i += x;
				break;
			}
			}
			i++;
		}
	}

	private String substr(String buf, int start, int end)
	{
		return substr(new StringBuffer(buf), start, end);
	}

	private String substr(StringBuffer buf, int start, int end)
	{
		if (start < 0)
			return "?";
		if (end < start)
			end = start + 80;
		if (end > buf.length())
			end = buf.length();
		String s = buf.substring(start, end);
		s = Util.replaceAll(s, "\n", "\\n");
		s = Util.replaceAll(s, "\r", "\\r");
		return s;
	}

	public int processTag(StringBuffer buf, int i)
	{
		if ((mode() == MXP.MODE_LINE_LOCKED) || (mode() == MXP.MODE_LOCK_LOCKED))
		{
			buf.setCharAt(i, '&');
			buf.insert(i + 1, "lt;");
			return 3;
		}

		// first step is to parse the motherfather
		// if we can't parse it, we convert the < char at i into &lt;
		// remember, incomplete tags should nodify the main filterdude
		final Vector<String> parts = new Vector<String>();
		int oldI = i;
		final int oldOldI = i;
		char lastC = ' ';
		char quotes = '\0';
		StringBuffer bit = new StringBuffer("");

		// allowing the ! and / as a second char in a tag is an EXCEPTION!
		if (((i + 1) < buf.length()) && ((buf.charAt(i + 1) == '!') || (buf.charAt(i + 1) == '/')))
		{
			i++;
			bit.append(buf.charAt(i));
		}
		if (tagDebug)
		{
			System.out.println("/***:::!!!TAG/" + oldI + "/" + substr(buf, oldI, oldI + 10));
			System.out.flush();
		}
		if (tagDebugLong)
		{
			System.out.println("/TAG>" + substr(buf, 0, buf.length()));
			System.out.flush();
		}
		while ((bit != null) && ((++i) < buf.length()))
		{
			switch (buf.charAt(i))
			{
			case '\n':
			case '\r':
				buf.setCharAt(oldI, '&');
				buf.insert(oldI + 1, "lt;");
				if (tagDebug)
				{
					System.out.println("/TAG*/****/Tag has CR!!!!");
					System.out.flush();
				}
				return 3;
			case ' ':
			case '\t':
				if (quotes == '\0')
				{
					if (bit.length() > 0)
						parts.addElement(bit.toString());
					bit.setLength(0);
				}
				else
					bit.append(buf.charAt(i));
				break;
			case '"':
			case '\'':
				if (lastC == '\\')
				{
					if ((quotes != '\0') || (bit.length() > 0))
						bit.append(buf.charAt(i));
					else
					{
						// DANGER WILL ROBINSON! DANGER!
						buf.setCharAt(oldI, '&');
						buf.insert(oldI + 1, "lt;");
						if (tagDebug)
						{
							System.out.println("/TAG*/****/Tag has wierd quote!!!!");
							System.out.flush();
						}
						return 3;
					}
				}
				else 
				if ((lastC == '=') || (quotes != '\0') || ((quotes == '\0') && ((lastC == ' ') || (lastC == '\t'))))
				{
					if ((quotes != '\0') && (quotes == buf.charAt(i)))
					{
						quotes = '\0';
						parts.addElement(bit.toString());
						bit.setLength(0);
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
			case '<':
				if (quotes != '\0')
					bit.append(buf.charAt(i));
				else
				{
					// argh! abort! abort!
					buf.setCharAt(oldI, '&');
					buf.insert(oldI + 1, "lt;");
					if (tagDebug)
					{
						System.out.println("/TAG*/****/Tag has REOPEN!!!!");
						System.out.flush();
					}
					return 3;
				}
				break;
			case '>':
				if (quotes != '\0')
					bit.append(buf.charAt(i));
				else
				{
					if (bit.length() > 0)
						parts.add(bit.toString());
					bit = null;
				}
				break;
			default:
				if ((quotes != '\0') || (Character.isLetter(buf.charAt(i))) || (bit.length() > 0))
					bit.append(buf.charAt(i));
				else
				{
					// DANGER WILL ROBINSON! DANGER!
					buf.setCharAt(oldI, '&');
					buf.insert(oldI + 1, "lt;");
					if (tagDebug)
					{
						System.out.println("/TAG*/****/Char is illegal!!!");
						System.out.flush();
					}
					return 3;
				}
				break;
			}
			lastC = buf.charAt(i);
		}
		// never hit the end, so let papa know
		if ((i >= buf.length()) || (buf.charAt(i) != '>'))
		{
			if (tagDebug)
			{
				System.out.println("/TAG*/****/Tag is unclosed!!!!");
				System.out.flush();
			}
			return Integer.MAX_VALUE;
		}
		final int endI = i + 1;

		// nothing doin
		String tag = (parts.size() > 0) ? parts.firstElement().toUpperCase().trim() : "";
		final String oldString = buf.substring(oldI, endI);
		if (tag.startsWith("!"))
			tag = tag.substring(1);
		final boolean endTag = tag.startsWith("/");
		if (endTag)
			tag = tag.substring(1);
		tag = tag.toUpperCase().trim();
		if ((tag.length() == 0) || (!elements.containsKey(tag)))
		{
			buf.setCharAt(oldI, '&');
			buf.insert(oldI + 1, "lt;");
			if (tagDebug)
			{
				System.out.println("/TAG*/****/Tag is unknown!!!!");
				System.out.flush();
			}
			return 3;
		}
		MXPElement E = elements.get(tag);
		String text = "";
		if (endTag)
		{
			MXPElement troubleE = null;
			int foundAt = -1;
			for (int x = openElements.size() - 1; x >= 0; x--)
			{
				E = openElements.elementAt(x);
				if (E.name().equals(tag))
				{
					foundAt = x;
					openElements.removeElementAt(x);
					break;
				}
				if (E.needsText())
					troubleE = E;
			}
			// as of this moment, we need the telnet parser
			// to back up one character so continue.
			buf.delete(oldI, endI);
			if (foundAt < 0)
			{
				if (tagDebug)
				{
					System.out.println("/TAG*/****/Closed tag never opened!!!");
					System.out.flush();
				}
				return -1;
			}
			// a close tag of an mxp element always erases an
			// **INTERIOR** needstext element
			if (troubleE != null)
				openElements.removeElement(troubleE);
			final String close = closeTag(E);
			if (tagDebug)
			{
				System.out.println("/TAG/ENDENTITY=" + E.name() + "/CLOSE=" + close);
				System.out.flush();
			}
			if (close.length() > 0)
				buf.insert(oldI, close + ">");
			if (tagDebug)
			{
				if (!E.needsText())
				{
					if (tagDebugLong)
						System.out.println("/TAG>" + substr(buf, 0, buf.length()));
					System.out.println("/TAG/END/!2!/" + substr(buf, oldI + 1 + close.length(), oldI + 40));
					System.out.flush();
				}
			}
			if (E.needsText())
			{
				if ((E.getBufInsert() < 0) || (oldI < E.getBufInsert()))
				{
					// wish i could log
				}
				else
				{
					text = buf.substring(E.getBufInsert(), oldI);
					text = Util.stripBadHTMLTags(Util.replaceAll(text, "&nbsp;", " "));
					oldI = E.getBufInsert();
					if (tagDebug)
					{
						System.out.println("/TAG/END/text=" + substr(text, 0, 100));
						System.out.flush();
					}
				}
			}
			else 
			if (E.isHTML())
			{
				if (E.isSpecialProcessor())
					specialProcessorElements(E, true);
				if (tagDebug)
				{
					System.out.println("/TAG*/END/3/adji=!2.5!/" + (close.length() - 1));
					System.out.flush();
				}
				return close.length();
			}
			else 
			if (close.equals("</" + E.name()))
			{
				if (tagDebug)
				{
					System.out.println("/TAG*/END/4/adji=!2.5!/" + (close.length() - 1));
					System.out.flush();
				}
				return close.length();
			}
			else 
			if (E.getBufInsert() < oldI)
			{
				if (tagDebug)
				{
					System.out.println("/TAG/END/5/adji=" + (-((oldI - E.getBufInsert()) + 1)));
					System.out.flush();
				}
				if (tagDebug)
				{
					System.out.println("/TAG*/END/6/willbe=" + substr(buf, oldI - ((oldI - E.getBufInsert())), oldI + 80));
					System.out.flush();
				}
				return -((oldI - E.getBufInsert()) + 1);
			}
			else
			{
				if (tagDebug)
				{
					System.out.println("/TAG*/END/6/adji=!2.5!/" + (close.length() - 1));
					System.out.flush();
				}
				return close.length();
			}
		}
		else
		{
			E = E.copyOf();
			parts.removeElementAt(0); // because the TAG itself is 0
			E.saveSettings(oldI, parts);
			if (!E.isCommand())
				openElements.addElement(E);
			buf.delete(oldI, endI);
			if (tagDebug)
			{
				System.out.println("/TAG/ENTITY=" + E.name());
				System.out.flush();
			}
			if (E.needsText())
			{
				if (tagDebugLong)
				{
					System.out.println("/TAG>" + substr(buf, 0, buf.length()));
					System.out.flush();
				}
				if (tagDebug)
				{
					System.out.println("/TAG*/Entity needs text, so purge and look for close.");
					System.out.flush();
				}
				return -1; // we want it to continue to look for closing tag
			}
		}
		final String totalDefinition = E.getFoldedDefinition(text);
		if ((endTag) && (!E.isCommand()) && (E.getFlag() != null) && (E.getFlag().length() > 0))
		{
			String f = E.getFlag().trim();
			if (f.toUpperCase().startsWith("SET "))
				f = f.substring(4).trim();
			modifyEntity(f, text);
		}

		if (E.isSpecialProcessor())
			specialProcessorElements(E, endTag);
		if ((E.isHTML()) || (totalDefinition.equalsIgnoreCase(oldString)))
		{
			buf.insert(oldI, totalDefinition);
			if (tagDebugLong)
			{
				System.out.println("/TAG>" + substr(buf, 0, buf.length()));
				System.out.flush();
			}
			if ((endTag) && (oldI < oldOldI))
			{
				if (tagDebug)
				{
					System.out.println("/TAG*/ENDEND1/" + substr(buf, (oldOldI + 1 + (-((oldOldI - oldI) + 1))), oldOldI + 80));
					System.out.flush();
				}
				return -((oldOldI - oldI) + 1);
			}
			if (tagDebug)
			{
				System.out.println("/TAG*/THEEND1/" + substr(buf, (oldOldI + (totalDefinition.length() - 1)) + 1, oldOldI + 80));
				System.out.flush();
			}
			return totalDefinition.length() - 1;
		}
		final StringBuffer def = new StringBuffer(totalDefinition);
		processAnyEntities(def, E);
		buf.insert(oldI, def.toString());
		if (tagDebugLong)
		{
			System.out.println("/TAG/" + substr(buf, 0, buf.length()));
			System.out.flush();
		}
		if ((endTag) && (oldI < oldOldI))
		{
			if (tagDebug)
			{
				System.out.println("/TAG*/ENDEND2/" + oldI + "/" + oldOldI + "/" + substr(buf, oldOldI + 1 + (-((oldOldI - oldI) + 1)), oldOldI + 80));
				System.out.flush();
			}
			return -((oldOldI - oldI) + 1);
		}
		if ((def.toString().equalsIgnoreCase(oldString)) || (E.name().toUpperCase().trim().equals(getFirstTag(def.toString().trim()))))
		{
			if (tagDebug)
			{
				System.out.println("/TAG*/THEEND2/" + substr(buf, oldOldI + 1 + (def.toString().length() - 1), oldOldI + 80));
				System.out.flush();
			}
			return def.toString().length() - 1;
		}
		if (tagDebug)
		{
			System.out.println("/TAG*/THEEND3/" + substr(buf, (oldOldI - 1) + 1, oldOldI + 80));
			System.out.flush();
		}
		return -1;
	}

	public String getFirstTag(String s)
	{
		if (!s.startsWith("<"))
			return "";
		int x = s.indexOf(' ');
		if (x < 0)
			x = s.indexOf('>');
		if (x < 0)
			return "";
		return s.substring(1, x).toUpperCase().trim();
	}

	private void specialProcessorElements(MXPElement E, boolean endTag)
	{
		if (E.name().equals("FONT"))
		{
			String style = E.getAttributeValue("STYLE");
			final String color = E.getAttributeValue("COLOR");
			final String back = E.getAttributeValue("BACK");
			final String face = E.getAttributeValue("FACE");
			final String size = E.getAttributeValue("SIZE");
			if ((style != null) && (color == null) && (back == null) && (face == null) && (size == null))
			{
				String s = null;
				String v = null;
				while (style.length() > 0)
				{
					final int x = style.indexOf(';');
					if (x >= 0)
					{
						s = style.substring(0, x).trim();
						style = style.substring(x + 1).trim();
					}
					else
					{
						s = style.trim();
						style = "";
					}
					final int y = s.indexOf(':');
					if (y >= 0)
					{
						v = s.substring(y + 1);
						s = s.substring(0, y);
						if (s.equalsIgnoreCase("color"))
							E.setAttributeValue("COLOR", v);
						else 
						if (s.equalsIgnoreCase("background-color"))
							E.setAttributeValue("BACK", v);
						else 
						if (s.equalsIgnoreCase("font-size"))
							E.setAttributeValue("SIZE", v);
						else 
						if (s.equalsIgnoreCase("font-family"))
							E.setAttributeValue("FACE", v);
					}
				}
				E.setAttributeValue("STYLE", null);
			}
		}
		else 
		if (E.name().equals("NOBR"))
			eatNextEOLN = true;
		else 
		if (E.name().equals("P"))
		{
			if (endTag)
			{
				eatAllEOLN = false;
				eatNextEOLN = false;
			}
			else
			{
				eatAllEOLN = true;
				eatNextEOLN = true;
			}
		}
		else 
		if (E.name().equals("SEND"))
		{
			String prompt = E.getAttributeValue("PROMPT");
			if ((prompt != null) && (prompt.length() > 0))
				return;
			if (prompt == null)
				prompt = "false";
			else
				prompt = "true";
			E.setAttributeValue("PROMPT", prompt);
			String href = E.getAttributeValue("HREF");
			String hint = E.getAttributeValue("HINT");
			if ((href == null) || (href.trim().length() == 0))
				href = "if(window.alert) alert('Nothing done.');";
			if ((hint == null) || (hint.trim().length() == 0))
				hint = "Click here!";
			hint = Util.replaceAllIgnoreCase(hint, "RIGHT-CLICK", "click");
			hint = Util.replaceAllIgnoreCase(hint, "RIGHT-MOUSE", "click mouse");
			E.setAttributeValue("ONCLICK", "");
			E.setAttributeValue("HREF", "");
			E.setAttributeValue("HINT", "");
			final Vector<String> hrefV = Util.parsePipes(href, true);
			final Vector<String> hintV = Util.parsePipes(hint, true);
			if (hrefV.size() == 1)
			{
				href = Util.replaceAll( hrefV.firstElement(), "'", "\\'");
				E.setAttributeValue("HREF", "javascript:addToPrompt('" + href + "'," + prompt + ")");
				if (hintV.size() > 1)
					hint = hintV.firstElement();
				E.setAttributeValue("HINT", hint);
			}
			else 
			if (hintV.size() > hrefV.size())
			{
				E.setAttributeValue("HINT", hintV.firstElement());
				hintV.removeElementAt(0);
				E.setAttributeValue("HREF", "javascript:goDefault(0);");
				final StringBuffer newHint = new StringBuffer("");
				for (int i = 0; i < hintV.size(); i++)
				{
					newHint.append(hintV.elementAt(i));
					if (i < (hintV.size() - 1))
						newHint.append("|");
				}
				href = Util.replaceAll(href, "'", "\\'");
				hint = Util.replaceAll(newHint.toString(), "'", "\\'");
				E.setAttributeValue("ONCLICK", "return dropdownmenu(this, event, getSendMenu(this,'" + href + "','" + hint + "','" + prompt + "'), '200px');");
			}
			else
			{
				E.setAttributeValue("HINT", "Click to open menu");
				E.setAttributeValue("HREF", "javascript:goDefault(0);");
				href = Util.replaceAll(href, "'", "\\'");
				hint = Util.replaceAll(hint, "'", "\\'");
				E.setAttributeValue("ONCLICK", "return dropdownmenu(this, event, getSendMenu(this,'" + href + "','" + hint + "','" + prompt + "'), '200px');");
			}
		}
		else 
		if (E.name().equals("ELEMENT") || E.name().equals("EL"))
		{
			final String name = E.getAttributeValue("NAME");
			String definition = E.getAttributeValue("DEFINITION");
			String attributes = E.getAttributeValue("ATT");
			final String tag = E.getAttributeValue("TAG");
			final String flags = E.getAttributeValue("FLAG");
			final String OPEN = E.getAttributeValue("OPEN");
			final String DELETE = E.getAttributeValue("DELETE");
			final String EMPTY = E.getAttributeValue("EMPTY");
			if (name == null)
				return;
			if ((DELETE != null) && (elements.containsKey(name)))
			{
				E = elements.get(name);
				if (E.isOpen())
					elements.remove(name);
				return;
			}
			if (definition == null)
				definition = "";
			if (attributes == null)
				attributes = "";
			int bitmap = 0;
			if (OPEN != null)
				bitmap |= MXPElement.BIT_OPEN;
			if (EMPTY != null)
				bitmap |= MXPElement.BIT_COMMAND;
			final MXPElement L = new MXPElement(name.toUpperCase().trim(), definition, attributes, flags, bitmap);
			L.setNotBasicElement();
			elements.remove(L.name());
			elements.put(L.name(), L);
			if ((tag != null) && (Util.isInteger(tag)) && (Util.s_int(tag) > 19) && (Util.s_int(tag) < 100))
			{
				final int tagNum = Util.s_int(tag);
				if (tags.containsKey(Integer.valueOf(tagNum)))
					tags.remove(Integer.valueOf(tagNum));
				tags.put(Integer.valueOf(tagNum), L);
			}
			return;
		}
		else 
		if (E.name().equals("ENTITY") || E.name().equals("EN"))
		{
			final String name = E.getAttributeValue("NAME");
			final String value = E.getAttributeValue("VALUE");
			// String desc=E.getAttributeValue("DESC");
			// String PRIVATE=E.getAttributeValue("PRIVATE");
			// String PUBLISH=E.getAttributeValue("PUBLISH");
			final String DELETE = E.getAttributeValue("DELETE");
			final String REMOVE = E.getAttributeValue("REMOVE");
			final String ADD = E.getAttributeValue("ADD");
			if ((name == null) || (name.length() == 0))
				return;
			if (DELETE != null)
			{
				entities.remove(name);
				return;
			}
			if (REMOVE != null)
			{
				// whatever a string list is (| separated things) this removes
				// it
			}
			else 
			if (ADD != null)
			{
				// whatever a string list is (| separated things) this removes
				// it
			}
			else
				modifyEntity(name, value);
			return;
		}
		else 
		if ((E.name().equals("VAR") || E.name().equals("V")) && (endTag))
		{
			final String name = E.getAttributeValue("NAME");
			// String PRIVATE=E.getAttributeValue("PRIVATE");
			// String PUBLISH=E.getAttributeValue("PUBLISH");
			final String DELETE = E.getAttributeValue("DELETE");
			final String REMOVE = E.getAttributeValue("REMOVE");
			String VALUE = E.getAttributeValue("TEXT");
			if (VALUE == null)
				VALUE = "";
			final String ADD = E.getAttributeValue("ADD");
			if ((name == null) || (name.length() == 0))
				return;
			if (DELETE != null)
			{
				entities.remove(name);
				return;
			}
			if (REMOVE != null)
			{
				// whatever a string list is (| separated things) this removes
				// it
			}
			else 
			if (ADD != null)
			{
				// whatever a string list is (| separated things) this removes
				// it
			}
			else
				modifyEntity(name, VALUE);
			return;
		}
		else 
		if (E.name().equalsIgnoreCase("VERSION"))
			responses.append("\033[1z<VERSION MXP=1.0 STYLE=1.0 CLIENT=Siplet VERSION=" + TelnetFilter.getSipletVersion() + " REGISTERED=NO>\n");
		else 
		if (E.name().equalsIgnoreCase("GAUGE"))
		{
			String ENTITY = E.getAttributeValue("ENTITY");
			String MAX = E.getAttributeValue("MAX");
			if ((ENTITY == null) || (MAX == null))
				return;
			ENTITY = ENTITY.toLowerCase();
			MAX = MAX.toLowerCase();
			String CAPTION = E.getAttributeValue("CAPTION");
			if (CAPTION == null)
				CAPTION = "";
			String COLOR = E.getAttributeValue("COLOR");
			if (COLOR == null)
				COLOR = "WHITE";
			final String initEntity = getEntityValue(ENTITY, null);
			int initValue = 0;
			if ((initEntity != null) && (Util.isInteger(initEntity)))
				initValue = Util.s_int(initEntity);
			final String maxEntity = getEntityValue(MAX, null);
			int maxValue = 100;
			if ((maxEntity != null) && (Util.isInteger(maxEntity)))
				maxValue = Util.s_int(maxEntity);
			if (maxValue < initValue)
				maxValue = (initValue <= 0) ? 100 : initValue;
			if (initValue > 0)
				initValue = (int) Math.round(Util.mul(100.0, initValue / maxValue));
			synchronized (jscriptBuffer)
			{
				jscriptBuffer.append("createGauge('" + ENTITY + "','" + CAPTION + "','" + COLOR + "'," + initValue + "," + maxValue + ");");
				final String[] gauge = new String[2];
				gauge[0] = ENTITY;
				gauge[1] = MAX;
				gauges.addElement(gauge);
			}
		}
		else 
		if (E.name().equalsIgnoreCase("ATTLIST") || E.name().equalsIgnoreCase("ATT"))
		{
			final String name = E.getAttributeValue("NAME");
			final String value = E.getAttributeValue("ATT");
			if ((name == null) || (value == null))
				return;
			final MXPElement E2 = elements.get(name.toUpperCase().trim());
			if (E2 == null)
				return;
			E2.setAttributes(value);
		}
		else 
		if (E.name().equalsIgnoreCase("SUPPORT"))
		{
			final StringBuffer supportResponse = new StringBuffer("");
			final List<String> V = E.getUserParms();
			if ((V == null) || (V.size() == 0))
			{
				for (final Enumeration<MXPElement> e = elements.elements(); e.hasMoreElements();)
				{
					final MXPElement E2 = e.nextElement();
					if (!E2.isBasicElement())
						continue;
					final Vector<String> unsupportedParms = E2.getUnsupportedParms();
					if (!E2.isGenerallySupported())
						supportResponse.append(" -" + E2.name());
					else
					{
						supportResponse.append(" +" + E2.name());
						if (unsupportedParms.size() > 0)
						{
							for (int x = 0; x < unsupportedParms.size(); x++)
								supportResponse.append(" -" + E2.name() + "." + (unsupportedParms.elementAt(x)));
						}
					}
				}
			}
			else
				for (int v = 0; v < V.size(); v++)
				{
					String request = V.get(v).trim().toUpperCase();
					if (request.startsWith("\""))
						request = request.substring(1).trim();
					if (request.endsWith("\""))
						request = request.substring(0, request.length() - 1).trim();
					if (request.startsWith("\'"))
						request = request.substring(1).trim();
					if (request.endsWith("\'"))
						request = request.substring(0, request.length() - 1).trim();
					final int x = request.indexOf('.');
					String tag = request;
					String parm = "";
					if (x > 0)
					{
						tag = request.substring(0, x).trim();
						parm = request.substring(x + 1).trim();
					}
					final MXPElement RE = elements.get(tag);
					if ((RE == null) || (!RE.isGenerallySupported()))
					{
						if ((parm.length() > 0) && (!parm.equals("*")))
							supportResponse.append(" -" + tag + "." + parm);
						else
							supportResponse.append(" -" + tag);
						continue;
					}
					if (parm.length() == 0)
					{
						supportResponse.append(" +" + tag);
						continue;
					}
					final Vector<String> unsupportedParms = RE.getUnsupportedParms();
					final Vector<String> allAttributes = RE.getParsedAttributes();
					if (parm.equals("*"))
					{
						for (int a = 0; a < allAttributes.size(); a++)
						{
							final String att = allAttributes.elementAt(a);
							if (!unsupportedParms.contains(att))
								supportResponse.append(" +" + tag + "." + att);
						}
						continue;
					}
					if ((unsupportedParms.contains(parm)) || (!allAttributes.contains(parm)))
						supportResponse.append(" -" + tag + "." + parm);
					else
						supportResponse.append(" +" + tag + "." + parm);
				}
			responses.append("\033[1z<SUPPORTS" + supportResponse.toString() + ">\n");
		}
		else 
		if (E.name().equals("TAG"))
		{
			addElement(new MXPElement("TAG", "", "INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE", "", MXPElement.BIT_SPECIAL | MXPElement.BIT_COMMAND));
			// String window=E.getAttributeValue("WINDOWNAME");
			final String index = E.getAttributeValue("INDEX");
			if (!Util.isNumber(index))
				return;
			final int number = Util.s_int(index);
			if ((number < 20) || (number > 99))
				return;
			String foreColor = E.getAttributeValue("FORE");
			if (foreColor == null)
				foreColor = "";
			String backColor = E.getAttributeValue("BACK");
			if (backColor == null)
				backColor = "";
			final String gag = E.getAttributeValue("GAG");
			final String enable = E.getAttributeValue("ENABLE");
			final String disable = E.getAttributeValue("DISABLE");
			final StringBuffer parms = new StringBuffer("");
			if ((foreColor.length() > 0) || (backColor.length() > 0))
			{
				parms.append("<FONT ");
				if (foreColor.length() > 0)
					parms.append(" COLOR=" + foreColor);
				if (backColor.length() > 0)
					parms.append(" BACK=" + backColor);
				parms.append(">");
			}
			final MXPElement L = tags.get(Integer.valueOf(number));
			if (L == null)
				return;
			int newBitmap = L.getBitmap();
			if (gag != null)
				newBitmap |= MXPElement.BIT_EATTEXT;
			else 
			if (disable != null)
				newBitmap |= MXPElement.BIT_DISABLED;
			else 
			if (L.isDisabled() && (enable != null))
				newBitmap -= MXPElement.BIT_DISABLED;
			L.setBitmap(newBitmap);
			if (parms.length() > 0)
			{
				final String definition = Util.stripBadHTMLTags(L.getDefinition());
				L.setDefinition(definition + parms.toString());
			}
			return;
		}
	}

	public String getEntityValue(String tag, MXPElement currentE)
	{
		String val = null;
		if (tag.equalsIgnoreCase("lcc"))
			val = "color: " + lastForeground + "; background-color: " + lastBackground;
		if (val == null)
			val = (currentE != null) ? currentE.getAttributeValue(tag) : null;
		if ((val == null) && (currentE != null))
			val = currentE.getAttributeValue(tag.toLowerCase());
		if ((val == null) && (currentE != null))
			val = currentE.getAttributeValue(tag.toUpperCase());
		if ((val == null) && (currentE == null))
		{
			for (int x = openElements.size() - 1; x >= 0; x--)
			{
				final MXPElement E = openElements.elementAt(x);
				val = E.getAttributeValue(tag);
				if (val != null)
					break;
			}
			if (val == null)
			{
				for (int x = openElements.size() - 1; x >= 0; x--)
				{
					final MXPElement E = openElements.elementAt(x);
					val = E.getAttributeValue(tag.toLowerCase());
					if (val != null)
						break;
				}
			}
			if (val == null)
			{
				for (int x = openElements.size() - 1; x >= 0; x--)
				{
					final MXPElement E = openElements.elementAt(x);
					val = E.getAttributeValue(tag.toUpperCase());
					if (val != null)
						break;
				}
			}
		}
		if (val == null)
		{
			MXPEntity N = entities.get(tag);
			if (N == null)
				N = entities.get(tag.toLowerCase());
			if (N == null)
				N = entities.get(tag.toUpperCase());
			if (N != null)
				val = N.getDefinition();
		}
		return val;
	}

	public void modifyEntity(String name, String value)
	{
		name = name.toLowerCase();
		MXPEntity X = entities.get(name);
		if (X == null)
		{
			X = new MXPEntity(name, value);
			entities.put(name, X);
		}
		else
		{
			if (X.getDefinition().equalsIgnoreCase(value))
				return;
			X.setDefinition(value);
		}
		String[] gauge = null;
		for (int g = 0; g < gauges.size(); g++)
		{
			gauge = gauges.elementAt(g);
			if ((gauge[0].equalsIgnoreCase(name)) || (gauge[1].equalsIgnoreCase(name)))
			{
				final String initEntity = getEntityValue(gauge[0], null);
				int initValue = 0;
				if ((initEntity != null) && (Util.isInteger(initEntity)))
					initValue = Util.s_int(initEntity);
				final String maxEntity = getEntityValue(gauge[1], null);
				int maxValue = 100;
				if ((maxEntity != null) && (Util.isInteger(maxEntity)))
					maxValue = Util.s_int(maxEntity);
				if (maxValue < initValue)
					maxValue = (initValue <= 0) ? 100 : initValue;
				if (initValue > 0)
					initValue = (int) Math.round(Util.mul(100.0, Util.div(initValue, maxValue)));
				synchronized (jscriptBuffer)
				{
					jscriptBuffer.append("modifyGauge('" + gauge[0] + "'," + initValue + "," + maxValue + ");");
				}
			}
		}
	}

	public void shutdownMXP()
	{
		openElements.clear();
		eatAllEOLN = false;
		eatNextEOLN = false;
		eatTextUntilEOLN = false;
		initMXP();
		jscriptBuffer.setLength(0);
		responses.setLength(0);
		while (gauges.size() > 0)
		{
			synchronized (jscriptBuffer)
			{
				jscriptBuffer.append("removeGauge('" + gauges.elementAt(0)[0] + "');");
			}
			gauges.removeElementAt(0);
		}
		mode = 0;
		defaultMode = 0;
		tags.clear();
	}

	public int processEntity(StringBuffer buf, int i, MXPElement currentE, boolean convertIfNecessary)
	{
		if ((mode() == MXP.MODE_LINE_LOCKED) || (mode() == MXP.MODE_LOCK_LOCKED))
			return 0;
		boolean convertIt = false;
		final int oldI = i;
		final StringBuffer content = new StringBuffer("");
		if ((i < buf.length() - 3) && (buf.charAt(i + 1) == '#') && (Character.isDigit(buf.charAt(i + 2))))
		{
			i++; // skip to the hash, the next line will skip to the digit
			while ((++i) < buf.length())
			{
				if (buf.charAt(i) == ';')
				{
					convertIt = false;
					break;
				}
				else 
				if (!Character.isDigit(buf.charAt(i)))
				{
					convertIt = true;
					break;
				}
			}
		}
		else
		{
			while ((++i) < buf.length())
			{
				if (buf.charAt(i) == ';')
				{
					convertIt = false;
					break;
				}
				else 
				if (!Character.isLetterOrDigit(buf.charAt(i)))
				{
					convertIt = true;
					break;
				}
				else 
				if ((!Character.isLetter(buf.charAt(i))) && (content.length() == 0))
				{
					convertIt = true;
					break;
				}
				content.append(buf.charAt(i));
				if (content.length() > 20)
					break;
			}
		}
		if ((i >= buf.length()) && (content.length() > 0) && ((buf.length() - i) < 10))
		{
			if (entityDebug)
				System.out.println("e=INCOMPLETE: " + content.toString());
			return Integer.MAX_VALUE;
		}
		if ((convertIt) || (content.length() == 0) || (buf.charAt(i) != ';'))
		{
			if (entityDebug)
				System.out.println("e=ILLEGAL1: " + content.toString());
			if (convertIfNecessary)
			{
				buf.insert(oldI + 1, "amp;");
				return 4;
			}
			return 0;
		}
		final String tag = content.toString().trim();
		final String val = getEntityValue(tag, currentE);
		final String oldValue = buf.substring(oldI, i + 1);
		if (entityDebug)
			System.out.println("entity=" + tag + ", val=" + val);
		buf.delete(oldI, i + 1);
		if (val != null)
		{
			if ((currentE != null) && (currentE.name().equalsIgnoreCase("FONT")))
			{
				if (tag.equalsIgnoreCase("COLOR"))
					lastForeground = val;
				else 
				if (tag.equalsIgnoreCase("BACK"))
					lastBackground = val;
			}
			buf.insert(oldI, val);
			if ((val.equalsIgnoreCase(oldValue)) || (currentE != null))
				return val.length() - 1;
			return -1;
		}
		return -1;
	}

}
