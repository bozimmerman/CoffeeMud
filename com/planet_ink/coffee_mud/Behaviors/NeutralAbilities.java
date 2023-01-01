package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2023 Bo Zimmerman

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

public class NeutralAbilities extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "NeutralAbilities";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	protected CompiledZMask	mask			= null;
	protected List<Ability>	mySkills		= null;
	protected int			numAllSkills	= -1;

	public NeutralAbilities()
	{
		super();
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "random indifferent skill using";
	}

	@Override
	public void setParms(final String newParms)
	{
		String parms = newParms;
		String mask = "";
		final int x=newParms.indexOf(';');
		if(x>0)
		{
			parms = newParms.substring(0,x);
			mask = newParms.substring(x+1);
		}
		super.setParms(parms);
		super.parms = newParms;
		this.mask = null;
		if(mask.trim().length()>0)
			this.mask = CMLib.masking().getPreCompiledMask(mask.trim());
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			final Room thisRoom=mob.location();
			if(thisRoom==null)
				return true;

			final double aChance=CMath.div(mob.curState().getMana(),mob.maxState().getMana());
			if((Math.random()>aChance)||(mob.curState().getMana()<50))
				return true;

			if(thisRoom.numPCInhabitants()>0)
			{
				MOB target=null;
				if(mask == null)
					target=mob;
				else
				{
					final List<MOB> targets = new ArrayList<MOB>(1);
					for(int i=0;i<thisRoom.numInhabitants();i++)
					{
						final MOB M=thisRoom.fetchInhabitant(i);
						if((M!=null)
						&&(M!=mob)
						&&(CMLib.masking().maskCheck(mask, M, false)))
							targets.add(M);
					}
					if(targets.size()==0)
						return true;
					target = targets.get(CMLib.dice().roll(1, targets.size(), -1));
				}
				if((target != null)
				&&((numAllSkills!=mob.numAllAbilities())||(mySkills==null)))
				{
					numAllSkills=mob.numAbilities();
					mySkills=new ArrayList<Ability>();
					for(final Enumeration<Ability> e=mob.allAbilities(); e.hasMoreElements();)
					{
						final Ability tryThisOne=e.nextElement();
						if((tryThisOne!=null)
						&&(tryThisOne.abstractQuality()==Ability.QUALITY_INDIFFERENT)
						&&(((tryThisOne.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER)
							||tryThisOne.appropriateToMyFactions(mob)))
						{
							mySkills.add(tryThisOne);
						}
					}
				}
				if(mySkills.size()>0)
				{
					final Ability tryThisOne=mySkills.get(CMLib.dice().roll(1, mySkills.size(), -1));
					if((mob.fetchEffect(tryThisOne.ID())==null)
					&&(tryThisOne.castingQuality(mob,null)==Ability.QUALITY_INDIFFERENT))
					{
						tryThisOne.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,tryThisOne.ID()));
						final Vector<String> V=new XVector<String>();
						if((target != mob)&&(target != null)&&(tryThisOne.canTarget(Ability.CAN_MOBS)))
						{
							V.add("$"+target.Name()+"$");
							tryThisOne.invoke(mob,V,null,false,0);
						}
						else
							tryThisOne.invoke(mob,V,target,false,0);
					}
				}
			}
		}
		return true;
	}
}
