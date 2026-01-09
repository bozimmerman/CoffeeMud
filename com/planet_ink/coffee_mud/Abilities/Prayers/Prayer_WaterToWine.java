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
   Copyright 2025-2026 Bo Zimmerman

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
public class Prayer_WaterToWine extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_WaterToWine";
	}

	private final static String localizedName = CMLib.lang().L("Water To Wine");

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
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL | Ability.FLAG_INTOXICATING;
	}

	public String	newName		= "";
	public boolean	drinkFlag	= false;
	public Ability	myPoison	= null;

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
		else
		if ((myPoison == null) || (myPoison.affecting() == null) || (myPoison.amDestroyed()))
		{
			super.unInvoke(); // skip my messaging
			affected.delEffect(this);
			return;
		}
		else
		if(newName.length()>0)
			affectableStats.setName(newName);
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof Item)
		&&(super.canBeUninvoked()))
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
						if (I2 instanceof Drink)
						{
							if(((Drink)I2).liquidType()==RawMaterial.RESOURCE_LIQUOR)
							{
								((Drink)I2).setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
								((Drink)I2).setLiquidTypeName(null);
							}
							if(I2.material()==RawMaterial.RESOURCE_LIQUOR)
								I2.setMaterial(RawMaterial.RESOURCE_FRESHWATER);
							final Ability alchiA=I2.fetchEffect("Poison_Alcohol");
							if (alchiA != null)
							{
								alchiA.unInvoke();
								I2.delEffect(alchiA);
							}
						}
					}
				}
			}
			if((I instanceof Drink)&&(((Drink)I).liquidType()==RawMaterial.RESOURCE_LIQUOR))
			{
				((Drink)I).setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
				((Drink)I).setLiquidTypeName(null);
			}
			if(I.material()==RawMaterial.RESOURCE_LIQUOR)
				I.setMaterial(RawMaterial.RESOURCE_FRESHWATER);
			if(this.myPoison != null)
			{
				this.myPoison.unInvoke();
				I.delEffect(this.myPoison);
				this.myPoison = null;
			}
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
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		int oldMaterial=target.material();
		if((target instanceof Drink)
		&&(((Drink)target).containsLiquid()))
		{
			oldMaterial = ((Drink)target).liquidType();
			drinkFlag = true;
		}
		if((oldMaterial != RawMaterial.RESOURCE_FRESHWATER)
		|| (CMLib.flags().isAlcoholic(target)))
		{
			mob.tell(L("@x1 is not fresh water.", target.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					L("^S<S-NAME> @x1 while moving <S-HIS-HER> hands around <T-NAMESELF>.^?", prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(CMClass.getAbility("Poison_Alcohol") == null)
				{
					mob.tell(L("Something goes wrong..."));
					return false;
				}
				final String oldResourceName=RawMaterial.CODES.NAME(oldMaterial);
				this.myPoison=null;
				if(drinkFlag)
				{
					if(target instanceof Container)
					{
						final List<Item> liqItems = ((Container)target).getContents();
						for (int i = 0; i < liqItems.size(); i++)
						{
							final Item I = liqItems.get(i);
							if (I instanceof Drink)
							{
								if((target instanceof Drink)&&(((Drink)I).liquidType()==RawMaterial.RESOURCE_FRESHWATER))
									((Drink)I).setLiquidType(RawMaterial.RESOURCE_LIQUOR);
								if(I.material()==RawMaterial.RESOURCE_FRESHWATER)
									I.setMaterial(RawMaterial.RESOURCE_LIQUOR);
								final Ability alchiA=CMClass.getAbility("Poison_Alcohol");
								if (alchiA != null)
								{
									alchiA.setInvoker(mob);
									target.addEffect(alchiA);
									alchiA.makeLongLasting();
									((Drink)I).setLiquidTypeName(L("wine"));
								}
							}
						}
					}
					if((target instanceof Drink)
					&&(((Drink)target).liquidType()==RawMaterial.RESOURCE_FRESHWATER))
					{
						((Drink)target).setLiquidType(RawMaterial.RESOURCE_LIQUOR);
						((Drink)target).setLiquidTypeName(L("wine"));
					}
					if(target.material()==RawMaterial.RESOURCE_FRESHWATER)
						target.setMaterial(RawMaterial.RESOURCE_LIQUOR);
					if(target.material()==oldMaterial)
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> change(s) into wine!"));
					else
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("The @x1 in <T-NAME> change(s) into wine!",oldResourceName.toLowerCase()));
				}
				else
					mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> change(s) into wine!"));
				this.myPoison=CMClass.getAbility("Poison_Alcohol");
				if (this.myPoison != null)
				{
					myPoison.setInvoker(mob);
					target.addEffect(myPoison);
					myPoison.makeLongLasting();
				}
				final String oldMaterialName=RawMaterial.Material.findByMask(oldMaterial&RawMaterial.MATERIAL_MASK).desc();
				String oldName=target.name().toUpperCase();
				final String newMaterialName = L("wine");
				oldName=CMStrings.replaceAll(oldName,oldResourceName,newMaterialName);
				oldName=CMStrings.replaceAll(oldName,oldMaterialName,newMaterialName);
				if(oldName.indexOf(newMaterialName)<0)
				{
					final int x=oldName.lastIndexOf(' ');
					if(x<0)
						oldName=newMaterialName+" "+oldName;
					else
						oldName=oldName.substring(0,x)+" "+newMaterialName+oldName.substring(x);
				}
				newName=CMStrings.capitalizeAndLower(oldName);
				final Prayer_WaterToWine maA = (Prayer_WaterToWine)beneficialAffect(mob,target,asLevel,0);
				if (maA != null)
					maA.myPoison = this.myPoison;
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 while moving <S-HIS-HER> hands around <T-NAMESELF>, but nothing happens.", prayWord(mob)));

		// return whether it worked
		return success;
	}
}
