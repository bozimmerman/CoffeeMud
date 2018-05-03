package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class Mime extends ActiveTicker
{
	protected volatile boolean disabled=false;
	protected volatile CMMsg lastMsg=null;

	@Override
	public String ID()
	{
		return "Mime";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_MOBILITY;
	}

	@Override
	public String accountForYourself()
	{
		return "miming";
	}

	public Mime()
	{
		super();
		minTicks=1; maxTicks=1; chance=100;
		tickReset();
	}

	@Override
	protected int canImproveCode(){return Behavior.CAN_MOBS
										  |Behavior.CAN_EXITS
										  |Behavior.CAN_ITEMS
										  |Behavior.CAN_ROOMS;}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((affecting instanceof MOB)&&(!canFreelyBehaveNormal(affecting)))
			return;
		if(disabled)
			return;
		if(((!(affecting instanceof MOB))||(!msg.amISource((MOB)affecting)))
		&&(msg.sourceMinor()==CMMsg.TYP_EMOTE)
		||(msg.tool() instanceof Social))
			lastMsg=msg;
	}

	public void fixSNameTo(CMMsg msg, MOB sMOB, Environmental ticking)
	{
		//String src=msg.sourceMessage();
		String trg=msg.targetMessage();
		String oth=msg.othersMessage();
		//if(src!=null) src=CMStrings.replaceAll(src,"<S-NAME>",ticking.name());
		//if(src!=null) src=CMStrings.replaceAll(src,"You ",ticking.name()+" ");
		//if(src!=null) src=CMStrings.replaceAll(src,"Your ",ticking.name()+"`s ");
		if(trg!=null)
			trg=CMStrings.replaceAll(trg,"<S-NAME>",ticking.name());
		if(oth!=null)
			oth=CMStrings.replaceAll(oth,"<S-NAME>",ticking.name());
		//if(src!=null) src=CMStrings.replaceAll(src,"<S-HIM-HERSELF>","itself");
		if(trg!=null)
			trg=CMStrings.replaceAll(trg,"<S-HIM-HERSELF>","itself");
		if(oth!=null)
			oth=CMStrings.replaceAll(oth,"<S-HIM-HERSELF>","itself");
		//if(src!=null) src=CMStrings.replaceAll(src,"<S-HIS-HERSELF>","itself");
		if(trg!=null)
			trg=CMStrings.replaceAll(trg,"<S-HIS-HERSELF>","itself");
		if(oth!=null)
			oth=CMStrings.replaceAll(oth,"<S-HIS-HERSELF>","itself");
		//if(src!=null) src=CMStrings.replaceAll(src,"<S-HIM-HER>","it");
		if(trg!=null)
			trg=CMStrings.replaceAll(trg,"<S-HIM-HER>","it");
		if(oth!=null)
			oth=CMStrings.replaceAll(oth,"<S-HIM-HER>","it");
		//if(src!=null) src=CMStrings.replaceAll(src,"<S-HE-SHE>","it");
		if(trg!=null)
			trg=CMStrings.replaceAll(trg,"<S-HE-SHE>","it");
		if(oth!=null)
			oth=CMStrings.replaceAll(oth,"<S-HE-SHE>","it");
		//if(src!=null) src=CMStrings.replaceAll(src,"<S-HIS-HER>","its");
		if(trg!=null)
			trg=CMStrings.replaceAll(trg,"<S-HIS-HER>","its");
		if(oth!=null)
			oth=CMStrings.replaceAll(oth,"<S-HIS-HER>","its");
		msg.modify(sMOB,sMOB,msg.tool(),
				   msg.sourceCode(),oth,
				   msg.targetCode(),trg,
				   msg.othersCode(),oth);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		CMMsg msg=lastMsg;
		if(msg==null)
			return true;
		lastMsg=null;
		if(((ticking instanceof MOB)&&(!canFreelyBehaveNormal(ticking)))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.EMOTERS))
		||(!canAct(ticking,tickID)))
			return true;
		msg=(CMMsg)msg.copyOf();
		final MOB sMOB=msg.source();
		if(msg.sourceMinor()==CMMsg.TYP_EMOTE)
		{
			if(ticking instanceof MOB)
				msg.modify((MOB)ticking,msg.target(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				final MOB newSMOB=CMClass.getFactoryMOB();
				newSMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
				newSMOB.setName(ticking.name());
				newSMOB.recoverCharStats();
				msg.modify(newSMOB,msg.source(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			}
		}
		else
		if(msg.tool() instanceof Social)
		{
			MOB target=null;
			if(msg.target() instanceof MOB)
				target=msg.source();
			if(ticking instanceof MOB)
				msg.modify((MOB)ticking,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				final MOB newSMOB=CMClass.getFactoryMOB();
				newSMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
				newSMOB.setName(ticking.name());
				newSMOB.recoverCharStats();
				msg.modify(newSMOB,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			}
		}
		else
			return true;
		disabled=true;
		if((sMOB.location()!=null)
		&&(sMOB.location().okMessage(sMOB,msg)))
		{
			if(msg.source().location()==null)
			{
				msg.source().setLocation(sMOB.location());
				sMOB.location().send(msg.source(),msg);
				msg.source().setLocation(null);
			}
			else
				sMOB.location().send(msg.source(),msg);
		}
		disabled=false;
		return true;
	}
}
