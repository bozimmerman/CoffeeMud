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

public class Chant_ExplosiveDecompression extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_ExplosiveDecompression";
	}

	private final static String localizedName = CMLib.lang().L("Explosive Decompression");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	@Override
	public void affectPhyStats(Physical affecting, PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		if((affected instanceof MOB)&&(((MOB)affected).charStats().getBreathables().length>0))
			stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.location().domainType()&Room.INDOORS)==0)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null)
			return false;
		if((!auto)&&((target.domainType()&Room.INDOORS)==0))
		{
			mob.tell(L("This chant only works indoors."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s) loudly.  A ball of fire forms around <S-NAME>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The ball of fire **EXPLODES**!"));
					for(int i=0;i<target.numInhabitants();i++)
					{
						final MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(M!=mob))
						{
							final CMMsg msg2=CMClass.getMsg(mob,M,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_FIRE,null);
							if(mob.location().okMessage(mob,msg2))
							{
								mob.location().send(mob,msg2);
								invoker=mob;
								final int numDice = adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob));
								int damage = CMLib.dice().roll(numDice, 5, 25);
								if(msg2.value()>0)
									damage = (int)Math.round(CMath.div(damage,2.0));
								CMLib.combat().postDamage(mob,M,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The flaming blast <DAMAGE> <T-NAME>!"));
							}
							if((M.charStats().getBodyPart(Race.BODY_FOOT)>0)
							&&(!CMLib.flags().isFlying(M))&&(CMLib.flags().isStanding(M)))
								mob.location().show(M,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_SIT,L("<S-NAME> <S-IS-ARE> blown off <S-HIS-HER> feet!"));
						}
					}
					maliciousAffect(mob,target,asLevel,20,-1);
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The fire burns off all the air here!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) loudly, but nothing happens."));
		// return whether it worked
		return success;
	}
}
