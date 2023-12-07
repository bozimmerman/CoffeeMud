package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Properties.Prop_RideResister;
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
import java.util.concurrent.atomic.AtomicBoolean;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_RacialMount extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_RacialMount";
	}

	private final static String localizedName = CMLib.lang().L("Racial Mount");

	@Override
	public String name()
	{
		return localizedName;
	}


	private String desc = null;

	@Override
	public String displayText()
	{
		if(desc == null)
		{
			if(affected instanceof MOB)
			{
				final Pair<String, String> p = getMountInfo((MOB)affected);
				if (p != null)
					desc = p.first;
			}
		}
		if(desc == null)
			return L("(Affinity for a mount)");
		else
			return L("(Affinity for "+desc+" mounts)");
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		desc = null;
		super.setAffectedOne(P);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public String text()
	{
		if(affected instanceof MOB)
		{
			final Pair<String, String> p = getMountInfo((MOB)affected);
			if (p != null)
				return p.first+","+p.second;
		}
		return super.text();
	}

	protected static String getRacialCategory(final String str)
	{
		for(final Enumeration<Race> r = CMClass.races();r.hasMoreElements();)
		{
			final Race R = r.nextElement();
			if(R.racialCategory().equalsIgnoreCase(str))
				return R.racialCategory();
		}
		return null;
	}

	final static Pair<String, String> defaultMount = new Pair<String,String>("Equine","Horse");

	protected static Map<String,Pair<String,String>> getRacialMountChoices()
	{
		@SuppressWarnings("unchecked")
		Map<String,Pair<String,String>> mapH=(Map<String,Pair<String,String>>)Resources.getResource("PARSED_RECIPE: racialmounts.txt");
		if(mapH==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+"racialmounts.txt",null,CMFile.FLAG_LOGERRORS).text();
			mapH = new Hashtable<String,Pair<String,String>>();
			for(final String lstr :  Resources.getFileLineVector(str) )
			{
				if(lstr.startsWith("#"))
					continue;
				final List<String> V = CMParms.parseTabs(lstr, true);
				if(V.size()>=3)
				{
					final String key;
					final String posskey = V.get(0);
					if(posskey.equals("*"))
						key = "*";
					else
					{
						final Race sR = CMClass.getRace(posskey);
						if(sR != null)
							key = sR.ID();
						else
							key = getRacialCategory(posskey);
					}
					if(key == null)
						Log.errOut("Fighter_RacialMount","Unknown racial mount host key "+posskey);
					else
					{
						final String cat = getRacialCategory(V.get(1));
						final Race rR = CMClass.getRace(V.get(2));
						if (rR == null)
							Log.errOut("Fighter_RacialMount","Unknown racial mount race "+V.get(2));
						else
						if (cat == null)
							Log.errOut("Fighter_RacialMount","Unknown racial mount category "+V.get(1));
						else
							mapH.put(key, new Pair<String,String>(cat,rR.ID()));
					}
				}
			}
			Resources.submitResource("PARSED_RECIPE: racialmounts.txt",mapH);
		}
		return mapH;
	}

	protected static Pair<String, String> getMountInfo(final MOB mob)
	{
		if(mob == null)
			return null;
		final Race R = mob.baseCharStats().getMyRace();
		final Map<String,Pair<String,String>> mapH = Fighter_RacialMount.getRacialMountChoices();
		Pair<String,String> p = mapH.get(R.ID());
		if(p == null)
		{
			p = mapH.get(R.racialCategory());
			if(p == null)
			{
				p = mapH.get("*");
				if(p == null)
					p = defaultMount;
			}
		}
		return p;
	}

	protected boolean isMount(final MOB mob, final MOB P)
	{
		return (mob.riding()==P)
			&&(getMountInfo(mob).first.equals(P.baseCharStats().getMyRace().racialCategory()));
	}

	private volatile long lastLeave = 0;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((lastLeave != 0) && ((System.currentTimeMillis() - lastLeave)<250))
			affectableStats.setSpeed(affectableStats.speed()*4.0);
	}

	protected boolean isGoCommand(final MOB mob)
	{
		final Pair<Object,List<String>> top = mob.getTopCommand();
		if(top != null)
		{
			final Object O = top.first;
			if(O instanceof Command)
			{
				final Command goC = CMClass.getCommand("Go");
				if((goC!=null)&&(goC.getClass().isInstance(O)))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if(msg.source() instanceof Rideable)
			{
				final MOB mob = (MOB)this.affected;
				if((msg.targetMinor() == CMMsg.TYP_LEAVE)
				&&(msg.target() instanceof Room)
				&&(mob.commandQueSize()>0)
				&&(isGoCommand(mob))
				&&(isMount(mob,msg.source()))
				&&(proficiencyCheck(mob,0,false)))
				{
					lastLeave = System.currentTimeMillis();
					mob.recoverPhyStats();
				}
				else
				if(lastLeave != 0)
				{
					lastLeave = 0;
					mob.recoverPhyStats();
				}
			}
			else
			if((msg.source()==affected)
			&&(msg.source().riding() instanceof MOB))
			{
				if((msg.sourceMinor()==CMMsg.TYP_GIVE)
				&&(msg.target() instanceof MOB)
				&&(isMount(msg.source(),(MOB)msg.source().riding()))
				&&(((MOB)msg.target()).riding()==null))
					msg.setTargetCode(msg.targetCode()|CMMsg.MASK_ALWAYS);
					// cavy as target doesn't work because eval-order
				else
				if((msg.sourceMinor()==CMMsg.TYP_GET)
				&&(msg.target() instanceof Item)
				&&(((Item)msg.target()).owner() instanceof Room)
				&&(!CMLib.utensils().reachableItem(msg.source(),msg.target()))
				&&(isMount(msg.source(),(MOB)msg.source().riding())))
					msg.setTargetCode(msg.targetCode()|CMMsg.MASK_ALWAYS);
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((lastLeave != 0)
		&&((System.currentTimeMillis() - lastLeave)>=250))
		{
			lastLeave=0;
			final Physical affected = this.affected;
			if(affected instanceof MOB)
				((MOB)affected).recoverPhyStats();
		}
		return true;
	}
}
