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
   Copyright 2003-2025 Bo Zimmerman

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
public class Chant_NectarWeave extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_NectarWeave";
	}

	private final static String localizedName = CMLib.lang().L("Nectarweave");

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
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_NATURELORE;
	}

	public String newName="";
	public int oldMaterial=0;
	public String oldSubType="";
	public boolean drinkFlag=false;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(drinkFlag
		&& (super.canBeUninvoked())
		&& (affected instanceof Drink)
		&& (!((Drink)affected).containsLiquid()))
		{
			super.unInvoke(); // skip my messaging
			affected.delEffect(this);
			return;
		}
		if(newName.length()>0)
			affectableStats.setName(newName);
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof Item)&&(super.canBeUninvoked()))
		{
			final Item I=(Item)affected;
			if(drinkFlag && (I instanceof Drink))
			{
				if (I instanceof Container)
				{
					final List<Item> liqItems = ((Container) I).getContents();
					for (int i = 0; i < liqItems.size(); i++)
					{
						final Item I2 = liqItems.get(i);
						if ((I2 instanceof Drink) && (((Drink) I2).liquidType() == ((Drink) I).liquidType()))
							((Drink) I2).setLiquidType(oldMaterial);
					}
				}
				if(I.material()==((Drink)I).liquidType())
					I.setMaterial(oldMaterial);
				((Drink)I).setLiquidType(oldMaterial);
			}
			else
				I.setMaterial(oldMaterial);
			if(I.owner() instanceof Room)
				((Room)I.owner()).showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 reverts to its natural form.",I.name()));
			else
			if(I.owner() instanceof MOB)
				((MOB)I.owner()).tell(L("@x1 reverts to its natural form.",I.name(((MOB)I.owner()))));
		}
		super.unInvoke();
	}


	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.source().isMine(affected)))
		||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		||(msg.targetMinor()==CMMsg.TYP_EXPIRE)
		||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
			unInvoke();
		return super.okMessage(host,msg);
	}
	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String material="honey";
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(material);
		if(m==null)
			m=RawMaterial.Material.startsWithIgnoreCase(material);
		int newMaterial=(m==null)?-1:m.mask();
		if((newMaterial>=0)&&(m!=null))
		{
			final List<Integer> rscs = RawMaterial.CODES.COMPOSE_RESOURCES(newMaterial);
			if(rscs.size()>0)
			{
				newMaterial=rscs.get(0).intValue();
				material=RawMaterial.CODES.NAME(newMaterial);
			}
			else
			{
				material=m.desc();
				newMaterial=m.mask();
			}
		}
		else
		{
			newMaterial=RawMaterial.CODES.FIND_IgnoreCase(material.trim());
			if(newMaterial<0)
				newMaterial=RawMaterial.CODES.FIND_StartsWith(material.trim());
			if(newMaterial>=0)
				material=RawMaterial.CODES.NAME(newMaterial);
		}
		if(newMaterial<0)
		{
			mob.tell(L("'@x1' is not a known substance!",material));
			return false;
		}
		oldMaterial=target.material();
		if((target instanceof Drink)
		&&(((Drink)target).containsLiquid()))
		{
			oldMaterial = ((Drink)target).liquidType();
			drinkFlag = true;
		}
		if (oldMaterial == newMaterial)
		{
			mob.tell(L("@x1 is already made of @x2.", target.name(), material));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":L("^S<S-NAME> chant(s) over <T-NAMESELF> slowly and sweetly.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				material=CMStrings.capitalizeAndLower(material);
				final String oldResourceName=RawMaterial.CODES.NAME(oldMaterial);
				if(drinkFlag)
				{
					((Drink)target).setLiquidType(newMaterial);
					if(target instanceof Container)
					{
						final List<Item> liqItems = ((Container)target).getContents();
						for (int i = 0; i < liqItems.size(); i++)
						{
							final Item I = liqItems.get(i);
							if ((I instanceof Drink)
							&& (((Drink) I).liquidType() == oldMaterial))
								((Drink) I).setLiquidType(newMaterial);
						}
					}
					if(target.material()==oldMaterial)
					{
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> change(s) into @x1!",material));
						target.setMaterial(newMaterial);
					}
					else
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("The @x1 in <T-NAME> change(s) into @x2!",oldResourceName.toLowerCase(), material));
				}
				else
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> change(s) into @x1!",material));
					target.setMaterial(newMaterial);
				}
				final String oldMaterialName=RawMaterial.Material.findByMask(oldMaterial&RawMaterial.MATERIAL_MASK).desc();
				String oldName=target.name().toUpperCase();
				oldName=CMStrings.replaceAll(oldName,oldResourceName,material);
				oldName=CMStrings.replaceAll(oldName,oldMaterialName,material);
				if(oldName.indexOf(material)<0)
				{
					final int x=oldName.lastIndexOf(' ');
					if(x<0)
						oldName=material+" "+oldName;
					else
						oldName=oldName.substring(0,x)+" "+material+oldName.substring(x);
				}
				newName=CMStrings.capitalizeAndLower(oldName);
				beneficialAffect(mob,target,asLevel,100);
			}

		}
		else
			beneficialWordsFizzle(mob,target,L("^S<S-NAME> chant(s) over <T-NAMESELF>, but the magic sours.^?"));

		// return whether it worked
		return success;
	}
}
