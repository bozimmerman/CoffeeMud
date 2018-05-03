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
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
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
	public boolean tick(Tickable ticking, int tickID)
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
	
	protected void finishSalvage(final MOB mob, final Item found, final int amount)
	{
		final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
		msg.setValue(amount);
		if(mob.location().okMessage(mob, msg))
		{
			String s="s";
			if(msg.value()==1)
				s="";
			msg.modify(L("<S-NAME> manage(s) to salvage @x1 pound@x2 of @x3.",""+msg.value(),s,RawMaterial.CODES.NAME(found.material()).toLowerCase()));
			mob.location().send(mob, msg);
			for(int i=0;i<msg.value();i++)
			{
				final Item newFound=(Item)found.copyOf();
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
						commonTell(mob,L("You've messed up salvaging @x1!",oldItemName));
					else
					{
						Item baseShip=found;
						int finalAmount=amount*(baseYield()+abilityCode());
						finishSalvage(mob,baseShip, finalAmount);
						if((baseShip.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
						{
							Item metalFound=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON);
							int metalAmount = Math.round(CMath.sqrt(finalAmount));
							finishSalvage(mob,metalFound, metalAmount);
						}
						if((baseShip.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
						{
							Item clothFound=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_COTTON);
							int metalAmount = Math.round(CMath.sqrt(finalAmount));
							int clothAmount = Math.round(CMath.sqrt(metalAmount));
							finishSalvage(mob,clothFound, clothAmount);
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("salvaging");
		final String str=CMParms.combine(commands,0);
		final Item I=mob.location().findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,L("You don't see anything called '@x1' here.",str));
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
			commonTell(mob,L("You don't know how to salvage @x1.",I.name(mob)));
			return false;
		}

		if(I instanceof RawMaterial)
		{
			commonTell(mob,L("@x1 already looks like salvage.",I.name(mob)));
			return false;
		}

		if(CMLib.flags().isEnchanted(I))
		{
			commonTell(mob,L("@x1 is enchanted, and can't be salvaged.",I.name(mob)));
			return false;
		}

		final LandTitle t=CMLib.law().getLandTitle(mob.location());
		if((t!=null)&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
		{
			mob.tell(L("You are not allowed to salvage anything here."));
			return false;
		}

		if((!(I instanceof SailingShip))
		||((((SailingShip)I).subjectToWearAndTear())&&(((SailingShip)I).usesRemaining()>0))
		||(((SailingShip)I).getShipArea()==null))
		{
			mob.tell(L("You can only salvage large sunk sailing ships, which @x1 is not.",I.Name()));
			return false;
		}
		SailingShip ship=(SailingShip)I;
		Area shipArea=ship.getShipArea();
		
		int totalWeight=I.phyStats().weight();
		final Vector<Item> itemsToMove=new Vector<Item>();
		for(Enumeration<Room> r=shipArea.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(R!=null)
			{
				if(R.numInhabitants()>0)
				{
					mob.tell(L("There are still people aboard!"));
					return false;
				}
				for(Enumeration<Item> i=R.items();i.hasMoreElements();)
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
		amount=I.phyStats().weight();
		messedUp=!proficiencyCheck(mob,0,auto);
		found=CMLib.materials().makeItemResource(I.material());
		playSound="ripping.wav";
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),L("<S-NAME> start(s) salvaging @x1.",I.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(Item I2 : itemsToMove)
				mob.location().moveItemTo(I2);
			I.destroy();
			mob.location().recoverPhyStats();
			duration += CMath.sqrt(totalWeight/5);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
