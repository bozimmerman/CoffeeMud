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
   Copyright 2004-2025 Bo Zimmerman

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

public class NastyAbilities extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "NastyAbilities";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	protected boolean		fightok			= false;
	protected CompiledZMask	mask			= null;
	protected List<Ability>	mySkills		= null;
	protected int			numAllSkills	= -1;
	protected boolean		levelcheck		= false;

	public NastyAbilities()
	{
		super();
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "random malicious skill using";
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
		final List<String> V = CMParms.parse(parms.toUpperCase());
		fightok=V.contains("FIGHTOK");
		levelcheck=V.contains("CHECKLEVEL");
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
				final List<MOB> targets = new ArrayList<MOB>(1);
				for(int i=0;i<thisRoom.numInhabitants();i++)
				{
					final MOB M=thisRoom.fetchInhabitant(i);
					if((M!=null)
					&&(M!=mob)
					&&((mask==null)||CMLib.masking().maskCheck(mask, M, false))
					&&((!levelcheck)||(M.phyStats().level()>=(mob.phyStats().level()-CMProps.getIntVar(CMProps.Int.EXPRATE)))))
					{
						if(mask == null)
						{
							final MOB followMOB=M.getGroupLeader();
							if((followMOB.getVictim()==mob)
							||(followMOB.isMonster()))
								continue;
						}
						targets.add(M);
					}
				}
				if(targets.size()==0)
					return true;
				target = targets.get(CMLib.dice().roll(1, targets.size(), -1));
				if(target!=null)
				{
					if((numAllSkills!=mob.numAllAbilities())||(mySkills==null))
					{
						numAllSkills=mob.numAbilities();
						mySkills=new ArrayList<Ability>();
						for(final Enumeration<Ability> e=mob.allAbilities(); e.hasMoreElements();)
						{
							final Ability tryThisOne=e.nextElement();
							if((tryThisOne!=null)
							&&(tryThisOne.abstractQuality()==Ability.QUALITY_MALICIOUS)
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
						&&(tryThisOne.castingQuality(mob,target)==Ability.QUALITY_MALICIOUS))
						{
							final Map<MOB,MOB> H=new Hashtable<MOB,MOB>();
							for(int i=0;i<thisRoom.numInhabitants();i++)
							{
								final MOB M=thisRoom.fetchInhabitant(i);
								if((M!=null)&&(M.getVictim()!=null))
									H.put(M,M.getVictim());
							}
							tryThisOne.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,tryThisOne.ID()));
							final Vector<String> V=new Vector<String>();
							V.addElement(target.name());
							if((tryThisOne.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
								tryThisOne.invoke(mob,new Vector<String>(),null,false,0);
							else
								tryThisOne.invoke(mob,V,target,false,0);

							if(!fightok)
							{
								for(int i=0;i<thisRoom.numInhabitants();i++)
								{
									final MOB M=thisRoom.fetchInhabitant(i);
									if(H.containsKey(M))
										M.setVictim(H.get(M));
									else
										M.setVictim(null);
								}
							}
						}
					}
				}
			}
		}
		return true;
	}
}
