package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2024 Bo Zimmerman

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
public class Yahoomsgrabber extends StdTest
{
	@Override
	public String ID()
	{
		return "Yahoomsgrabber";
	}

	private Map<String,List<String>> parseHeaders(final String header)
	{
		final Map<String,List<String>> headers = new HashMap<String,List<String>>();
		for(final String rawHeader : header.split("\r\n"))
		{
			final int x=rawHeader.indexOf(':');
			final String headerKey=rawHeader.substring(0,x).trim().toUpperCase();
			final List<String> headerVals = new ArrayList<String>();
			for(final String s : Arrays.asList(CMLib.coffeeFilter().colorOnlyFilter(rawHeader.substring(x+1),null).trim().split(";")))
				headerVals.add(s.trim());
			headers.put(headerKey, headerVals);
		}
		return headers;
	}

	private String decodeQuotedPrintable(final String str)
	{
		final StringBuilder nstr=new StringBuilder("");
		for(int c=0;c<str.length()-2;c++)
		{
			final char ch=str.charAt(c);
			if(ch=='=')
			{
				final char ch1=Character.toUpperCase(str.charAt(++c));
				final char ch2=Character.toUpperCase(str.charAt(++c));
				final int hex1="0123456789ABCDEF".indexOf(ch1);
				final int hex2="0123456789ABCDEF".indexOf(ch2);
				if((hex1>=0)&&(hex2>=0))
					nstr.append((char)((hex1*16)+hex2));
			}
			else
				nstr.append(ch);
		}
		return nstr.toString();
	}

	private String stripHtmlHeaders(String html)
	{
		if(html.trim().startsWith("&lt;HTML")
		||html.trim().startsWith("&lt;html"))
		{
			html=CMStrings.replaceAll(html, "&lt;", "<");
			html=CMStrings.replaceAll(html, "&gt;", ">");
			html=CMStrings.replaceAll(html, "&quot;", "\"");
			html=CMStrings.replaceAll(html, "&#39;", "'");
		}
		if(html.trim().startsWith("<html")
			||html.trim().startsWith("<HTML"))
		{
			int x=html.lastIndexOf("</body>");
			if(x<0)
				x=html.lastIndexOf("</BODY>");
			if(x>0)
				html=html.substring(0,x);
			x=html.indexOf("<body");
			if(x<0)
				x=html.indexOf("<BODY");
			if(x>0)
			{
				x=html.indexOf(">",x+4);
				html=html.substring(x+1);
			}
		}
		return html;
	}

	public String copyYahooGroupMsg(final MOB mob, int lastMsgNum) throws Exception
	{
		long numTimes = 9999999;
		java.io.File dir;
		if(java.io.File.separatorChar=='\\')
			dir=new java.io.File("Z:\\_COFEHAS\\Misc\\yahoo-group");
		else
			dir=new java.io.File("/arc/_COFEHAS/Misc/yahoo-group");
		int numTotal=0;
		{
			final int baseTotal=dir.listFiles().length;
			numTotal = baseTotal;
			java.io.File F=new File(dir,""+numTotal+".json");
			while(!F.exists())
			{
				int diff=(numTotal/100);
				if(diff == 0)
					diff = 1;
				numTotal = numTotal-diff;
				F=new File(dir,""+numTotal+".json");
			}
			while(F.exists())
			{
				numTotal++;
				F=new File(dir,""+numTotal+".json");
				if(!F.exists())
				{
					for(int i=0;i<100;i++)
					{
						F=new File(dir,""+(i+numTotal)+".json");
						if(F.exists())
						{
							numTotal += i;
							break;
						}
					}
				}
			}
			F=new File(dir,""+numTotal+".json");
			while(!F.exists())
			{
				numTotal--;
				F=new File(dir,""+numTotal+".json");
			}
			mob.tell(numTotal+": highest mail file found.");
		}
		while ((--numTimes) >= 0)
		{
			lastMsgNum++;
			if (lastMsgNum > numTotal)
			{
				lastMsgNum = numTotal;
				return lastMsgNum + "of " + numTotal + " messages already processed";
			}
			final java.io.File F=new File(dir,""+lastMsgNum+".json");
			if(!F.exists())
				continue;
			final java.io.BufferedInputStream bin=new java.io.BufferedInputStream(new java.io.FileInputStream(F));
			final StringBuilder msgBuild = new StringBuilder("");
			for(int i=0;i<F.length();i++)
				msgBuild.append((char)bin.read());
			bin.close();
			final String msgPage = msgBuild.toString();
			final MiniJSON json=new MiniJSON();
			final MiniJSON.JSONObject msgObj = json.parseObject(msgPage).getCheckedJSONObject("ygData");

			String subject = CMLib.coffeeFilter().colorOnlyFilter(msgObj.getCheckedString("subject"),null);
			final long dateLong = CMath.s_long(msgObj.getCheckedString("postDate")) * 1000L;
			String author;
			if(msgObj.containsKey("profile"))
				author = msgObj.getCheckedString("profile");
			else
			if(msgObj.containsKey("authorName"))
				author = msgObj.getCheckedString("authorName");
			else
				author = "Unknown";
			if(author.trim().length()==0)
				author = "Unknown";
			String theMessage=msgObj.getCheckedString("rawEmail");
			final int headerEnd=theMessage.indexOf("\r\n\r\n");
			if(headerEnd<0)
				return "Failed: to find header in msg:" + lastMsgNum;
			Map<String,List<String>> headers = this.parseHeaders(theMessage.substring(0,headerEnd+4));
			if(!headers.containsKey("CONTENT-TYPE"))
				return "Failed: to find content-type in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
			String contentType=headers.get("CONTENT-TYPE").get(0);
			theMessage = theMessage.substring(headerEnd+4);
			if (theMessage.trim().length() == 0)
			{
				if(lastMsgNum == 18208)
					continue;
				return "Failed: to find lengthy msg in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
			}
			if(contentType.equalsIgnoreCase("multipart/mixed")||contentType.equalsIgnoreCase("multipart/related"))
			{
				String multiBoundary=null;
				final List<String> bounds = headers.get("CONTENT-TYPE");
				for(String s : bounds)
				{
					s=s.trim();
					if(s.toLowerCase().startsWith("boundary="))
					{
						multiBoundary=s.substring(9);
						if(multiBoundary.startsWith("\"") && multiBoundary.endsWith("\""))
							multiBoundary=multiBoundary.substring(1,multiBoundary.length()-1).trim();
					}
				}
				if(multiBoundary == null)
					return "Failed: missing multi-part-boundary in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
				boolean kaplah=false;
				for(String msgChoice : theMessage.split("--"+multiBoundary))
				{
					msgChoice=msgChoice.trim();
					if(msgChoice.length()==0)
						continue;
					if(msgChoice.startsWith("--"))
						break;
					final int innerHeaderDex=msgChoice.indexOf("\r\n\r\n");
					if(innerHeaderDex<0)
						return "Failed: missing innerHeaderDex in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
					final Map<String,List<String>> innerHeaders = this.parseHeaders(msgChoice.substring(0,innerHeaderDex+4));
					if(!innerHeaders.containsKey("CONTENT-TYPE"))
						return "Failed: to find content-type in inner header in :" + lastMsgNum + "/message/" + lastMsgNum;
					final String innerContentType=innerHeaders.get("CONTENT-TYPE").get(0);
					if(innerContentType.equalsIgnoreCase("multipart/alternative")
					||innerContentType.equalsIgnoreCase("text/plain")
					||innerContentType.equalsIgnoreCase("text/html"))
					{
						contentType=innerContentType;
						headers=innerHeaders;
						theMessage=msgChoice.substring(innerHeaderDex+4);
						kaplah=true;
						break;
					}
				}
				if(!kaplah)
					return "Failed: to find acceptable inner part in :" + lastMsgNum + "/message/" + lastMsgNum;
			}
			if(contentType.equalsIgnoreCase("multipart/alternative"))
			{
				String multiBoundary=null;
				final List<String> bounds = headers.get("CONTENT-TYPE");
				for(String s : bounds)
				{
					s=s.trim();
					if(s.toLowerCase().startsWith("boundary="))
					{
						multiBoundary=s.substring(9);
						if(multiBoundary.startsWith("\"") && multiBoundary.endsWith("\""))
							multiBoundary=multiBoundary.substring(1,multiBoundary.length()-1).trim();
					}
				}
				if(multiBoundary == null)
					return "Failed: missing multi-boundary in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
				boolean kaplah=false;
				for(String msgChoice : theMessage.split("--"+multiBoundary))
				{
					msgChoice=msgChoice.trim();
					if(msgChoice.length()==0)
						continue;
					if(msgChoice.startsWith("--"))
						break;
					final int innerHeaderDex=msgChoice.indexOf("\r\n\r\n");
					if(innerHeaderDex<0)
						return "Failed: missing innerHeaderDex in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
					final Map<String,List<String>> innerHeaders = this.parseHeaders(msgChoice.substring(0,innerHeaderDex+4));
					if(!innerHeaders.containsKey("CONTENT-TYPE"))
						return "Failed: to find content-type in inner header in :" + lastMsgNum + "/message/" + lastMsgNum;
					String encoding="7bit";
					if(innerHeaders.containsKey("CONTENT-TRANSFER-ENCODING"))
						encoding=innerHeaders.get("CONTENT-TRANSFER-ENCODING").get(0);
					//return "Failed: to find content-transfer-encoding in inner header in :" + lastMsgNum + "/message/" + lastMsgNum;
					msgChoice=msgChoice.substring(innerHeaderDex+4).trim();
					final String innerContentType=innerHeaders.get("CONTENT-TYPE").get(0);
					if(innerContentType.equalsIgnoreCase("text/plain")
					||innerContentType.equalsIgnoreCase("text/html"))
					{
						if(encoding.equalsIgnoreCase("base64"))
						{
							if(msgChoice.endsWith("\n(Message over 64 KB, truncated)"))
								msgChoice=msgChoice.substring(0,msgChoice.indexOf("\n(Message over 64 KB, truncated)"));
							theMessage=new String(B64Encoder.B64decode(msgChoice));
						}
						else
						if(encoding.equalsIgnoreCase("quoted-printable"))
							theMessage=decodeQuotedPrintable(msgChoice);
						else
						if((encoding.equalsIgnoreCase("7bit")) || (encoding.equalsIgnoreCase("8bit")))
							theMessage=msgChoice;
						else
							return "Failed: Invalid encoding '"+encoding+"' in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
						kaplah=true;
						if(innerContentType.equalsIgnoreCase("text/html"))
						{
							theMessage=stripHtmlHeaders(theMessage);
							//break; //kaplah
						}
						else
						if(innerContentType.equalsIgnoreCase("text/plain"))
						{
							theMessage=CMStrings.replaceAll(theMessage, "\n", "<BR>");
							break; //kaplah
						}
					}
				}
				if(!kaplah)
					return "Failed: to find acceptable inner message in :" + lastMsgNum + "/message/" + lastMsgNum;
			}
			else
			if(contentType.equalsIgnoreCase("text/plain"))
			{
				String encoding="7bit";
				if(headers.containsKey("CONTENT-TRANSFER-ENCODING"))
					encoding=headers.get("CONTENT-TRANSFER-ENCODING").get(0);
				if(encoding.equalsIgnoreCase("base64"))
				{
					if(theMessage.endsWith("\n(Message over 64 KB, truncated)"))
						theMessage=theMessage.substring(0,theMessage.indexOf("\n(Message over 64 KB, truncated)"));
					theMessage=new String(B64Encoder.B64decode(theMessage));
				}
				else
				if(encoding.equalsIgnoreCase("quoted-printable"))
					theMessage=decodeQuotedPrintable(theMessage);
				else
				if((!encoding.equalsIgnoreCase("7bit")) && (!encoding.equalsIgnoreCase("8bit")))
					return "Failed: Invalid encoding '"+encoding+"' in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
				theMessage=CMStrings.replaceAll(theMessage, "\n", "<BR>");
			}
			else
			if(contentType.equalsIgnoreCase("text/html"))
			{
				String encoding="7bit";
				if(headers.containsKey("CONTENT-TRANSFER-ENCODING"))
					encoding=headers.get("CONTENT-TRANSFER-ENCODING").get(0);
				if(encoding.equalsIgnoreCase("base64"))
				{
					if(theMessage.endsWith("\n(Message over 64 KB, truncated)"))
						theMessage=theMessage.substring(0,theMessage.indexOf("\n(Message over 64 KB, truncated)"));
					theMessage=new String(B64Encoder.B64decode(theMessage));
				}
				else
				if(encoding.equalsIgnoreCase("quoted-printable"))
					theMessage=decodeQuotedPrintable(theMessage);
				else
				if((!encoding.equalsIgnoreCase("7bit")) && (!encoding.equalsIgnoreCase("8bit")))
					return "Failed: Invalid encoding '"+encoding+"' in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;
				theMessage=stripHtmlHeaders(theMessage);
			}
			else
				return "Failed: Invalid content-type '"+contentType+"' in lastMsgNum:" + lastMsgNum + "/message/" + lastMsgNum;

			theMessage = CMStrings.replaceAll(theMessage, "&#39;", "`");
			theMessage = CMStrings.replaceAll(theMessage, "'", "`");
			theMessage = CMStrings.replaceAll(theMessage, "@", "&#64;");
			final JournalsLibrary.ForumJournal forum = CMLib.journals().getForumJournal("Support");
			if (forum == null)
				return "Failed: bad forum given";
			String email="";
			if(msgObj.containsKey("from"))
			{
				email=CMLib.coffeeFilter().colorOnlyFilter(msgObj.getCheckedString("from").trim(), null);
				final int dex=email.lastIndexOf('<');
				if(dex<0)
					email="";
				else
				if(email.endsWith(">"))
					email=email.substring(dex+1,email.length()-1);
				else
					email="";
			}
			if (email.indexOf('@') >= 0)
			{
				final MOB aM = CMLib.players().getLoadPlayerByEmail(email);
				if (aM != null)
					author = aM.Name();
				else
				if (CMProps.isUsingAccountSystem())
				{
					final PlayerAccount A = CMLib.players().getLoadAccountByEmail(email);
					if (A != null)
						author = A.getAccountName();
				}
				else
				if(!CMLib.players().playerExistsAllHosts(author))
					author = "_" + author;
			}
			else
			if(!CMLib.players().playerExistsAllHosts(author))
				author = "_" + author;

			String parent = "";
			if (subject.toLowerCase().startsWith("[coffeemud]"))
				subject = subject.substring(11).trim();
			if (subject.toUpperCase().startsWith("RE:"))
			{
				String subj = subject;
				while (subj.toUpperCase().startsWith("RE:") || subj.toLowerCase().startsWith("[coffeemud]"))
				{
					if (subj.toUpperCase().startsWith("RE:"))
						subj = subj.substring(3).trim();
					if (subj.toLowerCase().startsWith("[coffeemud]"))
						subj = subj.substring(11).trim();
				}

				final List<JournalEntry> journalEntries = CMLib.database().DBSearchAllJournalEntries(forum.NAME(), subj);
				if ((journalEntries != null) && (journalEntries.size() > 0))
				{
					JournalEntry WIN = null;
					for (final JournalEntry J : journalEntries)
					{
						if (J.subj().trim().equals(subj))
							WIN = J;
					}
					if (WIN == null)
					{
						for (final JournalEntry J : journalEntries)
						{
							if (J.subj().trim().equalsIgnoreCase(subj))
								WIN = J;
						}
					}
					if (WIN == null)
					{
						for (final JournalEntry J : journalEntries)
						{
							if (J.subj().trim().indexOf(subj) >= 0)
								WIN = J;
						}
					}
					if (WIN == null)
					{
						for (final JournalEntry J : journalEntries)
						{
							if (J.subj().toLowerCase().trim().indexOf(subj.toLowerCase()) >= 0)
								WIN = J;
						}
					}

					if (WIN != null)
						parent = WIN.key();
				}
				if (parent.length() == 0)
					subject = subj;
			}
			final JournalEntry msg = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
			msg.from (author);
			msg.subj (CMLib.webMacroFilter().clearWebMacros(subject));
			msg.msg (CMLib.webMacroFilter().clearWebMacros(theMessage));
			msg.dateStr(""+dateLong);
			msg.update (dateLong);
			msg.parent (parent);
			msg.msgIcon ("");
			msg.data ("");
			msg.to ("ALL");
			// check for dups
			final List<JournalEntry> chckEntries = CMLib.database().DBReadJournalMsgsNewerThan(forum.NAME(), "ALL", msg.date() - 1);
			boolean dup=false;
			for (final JournalEntry entry : chckEntries)
			{
				if ((entry.date() == msg.date())
				&& (entry.from().equals(msg.from()))
				&& (entry.subj().equals(msg.subj()))
				&& (entry.parent().equals(msg.parent())))
				{
					dup=true;
					break;
				}
			}
			if(dup)
			{
				if(mob != null)
				{
					mob.tell("Message "+lastMsgNum+" was a dup!");
					continue;
				}
				else
					return "Msg#" + lastMsgNum + " was a dup!";
			}
			CMLib.database().DBWriteJournal(forum.NAME(), msg);
			if (parent.length() > 0)
				CMLib.database().DBTouchJournalMessage(parent, msg.date());
			CMLib.journals().clearJournalSummaryStats(forum);
			if(mob != null)
				mob.tell("Message "+lastMsgNum+" posted.");
		}
		return "Post " + lastMsgNum + " submitted.";
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		try
		{
			//final int rememberMe=18201;
			final String rest=CMParms.combine(commands);
			if(CMath.isInteger(rest))
				mob.tell(copyYahooGroupMsg(mob,CMath.s_int(rest)));
			else
				mob.tell("18201 was a nice year.");
		}
		catch(final Exception e)
		{
			e.printStackTrace();
			Log.errOut(e);
			mob.tell(e.getMessage());
			return e.getMessage();
		}
		return null;
	}
}
