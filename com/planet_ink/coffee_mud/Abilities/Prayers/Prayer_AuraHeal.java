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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Prayer_AuraHeal extends Prayer
{
	@Override public String ID() { return "Prayer_AuraHeal"; }
	@Override public String name(){ return "Aura of Healing";}
	@Override public String displayText(){ return "(Heal Aura)";}
	@Override protected int canAffectCode(){return Ability.CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;}
	@Override public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALINGMAGIC;}
	private int ratingTickDown=4;

	public Prayer_AuraHeal()
	{
		super();

		ratingTickDown = 4;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		final Room R=(Room)affected;

		super.unInvoke();

		if(canBeUninvoked())
			R.showHappens(CMMsg.MSG_OK_VISUAL,_("The healing aura around you fades."));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if((--ratingTickDown)>=0) return super.tick(ticking,tickID);
		ratingTickDown=4;

		HashSet H=null;
		if((invoker()!=null)&&(invoker().location()==affected))
		{
			H=new HashSet();
			invoker().getGroupMembers(H);
		}
		final Room R=(Room)affected;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)
			   &&(M.curState().getHitPoints()<M.maxState().getHitPoints())
			   &&((H==null)
				||(M.getVictim()==null)
				||(!H.contains(M.getVictim()))))
			{
				final int oldHP=M.curState().getHitPoints();
				if(invoker()!=null)
				{
					final int healing=CMLib.dice().roll(2,adjustedLevel(invoker(),0),4);
					CMLib.combat().postHealing(invoker(),M,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
				}
				else
				{
					final int healing=CMLib.dice().roll(2,CMLib.ableMapper().lowestQualifyingLevel(ID()),4);
					CMLib.combat().postHealing(M,M,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
				}
				if(M.curState().getHitPoints()>oldHP)
					M.tell(_("You feel a little better!"));
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(((MOB)target).charStats().getMyRace().racialCategory().equals("Undead"))
					return Ability.QUALITY_INDIFFERENT;
				if(target!=mob)
				{
					if(((MOB)target).charStats().getMyRace().racialCategory().equals("Undead"))
						return super.castingQuality(mob, target,Ability.QUALITY_MALICIOUS);
				}
			}
			return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(_("The aura of healing is already here."));
			return false;
		}
		if(target.fetchEffect("Prayer_AuraHarm")!=null)
		{
			target.fetchEffect("Prayer_AuraHarm").unInvoke();
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for all to feel better.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("A healing aura descends over the area!"));
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,_("<S-NAME> @x1 for an aura of healing, but <S-HIS-HER> plea is not answered.",prayWord(mob)));


		// return whether it worked
		return success;
	}
}
