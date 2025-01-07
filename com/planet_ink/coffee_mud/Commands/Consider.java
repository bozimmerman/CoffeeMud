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
   Copyright 2004-2024 Bo Zimmerman

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
public class Consider extends StdCommand
{
	public Consider()
	{
	}

	private final String[] access=I(new String[]{"CONSIDER","COS","CO"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{MOB.class}};

	public int relativeLevelDiff(final MOB mob1, final MOB mob2)
	{
		if((mob1==null)||(mob2==null))
			return 0;
		final int mob2Armor=CMLib.combat().adjustedArmor(mob2);
		final int mob1Armor=CMLib.combat().adjustedArmor(mob1);
		final double mob1Attack=CMLib.combat().adjustedAttackBonus(mob1,mob2);
		final double mob2Attack=CMLib.combat().adjustedAttackBonus(mob2,mob1);
		final int mob2Dmg=mob2.phyStats().damage();
		final int mob1Dmg=mob1.phyStats().damage();
		final int mob2Hp=mob2.baseState().getHitPoints();
		final int mob1Hp=mob1.baseState().getHitPoints();

		final double mob2HitRound=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob2Attack/mob1Armor)),100.0))*CMath.div(mob2Dmg,2.0))+1.0)*CMath.mul(mob2.phyStats().speed(),1.0);
		final double mob1HitRound=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob1Attack/mob2Armor)),100.0))*CMath.div(mob1Dmg,2.0))+1.0)*CMath.mul(mob1.phyStats().speed(),1.0);
		final double mob2SurvivalRounds=CMath.div(mob2Hp,mob1HitRound);
		final double mob1SurvivalRounds=CMath.div(mob1Hp,mob2HitRound);

		//int levelDiff=(int)Math.round(CMath.div((mob1SurvivalRounds-mob2SurvivalRounds),1));
		final double levelDiff=(mob1SurvivalRounds-mob2SurvivalRounds)/2;
		final int levelDiffed=(int)Math.round(Math.sqrt(Math.abs(levelDiff)));

		return levelDiffed*(levelDiff<0.0?-1:1);
	}

	public int doConsider(final MOB mob, final Physical target, final boolean heShe, final String msgStr)
	{
		final Room R=mob.location();
		if(R==null)
			return 0;
		final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MASK_EYES|CMMsg.TYP_OK_VISUAL,null,msgStr,msgStr);
		if(R.okMessage(mob,msg))
			R.send(mob,msg);
		int lvlDiff=0;
		if(target instanceof MOB)
		{
			final MOB targetMOB=(MOB)target;
			final int relDiff=relativeLevelDiff(targetMOB,mob);
			lvlDiff=(target.phyStats().level()-mob.phyStats().level());
			final int realDiff=relDiff;//(relDiff+lvlDiff)/2;

			int theDiff=2;
			if(mob.phyStats().level()>20)
				theDiff=3;
			if(mob.phyStats().level()>40)
				theDiff=4;
			if(mob.phyStats().level()>60)
				theDiff=5;
			if(mob.phyStats().level()>80)
				theDiff=6;

			final String name = heShe?targetMOB.charStats().HeShe():"@x1";
			final String targetName = target.name(mob);
			final StringBuilder levelMsg=new StringBuilder("");
			if(lvlDiff==0)
				levelMsg.append(L(name+" is your equal", targetName));
			else
			if(lvlDiff<-CMProps.getIntVar(CMProps.Int.EXPRATE))
				levelMsg.append(L(name+" is vastly inferior to you", targetName));
			else
			if(lvlDiff>CMProps.getIntVar(CMProps.Int.EXPRATE))
				levelMsg.append(L(name+" is far superior to you", targetName));
			else
			if(CMProps.getIntVar(CMProps.Int.EXPRATE)!=0)
			{
				final int relLvlDiff=(lvlDiff<0)?-lvlDiff:lvlDiff;
				final double pct=CMath.div(relLvlDiff,CMProps.getIntVar(CMProps.Int.EXPRATE));
				if((lvlDiff<0)&&(pct<0.5))
					levelMsg.append(L(name+" is almost your equal", targetName));
				else
				if((lvlDiff<0)&&(pct<=1.0))
					levelMsg.append(L(name+" is somewhat inferior to you", targetName));
				else
				if((lvlDiff<0))
					levelMsg.append(L(name+" is inferior to you", targetName));
				else
				if((lvlDiff>0)&&(pct<0.5))
					levelMsg.append(L(name+" is almost your equal", targetName));
				else
				if((lvlDiff>0)&&(pct<0.8))
					levelMsg.append(L(name+" is somewhat superior to you", targetName));
				else
					levelMsg.append(L(name+" is superior to you", targetName));
			}

			final int levelDiff=Math.abs(realDiff);
			if(levelDiff<theDiff)
			{
				if(levelMsg.length()==0)
					levelMsg.append(targetName+" is ");
				else
					levelMsg.append(L((lvlDiff!=0)?" but ":" and "));
				levelMsg.append(L("is the perfect match!"));
			}
			else
			if(realDiff<0)
			{
				if(levelMsg.length()==0)
					levelMsg.append(targetName+" ");
				else
					levelMsg.append(L((lvlDiff<0)?" and ":" but "));
				if(realDiff>-(2*theDiff))
					levelMsg.append(L("might actually give you a fight."));
				else
				if(realDiff>-(3*theDiff))
					levelMsg.append(L("won't put up a big fight."));
				else
				if(realDiff>-(4*theDiff))
					levelMsg.append(L("is basically a pushover."));
				else
					levelMsg.append(L("is an easy kill."));
			}
			else
			{
				if(levelMsg.length()==0)
					levelMsg.append(targetName+" ");
				else
					levelMsg.append(L((lvlDiff>0)?" and ":" but "));
				if(realDiff<(2*theDiff))
					levelMsg.append(L("looks a little tough."));
				else
				if(realDiff<(3*theDiff))
					levelMsg.append(L("is a serious threat."));
				else
				if(realDiff<(4*theDiff))
					levelMsg.append(L("will clean your clock."));
				else
					levelMsg.append(L("WILL KILL YOU DEAD!"));
			}
			mob.tell(levelMsg.toString());
		}
		final StringBuffer withWhat=new StringBuffer("");
		final Vector<Ability> mendors=new Vector<Ability>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A instanceof MendingSkill)&&(((MendingSkill)A).supportsMending((target))))
				mendors.add(A);
		}
		for(int m=0;m<mendors.size();m++)
		{
			final Ability A=mendors.get(m);
			if(m==0)
				withWhat.append(L("You could probably help @x1 out with your @x2 skill",target.name(mob),A.name()));
			else
			if(m<mendors.size()-1)
				withWhat.append(L(", your @x1 skill",A.name()));
			else
				withWhat.append(L(" or your @x1 skill",A.name()));
		}

		if(withWhat.length()>0)
			mob.tell(withWhat.toString()+".");
		else
		if((!(target instanceof MOB))&&(msgStr!=null))
			mob.tell(L("You don't have any particular thoughts about that."));
		return lvlDiff;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Consider whom or what?"));
			return false;
		}
		commands.remove(0);
		String targetName=CMParms.combine(commands,0);
		final List<Physical> targets = new ArrayList<Physical>();
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
			Physical target=mob.location().fetchFromMOBRoomFavorsMOBs(mob,null,targetName,Wearable.FILTER_ANY);
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
					target = mob.location().fetchFromMOBRoomFavorsMOBs(mob, null, targetName+"."+(++ctr), Wearable.FILTER_ANY);
				}
			}
		}
		if(targets.size()>1)
			targets.remove(mob);
		for(final Iterator<Physical> p=targets.iterator();p.hasNext();)
		{
			final Physical P = p.next();
			if(!CMLib.flags().canBeSeenBy(P,mob))
				p.remove();
		}
		if(targets.size()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see '@x1' here.",targetName));
			return false;
		}
		String msgStr;
		if(targets.size()>1)
			msgStr = L("<S-NAME> consider(s) several things.");
		else
			msgStr = L("<S-NAME> consider(s) <T-NAMESELF>.");
		for(int p=0;p<targets.size();p++)
		{
			doConsider(mob,targets.get(p),false, msgStr);
			msgStr=null;
		}
		return true;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Integer.valueOf(0);
		final String msgStr = L("<S-NAME> consider(s) <T-NAMESELF>.");
		return Integer.valueOf(doConsider(mob, (MOB)args[0], false, msgStr));
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
