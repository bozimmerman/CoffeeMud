package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2006-2018 Bo Zimmerman

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

public class Prayer_Rot extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Rot";
	}

	private final static String localizedName = CMLib.lang().L("Rot");

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
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public int classificationCode()
	{
		return ((affecting() instanceof Food)&&(!canBeUninvoked()))?Ability.ACODE_PROPERTY:Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;
	}

	private long nextTry=System.currentTimeMillis();

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((host instanceof Item)
		&&(((msg.tool() instanceof ShopKeeper)&&(msg.targetMinor()==CMMsg.TYP_GET))
			||(msg.targetMinor()==CMMsg.TYP_ROOMRESET))
		&&(msg.target()==host))
		{
			if(host instanceof Decayable)
			{
				((Decayable)host).setDecayTime(0);
				if(host instanceof Physical)
				{
					final Ability A=((Physical)host).fetchEffect("Poison_Rotten");
					if(A!=null)
						((Physical)host).delEffect(A);
				}
			}
			else
			if(host instanceof Container)
			{
				final List<Item> V=((Container)host).getDeepContents();
				for(int f=0;f<V.size();f++)
				{
					if(V.get(f) != null)
					{
						if(V.get(f) instanceof Decayable)
							((Decayable)V.get(f)).setDecayTime(0);
						final Ability A=V.get(f).fetchEffect("Poison_Rotten");
						if(A!=null)
							V.get(f).delEffect(A);
					}
				}
			}
		}
	}

	public void setRot(Item I)
	{
		if(((I instanceof Decayable)&&(((Decayable)I).decayTime()==0))
		&&(I.owner()!=null)
		&&(I.fetchEffect("Poison_Rotten")==null))
		{
			long newTime=0;
			switch(I.material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_FLESH:
			{
				newTime=
				System.currentTimeMillis()+(
				   CMProps.getTickMillis()
					*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)
				);
				break;
			}
			case RawMaterial.MATERIAL_VEGETATION:
			{
				newTime=
				System.currentTimeMillis()+(
				CMProps.getTickMillis()
				*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)
				*5);
				break;
			}
			}
			if(I instanceof Drink)
			{
				switch(((Drink)I).liquidType())
				{
				case RawMaterial.RESOURCE_BLOOD:
					newTime=
						System.currentTimeMillis()+(
						CMProps.getTickMillis()
						*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY));
					break;
				case RawMaterial.RESOURCE_MILK:
					newTime=
						System.currentTimeMillis()+(
						CMProps.getTickMillis()
						*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)
						*5);
					break;
				}
			}
			switch(I.material())
			{
			case RawMaterial.RESOURCE_BLOOD:
				newTime=
					System.currentTimeMillis()+(
					CMProps.getTickMillis()
					*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY));
				break;
			case RawMaterial.RESOURCE_MILK:
				newTime=
					System.currentTimeMillis()+(
					CMProps.getTickMillis()
					*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)
					*5);
				break;
			case RawMaterial.RESOURCE_HERBS:
			case RawMaterial.RESOURCE_WAX:
			case RawMaterial.RESOURCE_COFFEEBEANS:
			case RawMaterial.RESOURCE_SEAWEED:
			case RawMaterial.RESOURCE_SUGAR:
			case RawMaterial.RESOURCE_COCOA:
			case RawMaterial.RESOURCE_MUSHROOMS:
			case RawMaterial.RESOURCE_FUNGUS:
			case RawMaterial.RESOURCE_VINE:
			case RawMaterial.RESOURCE_FLOWERS:
			case RawMaterial.RESOURCE_NUTS:
			case RawMaterial.RESOURCE_CRACKER:
			case RawMaterial.RESOURCE_PIPEWEED:
			case RawMaterial.RESOURCE_GARLIC:
			case RawMaterial.RESOURCE_SOAP:
			case RawMaterial.RESOURCE_ASH:
				newTime=0;
				break;
			}
			if(I instanceof Decayable)
				((Decayable)I).setDecayTime(newTime);
		}
		if((((Decayable)I).decayTime()>0)
		&&(System.currentTimeMillis()>((Decayable)I).decayTime())
		&&(!CMLib.flags().isABonusItems(I)))
		{
			if(I.fetchEffect("Poison_Rotten")==null)
			{
				final Ability A=CMClass.getAbility("Poison_Rotten");
				if(A!=null)
					I.addNonUninvokableEffect(A);
			}
			if(I instanceof Food)
				((Food)I).setNourishment(0);
			else
			if(I instanceof Drink)
				((Drink)I).setThirstQuenched(0);
			((Decayable)I).setDecayTime(0);
		}
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affecting()==null)||(System.currentTimeMillis()>nextTry))
		{
			nextTry=System.currentTimeMillis()+60000;
			if(host instanceof Item)
				setRot(((Item)host));
			else
			if(host instanceof Container)
			{
				final List<Item> V=((Container)host).getDeepContents();
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v) != null)
						setRot(V.get(v));
				}
			}
		}
		return super.okMessage(host,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof Food))
			&&(!(target instanceof Drink)))
		{
			mob.tell(L("You cannot rot @x1!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
									auto?"":L("^S<S-NAME> rot <T-NAMESELF>@x1.^?",inTheNameOf(mob)),
									auto?"":L("^S<S-NAME> rots <T-NAMESELF>@x1.^?",inTheNameOf(mob)),
									auto?"":L("^S<S-NAME> rots <T-NAMESELF>@x1.^?",inTheNameOf(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean doneSomething=false;
				if((target instanceof Drink)&&(((Drink)target).liquidType()!=RawMaterial.RESOURCE_SALTWATER))
				{
					((Drink)target).setLiquidType(RawMaterial.RESOURCE_SALTWATER);
					doneSomething=true;
				}
				final Ability A=CMClass.getAbility("Poison_Rotten");
				if(A!=null)
					target.addNonUninvokableEffect(A);
				if((target instanceof Pill)
				&&(!((Pill)target).getSpellList().equals("Prayer_DrunkenStupor")))
				{
					doneSomething=true;
					((Pill)target).setSpellList("Prayer_DrunkenStupor");
				}
				if((target instanceof Potion)
				&&(!((Potion)target).getSpellList().equals("Prayer_DrunkenStupor")))
				{
					doneSomething=true;
					((Potion)target).setSpellList("Prayer_DrunkenStupor");
				}
				if(doneSomething)
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 appears rotted!",target.name()));
				target.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for rotting, but nothing happens.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
