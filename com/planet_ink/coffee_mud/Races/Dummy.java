package com.planet_ink.coffee_mud.Races;
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
   Copyright 2012-2018 Bo Zimmerman

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
public class Dummy extends Doll
{
	@Override
	public String ID()
	{
		return "Dummy";
	}

	private final static String localizedStaticName = CMLib.lang().L("Dummy");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 68;
	}

	@Override
	public int shortestFemale()
	{
		return 64;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 150;
	}

	@Override
	public int weightVariance()
	{
		return 50;
	}

	@Override
	public void affectCharState(MOB mob, CharState affectableMaxState)
	{
		super.affectCharState(mob,affectableMaxState);
		affectableMaxState.setHitPoints(99999);
	}

	@Override
	public void affectPhyStats(Physical E, PhyStats affectableStats)
	{
		super.affectPhyStats(E,affectableStats);
		affectableStats.setArmor(100);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((myHost instanceof MOB)
		&&(msg.amISource((MOB)myHost)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_DEATH)
			{
				msg.source().tell(L("You are not allowed to die."));
				return false;
			}
			else
			if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
			{
				msg.source().curState().setHitPoints(99999);
				((MOB)myHost).makePeace(true);
				final Room room=((MOB)myHost).location();
				if(room!=null)
				for(int i=0;i<room.numInhabitants();i++)
				{
					final MOB mob=room.fetchInhabitant(i);
					if((mob.getVictim()!=null)&&(mob.getVictim()==myHost))
						mob.makePeace(true);
				}
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.target() instanceof Item))
			{
				msg.source().tell(L("Dummys cant get anything."));
				return false;
			}
		}
		return true;
	}

	@Override 
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_WOOD);
		}
		return body;
	}
	
	private static Vector<RawMaterial> resources=new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a pile of @x1 parts",name().toLowerCase()),RawMaterial.RESOURCE_WOOD));
			}
		}
		return resources;
	}
}
