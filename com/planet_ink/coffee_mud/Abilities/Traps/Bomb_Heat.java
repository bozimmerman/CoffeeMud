package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2022-2024 Bo Zimmerman

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
public class Bomb_Heat extends StdBomb
{
	@Override
	public String ID()
	{
		return "Bomb_Heat";
	}

	private final static String	localizedName	= CMLib.lang().L("heat bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Bomb_Heat()
	{
		super();
		trapLevel = 6;
	}

	@Override
	public String requiresToSet()
	{
		return "some wood";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_OAK));
		return V;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if((!(P instanceof RawMaterial))
		||((((Item)P).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN))
		{
			if(mob!=null)
				mob.tell(L("You need some wood to make this out of."));
			return false;
		}
		return true;
	}

	protected boolean doesInnerExplosionDestroy(final int material)
	{
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_SYNTHETIC:
		case RawMaterial.MATERIAL_GAS:
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
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_SYNTHETIC:
			return false;
		}
		return true;
	}

	@Override
	protected void explodeContainer(final Container C)
	{
		final Room roomR=CMLib.map().roomLocation(C);
		final Race R;
		String name;
		int weight=C.phyStats().weight();
		if((C instanceof DeadBody)
		&&(((DeadBody)C).charStats()!=null)
		&&(!((DeadBody)C).isPlayerCorpse())
		&&(((DeadBody)C).charStats().getMyRace()!=null))
		{
			final DeadBody D = (DeadBody)C;
			R=D.charStats().getMyRace();
			if(D.getSavedMOB()!=null)
			{
				weight=D.getSavedMOB().baseWeight();
				name=D.getSavedMOB().name();
			}
			else
			{
				name=R.name();
				weight=R.lightestWeight()+(CMLib.dice().roll(1, R.weightVariance(), 0));
			}
		}
		else
		{
			R=null;
			name=null;
		}
		super.explodeContainer(C);
		if((R!=null)&&(C.amDestroyed()))
		{
			final List<RawMaterial> usableMats = new ArrayList<RawMaterial>();
			for(final RawMaterial mat : R.myResources())
			{
				if((mat.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
					usableMats.add(mat);
				else
				if((mat.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
					usableMats.add(mat);
			}
			if(usableMats.size()>0)
			{
				final RawMaterial mat = usableMats.get(CMLib.dice().roll(1, usableMats.size(),-1));
				if(name==null)
					name=RawMaterial.CODES.NAME(mat.material());
				else
				if(CMLib.english().startsWithAnArticle(name))
					name=name.substring(name.indexOf(' ')+1).trim();
				final Food F = (Food)CMClass.getBasicItem("GenFood");
				F.setName("some cooked "+name);
				F.setDisplayText(F.Name()+" is sitting here");
				F.basePhyStats().setWeight(weight);
				F.setNourishment(weight/5);
				F.setBite(250>F.nourishment()?F.nourishment():250);
				F.recoverPhyStats();
				roomR.addItem(F, Expire.Monster_Body);
			}
		}
	}
	@Override
	public void spring(final MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) the heat burst!")));
			}
			else
			{
				final String triggerMsg = getTrigMsg(L("@x1 blasts hot all over <T-NAME>!",affected.name()));
				final String damageMsg = getDamMsg(L("The heat <DAMAGE> <T-NAME>!"));
				if(target.location().show(invoker(),target,this,CMMsg.MSG_OK_ACTION,
						triggerMsg+CMLib.protocol().msp("fireball.wav",30)))
				{
					super.spring(target);
					CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(trapLevel()+abilityCode(),2,1),
							CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,damageMsg);
				}
			}
		}
	}

}
