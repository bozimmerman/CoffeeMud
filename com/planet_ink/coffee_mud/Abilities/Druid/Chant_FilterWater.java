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

public class Chant_FilterWater extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_FilterWater";
	}

	private final static String	localizedName	= CMLib.lang().L("Filter Water");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
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
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		int x=super.miscText.indexOf(';');
		if(x>0)
		{
			int y=super.miscText.indexOf('~',x);
			if(y>x+1)
				affectableStats.setName(super.miscText.substring(x+1,y));
			else
			if((y<0)&&(x<super.miscText.length()-1))
				affectableStats.setName(super.miscText.substring(x+1));
		}
	}

	@Override
	public void unInvoke()
	{
		final MOB invoker = super.invoker();
		final Physical affected = this.affected;
		String splitMe=text();
		super.unInvoke();
		if((affected != null) && this.canBeUninvoked() && this.unInvoked && (!affected.amDestroyed()))
		{
			if((!(affected instanceof Item))||(((Item)affected).container()==null))
			{
				Room R=CMLib.map().roomLocation(affected);
				if((R!=null)&&(invoker!=null))
					R.show(invoker,affected,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> revert(s) to its previous state."));
			}
			int x=splitMe.indexOf('~');
			if(x>0)
			{
				affected.setMiscText(splitMe.substring(x+1));
				splitMe=splitMe.substring(0,x);
			}
			x=splitMe.indexOf(';');
			if(x>0)
				splitMe=splitMe.substring(0,x);
			x=splitMe.indexOf('/');
			int oldLiquidType;
			int oldMaterial;
			if(x>=0)
			{
				oldMaterial=CMath.s_int(splitMe.substring(0,x));
				oldLiquidType = CMath.s_int(splitMe.substring(x+1));
			}
			else
			{
				oldMaterial=CMath.s_int(splitMe);
				oldLiquidType = oldMaterial;
			}
			if(affected instanceof Item)
				((Item)affected).setMaterial(oldMaterial);
			if(affected instanceof Drink)
				((Drink)affected).setLiquidType(oldLiquidType);
		}
	}
	
	public boolean finalizeFreshness(MOB mob, Physical target, Drink D, int asLevel)
	{
		StringBuilder parms=new StringBuilder("");
		if(target instanceof Item)
		{
			Item I=(Item)target;
			String rscName = RawMaterial.CODES.NAME(I.material()).toLowerCase();
			parms.append(I.material()+"/"+((Drink)I).liquidType());
			int x=I.Name().toLowerCase().indexOf(" "+rscName+" ");
			parms.append(";");
			if(x>0)
				parms.append(I.Name().substring(0,x)+" fresh water "+I.Name().substring(x+rscName.length()+2));
			parms.append("~").append(I.text());
			if(I.material() == D.liquidType())
				I.setMaterial(RawMaterial.RESOURCE_FRESHWATER);
		}
		else
		{
			parms.append("0/"+D.liquidType());
			parms.append(";~").append(D.text());
		}
		if(target.fetchEffect(ID())==null)
		{
			D.delAllEffects(true);
			Ability A=this.beneficialAffect(mob, target, asLevel, 0);
			if(A!=null)
			{
				A.setMiscText(parms.toString());
				D.setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
				if(target instanceof SpellHolder)
					((SpellHolder)target).setSpellList("");
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=this.getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if(!(target instanceof Drink))
		{
			mob.tell(L("Your magic cannot filter @x1.",target.name(mob)));
			return false;
		}
		
		Drink D=(Drink)target;
		if((D.liquidType() == RawMaterial.RESOURCE_FRESHWATER)
		&&(!(D instanceof SpellHolder))
		&&(D.numEffects()==0))
		{
			mob.tell(L("@x1 already contains fresh water.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=D.containsDrink() && (D.thirstQuenched() > 0) && proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> gain(s) life experience!":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(finalizeFreshness(mob, target, D, asLevel)
				&&(target instanceof Container))
				{
					final List<Item> V=((Container)D).getContents();
					for(int v=0;v<V.size();v++)
					{
						Item I=V.get(v);
						if((I instanceof Drink)
						&&(I instanceof RawMaterial)
						&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
						&&(I.material()!=RawMaterial.RESOURCE_FRESHWATER))
							this.finalizeFreshness(mob, I, (Drink)I, asLevel);
					}
				}
				mob.location().show(mob, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME> begin(s) to flow clean and clear!"));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chants at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
