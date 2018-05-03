package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class Thief_Belay extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Belay";
	}

	private final static String	localizedName	= CMLib.lang().L("Belay");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BELAY" });

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_BINDING;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	public int	code	= 0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code = newCode;
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats stats)
	{
		super.affectPhyStats(host,stats);
		if(host instanceof Item)
			stats.setSensesMask(stats.sensesMask()|PhyStats.SENSE_ITEMNOTGET);
		stats.addAmbiance("belayed");
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL)))
		{
			final Physical P=affected;
			unInvoke();
			P.delEffect(this);
			P.recoverPhyStats();
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("What item would you like to belay?"));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof BoardableShip))
		||((R.domainType()&Room.INDOORS)!=0))
		{
			mob.tell(L("You must be on the deck of a ship to belay an item."));
			return false;
		}
		
		final Item item=super.getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(item==null)
			return false;

		if(!(item.owner() instanceof Room))
		{
			mob.tell(L("You need to put that on the deck before you can belay it."));
			return false;
		}
		
		if(item.fetchEffect(ID())!=null)
		{
			mob.tell(L("That is already secured."));
			return false;
		}
		
		if(((!CMLib.flags().isGettable(item))
			||(CMath.bset(item.phyStats().sensesMask(), PhyStats.SENSE_UNDESTROYABLE)))
		&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
		{
			mob.tell(L("You may not belay that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,item,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> belay(s) <T-NAME>."),CMMsg.MSG_THIEF_ACT,null,CMMsg.MSG_THIEF_ACT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=(Ability)super.copyOf();
				A.setInvoker(mob);
				A.setAbilityCode((adjustedLevel(mob,asLevel)*2)-item.phyStats().level());
				if(CMLib.law().doesOwnThisLand(mob,R))
					item.addNonUninvokableEffect(A);
				else
					A.startTickDown(mob,item,15*(adjustedLevel(mob,asLevel)));
				item.recoverPhyStats();
				item.recoverPhyStats();
			}
		}
		else
			beneficialVisualFizzle(mob,item,L("<S-NAME> attempt(s) to belay <T-NAME>, but fail(s)."));
		return success;
	}
}
