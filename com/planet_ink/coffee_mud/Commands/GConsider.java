package com.planet_ink.coffee_mud.Commands;
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
public class GConsider extends StdCommand
{
	public GConsider()
	{
	}

	private final String[] access=I(new String[]{"GCONSIDER","GCOS","GCO"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public int relativeLevelDiff(final MOB mob1, final Set<MOB> mobs)
	{
		if((mob1==null)||(mobs==null))
			return 0;
		final MOB mob2=mobs.iterator().next().getGroupLeader();
		final int mob2Armor=CMLib.combat().adjustedArmor(mob2);
		final int mob1Armor=CMLib.combat().adjustedArmor(mob1);
		final double mob1Attack=CMLib.combat().adjustedAttackBonus(mob1,mob2);
		final int mob1Dmg=mob1.phyStats().damage();
		final int mob2Hp=mob2.baseState().getHitPoints();
		final int mob1Hp=mob1.baseState().getHitPoints();

		double mob2HitRound=0.0;
		for (final Object element : mobs)
		{
			final MOB mob=(MOB)element;
			final double mob2Attack=CMLib.combat().adjustedAttackBonus(mob,mob1);
			final int mob2Dmg=mob.phyStats().damage();
			mob2HitRound+=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob2Attack/mob1Armor)),100.0))*CMath.div(mob2Dmg,2.0))+1.0)*CMath.mul(mob.phyStats().speed(),1.0);
		}
		final double mob1HitRound=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob1Attack/mob2Armor)),100.0))*CMath.div(mob1Dmg,2.0))+1.0)*CMath.mul(mob1.phyStats().speed(),1.0);
		final double mob2SurvivalRounds=CMath.div(mob2Hp,mob1HitRound);
		final double mob1SurvivalRounds=CMath.div(mob1Hp,mob2HitRound);

		//int levelDiff=(int)Math.round(CMath.div((mob1SurvivalRounds-mob2SurvivalRounds),1));
		final double levelDiff=(mob1SurvivalRounds-mob2SurvivalRounds)/2;
		final int levelDiffed=(int)Math.round(Math.sqrt(Math.abs(levelDiff)));

		return levelDiffed*(levelDiff<0.0?-1:1);
	}

	protected boolean doGConsider(final MOB mob, final MOB target)
	{
		final int relDiff=relativeLevelDiff(target,mob.getGroupMembers(new HashSet<MOB>()));
		final int lvlDiff=(target.phyStats().level()-mob.phyStats().level());
		final int realDiff=(relDiff+lvlDiff)/2;

		int theDiff=2;
		if(mob.phyStats().level()>20)
			theDiff=3;
		if(mob.phyStats().level()>40)
			theDiff=4;
		if(mob.phyStats().level()>60)
			theDiff=5;
		if(mob.phyStats().level()>80)
			theDiff=6;

		final int levelDiff=Math.abs(realDiff);
		if(levelDiff<theDiff)
		{
			mob.tell(L("The perfect match!"));
			return false;
		}
		else
		if(realDiff<0)
		{
			if(realDiff>-(2*theDiff))
			{
				mob.tell(L("@x1 might give you a fight.",target.charStats().HeShe()));
				return false;
			}
			else
			if(realDiff>-(3*theDiff))
			{
				mob.tell(L("@x1 is hardly worth your while.",target.charStats().HeShe()));
				return false;
			}
			else
			if(realDiff>-(4*theDiff))
			{
				mob.tell(L("@x1 is a pushover.",target.charStats().HeShe()));
				return false;
			}
			else
			{
				mob.tell(L("@x1 is not worth the effort.",target.charStats().HeShe()));
				return false;
			}

		}
		else
		if(realDiff<(2*theDiff))
		{
			mob.tell(L("@x1 looks a little tough.",target.charStats().HeShe()));
			return false;
		}
		else
		if(realDiff<(3*theDiff))
		{
			mob.tell(L("@x1 is a serious threat.",target.charStats().HeShe()));
			return false;
		}
		else
		if(realDiff<(4*theDiff))
		{
			mob.tell(L("@x1 will clean your clock.",target.charStats().HeShe()));
			return false;
		}
		else
		{
			mob.tell(L("@x1 WILL KILL YOU DEAD!",target.charStats().HeShe()));
			return false;
		}
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("GConsider whom?"));
			return false;
		}
		commands.remove(0);
		String targetName=CMParms.combine(commands,0);
		final List<MOB> targets = new ArrayList<MOB>();
		if(targetName.equalsIgnoreCase("SELF")||targetName.equalsIgnoreCase("ME"))
			targets.add(mob);
		else
		{
			boolean allFlag = targetName.equalsIgnoreCase("ALL");
			if ((targetName.toUpperCase().startsWith("ALL."))||(targetName.toUpperCase().startsWith("ALL ")))
			{
				allFlag = true;
				targetName = "ALL " + targetName.substring(4);
			}
			else
			if (targetName.toUpperCase().endsWith(".ALL"))
			{
				allFlag = true;
				targetName = "ALL " + targetName.substring(0, targetName.length() - 4);
			}
			MOB target=mob.location().fetchInhabitant(targetName);
			int ctr=1;
			if(target!=null)
			{
				if(((!allFlag)||(targetName.indexOf('.')>0))
				&&(CMLib.flags().canBeSeenBy(target, mob)))
					targets.add(target);
				else
				while ((target != null)
				&&((allFlag||(!CMLib.flags().canBeSeenBy(target, mob))))
				&&(targetName.indexOf('.')<0))
				{
					targets.add(target);
					target = mob.location().fetchInhabitant(targetName+"."+(++ctr));
				}
			}
		}
		if(targets.size()>1)
			targets.remove(mob);
		for(final Iterator<MOB> p=targets.iterator();p.hasNext();)
		{
			final MOB P = p.next();
			if(!CMLib.flags().canBeSeenBy(P,mob))
				p.remove();
		}
		if(targets.size()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see '@x1' here.",targetName));
			return false;
		}
		for(final MOB P : targets)
			doGConsider(mob,P);
		return true;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
