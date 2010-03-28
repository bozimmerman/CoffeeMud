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
public class JournalFunction extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public JournalsLibrary.JournalEntry getNextEntry(Vector<JournalsLibrary.JournalEntry> info, String key)
	{
		if(info==null)
			return null;
		for(Enumeration<JournalsLibrary.JournalEntry> e=info.elements();e.hasMoreElements();)
		{
			JournalsLibrary.JournalEntry entry = e.nextElement();
			if((key == null)||(key.length()==0))
				return entry;
			if(entry.key.equalsIgnoreCase(key))
			{
				if(e.hasMoreElements())
					return e.nextElement();
				return null;
			}
		}
		return null;
	}
	
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
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String journalName=httpReq.getRequestParameter("JOURNAL");
		if(journalName==null) return "Function not performed -- no Journal specified.";
		String page=httpReq.getRequestParameter("JOURNALPAGE");
		if((page==null)||(page.trim().length()==0))
			page="0";
		
		JournalsLibrary.ForumJournal forum = CMLib.journals().getForumJournal(journalName);
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(CMLib.journals().isArchonJournalName(journalName))
		{
			if((M==null)||(!CMSecurity.isASysOp(M)))
			    return " @break@";
		}
		String from="Unknown";
		if(M!=null) from=M.Name();
		if(parms.containsKey("NEWPOST"))
		{
			if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.POST)))
				return "Post not submitted -- Unauthorized.";
			String to=httpReq.getRequestParameter("TO");
			if((to==null)||(M==null)||(to.equalsIgnoreCase("all"))) to="ALL";
			if((!to.equals("ALL"))&&(!to.toUpperCase().trim().startsWith("MASK=")))
			{
				if(!CMLib.players().playerExists(to))
					return "Post not submitted -- TO user does not exist.  Try 'All'.";
				to=CMStrings.capitalizeAndLower(to);
			}
            else
            if(journalName.equalsIgnoreCase(CMProps.getVar(CMProps.SYSTEM_MAILBOX))
            &&(!CMSecurity.isAllowedEverywhere(M,"JOURNALS")))
                return "Post not submitted -- You are not authorized to send email to ALL.";
			String subject=httpReq.getRequestParameter("SUBJECT");
			if(subject.length()==0)
				return "Post not submitted -- No subject!";
			String parent=httpReq.getRequestParameter("PARENT");
			String icon=httpReq.getRequestParameter("MSGICON");
			Vector<String> flags=CMParms.parseCommas(httpReq.getRequestParameter("FLAGS"), true);
			if((flags.size()>0)&&(forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
				return "Post not submitted -- Unauthorized flags.";
			String text=httpReq.getRequestParameter("NEWTEXT");
			if(text.length()==0)
				return "Post not submitted -- No text!";
            if(journalName.equalsIgnoreCase(CMProps.getVar(CMProps.SYSTEM_MAILBOX))
            &&(CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX)>0)
            &&(!to.equalsIgnoreCase("ALL")))
            {
                int count=CMLib.database().DBCountJournal(journalName,null,to);
                if(count>=CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX))
                    return "Post not submitted -- Mailbox is full!";
            }
            JournalsLibrary.JournalEntry msg = new JournalsLibrary.JournalEntry();
            msg.from=from;
            msg.subj=subject;
            msg.msg=text;
            msg.date=System.currentTimeMillis();
            msg.update=System.currentTimeMillis();
            msg.parent=(parent==null)?"":parent;
            msg.msgIcon=(icon==null)?"":icon;
            if(flags.contains("STUCKY"))
	            msg.attributes|=JournalsLibrary.JournalEntry.ATTRIBUTE_STUCKY;
            if(flags.contains("PROTECTED"))
	            msg.attributes|=JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED;
            msg.data="";
            msg.to=to;
			CMLib.database().DBWriteJournal(journalName,from,to,subject,text);
			httpReq.getRequestObjects().remove("JOURNAL: "+journalName+": "+page);
			if(parent!=null)
				CMLib.database().DBTouchJournalMessage(parent);
			return "Post submitted.";
		}
		Vector<JournalsLibrary.JournalEntry> info=(Vector<JournalsLibrary.JournalEntry>)httpReq.getRequestObjects().get("JOURNAL: "+journalName+": "+page);
		if(info==null)
		{
			info=CMLib.database().DBReadJournalMsgsNewerThan(journalName, null, CMath.s_long(page));
			httpReq.getRequestObjects().put("JOURNAL: "+journalName+": "+page,info);
		}
		String msgKey=httpReq.getRequestParameter("JOURNALMESSAGE");
		int cardinalNumber = CMath.s_int(httpReq.getRequestParameter("JOURNALCARDINAL"));
        String srch=httpReq.getRequestParameter("JOURNALMESSAGESEARCH");
        if(srch!=null) 
        	srch=srch.toLowerCase();
		boolean doThemAll=parms.containsKey("EVERYTHING");
		if(doThemAll)
		{
			JournalsLibrary.JournalEntry entry = getNextEntry(info, null);
			if(entry==null)
				msgKey="";
			else
				msgKey=entry.key;
			cardinalNumber=1;
		}
		StringBuffer messages=new StringBuffer("");
		boolean keepProcessing=((msgKey!=null)&&(msgKey.length()>0));
		String fieldSuffix="";
		while(keepProcessing)
		{
			if(doThemAll)
			{
				parms.clear();
				parms.put("EVERYTHING","EVERYTHING");
				String fate=httpReq.getRequestParameter("FATE"+msgKey);
				String replyemail=httpReq.getRequestParameter("REPLYEMAIL"+msgKey);
				cardinalNumber = CMath.s_int(httpReq.getRequestParameter("CARDINAL"+msgKey));
				if((fate!=null)&&(fate.length()>0)&&(CMStrings.isUpperCase(fate)))
					parms.put(fate,fate);
				if((replyemail!=null)&&(replyemail.length()>0)&&(CMStrings.isUpperCase(replyemail)))
					parms.put(replyemail,replyemail);
				if(parms.size()==1)
				{
					JournalsLibrary.JournalEntry entry = getNextEntry(info, msgKey);
					while((entry!=null) && (!CMLib.journals().canReadMessage(entry,srch,M,parms.contains("NOPRIV"))))
						entry = getNextEntry(info, entry.key);

					if(entry==null)
						keepProcessing=false;
					else
						msgKey=entry.key;
					continue;
				}
				fieldSuffix=msgKey;
			}
			else 
				keepProcessing=false;
			JournalsLibrary.JournalEntry entry = getEntry(info, msgKey);
			if(entry == null)
				return "Function not performed -- illegal journal message specified.<BR>";
			if(!doThemAll)
				entry.cardinal=cardinalNumber;
			String to=entry.to;
			if((M!=null)
			&&(CMSecurity.isAllowedAnywhere(M,"JOURNALS")||(to.equalsIgnoreCase(M.Name())))
			&&((forum==null)||(forum.authorizationCheck(M, ForumJournalFlags.READ))))
			{
				if(parms.containsKey("REPLY"))
				{
					if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.REPLY)))
						return "Reply not submitted -- Unauthorized.";
					String text=httpReq.getRequestParameter("NEWTEXT"+fieldSuffix);
					if((text==null)||(text.length()==0))
						messages.append("Reply to #"+cardinalNumber+" not submitted -- No text!<BR>");
					else
					{
						CMLib.database().DBWriteJournalReply(journalName,entry.key,from,"","",text);
						httpReq.getRequestObjects().remove("JOURNAL: "+journalName+": "+page);
						messages.append("Reply to #"+cardinalNumber+" submitted<BR>");
					}
				}
	            else
	            if(parms.containsKey("EMAIL"))
	            {
					if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.REPLY)))
						return "Email not submitted -- Unauthorized.";
	                String replyMsg=httpReq.getRequestParameter("NEWTEXT"+fieldSuffix);
	                if(replyMsg.length()==0)
						messages.append("Email to #"+cardinalNumber+" not submitted -- No text!<BR>");
	                else
	                {
		                String toName=entry.from;
		                MOB toM=CMLib.players().getLoadPlayer(toName);
		                if((toM==null)||(toM.playerStats()==null)||(toM.playerStats().getEmail().indexOf("@")<0))
							messages.append("Player '"+toName+"' does not exist, or has no email address.<BR>");
		                else
		                {
			                CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
			                                                  M.Name(),
			                                                  toM.Name(),
			                                                  "RE: "+entry.subj,
			                                                  replyMsg);
			                httpReq.getRequestObjects().remove("JOURNAL: "+journalName+": "+page);
							messages.append("Email to #"+cardinalNumber+" queued<BR>");
		                }
	                }
	            }
				if(parms.containsKey("DELETE"))
				{
					if(M==null)	
						messages.append("Can not delete #"+cardinalNumber+"-- required logged in user.<BR>");
					else
					{
						if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
							return "Email not submitted -- Unauthorized.";
						CMLib.database().DBDeleteJournal(journalName,entry.key);
						httpReq.addRequestParameters("JOURNALMESSAGE","");
						httpReq.getRequestObjects().remove("JOURNAL: "+journalName+": "+page);
						messages.append("Message #"+cardinalNumber+" deleted.<BR>");
					}
				}
				else
	            if(CMSecurity.isAllowedAnywhere(M,"JOURNALS"))
	            {
	                if(parms.containsKey("TRANSFER"))
	                {
						if((forum!=null)&&(!forum.authorizationCheck(M, ForumJournalFlags.ADMIN)))
							return "Email not submitted -- Unauthorized.";
	                    String journal=httpReq.getRequestParameter("NEWJOURNAL"+fieldSuffix);
	                    if((journal==null) || (journal.length()==0))
	    					messages.append("Transfer #"+cardinalNumber+" not completed -- No journal!<BR>");
	                    String realName=null;
	                    if(journal!=null)
		                    for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		                    {
		                    	JournalsLibrary.CommandJournal CMJ=e.nextElement();
		                        if(journal.equalsIgnoreCase(CMJ.NAME())
		                        ||journal.equalsIgnoreCase(CMJ.NAME()+"s")
		                        ||journal.equalsIgnoreCase(CMJ.JOURNAL_NAME()))
		                        {
		                            realName=CMJ.JOURNAL_NAME();
		                            break;
		                        }
		                    }
	                    if(realName==null)
	                        realName=CMLib.database().DBGetRealJournalName(journal);
	                    if((realName==null)&&(journal!=null))
	                        realName=CMLib.database().DBGetRealJournalName(journal.toUpperCase());
	                    if(realName==null)
	    					messages.append("The journal '"+journal+"' does not presently exist.  Aborted.<BR>");
	                    else
	                    {
		                    CMLib.database().DBDeleteJournal(journalName,entry.key);
		                    CMLib.database().DBWriteJournal(realName,entry);
		                    httpReq.addRequestParameters("JOURNALMESSAGE","");
		                    httpReq.getRequestObjects().remove("JOURNAL: "+journalName+": "+page);
							messages.append("Message #"+cardinalNumber+" transferred<BR>");
	                    }
	                }
	            }
	            else
					messages.append("You are not allowed to perform this function on message #"+cardinalNumber+".<BR>");
			}
			if(keepProcessing)
			{
				cardinalNumber++;
				entry = getNextEntry(info, msgKey);
				while((entry!=null) && (!CMLib.journals().canReadMessage(entry,srch,M,parms.contains("NOPRIV"))))
					entry = getNextEntry(info, entry.key);
				if(entry==null)
					keepProcessing=false;
				else
					msgKey=entry.key;
			}
		}
        return messages.toString();
	}
}
