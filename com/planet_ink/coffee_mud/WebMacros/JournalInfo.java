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
   Copyright 2003-2024 Bo Zimmerman

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

	public static JournalEntry getEntry(final List<JournalEntry> msgs, final String key)
	{
		if(msgs==null)
			return null;
		if((key==null)||(key.length()==0))
			return null;
		int index=-1;
		for(int i=0;i<msgs.size();i++)
		{
			final JournalEntry E=msgs.get(i);
			if(E != null)
			{
				if(E.key().equals(key))
				{
					index=i;
					break;
				}
			}
		}
		if(index >=0)
		{
			if(index<msgs.size())
				return msgs.get(index);
			return null;
		}
		for (final JournalEntry entry : msgs)
		{

			if((entry != null)
			&&(entry.key().equalsIgnoreCase(key)))
				return entry;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<JournalEntry> getMessages(final String journalName, final JournalsLibrary.ForumJournal forumJournal,
			String page, final String mpage, String parent, String dbsearch, final int msgLimit, final Map<String,Object> objs)
	{

		if(parent==null)
			parent="";
		else
		if(parent.equals("*"))
			parent=null;
		else
		if(parent.length()>0)
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
		final String httpkey="JOURNAL: "+journalName+": "+parent+": "+dbsearch+": "+page;
		List<JournalEntry> msgs=(objs==null)?null:(List<JournalEntry>)objs.get(httpkey);
		if(msgs==null)
		{
			if((page==null)||(page.length()==0))
				msgs=CMLib.database().DBReadJournalMsgsByUpdateDate(journalName, true);
			else
			{
				final JournalsLibrary.JournalMetaData metaData = CMLib.journals().getJournalStats(forumJournal);
				final long pageDate = CMath.s_long(page);
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
				msgs.addAll(CMLib.database().DBReadJournalPageMsgs(journalName, parent, dbsearch, pageDate, msgLimit));
				//if((dbsearch!=null)&&(dbsearch.length()>0)) // parent filtering
				//	pageDate=mergeParentMessages(journalName, msgs, pageDate);
			}
			if(objs!=null)
				objs.put(httpkey,msgs);
		}
		return msgs;
	}

	public static void clearJournalCache(final HTTPRequest httpReq, final String journalName)
	{
		final List<String> h = new XVector<String>(httpReq.getRequestObjects().keySet());
		for(final String o : h)
		{
			if((o!=null)&&(o.startsWith("JOURNAL: "+journalName+": ")))
				httpReq.getRequestObjects().remove(o.toString());
		}
	}

	public static JournalEntry getNextEntry(final List<JournalEntry> info, final String key)
	{
		if(info==null)
			return null;
		if((key != null)&&(key.length()>0))
		{
			int index=-1;
			for(int i=0;i<info.size();i++)
			{
				final JournalEntry E=info.get(i);
				if(E!= null)
				{
					if(E.key().equals(key))
					{
						index=i;
						break;
					}
				}
			}
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
			if(entry == null)
				continue;
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
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
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
			final List<String> assigns=new ArrayList<String>();
			if(CJ!=null)
				assigns.addAll(CMParms.parseAny(CJ.getFlag(CommandJournalFlags.ASSIGN), ':', true));
			if(!assigns.contains("ALL"))
				assigns.add("ALL");
			if(!assigns.contains("FROM"))
				assigns.add("FROM");
			if(!assigns.contains("MAILBOX"))
				assigns.add("MAILBOX");

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
			int pageLimit;
			if(httpReq.isUrlParameter("JOURNALPAGELIMIT"))
				pageLimit = CMath.s_int(httpReq.getUrlParameter("JOURNALPAGELIMIT"));
			else
				pageLimit = CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
			if(pageLimit <= 0)
				pageLimit=Integer.MAX_VALUE;
			if((page!=null)&&(page.length()>0))
			{
				final List<JournalEntry> msgs=JournalInfo.getMessages(journalName,journal,page,mpage,parent,dbsearch,pageLimit, httpReq.getRequestObjects());
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
				return String.valueOf(CMath.bset(entry.attributes(), JournalEntry.JournalAttrib.STUCKY.bit));
			else
			if(parms.containsKey("ISPROTECTED"))
				return String.valueOf(CMath.bset(entry.attributes(), JournalEntry.JournalAttrib.PROTECTED.bit));
			else
			if(parms.containsKey("ISATTACHMENT"))
				return String.valueOf(CMath.bset(entry.attributes(), JournalEntry.JournalAttrib.ATTACHMENT.bit));
			else
			if(parms.containsKey("MSGICON"))
				return clearWebMacros(entry.msgIcon());
			else
			if(parms.containsKey("MSGTYPEICON"))
			{
				if(entry.attributes()==0)
					return "doc.gif";
				if(CMath.bset(entry.attributes(), JournalEntry.JournalAttrib.STUCKY.bit))
					return "doclocked.gif";
				if(CMath.bset(entry.attributes(), JournalEntry.JournalAttrib.PROTECTED.bit))
					return "docstar.gif";
				if(CMath.bset(entry.attributes(), JournalEntry.JournalAttrib.ATTACHMENT.bit))
					return "docclip.gif";
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
			if(parms.containsKey("ATTACHNEXT"))
			{
				final String last=httpReq.getUrlParameter("ATTACHID");
				if(parms.containsKey("RESET"))
				{
					if(last!=null)
						httpReq.removeUrlParameter("ATTACHID");
					return "";
				}
				String lastID="";
				for(int i=0;i<entry.attachmentKeys().size();i++)
				{
					final String id = entry.attachmentKeys().get(i);
					if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!id.equals(lastID))))
					{
						httpReq.addFakeUrlParameter("ATTACHID",id);
						String anam = id;
						if(anam.startsWith(entry.key()+"/"))
						{
							anam=anam.substring(entry.key().length()+1);
							final int x = anam.indexOf('/');
							if(x > 0)
							{
								httpReq.addFakeUrlParameter("ATTACHPATH",id.substring(0,x+entry.key().length()+1));
								anam=anam.substring(x+1);
							}
						}
						httpReq.addFakeUrlParameter("ATTACHNAME",anam);
						return "";
					}
					lastID=id;
				}
				httpReq.addFakeUrlParameter("ATTACHID","");
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
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
				if(parms.containsKey("SMARTTAGS"))
				{
					final boolean forum = CMLib.journals().getForumJournal(journalName) != null;
					if(!forum)
					{
						s=CMStrings.replaceAll(s,"&","&amp;");
						s=CMStrings.replaceAll(s,"<","&lt;");
						s=CMStrings.replaceAll(s,">","&gt;");
						s=CMStrings.replaceAll(s,JournalsLibrary.JOURNAL_BOUNDARY,"\n<HR>");
						s=CMStrings.replaceAll(s,"%0D","<BR>");
					}
				}
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
				if(parms.containsKey("EDIT"))
				{
					//s=CMStrings.replaceAll(s,JournalsLibrary.JOURNAL_BOUNDARY,"\n<HR>");
					//s=CMStrings.replaceAll(s,"%0D","\n");
					s=CMStrings.replaceAll(s,"<BR>","\n");
					//s=CMStrings.replaceAll(s,"&","&amp;");
					//s=CMStrings.replaceAll(s,"<","&lt;");
					//s=CMStrings.replaceAll(s,">","&gt;");
					//s=CMStrings.removeColors(s);
				}
				else
				if(parms.containsKey("SHOWTAGS"))
				{
					int x=0;
					int y=s.indexOf(JournalsLibrary.JOURNAL_BOUNDARY,x+1);
					if(y<0)
						y=s.length();
					while((x>=0)&&(y>x))
					{
						final boolean done=y==s.length();
						s=s.substring(0,x)
							+CMStrings.replaceAll(s.substring(x, y), "<","&lt;")
							+s.substring(y);
						y=s.indexOf(JournalsLibrary.JOURNAL_BOUNDARY,x+1);
						if(y<0)
							y=s.length();
						if(done)
							break;
						else
						{
							x=y;
							if(s.substring(x).startsWith(JournalsLibrary.JOURNAL_BOUNDARY))
								x+=JournalsLibrary.JOURNAL_BOUNDARY.length();
							y=s.indexOf(JournalsLibrary.JOURNAL_BOUNDARY,x+1);
							if(y<0)
								y=s.length();
						}
					}
					s=CMStrings.replaceAll(s,JournalsLibrary.JOURNAL_BOUNDARY,"<HR>");
					s=CMStrings.replaceAll(s,"%0D","<BR>");
					s=CMStrings.replaceAll(s,"\n","<BR>");
					s=CMStrings.replaceAll(s,"\r","");
					s=colorwebifyOnly(new StringBuffer(s)).toString();
					s=clearWebMacros(s);
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
