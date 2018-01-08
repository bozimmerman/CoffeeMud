package com.planet_ink.coffee_mud.Abilities.Archon;
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
   Copyright 2011-2018 Bo Zimmerman

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

public class Archon_Injure extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Injure";
	}

	private final static String localizedName = CMLib.lang().L("Injure");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings = I(new String[] { "INJURE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String part=null;
		if((commands.size()<3)&&(mob.isInCombat()))
		{
			part=CMParms.combine(commands).toUpperCase();
			commands.clear();
		}
		else
		if((commands.size()==2)||(commands.size()==3))
		{
			part=CMParms.combine(commands,1).toUpperCase();
			commands.remove(1);
			if(commands.size()>1)
				commands.remove(1);
		}
		else
		if(commands.size()>3)
		{
			part=CMParms.combine(commands,2).toUpperCase();
			commands.remove(1);
		}
		final MOB target=getTargetAnywhere(mob,commands,givenTarget,false,true,true);
		if(target==null)
			return false;
		LimbDamage ampuA=(LimbDamage)target.fetchEffect("Amputation");
		if(ampuA==null)
		{
			ampuA=(LimbDamage)CMClass.getAbility("Amputation");
			ampuA.setAffectedOne(target);
		}
		final List<String> remainingLimbList=ampuA.unaffectedLimbSet();
		if(target.charStats().getBodyPart(Race.BODY_HEAD)>0)
			remainingLimbList.add("head");
		if(target.charStats().getBodyPart(Race.BODY_TORSO)>0)
			remainingLimbList.add("torso");
		String gone=null;
		for(int i=0;i<remainingLimbList.size();i++)
		{
			if((part==null)||remainingLimbList.get(i).toUpperCase().endsWith(part))
			{
				gone=remainingLimbList.get(i);
				break;
			}
		}
		if(gone==null)
		{
			if(part==null)
				mob.tell(target,null,null,L("<S-NAME> has no parts."));
			else
				mob.tell(target,null,null,L("<S-NAME> has no part called '@x1'.",part.toLowerCase()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("A stink cloud surrounds <T-NAME>!"):L("^F<S-NAME> injure(s) <T-YOUPOSS> @x1.^?",gone.toLowerCase()));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Log.sysOut("Archon_Injure",mob.Name()+" injures "+target.name()+".");
				Ability injuryA=CMClass.getAbility("Injury");
				if(injuryA!=null)
				{
					final int percentOff=target.maxState().getHitPoints()/5;
					if(target.curState().getHitPoints()>(target.curState().getHitPoints()-percentOff))
						target.curState().setHitPoints(target.curState().getHitPoints()-percentOff);
					injuryA.invoke(mob,new XVector<String>(),target,true,0);
					injuryA=target.fetchEffect("Injury");
					if(injuryA!=null)
						injuryA.setMiscText("+"+gone.toLowerCase()+"=20");
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to injure <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
