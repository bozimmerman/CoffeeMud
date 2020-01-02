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
   Copyright 2003-2020 Bo Zimmerman

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
public class Chant_Fertilization extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Fertilization";
	}

	private final static String localizedName = CMLib.lang().L("Fertilization");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private volatile int oldResource = -1;
	private boolean hasTicked = false;

	@Override
	public void unInvoke()
	{
		if(this.canBeUninvoked() && (affected instanceof Room))
		{
			final Room R=(Room)affected;
			if((R!=null)&&(oldResource>0))
				R.setResource(oldResource);
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)
		&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			hasTicked=true;
			if((R.myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			{
				oldResource=R.myResource();
				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB M=R.fetchInhabitant(m);
					if(M!=null)
					{
						Ability A=M.fetchEffect("Farming");
						if(A==null)
							A=M.fetchEffect("Foraging");
						if(A==null)
							A=M.fetchEffect("MasterFarming");
						if(A==null)
							A=M.fetchEffect("MasterForaging");
						if(A==null)
							A=M.fetchEffect("MasterGardening");
						if(A==null)
							A=M.fetchEffect("Gardening");
						if(A!=null)
							A.setAbilityCode(3);
					}
				}
			}
		}
		return super.tick(ticking,tickID);

	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(!canBeUninvoked()
		&&(!hasTicked))
		{
			if((msg.source() != null)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() == affected)
			&&(affected instanceof Room))
			{
				final Room R=(Room)affected;
				if((R!=null)
				&&(!hasTicked))
				{
					if((!CMLib.threads().isTicking(this, -1))
					&&(!CMLib.threads().isTicking(R, -1)))
						CMLib.threads().startTickDown(this, Tickable.TICKID_SPELL_AFFECT, 3);
				}
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{

		final Room R=mob.location();
		if(R==null)
			return false;

		if(R.fetchEffect(ID())!=null)
		{
			mob.tell(L("This place is already fertile."));
			return false;
		}

		final int type=R.domainType();
		if(((type&Room.INDOORS)>0)
			||(type==Room.DOMAIN_OUTDOORS_AIR)
			||(type==Room.DOMAIN_OUTDOORS_CITY)
			||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
			||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell(L("That magic won't work here."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,R,this,verbalCastCode(mob,R,auto),auto?"":L("^S<S-NAME> chant(s) to make the land fruitful.^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				this.oldResource=R.myResource();
				final long ticksPerMudday = (CMProps.getMillisPerMudHour() * R.getArea().getTimeObj().getHoursInDay() ) / CMProps.getTickMillis();
				final int qualClassLevel = CMLib.ableMapper().qualifyingClassLevel( mob, this );
				if((R instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,R)))
				{
					R.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom(R);
					R.setResource(RawMaterial.RESOURCE_DIRT);
				}
				else
				if(beneficialAffect(mob, R, asLevel, (int)(qualClassLevel * ticksPerMudday) ) != null )
				{
					R.setResource(RawMaterial.RESOURCE_DIRT);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to make the land fruitful, but nothing happens."));

		// return whether it worked
		return success;
	}
}
