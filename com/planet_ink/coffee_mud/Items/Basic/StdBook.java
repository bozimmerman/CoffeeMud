package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;
import java.util.*;
import java.io.IOException;

/* 
   Copyright 2006-2010 Rob McClellan, Bo Zimmerman

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
public class StdBook extends StdItem
{
	public String ID(){	return "StdBook";}
	public StdBook()
	{
		super();
		setName("a book");
		setDisplayText("a book sits here.");
		setDescription("Enter `READ [NUMBER] [BOOK]` to read a chapter.%0D%0AUse your WRITE skill to add new chapters. ");
		material=RawMaterial.RESOURCE_PAPER;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		recoverEnvStats();
	}

	protected MOB lastReadTo=null;
	protected long lastDateRead=-1;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
        {
            String adminReq=getAdminReq().trim();
            boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,msg.source(),true);
			if((!CMLib.masking().maskCheck(getWriteReq(),msg.source(),true))
            &&(!admin)
            &&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS"))))
			{
				msg.source().tell("You are not allowed to write on "+name());
				return false;
			}
			return true;
        }
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READ:
			if(!CMLib.flags().canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if((!mob.isMonster())
			&&(mob.playerStats()!=null))
			{
                String adminReq=getAdminReq().trim();
                boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true);
				long lastTime=mob.playerStats().lastDateTime();
				if((admin)&&(!CMLib.masking().maskCheck(getReadReq(),mob,true)))
				{
					mob.tell("You are not allowed to read "+name()+".");
					return;
				}
				int which=-1;
				boolean newOnly=false;
				boolean all=false;
				Vector parse=CMParms.parse(msg.targetMessage());
				for(int v=0;v<parse.size();v++)
				{
				    String s=(String)parse.elementAt(v);
					if(CMath.s_long(s)>0)
						which=CMath.s_int(msg.targetMessage());
					else
					if(s.equalsIgnoreCase("NEW"))
					    newOnly=true;
					else
					if(s.equalsIgnoreCase("ALL")||s.equalsIgnoreCase("OLD"))
					    all=true;
				}
				Vector read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
				boolean megaRepeat=true;
				while(megaRepeat)
				{
				    megaRepeat=false;
					StringBuffer entry=(StringBuffer)read.lastElement();
					if(entry.charAt(0)=='#')
					{
						which=-1;
						entry.setCharAt(0,' ');
					}
					if((entry.charAt(0)=='*')
                       ||(admin)
					   ||(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")))
						entry.setCharAt(0,' ');
					else
					if((newOnly)&&(msg.value()>0))
					    return;
					mob.tell(entry.toString()+"\n\r");
					if((entry.toString().trim().length()>0)
					&&(which>0)
					&&(CMLib.masking().maskCheck(getWriteReq(),mob,true)
                        ||(admin)
                        ||(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS"))))
					{
					}
					else
					if(which<0)
						mob.tell(description());
				}
				return;
			}
			return;
		case CMMsg.TYP_WRITE:
			try
			{
                String adminReq=getAdminReq().trim();
                boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true);
				if(!mob.isMonster())
				{
					String to="ALL";
					String subject=mob.session().prompt("Enter the name of the chapter (Chapter 1: Start of book),etc : ");
					if(subject.trim().length()==0)
					{
						mob.tell("Aborted.");
						return;
					}
					String message=mob.session().prompt("\n\rEnter the contents of this chapter\n\r: ");
					if(message.trim().length()==0)
					{
						mob.tell("Aborted.");
						return;
					}
					if(message.startsWith("<cmvp>")
                    &&(!admin)
					&&(!(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS"))))
					{
						mob.tell("Illegal code, aborted.");
						return;
					}

					CMLib.database().DBWriteJournal(Name(),mob.Name(),to,subject,message);
					mob.tell("Chapter added.");
				}
				return;
			}
			catch(IOException e)
			{
				Log.errOut("JournalItem",e.getMessage());
			}
			return;
		}
		super.executeMsg(myHost,msg);
	}

	public Vector DBRead(MOB readerMOB, String Journal, int which, long lastTimeDate, boolean newOnly, boolean all)
	{
		StringBuffer buf=new StringBuffer("");
		Vector reply=new Vector();
		Vector journal=CMLib.database().DBReadJournalMsgs(Journal);
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append("\n\rTable of Contents\n\r");
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journal==null)
			{
				reply.addElement("");
				reply.addElement("");
				reply.addElement(buf);
				return reply;
			}
		}

		if((which<0)||(which>=journal.size()))
		{
			if(journal.size()>0)
			{
				reply.addElement(((JournalsLibrary.JournalEntry)journal.firstElement()).from);
				reply.addElement(((JournalsLibrary.JournalEntry)journal.firstElement()).subj);
			}
			Vector selections=new Vector();
			for(int j=0;j<journal.size();j++)
			{
				JournalsLibrary.JournalEntry entry=(JournalsLibrary.JournalEntry)journal.elementAt(j);
				String from=entry.from;
				String to=entry.to;
				String subject=entry.subj;
				StringBuffer selection=new StringBuffer("");
				if(to.equals("ALL")
                ||to.equalsIgnoreCase(readerMOB.Name())
                ||from.equalsIgnoreCase(readerMOB.Name())
                ||(to.toUpperCase().trim().startsWith("MASK=")&&CMLib.masking().maskCheck(to.trim().substring(5),readerMOB,true)))
				{
					//if(CMath.s_long(compdate)>lastTimeDate)
					//    selection.append("*");
					//else
					//if(newOnly)
					//    continue;
					//else
					//    selection.append(" ");
					selection.append(subject+"\n\r");
					selections.addElement(selection);
				}
			}
			int numToAdd=CMProps.getIntVar(CMProps.SYSTEMI_JOURNALLIMIT);
			if((numToAdd==0)||(all)) numToAdd=Integer.MAX_VALUE;
			for(int v=selections.size()-1;v>=0;v--)
			{
			    if(numToAdd==0){ selections.setElementAt("",v); continue;}
			    StringBuffer str=(StringBuffer)selections.elementAt(v);
			    if((newOnly)&&(str.charAt(0)!='*'))
			    { selections.setElementAt("",v); continue;}
			    numToAdd--;
			}
			boolean notify=false;
			for(int v=0;v<selections.size();v++)
			{
			    if(!(selections.elementAt(v) instanceof StringBuffer))
			    {
			        notify=true;
			        continue;
			    }
			    buf.append((StringBuffer)selections.elementAt(v));
			}
			if(notify)
			    buf.append("\n\rUse READ ALL [BOOK] to see missing chapters.");
		}
		else
		{
			JournalsLibrary.JournalEntry entry=(JournalsLibrary.JournalEntry)journal.elementAt(which);
			String from=entry.from;
			String to=entry.to;
			String subject=entry.subj;
			String message=entry.msg;
			
			reply.addElement(entry.from);
			reply.addElement(entry.subj);
			
			//String compdate=(String)entry.elementAt(6);
			boolean mineAble=to.equalsIgnoreCase(readerMOB.Name())
                            ||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),readerMOB,true)))
                            ||from.equalsIgnoreCase(readerMOB.Name());
			if(mineAble)
				buf.append("*");
			else
				buf.append(" ");
			try
			{
				if(message.startsWith("<cmvp>"))
					message=new String(CMLib.httpUtils().doVirtualPage(message.substring(6).getBytes()));
			}
			catch(HTTPRedirectException e){}

			if(to.equals("ALL")||mineAble)
				buf.append("\n\r"+subject
						   +"\n\r"+message);
		}
		while(reply.size()<2)
			reply.addElement("");
		reply.addElement(buf);
		return reply;
	}

    private String getParm(String parmName)
    {
        if(readableText().length()==0) return "";
        Hashtable h=CMParms.parseEQParms(readableText().toUpperCase(),
                                         new String[]{"READ","WRITE","REPLY","ADMIN"});
        String req=(String)h.get(parmName.toUpperCase().trim());
        if(req==null) req="";
        return req;
    }
    
    protected String getReadReq() { return getParm("READ");}
    protected String getWriteReq() { return getParm("WRITE");}
    private String getAdminReq() { return getParm("ADMIN");}
	public void recoverEnvStats(){CMLib.flags().setReadable(this,true); super.recoverEnvStats();}
}
