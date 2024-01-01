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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_FavoredMount extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_FavoredMount";
	}

	private final static String localizedName = CMLib.lang().L("Favored Mount");

	@Override
	public String name()
	{
		return localizedName;
	}


	private String desc = null;
	private Pair<String, String> pair = null;

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
			return L("(Favor for a mount)");
		else
			return L("(Favoring "+desc+" mounts)");
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
		if(!(affected instanceof MOB))
			return super.miscText;
		return super.miscText;
	}

	final static Pair<String, String> defaultMount = new Pair<String,String>("Equine","Horse");

	protected Pair<String,String> pickMount(final MOB mob)
	{
		if(mob == null)
			return defaultMount;
		synchronized(("MOUNTS_"+mob).intern())
		{
			final Map<String,Pair<String,String>> mapH=Fighter_RacialMount.getRacialMountChoices();
			final Pair<String,String> racialMount = Fighter_RacialMount.getMountInfo(mob);
			final Set<String> alreadyChosens = new XTreeSet<String>();
			if(racialMount != null)
				alreadyChosens.add(racialMount.first+","+racialMount.second);
			for(final Pair<String, Race> choice : CMLib.utensils().getFavoredMounts(mob))
				alreadyChosens.add(choice.first+","+choice.second.ID());
			final PairList<String,String> choices = new PairArrayList<String,String>();
			for(final String key : mapH.keySet())
			{
				final Pair<String, String> p = mapH.get(key);
				if(!alreadyChosens.contains(p.first+","+p.second))
					choices.add(p);
			}
			if(choices.size()>0)
			{
				final Pair<String,String> chosen = choices.get(CMLib.dice().roll(1, choices.size(), -1));
				return chosen;
			}
			return defaultMount;
		}
	}

	protected Pair<String, String> getMountInfo(final MOB mob)
	{
		if(pair != null)
			return pair;
		if(super.miscText.indexOf(',')<=0)
		{
			final Pair<String,String> p  = pickMount((MOB)affected);
			if (p != null)
			{
				super.setMiscText(p.first+","+p.second);
				if(this.isNowAnAutoEffect())
				{
					final Ability A = mob.fetchAbility(ID());
					if(A!=null)
						A.setMiscText(p.first+","+p.second);
				}
				else
				{
					final Ability A = mob.fetchEffect(ID());
					if(A!=null)
						A.setMiscText(p.first+","+p.second);
				}
			}
		}
		if(pair == null)
		{
			final int x = super.miscText.indexOf(',');
			if(x>0)
				pair = new Pair<String,String>(super.miscText.substring(0,x),super.miscText.substring(x+1));
		}
		if(pair != null)
			return pair;
		return defaultMount;
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
		if((lastLeave != 0)
		&& ((System.currentTimeMillis() - lastLeave)<250))
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
				if((goC!=null)
				&&(goC.getClass().isInstance(O)))
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
				{
					if(!msg.target().okMessage(myHost, msg))
						return false;
					if((msg.tool() instanceof Physical)
					&&(!msg.tool().okMessage(myHost, msg)))
						return false;
					msg.setTargetCode(msg.targetCode()|CMMsg.MASK_ALWAYS);
					// cavy as target doesn't work because eval-order
				}
				else
				if((msg.sourceMinor()==CMMsg.TYP_GET)
				&&(msg.target() instanceof Item)
				&&(((Item)msg.target()).owner() instanceof Room)
				&&(!CMLib.utensils().reachableItem(msg.source(),msg.target()))
				&&(isMount(msg.source(),(MOB)msg.source().riding())))
				{
					if(!msg.target().okMessage(myHost, msg))
						return false;
					msg.setTargetCode(msg.targetCode()|CMMsg.MASK_ALWAYS);
				}
			}
			else
			if((msg.target()==affected)
			&&(((MOB)msg.target()).riding() instanceof MOB)
			&&(msg.sourceMinor()==CMMsg.TYP_GIVE)
			&&(isMount((MOB)msg.target(),(MOB)((MOB)msg.target()).riding()))
			&&(msg.source().riding()==null))
			{
				if(!msg.target().okMessage(myHost, msg))
					return false;
				if((msg.tool() instanceof Physical)
				&&(!msg.tool().okMessage(myHost, msg)))
					return false;
				msg.setTargetCode(msg.targetCode()|CMMsg.MASK_ALWAYS);
				// cavy as target doesn't work because eval-order
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
