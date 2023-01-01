package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2022-2023 Bo Zimmerman

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
public class Spell_DetonateBombs extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DetonateBombs";
	}

	private final static String localizedName = CMLib.lang().L("Detonate Bombs");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}
	Room lastRoom=null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	public Trap bombGather(final MOB mob, final Physical P)
	{
		if(P!=null)
		{
			final Trap T=CMLib.utensils().fetchMyTrap(P);
			if((T!=null)
			&&(T.isABomb())
			&&(T.invoker()==mob))
				return T;
		}
		return null;
	}

	public void bombsHere(final List<Trap> traps, final MOB mob, final Physical P)
	{
		if(P==null)
			return;
		if((P instanceof Room)
		&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			final Trap T=bombGather(mob, mob.location());
			if(T!=null)
				traps.add(T);
		}
		else
		if((P instanceof Container)
		&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			final Container C=(Container)P;
			final List<Item> V=C.getDeepContents();
			for(int v=0;v<V.size();v++)
			{
				final Trap T=bombGather(mob, V.get(v));
				if(T!=null)
					traps.add(T);
			}
		}
		else
		if((P instanceof Item)
		&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			final Trap T=bombGather(mob, P);
			if(T!=null)
				traps.add(T);
		}
		else
		if((P instanceof Exit)
		&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			final Room room=mob.location();
			if(room!=null)
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if(room.getExitInDir(d)==P)
					{
						final Exit E2=room.getReverseExit(d);
						final Room R2=room.getRoomInDir(d);
						Trap T=bombGather(mob, P);
						if(T!=null)
							traps.add(T);
						T=bombGather(mob, E2);
						if(T!=null)
							traps.add(T);
						T=bombGather(mob, R2);
						if(T!=null)
							traps.add(T);
						break;
					}
				}
			}
		}
		else
		if((P instanceof MOB)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			for(int i=0;i<((MOB)P).numItems();i++)
			{
				final Item I=((MOB)P).getItem(i);
				final Trap T=bombGather(mob, I);
				if(T!=null)
					traps.add(T);
			}
			final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(P);
			if(SK!=null)
			{
				for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					if(E2 instanceof Item)
					{
						final Trap T=bombGather(mob, (Item)E2);
						if(T!=null)
							traps.add(T);
					}
				}
			}
		}
	}

	public List<MOB> getTargets(final MOB mob, final Trap T)
	{
		final List<MOB> targetMs=new ArrayList<MOB>();
		if((T.affecting() instanceof Item)
		&&(((Item)T.affecting()).owner() instanceof MOB))
			targetMs.add((MOB)((Item)T.affecting()).owner());
		else
		if(T.affecting() instanceof MOB)
			targetMs.add((MOB)((Item)T.affecting()).owner());
		else
		{
			final Room R=CMLib.map().roomLocation(T.affecting());
			if(R!=null)
			{
				for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if(M!=null)
						targetMs.add(M);
				}
			}
		}
		return targetMs;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if(target instanceof Exit)
		{
			final int dir = CMLib.map().getExitDir(mob.location(), (Exit)target);
			if(dir > 0)
				target=mob.location().getRoomInDir(dir);
			if(target == null)
			{
				mob.tell(L("There is nothing that way."));
				return false;
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		final List<Trap> traps = new ArrayList<Trap>();
		bombsHere(traps, mob, target);
		success = success || traps.size()>0;
		int numTargets = 0;
		for(final Trap T : traps )
			numTargets += getTargets(mob,T).size();
		success = success || numTargets > 0;
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":L("^S<S-NAME> incant(s) at <T-NAME> softly, making a long slow hissing sound...^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(final Trap T : traps )
				{
					final List<MOB> targetMs = getTargets(mob, T);
					if(targetMs.size()>0)
					{
						final MOB mob2=targetMs.get(CMLib.dice().roll(1, targetMs.size(), 0));
						T.spring(mob2);
					}
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> incant(s) at <T-NAME>, but nothing happens."));

		return success;
	}
}
