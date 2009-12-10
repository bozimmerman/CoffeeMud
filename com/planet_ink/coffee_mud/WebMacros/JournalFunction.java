package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("JOURNAL");
		if(last==null) return "Function not performed -- no Journal specified.";
		
		Vector info=(Vector)httpReq.getRequestObjects().get("JOURNAL: "+last);
		if(info==null)
		{
			info=CMLib.database().DBReadJournalMsgs(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		MOB M=CMLib.players().getLoadPlayer(Authenticate.getLogin(httpReq));
		if(JournalMessageNext.isProtectedJournal(last))
		{
			if((M==null)||(!CMSecurity.isASysOp(M)))
			    return " @break@";
		}
		String from="Unknown";
		if(M!=null) from=M.Name();
		if(parms.containsKey("NEWPOST"))
		{
			String to=httpReq.getRequestParameter("TO");
			if((to==null)||(M==null)||(to.equalsIgnoreCase("all"))) to="ALL";
			if((!to.equals("ALL"))&&(!to.toUpperCase().trim().startsWith("MASK=")))
			{
				if(!CMLib.database().DBUserSearch(null,to))
					return "Post not submitted -- TO user does not exist.  Try 'All'.";
			}
            else
            if(last.equalsIgnoreCase(CMProps.getVar(CMProps.SYSTEM_MAILBOX))
            &&(!CMSecurity.isAllowedEverywhere(M,"JOURNALS")))
                return "Post not submitted -- You are not authorized to send email to ALL.";
			String subject=httpReq.getRequestParameter("SUBJECT");
			if(subject.length()==0)
				return "Post not submitted -- No subject!";
			String text=httpReq.getRequestParameter("NEWTEXT");
			if(text.length()==0)
				return "Post not submitted -- No text!";
            if(last.equalsIgnoreCase(CMProps.getVar(CMProps.SYSTEM_MAILBOX))
            &&(CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX)>0)
            &&(!to.equalsIgnoreCase("ALL")))
            {
                int count=CMLib.database().DBCountJournal(last,null,to);
                if(count>=CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX))
                    return "Post not submitted -- Mailbox is full!";
            }
			CMLib.database().DBWriteJournal(last,from,to,subject,text,-1);
			httpReq.getRequestObjects().remove("JOURNAL: "+last);
			return "Post submitted.";
		}
		String lastlast=httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=(parms.containsKey("EVERYTHING"))?info.size():0;
		StringBuffer messages=new StringBuffer("");
		String textFieldAddendum="";
		boolean keepProcessing=true;
		while(keepProcessing)
		{
			if(parms.containsKey("EVERYTHING"))
			{
				num--;
				parms.clear();
				parms.put("EVERYTHING","EVERYTHING");
				String fate=httpReq.getRequestParameter("FATE"+num);
				String replyemail=httpReq.getRequestParameter("REPLYEMAIL"+num);
				if((fate!=null)&&(fate.length()>0)&&(CMStrings.isUpperCase(fate)))
					parms.put(fate,fate);
				if((replyemail!=null)&&(replyemail.length()>0)&&(CMStrings.isUpperCase(replyemail)))
					parms.put(replyemail,replyemail);
				keepProcessing=num>=0;
				if(parms.size()==1) continue;
				textFieldAddendum=Integer.toString(num);
			}
			else 
			{
				keepProcessing=false;
				if(lastlast!=null) num=CMath.s_int(lastlast);
				if((num<0)||(num>=info.size()))
					return "Function not performed -- illegal journal message specified.<BR>";
			}
			JournalsLibrary.JournalEntry entry = (JournalsLibrary.JournalEntry)info.elementAt(num);
			String to=entry.to;
			if((M!=null)&&(CMSecurity.isAllowedAnywhere(M,"JOURNALS")||(to.equalsIgnoreCase(M.Name()))))
			{
				if(parms.containsKey("REPLY"))
				{
					String text=httpReq.getRequestParameter("NEWTEXT"+textFieldAddendum);
					if((text==null)||(text.length()==0))
						messages.append("Reply to #"+num+" not submitted -- No text!<BR>");
					else
					{
						CMLib.database().DBWriteJournal(last,from,"","",text,num);
						httpReq.getRequestObjects().remove("JOURNAL: "+last);
						messages.append("Reply to #"+num+" submitted<BR>");
					}
				}
	            else
	            if(parms.containsKey("EMAIL"))
	            {
	                String replyMsg=httpReq.getRequestParameter("NEWTEXT"+textFieldAddendum);
	                if(replyMsg.length()==0)
						messages.append("Email to #"+num+" not submitted -- No text!<BR>");
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
			                                                  replyMsg,-1);
			                httpReq.getRequestObjects().remove("JOURNAL: "+last);
							messages.append("Email to #"+num+" queued<BR>");
		                }
	                }
	            }
				if(parms.containsKey("DELETE"))
				{
					if(M==null)	
						messages.append("Can not delete #"+num+"-- required logged in user.<BR>");
					else
					{
						CMLib.database().DBDeleteJournal(last,num);
						httpReq.addRequestParameters("JOURNALMESSAGE","");
						httpReq.getRequestObjects().remove("JOURNAL: "+last);
						messages.append("Message #"+num+" deleted.<BR>");
					}
				}
				else
	            if(CMSecurity.isAllowedAnywhere(M,"JOURNALS"))
	            {
	                if(parms.containsKey("TRANSFER"))
	                {
	                    String journal=httpReq.getRequestParameter("NEWJOURNAL"+textFieldAddendum);
	                    if((journal==null) || (journal.length()==0))
	                        journal=httpReq.getRequestParameter("NEWJOURNAL"+textFieldAddendum+lastlast);
	                    if((journal==null) || (journal.length()==0))
	    					messages.append("Transfer #"+num+" not completed -- No journal!<BR>");
	                    String realName=null;
	                    if(journal!=null)
		                    for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().journals();e.hasMoreElements();)
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
		                    Vector journal2=CMLib.database().DBReadJournalMsgs(last);
		                    JournalsLibrary.JournalEntry entry2=(JournalsLibrary.JournalEntry)journal2.elementAt(num);
		                    String from2=(String)entry2.from;
		                    String to2=(String)entry2.to;
		                    String subject=(String)entry2.subj;
		                    String message=(String)entry2.msg;
		                    CMLib.database().DBDeleteJournal(last,num);
		                    CMLib.database().DBWriteJournal(realName,
		                                                      from2,
		                                                      to2,
		                                                      subject,
		                                                      message,-1);
		                    httpReq.addRequestParameters("JOURNALMESSAGE","");
		                    httpReq.getRequestObjects().remove("JOURNAL: "+last);
							messages.append("Message #"+num+" transferred<BR>");
	                    }
	                }
	            }
	            else
					messages.append("You are not allowed to perform this function on message #"+num+".<BR>");
			}
		}
        return messages.toString();
	}
}
