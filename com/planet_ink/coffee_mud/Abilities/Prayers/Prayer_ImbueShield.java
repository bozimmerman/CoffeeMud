package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2025 Bo Zimmerman

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
public class Prayer_ImbueShield extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_ImbueShield";
	}

	private final static String	localizedName	= CMLib.lang().L("Imbue Shield");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING|Ability.FLAG_NEUTRAL;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected void doImbue(final MOB mob, final Item targetI, final Ability imbuePrayerA)
	{
		mob.location().show(mob,targetI,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) with a sacred light!"));
		targetI.basePhyStats().setDisposition(targetI.basePhyStats().disposition()|PhyStats.IS_BONUS);
		targetI.basePhyStats().setLevel(targetI.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID())/2));
		final Ability A=CMClass.getAbility("Prop_WearSpellCast");
		A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
		targetI.addNonUninvokableEffect(A);
		targetI.recoverPhyStats();
	}

	protected boolean isOkPrayer(final Ability imbuePrayerA)
	{
		if((imbuePrayerA.ID().equals("Spell_Stoneskin"))
		||(imbuePrayerA.ID().equals("Spell_MirrorImage"))
		||(CMath.bset(imbuePrayerA.flags(), FLAG_SUMMONING))
		||(imbuePrayerA.abstractQuality()==Ability.QUALITY_MALICIOUS)
		||((!imbuePrayerA.canAffect(CAN_MOBS))&&(!imbuePrayerA.canTarget(CAN_MOBS))))
			return false;
		return true;
	}

	protected int getXPCost(final Ability imbuePrayerA)
	{
		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID()));
		return experienceToLose;
	}

	protected boolean isAppropriateItem(final Physical target)
	{
		return (target instanceof Shield);
	}

	protected boolean checkAlignment(final MOB mob, final Physical target, final boolean quiet)
	{
		return true;
	}

	protected int maxPrayerLevel()
	{
		return 15;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Imbue which prayer onto what?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(commands.size()-1))));
			return false;
		}
		final String deityName=mob.charStats().getWorshipCharID();
		if(deityName.length()==0)
		{
			mob.tell(L("You must worship a deity to begin the imbuing."));
			return false;
		}

		if(!this.isAppropriateItem(target))
		{
			mob.tell(mob,target,null,L("You can't imbue <T-NAME> with this prayer!"));
			return false;
		}
		if(!Prayer.checkInfusionMismatch(mob, target))
		{
			mob.tell(L("You can not imbue that repulsive thing."));
			return false;
		}

		if(!Prayer.checkRequiredInfusion(mob, target))
		{
			mob.tell(L("@x1 cannot be imbued until it has been empowered by your deity.",target.name(mob)));
			return false;
		}

		final Deity.DeityWorshipper zappA=CMLib.law().getClericInfusion(target);
		if(zappA instanceof Ability)
		{
			if((CMath.bset(((Ability)zappA).flags(),Ability.FLAG_ZAPPER))
			&&(!CMLib.masking().maskCheck(((Ability) zappA).text(), mob, true)))
			{
				mob.tell(L("You can't seem to focus on @x1.",target.name(mob)));
				return false;
			}
		}

		if(!checkAlignment(mob,target,false))
			return false;

		if(target.phyStats().ability()==0)
		{
			mob.tell(L("@x1 cannot be imbued until it has been empowered.",target.name(mob)));
			return false;
		}

		commands.remove(commands.size()-1);
		final Item targetI=(Item)target;

		final String prayerName=CMParms.combine(commands,0).trim();
		Ability imbuePrayerA=null;
		final List<Ability> ables=new ArrayList<Ability>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(!A.ID().equals(this.ID())))
				ables.add(A);
		}
		imbuePrayerA = (Ability)CMLib.english().fetchEnvironmental(ables,prayerName,true);
		if(imbuePrayerA==null)
			imbuePrayerA = (Ability)CMLib.english().fetchEnvironmental(ables,prayerName,false);
		if(imbuePrayerA==null)
		{
			mob.tell(L("You don't know how to imbue anything with '@x1'.",prayerName));
			return false;
		}

		if(!isOkPrayer(imbuePrayerA))
		{
			mob.tell(L("That prayer cannot be used to imbue anything."));
			return false;
		}
		final long flagMask=Ability.FLAG_NEUTRAL|Ability.FLAG_MODERATE;
		if((imbuePrayerA.flags()&flagMask) != (flags()&flagMask))
		{
			if(!(target instanceof Shield))
			{
				mob.tell(L("@x1 can not be imbued using the @x2 prayer.",imbuePrayerA.Name(),Name()));
				return false;
			}
		}

		if(!imbuePrayerA.mayBeEnchanted())
		{
			mob.tell(L("That prayer is too powerful to imbue into anything."));
			return false;
		}
		if(!targetI.isGeneric())
		{
			mob.tell(L("You can't imbue '@x1'.",targetI.name()));
			return false;
		}
		targetI.phyStats().setDisposition(targetI.phyStats().disposition()&~(PhyStats.IS_BONUS));
		if(CMLib.flags().isEnchanted(targetI))
		{
			mob.tell(L("@x1 already appears to be sufficiently magical.",targetI.name(mob)));
			return false;
		}
		targetI.recoverPhyStats();

		int experienceToLose=getXPCost(imbuePrayerA);
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to use this magic."));
			return false;
		}

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			setMiscText(imbuePrayerA.ID()); // important for reliquist discharge
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> @x1 while holding <T-NAMESELF> tightly.^?",super.prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				doImbue(mob,targetI,imbuePrayerA);
			}

		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 while holding <T-NAMESELF> tightly, and looking very frustrated.",super.prayWord(mob)));
		}
		// return whether it worked
		return success;
	}
}
