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

public class Chant_Flood extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Flood";
	}

	private final static String	localizedName	= CMLib.lang().L("Flood");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(room!=null)
			{
				if(text().length()>0)
				{
					int oldAtmo=CMParms.getParmInt(text(),"ATMOSPHERE",-1);
					room.showHappens(CMMsg.MSG_OK_ACTION, L("Finally, the flood waters recede."));
					room.setAtmosphere(oldAtmo);
				}
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!CMLib.flags().canBreatheThis(mob, RawMaterial.RESOURCE_FRESHWATER))
				return Ability.QUALITY_INDIFFERENT;
			if(getWaterRoomDir(mob.location())<0)
				return Ability.QUALITY_INDIFFERENT;
			if((mob.location().domainType()&Room.INDOORS)==0)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			final MOB invoker=invoker();
			for(final Enumeration<MOB> r=R.inhabitants();r.hasMoreElements();)
			{
				final MOB M=r.nextElement();
				if((M!=null)
				&&(M!=invoker)
				&&(!M.amDead())
				&&(M.location()!=null)
				&&(!CMLib.flags().canBreatheHere(M, M.location())))
					CMLib.combat().postRevengeAttack(M, invoker);
			}
		}
		return super.tick(ticking,tickID);
	}

	public int getWaterRoomDir(Room mobR)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d!=Directions.UP)&&(d!=Directions.DOWN))
			{
				final Room R=mobR.getRoomInDir(d);
				final Exit E=mobR.getExitInDir(d);
				if((R!=null)&&(E!=null)&&(E.isOpen()))
				{
					if(CMLib.flags().isWateryRoom(R))
					{
						return d;
					}
					final Room R2=R.getRoomInDir(d);
					final Exit E2=R.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()) && (CMLib.flags().isWateryRoom(R2)))
					{
						return d;
					}
				}
			}
		}
		return -1;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof Room)
		{
			switch(CMLib.tracking().isOkWaterSurfaceAffect((Room)affected,msg))
			{
			case CANCEL:
				return false;
			case FORCEDOK:
				return true;
			default:
			case CONTINUE:
				return super.okMessage(myHost,msg);
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.addAmbiance("Flooded!");
		if(affected instanceof Room)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room mobR=(givenTarget instanceof Room) ? (Room)givenTarget : mob.location();
		if(mobR==null)
			return false;
		if((mobR.domainType()&Room.INDOORS)==0)
		{
			mob.tell(L("This chant requires an enclosed indoor space to flood."));
			return false;
		}
		
		int newAtmosphere = RawMaterial.RESOURCE_FRESHWATER;
		
		String fromDir;
		if(CMLib.flags().isWateryRoom(mobR))
		{
			fromDir="right here";
			if((mobR.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				newAtmosphere = mobR.getAtmosphere();
		}
		else
		{
			int waterDir = -1;
			if(mobR.getArea() instanceof BoardableShip)
			{
				if((mobR.domainType()&Room.INDOORS)==0)
				{
					Item I=((BoardableShip)mobR.getArea()).getShipItem();
					if((I!=null)&&(I.owner() instanceof Room))
					{
						Room R=(Room)I.owner();
						if(CMLib.flags().isWateryRoom(R))
							waterDir = CMLib.dice().roll(1, 4, -1);
						else
							waterDir = getWaterRoomDir(R);
					}
				}
			}
			else
				waterDir = getWaterRoomDir(mobR);
			if(waterDir < 0)
			{
				mob.tell(L("There is no water nearby to call in a flood from."));
				return false;
			}
			fromDir=CMLib.directions().getFromCompassDirectionName(waterDir);
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			Room R=mobR.getRoomInDir(d);
			if(R!=null)
			{
				if((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				{
					newAtmosphere =R.getAtmosphere();
					break;
				}
				R=R.getRoomInDir(d);
				if(R!=null)
				{
					if((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
					{
						newAtmosphere =R.getAtmosphere();
						break;
					}
				}
			}
		}

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mobR.show(mob,null,this,verbalCastCode(mob,null,auto), L(auto?"A flood rushes in from @x1":
				"^S<S-NAME> chant(s) thunderously as flood waters rush in from @x1.^?",fromDir)+CMLib.protocol().msp("earthquake.wav",40)))
			{
				int oldAtmo=mobR.getAtmosphereCode();
				Ability A=maliciousAffect(mob,mobR,asLevel,0,-1);
				if(A!=null)
				{
					A.setMiscText("ATMOSPHERE="+oldAtmo);
					if(mobR.getAtmosphere()!=newAtmosphere)
						mobR.setAtmosphere(newAtmosphere);
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> chant(s) thunderously, but nothing happens."));

		// return whether it worked
		return success;
	}
}
