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
   Copyright 2020-2020 Bo Zimmerman

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
public class Prayer_DeflectPrayer extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_DeflectPrayer";
	}

	private final static String localizedName = CMLib.lang().L("Deflect Prayer");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Deflect Prayer)");

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
		return CAN_ITEMS;
	}

	protected long timeToNextCast = 0;

	@Override
	protected int getTicksBetweenCasts()
	{
		return 15;
	}

	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected void setTimeOfNextCast(final long absoluteTime)
	{
		timeToNextCast=absoluteTime;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_SHIELDUSE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public long flags()
	{
		return 0; // no, part of its skillness is being unaligned
	}

	protected Shield shield = null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(!mob.amDead()))
			mob.tell(L("Your deflection faith fades."));
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(!(affected instanceof MOB))
		{
			unInvoke();
			return false;
		}
		final MOB mob=(MOB)affected;
		if(shield == null)
		{
			final Item I=mob.fetchHeldItem();
			if(I instanceof Shield)
				shield=(Shield)I;
			else
			{
				unInvoke();
				return true;
			}
		}
		if((shield==null)
		||(shield.amDestroyed())
		||(!shield.amBeingWornProperly())
		||(mob.fetchHeldItem()!=shield))
		{
			unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;
		final MOB mob=(MOB)affected;

		if(shield == null)
		{
			final Item I=mob.fetchHeldItem();
			if(I instanceof Shield)
				shield=(Shield)I;
			else
			{
				unInvoke();
				return true;
			}
		}

		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(invoker!=null)
		&&((shield!=null)&&(mob.fetchHeldItem()==shield))
		&&(!mob.amDead()))
		{
			mob.location().show(mob,shield,CMMsg.MSG_OK_VISUAL,L("^S<S-YOUPOSS> <T-NAMENOART> deflects @x1!^?",msg.tool().name()));
			if(shield.subjectToWearAndTear())
			{
				final int prayerLevel=CMLib.ableMapper().lowestQualifyingLevel(msg.tool().ID());
				int damage = prayerLevel-(2*super.getXLEVELLevel(mob));
				if(damage < 1)
					damage = 1;
				shield.setUsesRemaining(shield.usesRemaining()-damage);
				if((shield.usesRemaining()<10)
				&&(shield.usesRemaining()>0))
					mob.tell(L("@x1 is nearly destroyed! (@x2%)",name(),""+shield.usesRemaining()));
				else
				if(shield.usesRemaining()<=0)
				{
					shield.setUsesRemaining(100);
					msg.addTrailerMsg(CMClass.getMsg(mob,null,null,
							CMMsg.MSG_OK_VISUAL,L("^I@x1 is destroyed!!^?",name()),CMMsg.NO_EFFECT,null,
							CMMsg.MSG_OK_VISUAL,L("^I@x1 being worn by <S-NAME> is destroyed!^?",name())));
					shield.unWear();
					shield.destroy();
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
					if(mob.location()!=null)
						mob.location().recoverRoomStats();
				}
			}
			unInvoke();
			return false;
		}
		return true;
	}

	protected boolean hasAppropriateItem(final MOB mob)
	{
		if(mob==null)
			return false;
		final Item shield=mob.fetchHeldItem();
		if(!(shield instanceof Shield))
			return false;
		if(CMLib.flags().isEnchanted(shield))
			return false;
		return true;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!hasAppropriateItem(mob))
				return Ability.QUALITY_INDIFFERENT;
			final MOB victim=mob.getVictim();
			if((victim!=null)&&(CMLib.flags().domainAbilities(victim,Ability.ACODE_PRAYER).size()==0))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already deflecting prayers."));
			return false;
		}

		final Item shield=mob.fetchHeldItem();
		if(!hasAppropriateItem(mob))
		{
			if(!(shield instanceof Shield))
			{
				mob.tell(L("You aren't holding a shield to hide behind."));
				return false;
			}
			mob.tell(L("@x1 is enchanted and can not be bolstered by faith alone.",shield.name(mob)));
			return false;
		}

		if(mob.charStats().getMyDeity()==null)
		{
			mob.tell(L("You need to have faith in a deity to do this."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 that <T-NAME> be protected behind @x1.^?",prayWord(mob),shield.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Prayer_DeflectPrayer A=(Prayer_DeflectPrayer)beneficialAffect(mob,target,asLevel,15);
				if(A!=null)
				{
					A.shield=(Shield)shield;
					mob.location().show(target,shield,CMMsg.MSG_OK_VISUAL,L("An aura of faith surrounds <S-YOUPOSS> <T-NAMENOART>."));
					A.setTimeOfNextCast(mob);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for protection behind @x1, but <S-HIS-HER> plea is not answered.",prayWord(mob),shield.name()));

		// return whether it worked
		return success;
	}
}
