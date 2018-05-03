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

public class Chant_Stonewalking extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Stonewalking";
	}

	private final static String localizedName = CMLib.lang().L("Stonewalking");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Stonewalking spell)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if(msg.amISource(mob)
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS))
		   ||((msg.sourceMinor()==CMMsg.TYP_GET)&&((msg.target()==null)||(!mob.isMine(msg.target()))))))
			unInvoke();
		return;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
		{
			final Room R=((MOB)affected).location();
			if((R.domainType()==Room.DOMAIN_INDOORS_CAVE)
			   ||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
			   ||(R.domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)
			   ||(R.domainType()==Room.DOMAIN_OUTDOORS_ROCKS)
			   ||((R.getAtmosphere()&RawMaterial.MATERIAL_ROCK)!=0))
			{
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
				affectableStats.setWeight(0);
				affectableStats.setHeight(-1);
			}
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
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				final Room R=mob.location();
				if((R.domainType()==Room.DOMAIN_INDOORS_CAVE)||(R.domainType()==Room.DOMAIN_INDOORS_STONE))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> drawn out of the walls."));
				else
				if((R.domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)||(R.domainType()==Room.DOMAIN_OUTDOORS_ROCKS))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> drawn out of the rocks."));
				else
					mob.tell(L("Your stone walk has ended."));
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
				&&(R.domainType()!=Room.DOMAIN_INDOORS_STONE)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS)
				&&((R.getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already @x1.",name()));
			return false;
		}

		final Room R=mob.location();
		if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
		   &&(R.domainType()!=Room.DOMAIN_INDOORS_STONE)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS)
		   &&((R.getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
		{
			mob.tell(L("You must be near walls of stone or massive rock to use this chant."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) quietly to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fade(s) into the walls!"));
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) quietly to <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
