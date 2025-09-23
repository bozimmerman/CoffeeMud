package com.planet_ink.coffee_mud.Abilities.Common;
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
import com.planet_ink.coffee_mud.core.interfaces.CostDef;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2025 Bo Zimmerman

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
public class Salvaging extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Salvaging";
	}

	private final static String	localizedName	= CMLib.lang().L("Salvaging");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SALVAGE", "SALVAGING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected CostDef getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_NATURELORE;
	}

	protected Item		found			= null;
	protected int		amount			= 0;
	protected String	oldItemName		= "";
	protected boolean	messedUp		= false;

	public Salvaging()
	{
		super();
		displayText = L("You are salvaging...");
		verb = L("salvaging");
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			if(found==null)
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	protected void finishSalvage(final MOB mob, final Item found, int amount)
	{
		final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
		msg.setValue(amount);
		if(mob.location().okMessage(mob, msg))
		{
			final String foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
			if(msg.value()<2)
				msg.modify(L("<S-NAME> manage(s) to salvage @x1.",found.name()));
			else
				msg.modify(L("<S-NAME> manage(s) to salvage @x1 pounds of @x2.",""+msg.value(),foundShortName));
			mob.location().send(mob, msg);
			amount=msg.value();
			int extra=0;
			int weight=1;
			if((amount>=20)
			&&(found instanceof RawMaterial))
			{
				weight=amount/10;
				extra=amount-(weight*10);
				amount=10;
			}
			for(int i=0;i<amount;i++)
			{
				final Item newFound=(Item)found.copyOf();
				if(newFound.basePhyStats().weight()<weight)
				{
					newFound.basePhyStats().setWeight(weight);
					newFound.phyStats().setWeight(weight);
					CMLib.materials().adjustResourceName(newFound);
				}
				if(!dropAWinner(mob,newFound))
					break;
			}
			for(int i=0;i<extra;i++)
			{
				final Item newFound=(Item)found.copyOf();
				if(newFound.basePhyStats().weight()<extra)
				{
					newFound.basePhyStats().setWeight(extra);
					newFound.phyStats().setWeight(extra);
					CMLib.materials().adjustResourceName(newFound);
				}
				if(!dropAWinner(mob,newFound))
					break;
			}

		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted)&&(mob.location()!=null))
				{
					if(messedUp)
						commonTelL(mob,"You've messed up salvaging @x1!",oldItemName);
					else
					{
						final Item baseShip=found;
						final int finalAmount=amount*(baseYield()+abilityCode());
						finishSalvage(mob,baseShip, finalAmount);
						if((baseShip.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
						{
							final Item metalFound=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON);
							final int metalAmount = Math.round(CMath.sqrt(finalAmount));
							finishSalvage(mob,metalFound, metalAmount);
						}
						if((baseShip.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
						{
							final Item clothFound=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_COTTON);
							final int metalAmount = Math.round(CMath.sqrt(finalAmount));
							final int clothAmount = Math.round(CMath.sqrt(metalAmount));
							finishSalvage(mob,clothFound, clothAmount);
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("salvaging");
		final String str=CMParms.combine(commands,0);
		final Item I=mob.location().findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonFaiL(mob,commands,"You don't see anything called '@x1' here.",str);
			return false;
		}
		boolean okMaterial=true;
		oldItemName=I.Name();
		switch(I.material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_FLESH:
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_PAPER:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_GAS:
		case RawMaterial.MATERIAL_VEGETATION:
		{
			okMaterial = false;
			break;
		}
		}
		if(!okMaterial)
		{
			commonFaiL(mob,commands,"You don't know how to salvage @x1.",I.name(mob));
			return false;
		}

		if(I instanceof RawMaterial)
		{
			commonFaiL(mob,commands,"@x1 already looks like salvage.",I.name(mob));
			return false;
		}

		if(CMLib.flags().isEnchanted(I))
		{
			commonFaiL(mob,commands,"@x1 is enchanted, and can't be salvaged.",I.name(mob));
			return false;
		}

		final LandTitle t=CMLib.law().getLandTitle(mob.location());
		if((t!=null)&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
		{
			commonFaiL(mob,commands,"You are not allowed to salvage anything here.");
			return false;
		}

		if((!(I instanceof NavigableItem))
		||(((NavigableItem)I).navBasis() != Rideable.Basis.WATER_BASED)
		||((((NavigableItem)I).subjectToWearAndTear())&&(((NavigableItem)I).usesRemaining()>0))
		||(((NavigableItem)I).getArea()==null))
		{
			commonFaiL(mob,commands,"You can only salvage large sunk sailing ships, which @x1 is not.",I.Name());
			return false;
		}
		final NavigableItem ship=(NavigableItem)I;
		final Area shipArea=ship.getArea();

		final int totalWeight=I.phyStats().weight();
		final List<Item> itemsToMove=new ArrayList<Item>();
		for(final Enumeration<Room> r=shipArea.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(R!=null)
			{
				if(R.numInhabitants()>0)
				{
					commonFaiL(mob,commands,"There are still people aboard!");
					return false;
				}
				for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
				{
					final Item I2=i.nextElement();
					if((I2!=null)&&(CMLib.flags().isGettable(I2))&&(I2.container()==null))
						itemsToMove.add(I2);
				}
			}
		}
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int duration=getDuration(45,mob,1,10);
		double pct = .1 +
				CMath.mul(0.05, super.getXLEVELLevel(mob)) +
				CMath.mul(CMath.div(adjustedLevel(mob,asLevel),15.0),0.05);
		if(pct > 1)
			pct = 1;
		amount=(int)Math.round(CMath.mul(pct, I.phyStats().weight()));
		if(amount < 1)
			amount = 1;
		messedUp=!proficiencyCheck(mob,0,auto);
		found=CMLib.materials().makeItemResource(I.material());
		playSound="ripping.wav";
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),L("<S-NAME> start(s) salvaging @x1.",I.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(final Item I2 : itemsToMove)
				mob.location().moveItemTo(I2);
			I.destroy();
			mob.location().recoverPhyStats();
			duration += CMath.sqrt(totalWeight/5);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
