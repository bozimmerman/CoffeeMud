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
   Copyright 2000-2007 Bo Zimmerman

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
			info=CMLib.database().DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		MOB M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
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
		int num=0;
		if(lastlast!=null) num=CMath.s_int(lastlast);
		if((num<0)||(num>=info.size()))
			return "Function not performed -- illegal journal message specified.";
		String to= ((String)((Vector)info.elementAt(num)).elementAt(3));
		if(CMSecurity.isAllowedAnywhere(M,"JOURNALS")||(to.equalsIgnoreCase(M.Name())))
		{
			if(parms.containsKey("DELETE"))
			{
				if(M==null)	return "Can not delete -- required logged in user.";
				CMLib.database().DBDeleteJournal(last,num);
				httpReq.addRequestParameters("JOURNALMESSAGE","");
				httpReq.getRequestObjects().remove("JOURNAL: "+last);
				return "Message #"+(num+1)+" deleted.";
			}
			else
			if(parms.containsKey("REPLY"))
			{
				String text=httpReq.getRequestParameter("NEWTEXT");
				if(text.length()==0)
					return "Reply not submitted -- No text!";
				CMLib.database().DBWriteJournal(last,from,"","",text,num);
				httpReq.getRequestObjects().remove("JOURNAL: "+last);
				return "Reply submitted";
			}
            else
            if(parms.containsKey("EMAIL"))
            {
                String replyMsg=httpReq.getRequestParameter("NEWTEXT");
                if(replyMsg.length()==0)
                    return "Email not submitted -- No text!";
                String toName=((String)((Vector)info.elementAt(num)).elementAt(1));
                if(replyMsg.length()==0)
                    return "Email not submitted -- No text!";
                MOB toM=CMLib.map().getLoadPlayer(toName);
                if((M==null)||(M.playerStats()==null)||(M.playerStats().getEmail().indexOf("@")<0))
                    return "Player '"+toName+"' does not exist, or has no email address.";
                CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                                                  M.Name(),
                                                  toM.Name(),
                                                  "RE: "+((String)((Vector)info.elementAt(num)).elementAt(4)),
                                                  replyMsg,-1);
                httpReq.getRequestObjects().remove("JOURNAL: "+last);
                return "Email queued";
            }
            else
            if(CMSecurity.isAllowedAnywhere(M,"JOURNALS"))
            {
                if(parms.containsKey("TRANSFER"))
                {
                    String journal=httpReq.getRequestParameter("NEWJOURNAL");
                    if(journal.length()==0)
                        return "Transfer not completed -- No journal!";
                    String realName=null;
                    for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
                        if(journal.equalsIgnoreCase(CMLib.journals().getCommandJournalName(i))
                        ||journal.equalsIgnoreCase(CMLib.journals().getCommandJournalName(i)+"s"))
                        {
                            realName="SYSTEM_"+CMLib.journals().getCommandJournalName(i).toUpperCase()+"S";
                            break;
                        }
                    if(realName==null)
                        realName=CMLib.database().DBGetRealJournalName(journal);
                    if(realName==null)
                        realName=CMLib.database().DBGetRealJournalName(journal.toUpperCase());
                    if(realName==null)
                        return  "The journal '"+journal+"' does not presently exist.  Aborted.";
                    Vector journal2=CMLib.database().DBReadJournal(last);
                    Vector entry2=(Vector)journal2.elementAt(num);
                    String from2=(String)entry2.elementAt(DatabaseEngine.JOURNAL_FROM);
                    String to2=(String)entry2.elementAt(DatabaseEngine.JOURNAL_TO);
                    String subject=(String)entry2.elementAt(DatabaseEngine.JOURNAL_SUBJ);
                    String message=(String)entry2.elementAt(DatabaseEngine.JOURNAL_MSG);
                    CMLib.database().DBDeleteJournal(last,num);
                    CMLib.database().DBWriteJournal(realName,
                                                      from2,
                                                      to2,
                                                      subject,
                                                      message,-1);
                    httpReq.addRequestParameters("JOURNALMESSAGE","");
                    httpReq.getRequestObjects().remove("JOURNAL: "+last);
                    return "Message transferred";
                }
                return "";
            }
			else
				return "";
		}
		return "You are not allowed to perform this function.";
	}
}
