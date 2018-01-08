package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Skill_Dirt extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Dirt";
	}

	private final static String	localizedName	= CMLib.lang().L("Dirt");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Dirt in your eyes)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DIRT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_DIRTYFIGHTING;
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

	boolean	doneTicking	= false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SEE);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("You can see again!"));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(!hereOK(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_FOOT)<=0)
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0))
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(!CMLib.flags().canSee((MOB)target)))
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isSleeping(target))
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isFlying(mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean hereOK(MOB mob)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(CMath.bset(R.getClimateType(),Places.CLIMASK_WET)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		 ||(CMLib.flags().isWateryRoom(R))
		 ||(R.domainType()==Room.DOMAIN_INDOORS_AIR)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_MAGIC)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_METAL)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_CAVE)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_WOOD)
		 ||(R.getAtmosphere()==RawMaterial.RESOURCE_SAND)
		 ||(R.getAtmosphere()==RawMaterial.RESOURCE_DUST))
			return false;
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if((target==null)||(R==null))
			return false;

		if(!hereOK(mob))
		{
			if(!auto)
				mob.tell(L("There's no dirt here to kick!"));
			return false;
		}

		if((!auto)&&(mob.charStats().getBodyPart(Race.BODY_FOOT)<=0))
		{
			mob.tell(L("You need feet to kick."));
			return false;
		}

		if((!auto)&&(target.charStats().getBodyPart(Race.BODY_EYE)==0))
		{
			mob.tell(L("@x1 has no eyes, and would not be affected.",target.name(mob)));
			return false;
		}

		if(CMLib.flags().isSleeping(target))
		{
			CMLib.commands().forceStandardCommand(target, "Wake", new XVector<String>("Wake"));
			if(CMLib.flags().isSleeping(target))
			{
				if(!auto)
					mob.tell(L("@x1 has @x2 eyes closed.",target.name(mob),target.charStats().hisher()));
				return false;
			}
		}

		if((!auto)&&CMLib.flags().isFlying(mob))
		{
			mob.tell(L("You are too far from the ground to kick dirt."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,-(target.charStats().getStat(CharStats.STAT_DEXTERITY)*3)+(2*getXLEVELLevel(mob)),auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("Dirt flies at <T-NAME>!"):L("^F^<FIGHT^><S-NAME> kick(s) dirt at <T-NAMESELF>.^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				R.show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> blinded!"));
				maliciousAffect(mob,target,asLevel,3+(getXLEVELLevel(mob)/3),-1);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to kick dirt at <T-NAMESELF>, but miss(es)."));
		return success;
	}
}
