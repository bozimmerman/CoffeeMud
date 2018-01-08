package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.CMath.CompiledOperation;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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
public class StatRejuvCharts extends StdWebMacro
{
	@Override
	public String name()
	{
		return "StatRejuvCharts";
	}

	protected String getReq(HTTPRequest httpReq, String tag)
	{
		String s=httpReq.getUrlParameter(tag);
		if(s==null)
			s="";
		return s;
	}

	public final int avgMath(final int stat, final int level, final int add, final CompiledFormula formula)
	{
		final double[] variables={
				level,
				stat,
				(double)stat+7,
				stat,
				(double)stat+7,
				stat,
				(double)stat+7,
				stat,
				stat
			};
		return add+(level*(int)Math.round(CMath.parseMathExpression(formula, variables, 0.0)));
	}
	
	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final StringBuffer buf=new StringBuffer("");
		final String which=httpReq.getUrlParameter("WHICH");
		final MOB mob=CMClass.getMOB("StdMOB");
		mob.baseState().setMana(100);
		mob.baseState().setMovement(100);
		mob.baseState().setHitPoints(100);
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.curState().setHunger(1000);
		mob.curState().setThirst(1000);
		if((which!=null)&&(which.equals("HP")))
			buf.append("<BR>Chart: Hit Points<BR>");
		else
		if((which!=null)&&(which.equals("MN")))
			buf.append("<BR>Chart: Mana<BR>");
		else
		if((which!=null)&&(which.equals("MV")))
			buf.append("<BR>Chart: Movement<BR>");
		else
			buf.append("<BR>Chart: Hit Points<BR>");
		buf.append("Flags: ");
		int disposition=0;

		if((getReq(httpReq,"SITTING").length()>0))
		{
			disposition=PhyStats.IS_SITTING;
			buf.append("Sitting ");
		}
		if((getReq(httpReq,"SLEEPING").length()>0))
		{
			disposition=PhyStats.IS_SLEEPING;
			buf.append("Sleeping ");
		}
		if((getReq(httpReq,"FLYING").length()>0))
		{
			disposition=PhyStats.IS_FLYING;
			buf.append("Flying ");
		}
		if((getReq(httpReq,"SWIMMING").length()>0))
		{
			disposition=PhyStats.IS_SWIMMING;
			buf.append("Swimming ");
		}
		if((getReq(httpReq,"RIDING").length()>0))
		{
			mob.setRiding((Rideable)CMClass.getMOB("GenRideable"));
			buf.append("Riding ");
		}
		final boolean hungry=(httpReq.getUrlParameter("HUNGRY")!=null)&&(httpReq.getUrlParameter("HUNGRY").length()>0);
		if(hungry)
		{
			buf.append("Hungry ");
			mob.curState().setHunger(0);
		}
		final boolean thirsty=(httpReq.getUrlParameter("THIRSTY")!=null)&&(httpReq.getUrlParameter("THIRSTY").length()>0);
		if(thirsty)
		{
			buf.append("Thirsty ");
			mob.curState().setThirst(0);
		}
		mob.basePhyStats().setDisposition(disposition);
		mob.recoverPhyStats();

		final int MAX_STAT=25;
		final int MAX_LEVEL=90;
		final int SKIP_STAT=2;
		final int SKIP_LEVEL=5;
		
		final int[][] hitpointcharts=new int[MAX_LEVEL+1][MAX_STAT+1];
		final int[][] manacharts=new int[MAX_LEVEL+1][MAX_STAT+1];
		final int[][] movementcharts=new int[MAX_LEVEL+1][MAX_STAT+1];
		final int sh=CMProps.getIntVar(CMProps.Int.STARTHP);
		final int sm=CMProps.getIntVar(CMProps.Int.STARTMANA);
		final int sv=CMProps.getIntVar(CMProps.Int.STARTMOVE);
		final Map<CharClass,CompiledFormula> hpformulas=new Hashtable<CharClass,CompiledFormula>(); 
		final Map<CharClass,CompiledFormula> mnformulas=new Hashtable<CharClass,CompiledFormula>(); 
		final Map<CharClass,CompiledFormula> mvformulas=new Hashtable<CharClass,CompiledFormula>(); 
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C1=c.nextElement();
			hpformulas.put(C1, CMath.compileMathExpression(C1.getHitPointsFormula()));
			mnformulas.put(C1, CMath.compileMathExpression(C1.getManaFormula()));
			mvformulas.put(C1, CMath.compileMathExpression(C1.getMovementFormula()));
		}
		
		for(int l=1;l<=MAX_LEVEL;l+=SKIP_LEVEL)
		{
			for(int s=4;s<=MAX_STAT;s+=SKIP_STAT)
			{
				int num=0;
				for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
				{
					final CharClass C1=c.nextElement();
					num++;
					hitpointcharts[l][s]+=avgMath(s,l,sh,hpformulas.get(C1));
					manacharts[l][s]+=avgMath(s,l,sm,mnformulas.get(C1));
					movementcharts[l][s]+=avgMath(s,l,sv,mvformulas.get(C1));
				}
				hitpointcharts[l][s]/=num;
				manacharts[l][s]/=num;
				movementcharts[l][s]/=num;
			}
		}
		
		buf.append("<P><TABLE WIDTH=100% BORDER=1>");
		buf.append("<TR><TD><B><FONT COLOR=WHITE>STATS:</FONT></B></TD>");
		for(int stats=4;stats<=MAX_STAT;stats+=SKIP_STAT)
			buf.append("<TD><B><FONT COLOR=WHITE>"+stats+"</FONT></B></TD>");
		buf.append("</TR>");
		
		CMath.CompiledFormula stateHitPointRecoverFormula = null;
		CMath.CompiledFormula stateManaRecoverFormula = null;
		CMath.CompiledFormula stateMovesRecoverFormula  = null;
		stateHitPointRecoverFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_HITPOINTRECOVER));
		stateManaRecoverFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_MANARECOVER));
		stateMovesRecoverFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_MOVESRECOVER));

		for(int level=1;level<=MAX_LEVEL;level+=SKIP_LEVEL)
		{
			buf.append("<TR>");
			buf.append("<TD><B><FONT COLOR=WHITE>LVL "+level+"</FONT></B></TD>");
			for(int stats=4;stats<=MAX_STAT;stats+=SKIP_STAT)
			{
				for(final int c: CharStats.CODES.BASECODES())
					mob.baseCharStats().setStat(c,stats);
				mob.recoverCharStats();
				mob.basePhyStats().setLevel(level);
				mob.recoverPhyStats();
				mob.curState().setMana(0);
				mob.curState().setMovement(0);
				mob.curState().setHitPoints(0);

				final CharStats charStats=mob.charStats();
				final CharState curState=mob.curState();
				final boolean isSleeping=(CMLib.flags().isSleeping(mob));
				final boolean isSittingOrRiding=(!isSleeping) && ((CMLib.flags().isSitting(mob))||(mob.riding()!=null));
				final boolean isFlying=(!isSleeping) && (!isSittingOrRiding) && CMLib.flags().isFlying(mob);
				final boolean isSwimming=(!isSleeping) && (!isSittingOrRiding) && (!isFlying) && CMLib.flags().isSwimming(mob);
				final double[] vals=new double[]{
					charStats.getStat(CharStats.STAT_CONSTITUTION),
					mob.phyStats().level(),
					(curState.getHunger()<1)?1.0:0.0,
					(curState.getThirst()<1)?1.0:0.0,
					(curState.getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					isSleeping?1.0:0.0,
					isSittingOrRiding?1.0:0.0,
					isFlying?1.0:0.0,
					isSwimming?1.0:0.0
				};

				if((which!=null)&&(which.equals("HP")))
				{
					final long hpGain = Math.round(CMath.parseMathExpression(stateHitPointRecoverFormula, vals, 0.0));
					buf.append("<TD><FONT COLOR=CYAN>"+hitpointcharts[level][stats]+"/"+hpGain+"="+(hitpointcharts[level][stats]/hpGain)+"</FONT></TD>");
				}
				else
				if((which!=null)&&(which.equals("MN")))
				{
					vals[0]=((charStats.getStat(CharStats.STAT_INTELLIGENCE)+charStats.getStat(CharStats.STAT_WISDOM)));
					final long manaGain = Math.round(CMath.parseMathExpression(stateManaRecoverFormula, vals, 0.0));
					buf.append("<TD><FONT COLOR=PINK>"+manacharts[level][stats]+"/"+manaGain+"="+(manacharts[level][stats]/manaGain)+"</FONT></TD>");
				}
				else
				if((which!=null)&&(which.equals("MV")))
				{
					vals[0]=charStats.getStat(CharStats.STAT_STRENGTH);
					final long moveGain = Math.round(CMath.parseMathExpression(stateMovesRecoverFormula, vals, 0.0));
					buf.append("<TD><FONT COLOR=YELLOW>"+movementcharts[level][stats]+"/"+moveGain+"="+(movementcharts[level][stats]/moveGain)+"</FONT></TD>");
				}
				else
				{
					final long hpGain = Math.round(CMath.parseMathExpression(stateHitPointRecoverFormula, vals, 0.0));
					buf.append("<TD><FONT COLOR=CYAN>"+hitpointcharts[level][stats]+"/"+hpGain+"="+(hitpointcharts[level][stats]/hpGain)+"</FONT></TD>");
				}
			}
			buf.append("</TR>");
		}
		mob.destroy();
		buf.append("</TABLE>");
		return clearWebMacros(buf);
	}

}
