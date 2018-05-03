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
   Copyright 2004-2018 Bo Zimmerman

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
	public Consider(){}

	private final String[] access=I(new String[]{"CONSIDER","COS","CO"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@SuppressWarnings("rawtypes")
	private final static Class[][] internalParameters=new Class[][]{{MOB.class}};

	public int relativeLevelDiff(MOB mob1, MOB mob2)
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

	public int doConsider(MOB mob, Physical target)
	{
		final Room R=mob.location();
		if(R==null)
			return 0;
		final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MASK_EYES|CMMsg.TYP_OK_VISUAL,null,L("<S-NAME> consider(s) <T-NAMESELF>."),L("<S-NAME> consider(s) <T-NAMESELF>."));
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

			StringBuilder levelMsg=new StringBuilder("");
			if(lvlDiff==0)
				levelMsg.append(L(targetMOB.charStats().HeShe()+" is your equal"));
			else
			if(lvlDiff<-CMProps.getIntVar(CMProps.Int.EXPRATE))
				levelMsg.append(L(targetMOB.charStats().HeShe()+" is vastly inferior to you"));
			else
			if(lvlDiff>CMProps.getIntVar(CMProps.Int.EXPRATE))
				levelMsg.append(L(targetMOB.charStats().HeShe()+" is far superior to you"));
			else
			if(CMProps.getIntVar(CMProps.Int.EXPRATE)!=0)
			{
				final int relLvlDiff=(lvlDiff<0)?-lvlDiff:lvlDiff;
				final double pct=CMath.div(relLvlDiff,CMProps.getIntVar(CMProps.Int.EXPRATE));
				if((lvlDiff<0)&&(pct<0.5))
					levelMsg.append(L(targetMOB.charStats().HeShe()+" is almost your equal"));
				else
				if((lvlDiff<0)&&(pct<=1.0))
					levelMsg.append(L(targetMOB.charStats().HeShe()+" is somewhat inferior to you"));
				else
				if((lvlDiff<0))
					levelMsg.append(L(targetMOB.charStats().HeShe()+" is inferior to you"));
				else
				if((lvlDiff>0)&&(pct<0.5))
					levelMsg.append(L("You are almost "+targetMOB.charStats().hisher()+" equal"));
				else
				if((lvlDiff>0)&&(pct<0.8))
					levelMsg.append(L(targetMOB.charStats().HeShe()+" is somewhat superior to you"));
				else
					levelMsg.append(L(targetMOB.charStats().HeShe()+" is superior to you"));
			}

			final int levelDiff=Math.abs(realDiff);
			if(levelDiff<theDiff)
			{
				levelMsg.append(L((lvlDiff!=0)?" but ":" and "));
				levelMsg.append(L("the perfect match!"));
			}
			else
			if(realDiff<0)
			{
				levelMsg.append(L((lvlDiff<0)?" and ":" but "));
				if(realDiff>-(2*theDiff))
					levelMsg.append(L(targetMOB.charStats().heshe()+" might actually give you a fight."));
				else
				if(realDiff>-(3*theDiff))
					levelMsg.append(L(targetMOB.charStats().heshe()+" won't put up a big fight."));
				else
				if(realDiff>-(4*theDiff))
					levelMsg.append(L(targetMOB.charStats().heshe()+" is basically a pushover."));
				else
					levelMsg.append(L(targetMOB.charStats().heshe()+" is an easy kill."));
			}
			else
			{
				levelMsg.append(L((lvlDiff>0)?" and ":" but "));
				if(realDiff<(2*theDiff))
					levelMsg.append(L(targetMOB.charStats().heshe()+" looks a little tough."));
				else
				if(realDiff<(3*theDiff))
					levelMsg.append(L(targetMOB.charStats().heshe()+" is a serious threat."));
				else
				if(realDiff<(4*theDiff))
					levelMsg.append(L(targetMOB.charStats().heshe()+" will clean your clock."));
				else
					levelMsg.append(L(targetMOB.charStats().heshe()+" WILL KILL YOU DEAD!"));
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
		if(!(target instanceof MOB))
			mob.tell(L("You don't have any particular thoughts about that."));
		return lvlDiff;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		Physical target=null;
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Consider whom or what?"));
			return false;
		}
		commands.remove(0);
		final String targetName=CMParms.combine(commands,0);
		if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
			target=mob;
		if(target==null)
			target=mob.location().fetchFromMOBRoomFavorsMOBs(mob,null,targetName,Wearable.FILTER_ANY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see '@x1' here.",targetName));
			return false;
		}
		doConsider(mob,target);
		return true;
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Integer.valueOf(0);
		return Integer.valueOf(doConsider(mob, (MOB)args[0]));
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
