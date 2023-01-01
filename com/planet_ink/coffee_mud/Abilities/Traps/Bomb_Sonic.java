package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Bomb_Sonic extends StdBomb
{
	@Override
	public String ID()
	{
		return "Bomb_Sonic";
	}

	private final static String	localizedName	= CMLib.lang().L("sonic bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Bomb_Sonic()
	{
		super();
		trapLevel = 8;
	}

	@Override
	public String requiresToSet()
	{
		return "a pound of reed";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_REED));
		return V;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if((!(P instanceof Item))
		||(((Item)P).material()!=RawMaterial.RESOURCE_REED))
		{
			if(mob!=null)
				mob.tell(L("You need some reed to make this out of."));
			return false;
		}
		return true;
	}

	@Override
	protected boolean canExplodeOutOf(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_SYNTHETIC:
			return false;
		}
		return true;
	}

	protected void springOnRoomMobs(final Room R)
	{
		super.springOnRoomMobs(R);
		if((R != null)
		&&(R.resourceChoices() != null)
		&&(R.myResource()!=RawMaterial.RESOURCE_NOTHING))
		{
			final List<Integer> fishes = new ArrayList<Integer>();
			for(final Integer I : R.resourceChoices())
			{
				if(CMParms.indexOf(RawMaterial.CODES.FISHES(), I.intValue())>=0)
					fishes.add(I);
			}
			if(fishes.size()>0)
			{
				final MOB mob=CMClass.getFactoryMOB(R.name(),trapLevel(),R);
				try
				{
					for(int i=0;i<trapLevel();i++)
					{
						final int rsc = fishes.get(CMLib.dice().roll(1, fishes.size(), -1)).intValue();
						final Item buildingI = CMLib.materials().makeItemResource(rsc);
						final CMMsg msg=CMClass.getMsg(mob,buildingI,this,CMMsg.TYP_ITEMGENERATED|CMMsg.MASK_ALWAYS,null);
						if(R.okMessage(mob,msg))
						{
							final Item I=(Item)msg.target();
							R.addItem(I,ItemPossessor.Expire.Resource);
							R.recoverRoomStats();
							R.send(mob,msg);
						}
					}
					Ability tempA=R.fetchEffect("TemporaryAffects");
					if(tempA==null)
					{
						tempA=CMClass.getAbility("TemporaryAffects");
						tempA.startTickDown(mob, R, 10);
						tempA.makeLongLasting();
					}
					tempA.setMiscText("++ResourceOverride "+CMProps.getTicksPerHour()+" min=1 max=1 chance=100 NOTHING");
				}
				finally
				{
					mob.destroy();
				}
				R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 dead fish are now floating around here.",""+trapLevel()));
			}
		}
	}

	@Override
	public void spring(final MOB target)
	{
		final Room R=target.location();
		if(R!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				R.show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) the bomb!")));
			}
			else
			{
				final String triggerMsg = getTrigMsg(L("@x1 reverbs all over <T-NAME>!",affected.name()));
				final String damageMsg = getDamMsg(L("The burst <DAMAGE> <T-NAME>!"));
				if(R.show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE, triggerMsg))
				{
					super.spring(target);
					if(CMLib.flags().isWaterySurfaceRoom(R)||CMLib.flags().isUnderWateryRoom(R))
					{
						CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(trapLevel()+abilityCode(),5,1),
								CMMsg.MASK_ALWAYS|CMMsg.TYP_SONIC,Weapon.TYPE_BURSTING,damageMsg);
					}
					else
					{
						Ability A=(miscText.length()>0)?CMClass.getAbility(miscText):null;
						if(A==null)
							A=CMClass.getAbility("Spell_Deafness");
						if(A!=null)
							A.invoke(target,target,true,trapLevel()+abilityCode());
						CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(trapLevel()+abilityCode(),1,1),
								CMMsg.MASK_ALWAYS|CMMsg.TYP_SONIC,Weapon.TYPE_BURSTING,damageMsg);
					}
				}
			}
		}
	}

}
