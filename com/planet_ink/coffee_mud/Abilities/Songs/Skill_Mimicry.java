package com.planet_ink.coffee_mud.Abilities.Songs;
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

public class Skill_Mimicry extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Mimicry";
	}

	private final static String localizedName = CMLib.lang().L("Mimicry");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"MIMICRY","MIMIC"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;
	}

	protected CMMsg lastMsg=null;
	protected boolean disabled=false;

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((affecting instanceof MOB)&&(!CMLib.flags().isAliveAwakeMobileUnbound((MOB)affecting,true)))
			return;
		if(disabled)
			return;

		if(((!(affecting instanceof MOB))||(!msg.amISource((MOB)affecting)))
		&&((text().length()==0)||(text().equalsIgnoreCase(msg.source().Name())))
		&&((msg.sourceMinor()!=CMMsg.TYP_EMOTE)
			||(msg.tool() instanceof Social)))
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
		if(((affected instanceof MOB)&&(!CMLib.flags().isAliveAwakeMobileUnbound((MOB)affected,true))))
			return true;
		msg=(CMMsg)msg.copyOf();
		final MOB sMOB=msg.source();
		if(msg.sourceMinor()==CMMsg.TYP_EMOTE)
		{
			if(affected instanceof MOB)
				msg.modify((MOB)affected,msg.target(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				final MOB newSMOB=CMClass.getFactoryMOB();
				newSMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
				newSMOB.setName(affected.name());
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
			if(affected instanceof MOB)
				msg.modify((MOB)affected,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				final MOB newSMOB=CMClass.getFactoryMOB();
				newSMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
				newSMOB.setName(affected.name());
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

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("<S-NAME> begin(s) mimicing <T-NAMESELF>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,0);
				final Ability A=mob.fetchEffect(ID());
				if((A!=null)&&(target!=mob))
					A.setMiscText(target.Name());
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to mimic <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
