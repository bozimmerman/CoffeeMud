package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_ProtGood extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_ProtGood";
	}

	private final static String localizedName = CMLib.lang().L("Protection Good");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Protection from Good)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return false;
		if(invoker==null)
			return false;

		final MOB mob=(MOB)affected;

		if((!CMLib.flags().isReallyEvil(mob))&&CMLib.flags().isGood(mob))
		{
			final int damage=(int)Math.round(CMath.div(mob.phyStats().level()+(2*getXLEVELLevel(invoker())),3.0));
			final MOB invoker=(invoker()!=null) ? invoker() : mob;
			CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,L("<T-HIS-HER> protective aura <DAMAGE> <T-NAME>!"));
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null)
			return true;
		if(!(affected instanceof MOB))
			return true;

		if((msg.target()==affected)&&(msg.source()!=affected))
		{
			if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
			&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY))
			&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
			{
				msg.source().location().show((MOB)affected,null,CMMsg.MSG_OK_VISUAL,L("The unholy field around <S-NAME> protect(s) <S-HIM-HER> from the goodly magic attack of @x1.",msg.source().name()));
				return false;
			}

		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if(mob.isInCombat())
		{
			final MOB victim=mob.getVictim();
			if(CMLib.flags().isGood(victim))
				affectableStats.setArmor(affectableStats.armor()-10-(2*getXLEVELLevel(invoker())));
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your protection from goodness fades."));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final MOB victim=mob.getVictim();
			if((victim!=null)&&(CMLib.flags().isGood(victim))&&(!CMLib.flags().isGood(mob)))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=mob;
		if((auto)&&(givenTarget!=null))
			target=givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,target,null,L("<T-NAME> <T-IS-ARE> already affected by @x1.",name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) protected from goodness."):L("^S<S-NAME> @x1 for protection from goodness.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 for protection, but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
