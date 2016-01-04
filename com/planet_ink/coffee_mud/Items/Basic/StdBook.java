package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2008-2016 Bo Zimmerman

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

public class StdBook extends StdItem
{
	@Override public String ID(){    return "StdBook";}
	public StdBook()
	{
		super();
		setName("a book");
		setDisplayText("a book sits here.");
		setDescription("Enter `READ [NUMBER] [BOOK]` to read a chapter.%0D%0AUse your WRITE skill to add new chapters. ");
		material=RawMaterial.RESOURCE_PAPER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	protected MOB lastReadTo=null;
	protected long lastDateRead=-1;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
		{
			final String adminReq=getAdminReq().trim();
			final boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,msg.source(),true);
			if((!CMLib.masking().maskCheck(getWriteReq(),msg.source(),true))
			&&(!admin)
			&&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
			{
				msg.source().tell(L("You are not allowed to write on @x1",name()));
				return false;
			}
			return true;
		}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READ:
			if(!CMLib.flags().canBeSeenBy(this,mob))
				mob.tell(L("You can't see that!"));
			else
			if((!mob.isMonster())
			&&(mob.playerStats()!=null))
			{
				final String adminReq=getAdminReq().trim();
				final boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true);
				final long lastTime=mob.playerStats().getLastDateTime();
				if((admin)&&(!CMLib.masking().maskCheck(getReadReq(),mob,true)))
				{
					mob.tell(L("You are not allowed to read @x1.",name()));
					return;
				}
				int which=-1;
				boolean newOnly=false;
				boolean all=false;
				final Vector<String> parse=CMParms.parse(msg.targetMessage());
				for(int v=0;v<parse.size();v++)
				{
					final String s=parse.elementAt(v);
					if(CMath.s_long(s)>0)
						which=CMath.s_int(msg.targetMessage());
					else
					if(s.equalsIgnoreCase("NEW"))
						newOnly=true;
					else
					if(s.equalsIgnoreCase("ALL")||s.equalsIgnoreCase("OLD"))
						all=true;
				}
				final Triad<String,String,StringBuffer> read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
				boolean megaRepeat=true;
				while(megaRepeat)
				{
					megaRepeat=false;
					final StringBuffer entry=read.third;
					if(entry.charAt(0)=='#')
					{
						which=-1;
						entry.setCharAt(0,' ');
					}
					if((entry.charAt(0)=='*')
					   ||(admin)
					   ||(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS)))
						entry.setCharAt(0,' ');
					else
					if((newOnly)&&(msg.value()>0))
						return;
					mob.tell(entry.toString()+"\n\r");
					if((entry.toString().trim().length()>0)
					&&(which>0)
					&&(CMLib.masking().maskCheck(getWriteReq(),mob,true)
						||(admin)
						||(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
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
				final String adminReq=getAdminReq().trim();
				final boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true);
				if(!mob.isMonster())
				{
					final String to="ALL";
					final String subject=mob.session().prompt(L("Enter the name of the chapter (Chapter 1: Start of book),etc : "));
					if(subject.trim().length()==0)
					{
						mob.tell(L("Aborted."));
						return;
					}
					final String messageTitle="The contents of this chapter";
					mob.session().println(L("\n\rEnter the contents of this chapter:"));
					final List<String> vbuf=new Vector<String>();
					if(CMLib.journals().makeMessage(mob, messageTitle, vbuf, true)==JournalsLibrary.MsgMkrResolution.CANCELFILE)
					{
						mob.tell(L("Aborted."));
						return;
					}
					final String message=CMParms.combineWith(vbuf, "\\n");
					if(message.startsWith("<cmvp>")
					&&(!admin)
					&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))))
					{
						mob.tell(L("Illegal code, aborted."));
						return;
					}

					CMLib.database().DBWriteJournal(Name(),mob.Name(),to,subject,message);
					mob.tell(L("Chapter added."));
				}
				return;
			}
			catch(final IOException e)
			{
				Log.errOut("JournalItem",e.getMessage());
			}
			return;
		}
		super.executeMsg(myHost,msg);
	}

	public Triad<String,String,StringBuffer> DBRead(MOB readerMOB, String Journal, int which, long lastTimeDate, boolean newOnly, boolean all)
	{
		final StringBuffer buf=new StringBuffer("");
		final Triad<String,String,StringBuffer> reply=new Triad<String,String,StringBuffer>("","",new StringBuffer(""));
		final List<JournalEntry> journal=CMLib.database().DBReadJournalMsgs(Journal);
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append(L("\n\rTable of Contents\n\r"));
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journal==null)
			{
				reply.first="";
				reply.second="";
				reply.third = buf;
				return reply;
			}
		}

		if((which<0)||(which>=journal.size()))
		{
			if(journal.size()>0)
			{
				reply.first = journal.get(0).from();
				reply.second = journal.get(0).subj();
			}
			final Vector<Object> selections=new Vector<Object>();
			for(int j=0;j<journal.size();j++)
			{
				final JournalEntry entry=journal.get(j);
				final String from=entry.from();
				final String to=entry.to();
				final String subject=entry.subj();
				final StringBuffer selection=new StringBuffer("");
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
			int numToAdd=CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
			if((numToAdd==0)||(all))
				numToAdd=Integer.MAX_VALUE;
			for(int v=selections.size()-1;v>=0;v--)
			{
				if (numToAdd == 0)
				{
					selections.setElementAt("", v);
					continue;
				}
				final StringBuffer str = (StringBuffer) selections.elementAt(v);
				if ((newOnly) && (str.charAt(0) != '*'))
				{
					selections.setElementAt("", v);
					continue;
				}
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
				buf.append(L("\n\rUse READ ALL [BOOK] to see missing chapters."));
		}
		else
		{
			final JournalEntry entry=journal.get(which);
			final String from=entry.from();
			final String to=entry.to();
			final String subject=entry.subj();
			String message=entry.msg();

			reply.first = entry.from();
			reply.second = entry.subj();

			//String compdate=(String)entry.elementAt(6);
			final boolean mineAble=to.equalsIgnoreCase(readerMOB.Name())
							||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),readerMOB,true)))
							||from.equalsIgnoreCase(readerMOB.Name());
			if(mineAble)
				buf.append("*");
			else
				buf.append(" ");
			try
			{
				if(message.startsWith("<cmvp>"))
					message=new String(CMLib.webMacroFilter().virtualPageFilter(message.substring(6).getBytes()));
			}
			catch(final HTTPRedirectException e){}

			if(to.equals("ALL")||mineAble)
				buf.append("\n\r"+subject
						   +"\n\r"+message);
		}
		reply.third = buf;
		return reply;
	}

	private String getParm(String parmName)
	{
		if(readableText().length()==0)
			return "";
		final Map<String,String> h=CMParms.parseEQParms(readableText().toUpperCase(), new String[]{"READ","WRITE","REPLY","ADMIN"});
		String req=h.get(parmName.toUpperCase().trim());
		if(req==null)
			req="";
		return req;
	}

	protected String getReadReq()
	{
		return getParm("READ");
	}

	protected String getWriteReq()
	{
		return getParm("WRITE");
	}

	private String getAdminReq()
	{
		return getParm("ADMIN");
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, true);
		super.recoverPhyStats();
	}
}
