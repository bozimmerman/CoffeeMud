package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournal;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.AreaScriptNext.AreaScriptInstance;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class JournalInfo extends StdWebMacro
{
	@Override
	public String name()
	{
		return "JournalInfo";
	}

	public static JournalEntry getEntry(List<JournalEntry> msgs, String key)
	{
		if(msgs==null)
			return null;
		if((key==null)||(key.length()==0))
			return null;
		int index = msgs.indexOf(key);
		if(index >=0)
		{
			if(index<msgs.size())
				return msgs.get(index);
			return null;
		}
		for (final JournalEntry entry : msgs)
		{
			if(entry.key().equalsIgnoreCase(key))
				return entry;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<JournalEntry> getMessages(String journalName, JournalsLibrary.ForumJournal forumJournal, String page, String mpage, String parent, String dbsearch, Map<String,Object> objs)
	{
		if((parent!=null)&&(parent.length()>0))
		{
			page=mpage;
			dbsearch=null;
		}
		if(page!=null)
		{
			if(page.length()==0)
				page="0";
			else
			{
				final int x=page.lastIndexOf(',');
				if(x>0)
					page=page.substring(x+1);
			}
		}
		if((dbsearch!=null)&&(dbsearch.length()>0))
			parent=null;
		else
		if(parent==null)
			parent="";
		final String httpkey="JOURNAL: "+journalName+": "+parent+": "+dbsearch+": "+page;
		List<JournalEntry> msgs=(List<JournalEntry>)objs.get(httpkey);
		if(msgs==null)
		{
			if((page==null)||(page.length()==0))
				msgs=CMLib.database().DBReadJournalMsgsByUpdateDate(journalName, true);
			else
			{
				final JournalsLibrary.JournalMetaData metaData = CMLib.journals().getJournalStats(forumJournal);
				final long pageDate = CMath.s_long(page);
				int limit = CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
				if(limit<=0)
					limit=Integer.MAX_VALUE;
				msgs = new Vector<JournalEntry>();
				if((pageDate <= 0)
				&& (metaData != null)
				&& (metaData.stuckyKeys()!=null)
				&& ((dbsearch==null)||(dbsearch.length()==0))
				&& ((parent != null)&&(parent.length()==0)))
				{
					for(final String stuckyKey : metaData.stuckyKeys())
					{
						final JournalEntry entry = CMLib.database().DBReadJournalEntry(journalName, stuckyKey);
						if(entry != null)
							msgs.add(entry);
					}
				}
				msgs.addAll(CMLib.database().DBReadJournalPageMsgs(journalName, parent, dbsearch, pageDate, limit));
				//if((dbsearch!=null)&&(dbsearch.length()>0)) // parent filtering
				//	pageDate=mergeParentMessages(journalName, msgs, pageDate);
			}
			objs.put(httpkey,msgs);
		}
		return msgs;
	}

	public static void clearJournalCache(HTTPRequest httpReq, String journalName)
	{
		final List<String> h = new XVector<String>(httpReq.getRequestObjects().keySet());
		for(final String o : h)
		{
			if((o!=null)&&(o.startsWith("JOURNAL: "+journalName+": ")))
				httpReq.getRequestObjects().remove(o.toString());
		}
	}

	public static JournalEntry getNextEntry(List<JournalEntry> info, String key)
	{
		if(info==null)
			return null;
		if((key != null)&&(key.length()>0))
		{
			int index = info.indexOf(key);
			if(index >=0)
			{
				if(index<info.size()-1)
					return info.get(index+1);
				return null;
			}
		}
		for(final Iterator<JournalEntry> e=info.iterator();e.hasNext();)
		{
			final JournalEntry entry = e.next();
			if((key == null)||(key.length()==0))
				return entry;
			if(entry.key().equalsIgnoreCase(key))
			{
				if(e.hasNext())
					return e.next();
				return null;
			}
		}
		return null;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String journalName=httpReq.getUrlParameter("JOURNAL");
		if(journalName==null)
			return " @break@";

		if(parms.containsKey("NOWTIMESTAMP"))
			return ""+System.currentTimeMillis();

		if(parms.containsKey("JOURNALLIMIT"))
			return ""+CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
		
		if(parms.containsKey("ASSIGN"))
		{
			CommandJournal CJ=CMLib.journals().getCommandJournal(journalName);
			if((CJ==null)&&(journalName.startsWith("SYSTEM_")))
				CJ=CMLib.journals().getCommandJournal(journalName.substring(7));
			if((CJ==null)&&(journalName.startsWith("SYSTEM_"))&&(journalName.endsWith("S")))
				CJ=CMLib.journals().getCommandJournal(journalName.substring(7,journalName.length()-1));
			List<String> assigns=new ArrayList<String>();
			if(CJ!=null)
				assigns.addAll(CMParms.parseAny(CJ.getFlag(CommandJournalFlags.ASSIGN), ':', true));
			if(!assigns.contains("ALL"))
				assigns.add("ALL");
			if(!assigns.contains("FROM"))
				assigns.add("FROM");
			
			if(parms.containsKey("NEXT")||parms.containsKey("RESET"))
			{
				String last=httpReq.getUrlParameter("JOURNALASSIGN");
				if(parms.containsKey("RESET"))
				{
					if(last!=null)
						httpReq.removeUrlParameter("JOURNALASSIGN");
					return "";
				}
				String lastID="";
				for(final String inst : assigns)
				{
					if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!inst.equals(lastID))))
					{
						httpReq.addFakeUrlParameter("JOURNALASSIGN",inst);
						last=inst;
						return "";
					}
					lastID=inst;
				}
				httpReq.addFakeUrlParameter("JOURNALASSIGN","");
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
			}
		}

		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if((CMLib.journals().isArchonJournalName(journalName))&&((M==null)||(!CMSecurity.isASysOp(M))))
			return " @break@";

		final String msgKey=httpReq.getUrlParameter("JOURNALMESSAGE");
		if(msgKey==null)
			return " @break@";

		final Clan setClan=CMLib.clans().getClan(httpReq.getUrlParameter("CLAN"));
		final JournalsLibrary.ForumJournal journal= CMLib.journals().getForumJournal(journalName,setClan);

		final String cardinal=httpReq.getUrlParameter("JOURNALCARDINAL");
		JournalEntry entry=null;
		if(msgKey.equalsIgnoreCase("FORUMLATEST"))
		{
			final JournalsLibrary.JournalMetaData metaData = CMLib.journals().getJournalStats(journal);
			if((metaData!=null)&&(metaData.latestKey()!=null)&&(metaData.latestKey().length()>0))
			{
				entry=CMLib.database().DBReadJournalEntry(journalName, metaData.latestKey());
				if(entry != null)
					httpReq.addFakeUrlParameter("JOURNALMESSAGE", entry.key());
			}
		}
		else
		{
			final String page=httpReq.getUrlParameter("JOURNALPAGE");
			final String mpage=httpReq.getUrlParameter("MESSAGEPAGE");
			final String parent=httpReq.getUrlParameter("JOURNALPARENT");
			final String dbsearch=httpReq.getUrlParameter("DBSEARCH");
			if((page!=null)&&(page.length()>0))
			{
				final List<JournalEntry> msgs=JournalInfo.getMessages(journalName,journal,page,mpage,parent,dbsearch,httpReq.getRequestObjects());
				entry= JournalInfo.getEntry(msgs,msgKey);
			}
		}

		if(entry==null)
			entry=CMLib.database().DBReadJournalEntry(journalName, msgKey);
		if(parms.containsKey("ISMESSAGE"))
			return String.valueOf(entry!=null);
		if(entry==null)
			return " @break@";
		if(cardinal!=null)
			entry.cardinal(CMath.s_int(cardinal));
		final boolean priviledged=CMSecurity.isAllowedAnywhere(M,CMSecurity.SecFlag.JOURNALS)&&(!parms.containsKey("NOPRIV"));
		String to=entry.to();
		if(to.equalsIgnoreCase("all")
		||((M!=null)
		   &&(priviledged
			   ||to.equalsIgnoreCase(M.Name())
			   ||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),M,true))))))
		{
			if(parms.containsKey("CANEDIT"))
			{
				if(M==null)
					return "false";
				return String.valueOf(
						entry.from().equals(M.Name())
						|| priviledged
						|| (journal!=null && journal.authorizationCheck(M, ForumJournalFlags.ADMIN))
						);
			}
			else
			if(parms.containsKey("QUOTEDTEXT") && httpReq.isUrlParameter("QUOTEDMESSAGE"))
			{
				final String quotedMessage=httpReq.getUrlParameter("QUOTEDMESSAGE");
				if((quotedMessage==null)||(quotedMessage.length()==0))
					return "";
				final JournalEntry quotedEntry =CMLib.database().DBReadJournalEntry(journalName, quotedMessage);
				return "<P><BLOCKQUOTE style=\"border : solid #000 1px; padding : 3px; margin-left: 1em; margin-bottom:0.2em; background: #f9f9f9 none; color: #000;\">"
					+"<FONT SIZE=-1>Quoted from "+quotedEntry.from()
					+" &nbsp;("+CMLib.time().date2String(quotedEntry.date())+"):</FONT><HR>"
					+ "<I>"+CMStrings.replaceAll(quotedEntry.msg(),"&","&amp;") + "</I></BLOCKQUOTE></P>";
			}
			else
			if(parms.containsKey("KEY"))
				return clearWebMacros(entry.key());
			else
			if(parms.containsKey("ISLASTENTRY"))
				return String.valueOf(entry.isLastEntry());
			else
			if(parms.containsKey("FROM"))
				return clearWebMacros(entry.from());
			else
			if(parms.containsKey("ISSTICKY")||parms.containsKey("ISSTUCKY"))
				return String.valueOf(CMath.bset(entry.attributes(), JournalEntry.ATTRIBUTE_STUCKY));
			else
			if(parms.containsKey("ISPROTECTED"))
				return String.valueOf(CMath.bset(entry.attributes(), JournalEntry.ATTRIBUTE_PROTECTED));
			else
			if(parms.containsKey("MSGICON"))
				return clearWebMacros(entry.msgIcon());
			else
			if(parms.containsKey("MSGTYPEICON"))
			{
				if(entry.attributes()==0)
					return "doc.gif";
				if(CMath.bset(entry.attributes(), JournalEntry.ATTRIBUTE_STUCKY))
					return "doclocked.gif";
				if(CMath.bset(entry.attributes(), JournalEntry.ATTRIBUTE_PROTECTED))
					return "docstar.gif";
				return "docunknown.gif";
			}
			else
			if(parms.containsKey("CARDINAL"))
				return clearWebMacros(entry.cardinal()+"");
			else
			if(parms.containsKey("DATE"))
				return CMLib.time().date2String(entry.date());
			else
			if(parms.containsKey("REPLIES"))
				return clearWebMacros(entry.replies()+"");
			else
			if(parms.containsKey("VIEWS"))
				return clearWebMacros(entry.views()+"");
			else
			if(parms.containsKey("DATEPOSTED"))
			{
				final Calendar meC=Calendar.getInstance();
				final Calendar C=Calendar.getInstance();
				meC.setTimeInMillis(entry.update());
				if(Calendar.getInstance().get(Calendar.YEAR)!=meC.get(Calendar.YEAR))
					return CMLib.time().date2Date2String(entry.update());
				final String dateString = CMLib.time().date2MonthDateString(entry.date(), false);
				final String todayString = CMLib.time().date2MonthDateString(System.currentTimeMillis(), false);
				if(dateString.equals(todayString))
					return "Today";
				C.add(Calendar.DATE, -1);
				final String yesterdayString = CMLib.time().date2MonthDateString(C.getTimeInMillis(), false);
				if(dateString.equals(yesterdayString))
					return "Yesterday";
				return dateString;
			}
			else
			if(parms.containsKey("TIMEPOSTED"))
			{
				return CMLib.time().date2APTimeString(entry.date());
			}
			else
			if(parms.containsKey("DATEUPDATED"))
			{
				final Calendar meC=Calendar.getInstance();
				final Calendar C=Calendar.getInstance();
				meC.setTimeInMillis(entry.update());
				if(Calendar.getInstance().get(Calendar.YEAR)!=meC.get(Calendar.YEAR))
					return CMLib.time().date2Date2String(entry.update());
				final String dateString = CMLib.time().date2MonthDateString(entry.update(), false);
				final String todayString = CMLib.time().date2MonthDateString(System.currentTimeMillis(), false);
				if(dateString.equals(todayString))
					return "Today";
				C.add(Calendar.DATE, -1);
				final String yesterdayString = CMLib.time().date2MonthDateString(C.getTimeInMillis(), false);
				if(dateString.equals(yesterdayString))
					return "Yesterday";
				return dateString;

			}
			else
			if(parms.containsKey("TIMEUPDATED"))
			{
				return CMLib.time().date2APTimeString(entry.update());
			}
			else
			if(parms.containsKey("UPDATED"))
			{
				return String.valueOf(entry.update());
			}
			else
			if(parms.containsKey("TO"))
			{
				if(to.toUpperCase().trim().startsWith("MASK="))
					to=CMLib.masking().maskDesc(to.trim().substring(5),true);
				return clearWebMacros(to);
			}
			else
			if(parms.containsKey("SUBJECT"))
				return clearWebMacros(entry.subj());
			else
			if(parms.containsKey("MESSAGE"))
			{
				String s=entry.msg();
				if(parms.containsKey("NOREPLIES"))
				{
					final int x=s.indexOf(JournalsLibrary.JOURNAL_BOUNDARY);
					if(x>=0)
						s=s.substring(0,x);
				}
				if(parms.containsKey("PLAIN"))
				{
					s=CMStrings.replaceAll(s,"%0D","\n");
					s=CMStrings.replaceAll(s,"<BR>","\n");
					s=CMStrings.removeColors(s);
				}
				else
				{
					s=CMStrings.replaceAll(s,JournalsLibrary.JOURNAL_BOUNDARY,"<HR>");
					s=CMStrings.replaceAll(s,"%0D","<BR>");
					s=CMStrings.replaceAll(s,"\n","<BR>");
					s=colorwebifyOnly(new StringBuffer(s)).toString();
					s=clearWebMacros(s);
				}
				if((entry.parent()==null)||(entry.parent().length()==0))
				{
					entry.views(entry.views()+1);
					CMLib.database().DBUpdateJournalMessageViews(entry.key(), entry.views());
				}
				return s;
			}
			if(parms.containsKey("EMAILALLOWED"))
			{
				return ""+((entry.from().length()>0)
						&&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
			}
		}
		return "";
	}
}
