package com.planet_ink.coffee_mud.Items.MiscMagic;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class Ingredients extends BagOfEndlessness
{
	@Override
	public String ID()
	{
		return "Ingredients";
	}

	boolean	alreadyFilled	= false;

	public Ingredients()
	{
		super();
		setName("an ingredients bag");
		secretIdentity="The Archon's Secret Ingredient Bag";
		recoverPhyStats();
	}

	protected Item makeResource(String name, int type)
	{
		Item I=null;
		if(((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
		||((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		I.setName(name);
		I.setDisplayText(L("@x1 has been left here.",name));
		I.setDescription(L("It looks like @x1",name));
		I.setMaterial(type);
		I.setBaseValue(RawMaterial.CODES.VALUE(type));
		I.basePhyStats().setWeight(1);
		CMLib.materials().addEffectsToResource(I);
		I.recoverPhyStats();
		I.setContainer(this);
		if(I instanceof Decayable)
		{
			((Decayable)I).setDecayTime(0);
			final Ability A=I.fetchEffect("Poison_Rotten");
			if(A!=null)
				I.delEffect(A);
		}
		if(owner() instanceof Room)
			((Room)owner()).addItem(I);
		else
		if(owner() instanceof MOB)
			((MOB)owner()).addItem(I);
		return I;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((!alreadyFilled)&&(owner()!=null))
		{
			alreadyFilled=true;
			if(!hasContent())
			{
				for(final int rsc : RawMaterial.CODES.ALL())
					makeResource(RawMaterial.CODES.NAME(rsc).toLowerCase(),rsc);
			}
		}
		else
		if(msg.amITarget(this)
		&&(msg.tool() instanceof Decayable)
		&&(msg.tool() instanceof Item)
		&&(((Item)msg.tool()).container()==this)
		&&(((Item)msg.tool()).owner() !=null))
		{
			((Decayable)msg.tool()).setDecayTime(0);
			final Ability A=((Item)msg.tool()).fetchEffect("Poison_Rotten");
			if(A!=null)
				((Item)msg.tool()).delEffect(A);
		}
		super.executeMsg(myHost,msg);
	}
}
