package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class UnderWater extends StdRoom implements Drink
{
	@Override
	public String ID()
	{
		return "UnderWater";
	}

	protected int liquidType = RawMaterial.RESOURCE_FRESHWATER;
	
	public UnderWater()
	{
		super();
		name="the water";
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SWIMMING);
		basePhyStats.setWeight(3);
		recoverPhyStats();
		climask=Places.CLIMASK_WET;
		atmosphere=liquidType;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_UNDERWATER;
	}

	@Override
	protected int baseThirst()
	{
		return 0;
	}

	@Override
	public long decayTime()
	{
		return 0;
	}

	@Override
	public void setDecayTime(long time)
	{
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}

	public static void sinkAffects(final Room room, final CMMsg msg)
	{
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			final MOB mob=msg.source();
			final boolean thirsty=mob.curState().getThirst()<=0;
			final boolean full=!mob.curState().adjThirst(((Drink)room).thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell(CMLib.lang().L("You are no longer thirsty."));
			else
			if(full)
				mob.tell(CMLib.lang().L("You have drunk all you can."));
		}

		if(msg.source().location()==room)
			CMLib.commands().handleHygienicMessage(msg, 100, PlayerStats.HYGIENE_WATERCLEAN);

		if(CMLib.flags().isSleeping(room))
			return;
		boolean foundReversed=false;
		boolean foundNormal=false;
		final Vector<Physical> needToSink=new Vector<Physical>();
		final Vector<Physical> mightNeedAdjusting=new Vector<Physical>();

		if((room.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(room.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
		{
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB mob=room.fetchInhabitant(i);
				if((mob!=null)
				&&((mob.getStartRoom()==null)||(mob.getStartRoom()!=room))
				&&(mob.riding()==null))
				{
					final Ability A=mob.fetchEffect("Sinking");
					if(A!=null)
					{
						if(CMath.s_bool(A.getStat("REVERSED")))
						{
							foundReversed=true;
							mightNeedAdjusting.addElement(mob);
						}
						foundNormal=foundNormal||(A.proficiency()<=0);
					}
					else
					if((!CMath.bset(mob.basePhyStats().disposition(),PhyStats.IS_SWIMMING))
					&&(!mob.charStats().getMyRace().racialCategory().equals("Amphibian"))
					&&(!mob.charStats().getMyRace().racialCategory().equals("Fish")))
						needToSink.addElement(mob);
				}
			}
		}
		for(int i=0;i<room.numItems();i++)
		{
			final Item item=room.getItem(i);
			if((item!=null)&&(item.container()==null))
			{
				final Ability A=item.fetchEffect("Sinking");
				if(A!=null)
				{
					if(A.proficiency()>=100)
					{
						foundReversed=true;
						mightNeedAdjusting.addElement(item);
					}
					foundNormal=foundNormal||(A.proficiency()<=0);
				}
				else
					needToSink.addElement(item);
			}
		}
		final boolean reversed=((foundReversed)&&(!foundNormal));
		for(final Physical P : mightNeedAdjusting)
		{
			final Ability A=P.fetchEffect("Sinking");
			if(A!=null)
				A.setStat("REVERSED", ""+reversed);
		}
		for(final Physical P : needToSink)
			CMLib.tracking().makeSink(P,room,reversed);
	}

	public static int isOkUnderWaterAffect(Room room, CMMsg msg)
	{
		if(CMLib.flags().isSleeping(room))
			return 0;

		if((msg.targetMinor()==CMMsg.TYP_FIRE)||(msg.sourceMinor()==CMMsg.TYP_FIRE)
		||(msg.targetMinor()==CMMsg.TYP_GAS)||(msg.sourceMinor()==CMMsg.TYP_GAS))
		{
			if((!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_ALWAYS)))
				msg.source().tell(CMLib.lang().L("That won't work underwater."));
			return -1;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool() instanceof Weapon))
		{
			final Weapon w=(Weapon)msg.tool();
			if((w.weaponDamageType()==Weapon.TYPE_SLASHING)
			||(w.weaponDamageType()==Weapon.TYPE_BASHING))
			{
				int damage=msg.value();
				damage=damage/3;
				damage=damage*2;
				msg.setValue(msg.value()-damage);
			}
		}
		else
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell(CMLib.lang().L("You don't want to be drinking saltwater."));
				return -1;
			}
			return 1;
		}
		return 0;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(UnderWater.isOkUnderWaterAffect(this,msg))
		{
		case -1:
			return false;
		case 1:
			return true;
		}
		return super.okMessage(myHost,msg);
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}

	@Override
	public int thirstQuenched()
	{
		return 500;
	}

	@Override
	public int liquidHeld()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Override
	public int liquidRemaining()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Override
	public int liquidType()
	{
		return liquidType;
	}

	@Override
	public void setLiquidType(int newLiquidType)
	{
		atmosphere=liquidType;
		liquidType = newLiquidType;
	}

	@Override
	public void setThirstQuenched(int amount)
	{
	}

	@Override
	public void setLiquidHeld(int amount)
	{
	}

	@Override
	public void setLiquidRemaining(int amount)
	{
	}

	@Override
	public boolean disappearsAfterDrinking()
	{
		return false;
	}

	@Override
	public boolean containsDrink()
	{
		return true;
	}

	@Override
	public int amountTakenToFillMe(Drink theSource)
	{
		return 0;
	}

	public static final Integer[] resourceList=
	{
		Integer.valueOf(RawMaterial.RESOURCE_SEAWEED),
		Integer.valueOf(RawMaterial.RESOURCE_FISH),
		Integer.valueOf(RawMaterial.RESOURCE_CATFISH),
		Integer.valueOf(RawMaterial.RESOURCE_SALMON),
		Integer.valueOf(RawMaterial.RESOURCE_CARP),
		Integer.valueOf(RawMaterial.RESOURCE_TROUT),
		Integer.valueOf(RawMaterial.RESOURCE_SAND),
		Integer.valueOf(RawMaterial.RESOURCE_CLAY),
		Integer.valueOf(RawMaterial.RESOURCE_LIMESTONE)
	};
	public static final List<Integer>	roomResources	= new Vector<Integer>(Arrays.asList(resourceList));

	@Override
	public List<Integer> resourceChoices()
	{
		return UnderWater.roomResources;
	}
}
