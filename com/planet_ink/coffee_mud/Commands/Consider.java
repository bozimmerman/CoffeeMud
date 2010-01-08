package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Consider extends StdCommand
{
	public Consider(){}

	private String[] access={"CONSIDER","COS","CO"};
	public String[] getAccessWords(){return access;}

	public int relativeLevelDiff(MOB mob1, MOB mob2)
	{
		if((mob1==null)||(mob2==null)) return 0;
		int mob2Armor=CMLib.combat().adjustedArmor(mob2);
		int mob1Armor=CMLib.combat().adjustedArmor(mob1);
		double mob1Attack=(double)CMLib.combat().adjustedAttackBonus(mob1,mob2);
        double mob2Attack=(double)CMLib.combat().adjustedAttackBonus(mob2,mob1);
		int mob2Dmg=mob2.envStats().damage();
		int mob1Dmg=mob1.envStats().damage();
		int mob2Hp=mob2.baseState().getHitPoints();
		int mob1Hp=mob1.baseState().getHitPoints();

		double mob2HitRound=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob2Attack/mob1Armor)),100.0))*CMath.div(mob2Dmg,2.0))+1.0)*CMath.mul(mob2.envStats().speed(),1.0);
		double mob1HitRound=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob1Attack/mob2Armor)),100.0))*CMath.div(mob1Dmg,2.0))+1.0)*CMath.mul(mob1.envStats().speed(),1.0);
		double mob2SurvivalRounds=CMath.div(mob2Hp,mob1HitRound);
		double mob1SurvivalRounds=CMath.div(mob1Hp,mob2HitRound);

		//int levelDiff=(int)Math.round(CMath.div((mob1SurvivalRounds-mob2SurvivalRounds),1));
		double levelDiff=(mob1SurvivalRounds-mob2SurvivalRounds)/2;
		int levelDiffed=(int)Math.round(Math.sqrt(Math.abs(levelDiff)));

		return levelDiffed*(levelDiff<0.0?-1:1);
	}


	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room R=mob.location();
		if(R==null) return false;
        Environmental target=null;
        if((commands.size()==1)&&(commands.firstElement() instanceof Environmental))
        	target=(Environmental)commands.firstElement();
        else
        {
    		if(commands.size()<2)
    		{
    			mob.tell("Consider whom or what?");
    			return false;
    		}
    		commands.removeElementAt(0);
    		String targetName=CMParms.combine(commands,0);
    		if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
    			target=mob;
    		if(target==null)
    			target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,targetName,Wearable.FILTER_ANY);
    		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
    		{
    			mob.tell("I don't see '"+targetName+"' here.");
    			return false;
    		}
        }
		CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MASK_EYES|CMMsg.TYP_OK_VISUAL,null,"<S-NAME> consider(s) <T-NAMESELF>.","<S-NAME> consider(s) <T-NAMESELF>.");
		if(R.okMessage(mob,msg))
			R.send(mob,msg);
        if(target instanceof MOB)
        {
        	MOB targetMOB=(MOB)target;
			int relDiff=relativeLevelDiff(targetMOB,mob);
			int lvlDiff=(target.envStats().level()-mob.envStats().level());
			int realDiff=relDiff;//(relDiff+lvlDiff)/2;

			int theDiff=2;
			if(mob.envStats().level()>20) theDiff=3;
			if(mob.envStats().level()>40) theDiff=4;
			if(mob.envStats().level()>60) theDiff=5;
			if(mob.envStats().level()>80) theDiff=6;

			String levelMsg=null;
			if(lvlDiff==0)
				levelMsg=targetMOB.charStats().HeShe()+" is your equal";
			else
			if(lvlDiff<-CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE))
				levelMsg=targetMOB.charStats().HeShe()+" is vastly inferior to you";
			else
			if(lvlDiff>CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE))
				levelMsg=targetMOB.charStats().HeShe()+" is far superior to you";
			else
			if(CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE)!=0)
			{
				int relLvlDiff=(lvlDiff<0)?-lvlDiff:lvlDiff;
				double pct=CMath.div(relLvlDiff,CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE));
				if((lvlDiff<0)&&(pct<0.5))
					levelMsg=targetMOB.charStats().HeShe()+" is almost your equal";
				else
				if((lvlDiff<0)&&(pct<=1.0))
					levelMsg=targetMOB.charStats().HeShe()+" is somewhat inferior to you";
				else
				if((lvlDiff<0))
					levelMsg=targetMOB.charStats().HeShe()+" is inferior to you";
				else
				if((lvlDiff>0)&&(pct<0.5))
					levelMsg="You are almost "+targetMOB.charStats().hisher()+" equal";
				else
				if((lvlDiff>0)&&(pct<0.8))
					levelMsg=targetMOB.charStats().HeShe()+" is somewhat superior to you";
				else
					levelMsg=targetMOB.charStats().HeShe()+" is superior to you";
			}

			int levelDiff=Math.abs(realDiff);
			if(levelDiff<theDiff)
			{
				levelMsg+=(lvlDiff!=0)?" but ":" and ";
				levelMsg+="the perfect match!";
			}
			else
			if(realDiff<0)
			{
				levelMsg+=(lvlDiff<0)?" and ":" but ";
				if(realDiff>-(2*theDiff))
					levelMsg+=targetMOB.charStats().heshe()+" might give you a fight.";
				else
				if(realDiff>-(3*theDiff))
					levelMsg+=targetMOB.charStats().heshe()+" is hardly worth your while.";
				else
				if(realDiff>-(4*theDiff))
					levelMsg+=targetMOB.charStats().heshe()+" is a pushover.";
				else
					levelMsg+=targetMOB.charStats().heshe()+" is not worth the effort.";
			}
			else
			{
				levelMsg+=(lvlDiff>0)?" and ":" but ";
				if(realDiff<(2*theDiff))
					levelMsg+=targetMOB.charStats().heshe()+" looks a little tough.";
				else
				if(realDiff<(3*theDiff))
					levelMsg+=targetMOB.charStats().heshe()+" is a serious threat.";
				else
				if(realDiff<(4*theDiff))
					levelMsg+=targetMOB.charStats().heshe()+" will clean your clock.";
				else
					levelMsg+=targetMOB.charStats().heshe()+" WILL KILL YOU DEAD!";
			}
			mob.tell(levelMsg);
        }
		StringBuffer withWhat=new StringBuffer("");
		Vector mendors=new Vector();
		for(int c=0;c<mob.numAbilities();c++)
		{
			Ability A=mob.fetchAbility(c);
			if((A instanceof MendingSkill)&&(((MendingSkill)A).supportsMending((target))))
				mendors.addElement(A);
		}
		for(int m=0;m<mendors.size();m++)
		{
			Ability A=(Ability)mendors.elementAt(m);
			if(m==0)
				withWhat.append("You could probably help "+target.name()+" out with your "+A.name()+" skill");
			else
			if(m<mendors.size()-1)
				withWhat.append(", your "+A.name()+" skill");
			else
				withWhat.append(" or your "+A.name()+" skill");
		}

		if(withWhat.length()>0)
			mob.tell(withWhat.toString()+".");
		else
		if(!(target instanceof MOB))
			mob.tell("You don't have any particular thoughts about that.");
		return true;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}


}
