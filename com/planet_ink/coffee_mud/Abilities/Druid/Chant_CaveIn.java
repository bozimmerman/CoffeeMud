package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Chant_CaveIn extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_CaveIn";
	}

	private final static String localizedName = CMLib.lang().L("Cave-In");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|CAN_EXITS;
	}

	public int amountRemaining=0;

	@Override
	public long flags()
	{
		return Ability.FLAG_PARALYZING;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affected instanceof Exit)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_OPEN))
		&&(msg.source().phyStats().height()>=0)
		&&((msg.tool()==affected)||(msg.target()==affected)))
		{
			msg.source().tell(L("This exit is blocked by rubble, and can not be moved through."));
			return false;
		}
		else
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			final MOB mob=(MOB)affected;
			if(msg.sourceMinor()==CMMsg.TYP_STAND)
				return false;
			if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&((msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) to get out from under the rocks."));
				amountRemaining-=(mob.charStats().getStat(CharStats.STAT_STRENGTH)*4);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okMessage(host,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BOUND);
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
			&&((mob.location().getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=null;
		if(mob.isMonster()&&(givenTarget instanceof MOB))
			target=mob.location();
		else
		if((commands.size()>0)&&(givenTarget==null))
		{
			final int dir=CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,0));
			if((dir>=0)&&(dir!=Directions.UP)&&(mob.location().getExitInDir(dir)!=null))
				target=mob.location().getExitInDir(dir);
		}
		if(target==null)
			target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if((target instanceof Item)||(target instanceof Room))
		{
			mob.tell(L("This chant can only target exits or creatures."));
			return false;
		}
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((mob.location().getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
		{
			mob.tell(L("This chant only works in caves."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":L("^S<S-NAME> chant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				amountRemaining=200;
				if(target instanceof Exit)
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("A cave-in causes rubble to fall, blocking <T-NAME>!"));
				else
				if(target instanceof MOB)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("A cave-in drops rocks on <T-NAME>!"));
					final int maxDie =  (adjustedLevel( mob, asLevel )+(2*super.getX1Level(mob))) / 2;
					int damage = CMLib.dice().roll(maxDie,3,maxDie);
					if(msg.value()>0)
						damage = (int)Math.round(CMath.div(damage,1.5));

					if(((MOB)target).location()==mob.location())
						CMLib.combat().postDamage(mob,(MOB)target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BASHING,L("The falling rubble <DAMAGE> <T-NAME>!"));
				}
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,asLevel,(target instanceof Exit)?0:10,0)!=null;
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
