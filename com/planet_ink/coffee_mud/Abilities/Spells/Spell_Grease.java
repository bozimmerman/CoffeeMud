package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_Grease extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Grease";
	}

	private final static String localizedName = CMLib.lang().L("Grease");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Covered in Grease)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	public final static int SIT = 0;
	public final static int FUMBLE_WEAPON = 1;
	public final static int BOTH = 2;

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-4);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		CMMsg msg2 = null;
		Item weapon = null;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_WEAPONATTACK:
			case CMMsg.TYP_THROW:
			case CMMsg.TYP_FLEE:
				if(invoker()!=null)
				{
					if(CMLib.dice().rollPercentage()>(mob.charStats().getStat(CharStats.STAT_DEXTERITY)*4))
					{
						final int greaseEffect = (int) Math.round(Math.random()*3);
						if((!CMLib.flags().isFlying(mob))||(greaseEffect==FUMBLE_WEAPON))
						{
							switch(greaseEffect)
							{
							case SIT:
								msg2=CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> slip(s) and slide(s) around in the grease!"));
								mob.phyStats().setDisposition(mob.phyStats().disposition() | PhyStats.IS_SITTING);
								if(mob.location().okMessage(mob,msg2))
									mob.location().send(mob,msg2);
								return false;
							case FUMBLE_WEAPON:
								weapon = mob.fetchWieldedItem();
								if((weapon!=null)&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(CharStats.STAT_DEXTERITY)*5))
								&&((weapon.rawProperLocationBitmap()==Wearable.WORN_WIELD)||(weapon.rawProperLocationBitmap()==Wearable.WORN_WIELD+Wearable.WORN_HELD)))
								{
									msg2=CMClass.getMsg(mob,weapon,null,CMMsg.MSG_DROP,L("<S-NAME> can't hold onto <S-HIS-HER> weapon since it's covered with grease."));
									weapon.unWear();
									if(mob.location().okMessage(mob,msg2))
										mob.location().send(mob,msg2);
								}
								return false;
							case BOTH:
								weapon = mob.fetchWieldedItem();
								if(weapon != null)
									msg2=CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> slip(s) and slide(s) around in the grease and lose(s) <S-HIS-HER> weapon."));
								else
									msg2=CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> slip(s) in the grease and fall(s) down."));
								if(mob.location().okMessage(mob,msg2))
								{
									mob.phyStats().setDisposition(mob.phyStats().disposition() | PhyStats.IS_SITTING);
									mob.location().send(mob,msg2);
									if((weapon!=null)&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(CharStats.STAT_DEXTERITY)*4))
									&&((weapon.rawProperLocationBitmap()==Wearable.WORN_WIELD)||(weapon.rawProperLocationBitmap()==Wearable.WORN_WIELD+Wearable.WORN_HELD)))
									{
										msg2=CMClass.getMsg(mob,weapon,null,CMMsg.MSG_DROP,L("<S-NAME> can't hold onto <S-HIS-HER> weapon since it's covered with grease."));
										weapon.unWear();
										if(mob.location().okMessage(mob,msg2))
											mob.location().send(mob,msg2);
									}
								}
								return false;
							default:
								msg2=CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> slip(s) and slide(s) around in the grease!"));
								if(mob.location().okMessage(mob,msg2))
								{
									mob.phyStats().setDisposition(mob.phyStats().disposition() | PhyStats.IS_SITTING);
									mob.location().send(mob,msg2);
								}
								return false;
							}
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> manage(s) to work <S-HIS-HER> way out of the grease."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invoke a spell at <T-NAME>s feet..^?"),CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL,auto?"":L("^S<S-NAME> invoke(s) a spell at your feet.^?"),verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invokes a spell at <T-NAME>s feet.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(CMLib.flags().isInFlight(target))
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) unaffected."));
				else
				if(msg.value()<=0)
				{
					if(target.location()==mob.location())
					{
						target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> begin(s) to slip and slide!"));
						success=maliciousAffect(mob,target,asLevel,8,-1)!=null;
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> cast(s) a spell on <T-NAMESELF>, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
