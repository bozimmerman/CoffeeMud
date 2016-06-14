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
   Copyright 2016-2016 Bo Zimmerman

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

public class Thief_WalkThePlank extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_WalkThePlank";
	}

	private final static String	localizedName	= CMLib.lang().L("Walk The Plank");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "WALKTHEPLANK", "PLANK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof BoardableShip))
		||((R.domainType()&Room.INDOORS)!=0))
		{
			mob.tell(L("You must be on the deck of a ship to make someone walk the plank."));
			return false;
		}
		final BoardableShip myShip=(BoardableShip)R.getArea();
		final Item myShipItem=myShip.getShipItem();
		if((myShipItem==null)
		||(!(myShipItem.owner() instanceof Room))
		||(!CMLib.flags().isWateryRoom((Room)myShipItem.owner())))
		{
			mob.tell(L("Your ship must be at sea to make someone walk the plank."));
			return false;
		}
		
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		
		boolean allowedToWalkThem=false;
		LegalBehavior B=CMLib.law().getLegalBehavior(R);
		if(B!=null)
		{
			List<LegalWarrant> warrants=B.getWarrantsOf(CMLib.law().getLegalObject(R),target);
			if((warrants.size()>0)&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.ABOVELAW)))
			{
				allowedToWalkThem=true;
			}
	
			if((!auto)&&(CMLib.flags().isBoundOrHeld(target)&&(!CMSecurity.isASysOp(mob))))
			{
				allowedToWalkThem=true;
			}
		}
		
		if(!allowedToWalkThem)
		{
			mob.tell(L("You can't make @x1 walk the plank.",target.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=target.phyStats().level()-((mob.phyStats().level()+super.getXLEVELLevel(mob))/2);
		boolean success=proficiencyCheck(mob,-adjustment,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> make(s) <T-NAME> walk the plank..^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE)!=null;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> call(s) for the stoning of <T-NAMESELF>."));


		// return whether it worked
		return success;
	}
}
