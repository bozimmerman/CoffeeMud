package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalEntry;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class YahooGroups extends StdWebMacro
{
	public String name(){return "YahooGroups";}
	public boolean isAdminMacro()	{return true;}

	
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String command=parms.get("COMMAND");
		if(command==null)
			return " @break@";
		if(command.equalsIgnoreCase("LOGIN"))
		{
			HttpClient H=(HttpClient)CMClass.getCommon("DefaultHttpClient");
			String user=parms.get("USER");
			if(user==null)
				return " @break@";
			String password=parms.get("PASSWORD");
			if(password==null)
				return " @break@";
			try {
				final String url="http://login.yahoo.com?login="+URLEncoder.encode(user,"UTF8")+"&passwd="+URLEncoder.encode(password,"UTF8");
				Map<String,List<String>> M = H.getHeaders(url);
				if(M==null)
					return "Fail: Http error";
				StringBuilder cookieSet=new StringBuilder("");
				List<String> cookies=M.get("Set-Cookie");
				if(cookies!=null)
					for(String val : cookies)
					{
						if(cookieSet.length()>0)
							cookieSet.append(" ; ");
						int x=val.indexOf(';');
						cookieSet.append((x>=0)?val.substring(0,x).trim():val.trim());
					}
				return cookieSet.toString().replace('&','#');
			} catch (UnsupportedEncodingException e) {
				Log.errOut(Thread.currentThread().getName(),e);
			}
			return " @break@";
		}
		String url=parms.get("URL");
		if(url==null)
			return " @break@";
		if(command.equalsIgnoreCase("NUMMSGS"))
		{
			String token=parms.get("TOKEN");
			if(token==null)
				return " @break@";
			HttpClient H=(HttpClient)CMClass.getCommon("DefaultHttpClient");
			byte[] b=H.getRawUrl(url,token.replace('#','&'));
			if(b==null)
				return "Failed: Bad login token?";
			StringBuilder s=new StringBuilder(new String(b));
			CMStrings.convertHtmlToText(s);
			String txt=s.toString();
			int x=txt.indexOf(" of ");
			int num=-1;
			while((num<0)&&(x>=0))
			{
				if(Character.isDigit(txt.charAt(x+4)))
				{
					int y=4;
					while(Character.isDigit(txt.charAt(x+y)))
						y++;
					return Integer.toString(CMath.s_int(txt.substring(x+4,x+y)));
				}
				else
					x=txt.indexOf(" of ",x+1);
			}
			return "Fail: no numbers found";
		}
		if(command.equalsIgnoreCase("GETAMSG")||command.equalsIgnoreCase("COPYAMSG"))
		{
			HttpClient H=(HttpClient)CMClass.getCommon("DefaultHttpClient");
			String user=parms.get("USER");
			if(user==null)
				return " @break@";
			String timeStr=parms.get("TIMES");
			int numTimes=1;
			if(timeStr!=null)
				numTimes=CMath.s_int(timeStr);
			String password=parms.get("PASSWORD");
			if(password==null)
				return " @break@";
			try {
				final String loginUrl="http://login.yahoo.com?login="+URLEncoder.encode(user,"UTF8")+"&passwd="+URLEncoder.encode(password,"UTF8");
				Map<String,List<String>> M = H.getHeaders(loginUrl);
				if(M==null)
					return "Fail: Http error hitting "+loginUrl;
				StringBuilder cookieSet=new StringBuilder("");
				List<String> cookies=M.get("Set-Cookie");
				if(cookies==null)
					return "Fail: Http get cookies from "+loginUrl;
				for(String val : cookies)
				{
					if(cookieSet.length()>0)
						cookieSet.append(" ; ");
					int x=val.indexOf(';');
					cookieSet.append((x>=0)?val.substring(0,x).trim():val.trim());
				}
				byte[] b=H.getRawUrl(url+"/messages",cookieSet.toString());
				if(b==null)
					return "Failed: to read page: "+url+"/messages";
				cookies=M.get("Set-Cookie");
				if(cookies!=null)
					for(String val : cookies)
					{
						if(cookieSet.length()>0)
							cookieSet.append(" ; ");
						int x=val.indexOf(';');
						cookieSet.append((x>=0)?val.substring(0,x).trim():val.trim());
					}
				StringBuilder s=new StringBuilder(new String(b));
				CMStrings.convertHtmlToText(s);
				String txt=s.toString();
				int x=txt.indexOf(" of ");
				int num=-1;
				while((num<0)&&(x>=0))
				{
					if(Character.isDigit(txt.charAt(x+4)))
					{
						int y=4;
						while(Character.isDigit(txt.charAt(x+y)))
							y++;
						num=CMath.s_int(txt.substring(x+4,x+y));
					}
					else
						x=txt.indexOf(" of ",x+1);
				}
				if(num<0)
					return "Failed: No numbers found in "+url+"/messages";
				CMFile f=new CMFile("::/resources/lastyahoomsg.txt",null,false);
				int lastMsgNum=-1;
				if(f.exists() && f.canRead())
					lastMsgNum=CMath.s_int(f.text().toString().trim());
				if(lastMsgNum>=num)
					return lastMsgNum+"of "+num+" messages already processed";
				while((--numTimes)>=0)
				{
					lastMsgNum++;
					b=H.getRawUrl(url+"/message/"+lastMsgNum,cookieSet.toString());
					if(b==null)
						return "Failed: to read message from url:"+url+"/message/"+lastMsgNum;
					String msgPage=new String(b);
					int startOfSubject=msgPage.indexOf("<em class=\"msg-bg msg-bd\"");
					if(startOfSubject<0)
						startOfSubject=msgPage.indexOf("<em class=\"msg-newfont\"");
					if(startOfSubject<0)
					{
						x=msgPage.indexOf("Message  does not exist in ");
						if((x>0)&&(msgPage.substring(0,x).trim().endsWith("<div class=\"ygrp-contentblock\">")))
						{
							f.saveText(Integer.toString(lastMsgNum));
							continue;
						}
						return "Failed: to find subject start in url:"+url+"/message/"+lastMsgNum;
					}
					startOfSubject=msgPage.indexOf(">",startOfSubject);
					int endOfSubject=msgPage.indexOf("</em>",startOfSubject);
					if(endOfSubject<0)
						return "Failed: to find subject end in url:"+url+"/message/"+lastMsgNum;
					String subject=msgPage.substring(startOfSubject+1,endOfSubject).trim();
					if((subject.length()==0)||(subject.length()>100)||(subject.indexOf('<')>=0)||(subject.indexOf('>')>=0))
						return "Failed: to find VALID subject '"+subject+"' in url:"+url+"/message/"+lastMsgNum;
					int startOfDate=msgPage.indexOf("<span class=\"msg-newfont\" title=\"");
					int endOfDate;
					if(startOfDate>0)
						startOfDate+=33;// MAGIC NUMBER
					else
					{
						startOfDate=msgPage.indexOf("<span class=\"msg-newfont\" title=\"");
						//if(startOfDate<0) System.out.println(msgPage);
						if(startOfDate<0)
							return "Failed: to find date start in url:"+url+"/message/"+lastMsgNum;
						startOfDate+=33;// MAGIC NUMBER
					}
					endOfDate=msgPage.indexOf("\"",startOfDate+1);
					if(endOfDate<0)
						return "Failed: to find date end in url:"+url+"/message/"+lastMsgNum;
					String dateStr=msgPage.substring(startOfDate,endOfDate).trim();
					SimpleDateFormat  format = new SimpleDateFormat("yyyy-M-d'T'HH:mm:ss'Z'");
					Date postDate;
					try
					{
						postDate=format.parse(dateStr);
					}
					catch(ParseException p)
					{
						return "Failed: to parse date '"+dateStr+"' in url:"+url+"/message/"+lastMsgNum;
					}
					int startOfAuthor=msgPage.indexOf("<span class=\"name\">");
					if(startOfAuthor<0)
						return "Failed: to find author start in url:"+url+"/message/"+lastMsgNum;
					startOfAuthor=msgPage.indexOf(">",startOfAuthor+4);
					int endOfAuthor=msgPage.indexOf("</span>",startOfAuthor);
					if(endOfAuthor<0)
						return "Failed: to find author end in url:"+url+"/message/"+lastMsgNum;
					String author=msgPage.substring(startOfAuthor+1,endOfAuthor).trim();
					author=CMStrings.replaceAll(author,"<wbr>","").trim();
					if(author.indexOf("profiles.yahoo.com")>0)
						author=author.substring(author.indexOf("\">")+2,author.lastIndexOf("</a>"));
					if((author.length()==0)||(author.length()>100))
						return "Failed: to find VALID author '"+author+"' in url:"+url+"/message/"+lastMsgNum;
					int startOfMsg=msgPage.indexOf("entry-content");
					if(startOfMsg<0)
						return "Failed: to find message in url:"+url+"/message/"+lastMsgNum;
					startOfMsg=msgPage.indexOf(">",startOfMsg);
					int endOfMsg=msgPage.indexOf("</div>",startOfMsg);
					if(endOfMsg<0)
						return "Failed: to find end of msg in url:"+url+"/message/"+lastMsgNum;
					String theMessage=msgPage.substring(startOfMsg+1,endOfMsg).trim();
					while(theMessage.startsWith("<br>"))
						theMessage=theMessage.substring(4).trim();
					while(theMessage.endsWith("<br>"))
						theMessage=theMessage.substring(0,theMessage.length()-4).trim();
					theMessage=CMStrings.replaceAll(theMessage,"\n","");
					theMessage=CMStrings.replaceAll(theMessage,"\r","");
					if(theMessage.trim().length()==0)
						return "Failed: to find lengthy msg in url:"+url+"/message/"+lastMsgNum;
					if(command.equalsIgnoreCase("GETAMSG"))
						return "Author: "+author+"<BR>\n\rSubject: "+subject+"<BR>\n\r"+theMessage;
					String journal=parms.get("JOURNAL");
					if(journal==null)
						return "Failed: no journal given";
					JournalsLibrary.ForumJournal forum=CMLib.journals().getForumJournal(journal);
					if(forum==null)
						return "Failed: bad forum given";
					if(author.indexOf('@')>=0)
					{
						MOB aM=CMLib.players().getLoadPlayerByEmail(author);
						if(aM!=null)
							author=aM.Name();
						else
						if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>0)
						{
							PlayerAccount A=CMLib.players().getLoadAccountByEmail(author);
							if(A==null)
								author=author.substring(0,author.indexOf('@'));
							else
								author=A.accountName();
						}
						else
							author=author.substring(0,author.indexOf('@'));
					}
					else
					if(CMLib.login().isOkName(author))
						author="_"+author;
					
					String parent="";
					if(subject.startsWith("RE:")||subject.startsWith("Re:"))
					{
						String subj=subject.substring(3).trim();
						if(subj.startsWith("[coffeemud]"))
							subj=subj.substring(11).trim();
						Vector<JournalEntry> journalEntries=CMLib.database().DBReadJournalPageMsgs(forum.NAME(), null, subj, 0, 0);
						if((journalEntries!=null)&&(journalEntries.size()>0))
						{
							JournalEntry WIN=null;
							for(JournalEntry J : journalEntries)
							{
								if(J.subj.trim().equals(subj))
									WIN=J;
							}
							if(WIN==null)
							for(JournalEntry J : journalEntries)
							{
								if(J.subj.trim().equalsIgnoreCase(subj))
									WIN=J;
							}
							if(WIN==null)
							for(JournalEntry J : journalEntries)
							{
								if(J.subj.trim().indexOf(subj)>=0)
									WIN=J;
							}
							if(WIN==null)
							for(JournalEntry J : journalEntries)
							{
								if(J.subj.toLowerCase().trim().indexOf(subj.toLowerCase())>=0)
									WIN=J;
							}
							
							if(WIN!=null)
								parent=WIN.key;
						}
					}
					JournalsLibrary.JournalEntry msg = new JournalsLibrary.JournalEntry();
					msg.from=author;
					msg.subj=clearWebMacros(subject);
					msg.msg=clearWebMacros(theMessage);
					msg.date=postDate.getTime();
					msg.update=postDate.getTime();
					msg.parent=parent;
					msg.msgIcon="";
					msg.data="";
					msg.to="ALL";
					// check for dups
					Vector<JournalsLibrary.JournalEntry> chckEntries = CMLib.database().DBReadJournalMsgsNewerThan(forum.NAME(), "ALL", msg.date-1);
					for(JournalsLibrary.JournalEntry entry : chckEntries)
						if((entry.date == msg.date)
						&&(entry.from.equals(msg.from))
						&&(entry.subj.equals(msg.subj))
						&&(entry.parent.equals(msg.parent)))
						{
							f.saveText(Integer.toString(lastMsgNum));
							return "Failed: DUP!";
						}
					CMLib.database().DBWriteJournal(forum.NAME(),msg);
					if(parent.length()>0)
						CMLib.database().DBTouchJournalMessage(parent,msg.date);
					JournalInfo.clearJournalCache(httpReq, forum.NAME());
					CMLib.journals().clearJournalSummaryStats(forum.NAME());
					f.saveText(Integer.toString(lastMsgNum));
				}
				if(numTimes>0)
					CMLib.s_sleep(8000+Math.round(8000*CMath.random()));
				return "Post "+lastMsgNum+" submitted.";
			} catch (UnsupportedEncodingException e) {
				Log.errOut(Thread.currentThread().getName(),e);
			}
			finally {
				H.finished();
			}
			return " @break@";
		}
		return "";
	}
}
