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

import java.util.Enumeration;
import java.util.List;

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

public class Chant_FavorableWinds extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_FavorableWinds";
	}

	private final static String	localizedName	= CMLib.lang().L("Favorable Winds");

	private int abilityCode=1;
	
	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Favorable Winds)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int abilityCode()
	{
		return abilityCode;
	}

	@Override
	public void setAbilityCode(int code)
	{
		abilityCode = code;
	}

	@Override
	public String text()
	{
		return ""+abilityCode;
	}

	@Override
	public void setMiscText(String text)
	{
		if(CMath.isInteger(text))
			abilityCode = CMath.s_int(text);
		super.setMiscText(text);
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof Item)
			{
				Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("The favorable winds die down."));
			}
			if(affected instanceof BoardableShip)
			{
				final Area A=((BoardableShip)affected).getShipArea();
				if(A!=null)
				{
					for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
							R.showHappens(CMMsg.MSG_OK_VISUAL,L("The favorable winds die down."));
					}
				}
			}
		}
		super.unInvoke();

	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof BoardableShip)
			affectableStats.setAbility(affectableStats.ability() + abilityCode());
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof BoardableShip))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R = mob.location();
		Room shipR = null;
		Item target=null;
		
		if((R!=null)&&(R.getArea() instanceof BoardableShip)&&(commands.size()==0))
		{
			target=((BoardableShip)R.getArea()).getShipItem();
			shipR = CMLib.map().roomLocation(target);
		}
		else
		{
			if((R!=null)&&(R.getArea() instanceof BoardableShip))
				shipR=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
			Room checkRoom = R;
			if((R==null)||(R.findItem(CMParms.combine(commands,0))==null))
				checkRoom=shipR;
			Item I=super.getTarget(mob, checkRoom, givenTarget, commands, Room.FILTER_ROOMONLY);
			if(I instanceof BoardableShip)
			{
				target=I;
				shipR=checkRoom;
			}
			else
			if(I == null)
				return false;
		}

		if((target == null)||(R==null)||(!(target instanceof BoardableShip)))
		{
			mob.tell(L("This magic only works when set upon a ship!"));
			return false;
		}
		if(shipR==R)
			shipR=null;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to the winds.^?"));
			if(R.okMessage(mob,msg) && ((shipR==null)||(shipR.okMessage(mob, msg))))
			{
				R.send(mob,msg);
				if(shipR!=null)
					shipR.send(mob, msg);
				Ability A=beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
				{
					int amt=1;
					amt += super.getXLEVELLevel(mob) / 3;
					A.setAbilityCode(amt);
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("Favorable winds begin to blow!"));
					if(shipR!=null)
						shipR.showHappens(CMMsg.MSG_OK_VISUAL,L("Favorable winds begin to blow!"));
				}
			}
		}
		else
			return super.beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to the winds, but the magic fades."));
		// return whether it worked
		return success;
	}
}
