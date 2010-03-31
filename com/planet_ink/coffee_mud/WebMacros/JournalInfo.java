package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class JournalInfo extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public JournalsLibrary.JournalEntry getEntry(Vector<JournalsLibrary.JournalEntry> info, String key)
	{
		if(info==null)
			return null;
		if(key==null)
			return null;
		for(Enumeration<JournalsLibrary.JournalEntry> e=info.elements();e.hasMoreElements();)
		{
			JournalsLibrary.JournalEntry entry = e.nextElement();
			if(entry.key.equalsIgnoreCase(key))
				return entry;
		}
		return null;
	}

	public Vector getMsgs(ExternalHTTPRequests httpReq, String journalName, String parent, String page)
	{
		Vector info=(Vector)httpReq.getRequestObjects().get("JOURNAL: "+journalName+": "+parent+": "+page);
		if(info==null)
		{
			if((page==null)||(page.length()==0))
				info=CMLib.database().DBReadJournalMsgs(journalName);
			else
			{
				int limit = CMProps.getIntVar(CMProps.SYSTEMI_JOURNALLIMIT);
				if(limit<=0) limit=Integer.MAX_VALUE;
				info=CMLib.database().DBReadJournalPageMsgs(journalName, parent, CMath.s_long(page), limit);
			}
			httpReq.getRequestObjects().put("JOURNAL: "+journalName+": "+parent+": "+page, info);
		}
		return info;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String journalName=httpReq.getRequestParameter("JOURNAL");
		if(journalName==null) 
			return " @break@";
		String page=httpReq.getRequestParameter("JOURNALPAGE");
		String parent=httpReq.getRequestParameter("JOURNALPARENT");
		if(parent==null) parent="";
		
		if(parms.containsKey("NOWTIMESTAMP"))
			return ""+System.currentTimeMillis();
		
		if(parms.containsKey("UNPAGEDCOUNT"))
			return ""+getMsgs(httpReq,journalName,parent,null).size();
		
		if(parms.containsKey("COUNT"))
			return ""+getMsgs(httpReq,journalName,parent,page).size();
		
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if((CMLib.journals().isArchonJournalName(journalName))&&((M==null)||(!CMSecurity.isASysOp(M))))
		    return " @break@";
		
		String msgKey=httpReq.getRequestParameter("JOURNALMESSAGE");
		String cardinal=httpReq.getRequestParameter("JOURNALCARDINAL");
        JournalsLibrary.JournalEntry entry=null;
        Vector info = null;
        if(msgKey.equalsIgnoreCase("FORUMLATEST"))
        {
        	JournalsLibrary.JournalSummaryStats stats = CMLib.journals().getJournalStats(journalName);
        	if(stats!=null)
        	{
        		entry=stats.latest;
        		if(entry != null)
	        		httpReq.addRequestParameters("JOURNALMESSAGE", entry.key);
        	}
        }
        else
        {
        	info = getMsgs(httpReq,journalName,parent,page);
	        entry= getEntry(info,msgKey);
        }
        if(parms.containsKey("ISMESSAGE"))
        	return String.valueOf(entry!=null);
		if(entry==null)	
			return " @break@";
		if(cardinal!=null)
			entry.cardinal=CMath.s_int(cardinal);
        boolean priviledged=CMSecurity.isAllowedAnywhere(M,"JOURNALS")&&(!parms.contains("NOPRIV"));
		String to=entry.to;
		if(to.equalsIgnoreCase("all")
        ||((M!=null)
           &&(priviledged
               ||to.equalsIgnoreCase(M.Name())
               ||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),M,true))))))
		{
			if(parms.containsKey("CANEDIT"))
			{
				JournalsLibrary.ForumJournal forum = CMLib.journals().getForumJournal(journalName);
				if(M==null) return "false";
                return String.valueOf(
                		entry.from.equals(M.Name())
                		|| priviledged
                		|| (forum!=null && forum.authorizationCheck(M, ForumJournalFlags.ADMIN))
                		);
			}
			else
			if(parms.containsKey("QUOTEDTEXT") && httpReq.isRequestParameter("QUOTEDMESSAGE"))
			{
				String quotedMessage=httpReq.getRequestParameter("QUOTEDMESSAGE");
				if((quotedMessage==null)||(quotedMessage.length()==0))
					return "";
				JournalsLibrary.JournalEntry quotedEntry =CMLib.database().DBReadJournalEntry(journalName, quotedMessage);
				return "<P><BLOCKQUOTE style=\"border : solid #000 1px; padding : 3px; margin-left: 1em; margin-bottom:0.2em; background: #f9f9f9 none; color: #000;\">"
				    +"<FONT SIZE=-1>Quoted from "+quotedEntry.from
					+" &nbsp;("+CMLib.time().date2String(quotedEntry.date)+"):</FONT><HR>"
				    + "<I>"+quotedEntry.msg + "</I></BLOCKQUOTE></P>";
			}
			else
			if(parms.containsKey("KEY"))
                return clearWebMacros(entry.key);
			else
			if(parms.containsKey("FROM"))
                return clearWebMacros(entry.from);
			else
			if(parms.containsKey("MSGICON"))
                return clearWebMacros(entry.msgIcon);
			else
			if(parms.containsKey("MSGTYPEICON"))
			{
				if(entry.attributes==0)
					return "images/doc.gif";
				if(CMath.bset(entry.attributes, JournalsLibrary.JournalEntry.ATTRIBUTE_STUCKY))
					return "images/doclock.gif";
				if(CMath.bset(entry.attributes, JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED))
					return "images/docstar.gif";
				return "images/docunknown.gif";
			}
			else
			if(parms.containsKey("CARDINAL"))
                return clearWebMacros(entry.cardinal+"");
			else
			if(parms.containsKey("DATE"))
				return CMLib.time().date2String(entry.date);
			else
			if(parms.containsKey("REPLIES"))
                return clearWebMacros(entry.replies+"");
			else
			if(parms.containsKey("VIEWS"))
                return clearWebMacros(entry.views+"");
			else
			if(parms.containsKey("DATEPOSTED"))
			{
				Calendar meC=Calendar.getInstance();
				Calendar C=Calendar.getInstance();
				meC.setTimeInMillis(entry.update);
				if(Calendar.getInstance().get(Calendar.YEAR)!=meC.get(Calendar.YEAR))
					return CMLib.time().date2Date2String(entry.update);
				String dateString = CMLib.time().date2MonthDateString(entry.date, false);
				String todayString = CMLib.time().date2MonthDateString(System.currentTimeMillis(), false);
				if(dateString.equals(todayString))
					return "Today";
				C.add(Calendar.DATE, -1);
				String yesterdayString = CMLib.time().date2MonthDateString(C.getTimeInMillis(), false);
				if(dateString.equals(yesterdayString))
					return "Yesterday";
				return dateString;
			}
			else
			if(parms.containsKey("TIMEPOSTED"))
			{
				return CMLib.time().date2APTimeString(entry.date);
			}
			else
			if(parms.containsKey("DATEUPDATED"))
			{
				Calendar meC=Calendar.getInstance();
				Calendar C=Calendar.getInstance();
				meC.setTimeInMillis(entry.update);
				if(Calendar.getInstance().get(Calendar.YEAR)!=meC.get(Calendar.YEAR))
					return CMLib.time().date2Date2String(entry.update);
				String dateString = CMLib.time().date2MonthDateString(entry.update, false);
				String todayString = CMLib.time().date2MonthDateString(System.currentTimeMillis(), false);
				if(dateString.equals(todayString))
					return "Today";
				C.add(Calendar.DATE, -1);
				String yesterdayString = CMLib.time().date2MonthDateString(C.getTimeInMillis(), false);
				if(dateString.equals(yesterdayString))
					return "Yesterday";
				return dateString;
				
			}
			else
			if(parms.containsKey("TIMEUPDATED"))
			{
				return CMLib.time().date2APTimeString(entry.update);
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
                return clearWebMacros(entry.subj);
			else
			if(parms.containsKey("MESSAGE"))
			{
				String s=entry.msg;
				if(parms.containsKey("NOREPLIES"))
				{
					int x=s.indexOf(JournalsLibrary.JOURNAL_BOUNDARY);
					if(x>=0) s=s.substring(0,x);
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
				if((entry.parent==null)||(entry.parent.length()==0))
					CMLib.database().DBViewJournalMessage(entry.key, ++entry.views);
                return s;
			}
            if(parms.containsKey("EMAILALLOWED"))
                return ""+((entry.from.length()>0)
                        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));
		}
		return "";
	}
}
