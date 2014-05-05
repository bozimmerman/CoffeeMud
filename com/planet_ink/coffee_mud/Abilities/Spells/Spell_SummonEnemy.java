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
@SuppressWarnings("rawtypes")
public class Spell_SummonEnemy extends Spell
{
	@Override public String ID() { return "Spell_SummonEnemy"; }
	public final static String localizedName = CMLib.lang()._("Summon Enemy");
	@Override public String name() { return localizedName; }
	@Override public String displayText(){return "(Enemy Summoning)";}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	@Override public long flags(){return Ability.FLAG_TRANSPORTING|Ability.FLAG_SUMMONING;}
	protected int overridemana(){return Ability.COST_PCT+50;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":_("^S<S-NAME> conjur(s) the dark shadow of a living creature...^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level());
				if(target!=null)
				{
					final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto)|CMMsg.MASK_MALICIOUS,null);
					if(mob.location().okMessage(mob, msg2))
					{
						mob.location().send(mob, msg2);
						beneficialAffect(mob,target,asLevel,0);
						target.setVictim(mob);
					}
					else
						CMLib.tracking().wanderAway(target, false, true);
				}
				else
					mob.tell(_("Your equal could not be summoned."));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> conjur(s), but nothing happens."));

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		if(caster==null) return null;
		if(caster.location()==null) return null;
		if(caster.location().getArea()==null) return null;
		MOB monster=null;
		int tries=10000;
		while((monster==null)&&((--tries)>0))
		{
			final Room room=CMLib.map().getRandomRoom();
			if((room!=null)&&CMLib.flags().canAccess(caster,room)&&(room.numInhabitants()>0))
			{
				final MOB mob=room.fetchRandomInhabitant();
				if((mob!=null)
				&&(!(mob instanceof Deity))
				&&(mob.phyStats().level()>=level-(CMProps.getIntVar(CMProps.Int.EXPRATE)/2))
				&&(mob.phyStats().level()<=(level+(CMProps.getIntVar(CMProps.Int.EXPRATE)/2)))
				&&(mob.charStats()!=null)
				&&(mob.charStats().getMyRace()!=null)
				&&(CMProps.isTheme(mob.charStats().getMyRace().availabilityCode()))
				&&((CMLib.flags().isGood(caster)&&CMLib.flags().isEvil(mob))
					|| (CMLib.flags().isNeutral(mob))
					|| (CMLib.flags().isEvil(caster)&&CMLib.flags().isGood(mob)))
				&&(caster.mayIFight(mob))
				)
					monster=mob;
			}
		}
		if(monster==null) return null;
		monster=(MOB)monster.copyOf();
		monster.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		monster.recoverCharStats();
		monster.recoverPhyStats();
		monster.recoverMaxState();
		monster.resetToMaxState();
		monster.text();
		monster.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(monster,null);
		monster.location().showOthers(monster,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> appears!"));
		caster.location().recoverRoomStats();
		monster.setStartRoom(null);
		return(monster);
	}
}
