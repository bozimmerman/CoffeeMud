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
   Copyright 2001-2020 Bo Zimmerman

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
public class Spell_Polymorph extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Polymorph";
	}

	private final static String localizedName = CMLib.lang().L("Polymorph");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Polymorph)");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	private Race newRace=null;
	protected volatile int newBaseWeightAdj = 0;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			if(affected.name().indexOf(' ')>0)
				affectableStats.setName(L("a @x1 called @x2",newRace.name(),affected.name()));
			else
				affectableStats.setName(L("@x1 the @x2",affected.name(),newRace.name()));
			//newRace.setHeightWeight(affectableStats,(char)((MOB)affected).charStats().getStat(CharStats.STAT_GENDER));
			affectableStats.setWeight(affectableStats.weight()+newBaseWeightAdj);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			final int oldBaseWeight = affected.baseWeight();
			affectableStats.setMyRace(newRace);
			if(this.newBaseWeightAdj == 0)
				this.newBaseWeightAdj = affected.baseWeight() - oldBaseWeight;
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
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
		{
			if((mob.location()!=null)
			&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> morph(s) back into <S-HIM-HERSELF> again."));
		}
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText!=null)&&(newMiscText.length()>0))
		{
			newRace=CMClass.findRace(newMiscText);
			if(newRace != null)
			{
				final Physical P=this.affected;
				if(P instanceof MOB)
				{
					final MOB target=(MOB)P;
					if(target.location()!=null)
						target.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) a @x1!",newRace.name()));
					target.recoverCharStats();
					target.recoverPhyStats();
					CMLib.utensils().confirmWearability(target);
				}
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.baseCharStats().getMyRace() != target.charStats().getMyRace())
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already polymorphed."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,-(target.charStats().getStat(CharStats.STAT_CONSTITUTION)*2),auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> form(s) a spell around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=null;
					while((newRace==null)||(newRace.ID().equals("StdRace"))
					||(!CMath.bset(newRace.availabilityCode(),Area.THEME_FANTASY))
					||((newRace==target.charStats().getMyRace())))
						newRace=CMClass.randomRace();
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) a @x1!",newRace.name()));
					success=beneficialAffect(mob,target,asLevel,0)!=null;
					target.recoverCharStats();
					target.recoverPhyStats();
					CMLib.utensils().confirmWearability(target);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> form(s) a spell around <T-NAMESELF>, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
