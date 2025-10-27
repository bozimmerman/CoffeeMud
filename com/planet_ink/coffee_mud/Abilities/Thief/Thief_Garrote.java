package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Fighter.FighterGrappleSkill;
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
   Copyright 2025-2025 Bo Zimmerman

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
public class Thief_Garrote extends FighterGrappleSkill
{
	@Override
	public String ID()
	{
		return "Thief_Garrote";
	}

	private final static String	localizedName	= CMLib.lang().L("Garrote");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay1	= CMLib.lang().L("(Garroting)");
	private final static String	localizedStaticDisplay2	= CMLib.lang().L("(Garroted)");

	@Override
	public String displayText()
	{
		if(affected==invoker)
			return localizedStaticDisplay1;
		return localizedStaticDisplay2;
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

	private static final String[]	triggerStrings	= I(new String[] { "GARROTE"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_GRAPPLING;
	}

	protected String	lastMOB		= "";

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().canBeSeenBy(mob,(MOB)target))
				return Ability.QUALITY_INDIFFERENT;
			if(lastMOB.equals(target+""))
				return Ability.QUALITY_INDIFFERENT;
			if(getAWhip(mob)==null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected == invoker())&&(this.getAWhip(invoker())==null))
		{
			unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected != invoker())&&(pairedWith!=null)&&(affected instanceof MOB))
		{
			if(((MOB)affected).charStats().getBreathables().length>0)
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
		}
	}

	public boolean isAWhip(final Item I)
	{
		if(I==null)
			return false;
		if(CMLib.flags().isARope(I))
			return true;
		if((I instanceof Weapon)
		&&(((Weapon)I).weaponClassification()==Weapon.CLASS_FLAILED)
		&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER))
			return true;
		return false;
	}

	public Item getAWhip(final MOB mob)
	{
		Item I = mob.fetchWieldedItem();
		if(isAWhip(I))
			return I;
		I = mob.fetchHeldItem();
		if(isAWhip(I))
			return I;
		CMLib.commands().postDraw(mob,false,true);
		I = mob.fetchWieldedItem();
		if(isAWhip(I))
			return I;
		I = mob.fetchHeldItem();
		if(isAWhip(I))
			return I;
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Garrote whom?"));
			return false;
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(CMLib.flags().canBeSeenBy(mob,target))
		{
			mob.tell(L("@x1 is watching you too closely to do that.",target.name(mob)));
			return false;
		}
		if((!auto)
		&&(!CMLib.flags().isStanding(mob))
		&&(mob!=target))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}
		if(lastMOB.equals(target+""))
		{
			failureTell(mob,target,auto,L("<T-NAME> is watching <T-HIS-HER> back too closely to fall for that again."), commands);
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(L("You are too busy to focus on garrotting right now."));
			return false;
		}
		if(target.charStats().getBodyPart(Race.BODY_NECK)==0)
		{
			mob.tell(mob,target,null,L("You don't see a neck on <T-NAME>."));
			return false;
		}
		final Item I=getAWhip(mob);
		if(I==null)
		{
			mob.tell(mob,target,null,L("Garrote <T-HIM-HER> with what? You need to wield/hold a whip or rope!"));
			return false;
		}

		if(!super.invoke(mob,commands,target,auto,asLevel))
			return false;

		final boolean hit=(auto)
				||(super.getGrappleA(target)!=null)
				||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,0,auto)&&(hit);

		final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT),
				auto?"":L("<S-NAME> sneak(s) up behind <T-NAMESELF> with @x1!",I.name(mob)));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			lastMOB=""+target;
			if(((!success)
			||(CMLib.flags().canBeSeenBy(mob,target))
			||(msg.value()>0))
			&&(!CMLib.flags().isSleeping(target)))
			{
				mob.location().show(target,mob,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_VISUAL,auto?"":L("<S-NAME> spot(s) <T-NAME>!"));
				if(target.getVictim()==null)
					target.setVictim(mob);
				return false;
			}
			else
			{
				success = finishGrapple(mob,14,target, asLevel);
				if(success)
					mob.location().show(mob,target,I,CMMsg.MSG_OK_VISUAL,L("<S-NAME> garrote(s) <T-NAME>!"));
			}
		}
		else
			success=false;
		return success;
	}

}
