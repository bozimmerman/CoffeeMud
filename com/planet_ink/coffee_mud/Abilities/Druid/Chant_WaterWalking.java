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
   Copyright 2002-2018 Bo Zimmerman

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

public class Chant_WaterWalking extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_WaterWalking";
	}

	private final static String localizedName = CMLib.lang().L("Water Walking");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Water Walking)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	protected boolean triggerNow=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(triggerNow||
				((mob.location()!=null)
				&&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				   ||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null)
			return true;
		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(mob.location()!=null)
		&&(msg.target() instanceof Room))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
			&&(msg.target()==mob.location().getRoomInDir(Directions.UP)))
			{
				msg.source().tell(L("Your water walking magic prevents you from ascending from the water surface."));
				return false;
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_LEAVE)
			&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(msg.tool() instanceof Exit))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=mob.location().getRoomInDir(d);
					if((R!=null)
					&&(mob.location().getReverseExit(d)==msg.tool())
					&&((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
					||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)))
					{
						triggerNow=true;
						msg.source().recoverPhyStats();
						return true;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(triggerNow)
			triggerNow=false;
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
			mob.tell(L("You have a sinking feeling that your water walking ability is gone."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already a water walker."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> feel(s) a little lighter!"));
					success=beneficialAffect(mob,target,asLevel,0)!=null;
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
