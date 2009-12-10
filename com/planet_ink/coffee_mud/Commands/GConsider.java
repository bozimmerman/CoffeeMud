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
public class GConsider extends StdCommand
{
	public GConsider(){}

	private String[] access={"GCONSIDER","GCOS","GCO"};
	public String[] getAccessWords(){return access;}

	public int relativeLevelDiff(MOB mob1, HashSet mobs)
	{
		if((mob1==null)||(mobs==null)) return 0;
	    MOB mob2=(MOB)mobs.iterator().next();
        if(mob2.amFollowing()!=null) mob2=mob2.amUltimatelyFollowing();

		int mob2Armor=CMLib.combat().adjustedArmor(mob2);
		int mob1Armor=CMLib.combat().adjustedArmor(mob1);
        double mob1Attack=(double)CMLib.combat().adjustedAttackBonus(mob1,mob2);
		int mob1Dmg=mob1.envStats().damage();
		int mob2Hp=mob2.baseState().getHitPoints();
		int mob1Hp=mob1.baseState().getHitPoints();

		double mob2HitRound=0.0;
		for(Iterator i=mobs.iterator();i.hasNext();)
		{
		    MOB mob=(MOB)i.next();
            double mob2Attack=(double)CMLib.combat().adjustedAttackBonus(mob,mob1);
			int mob2Dmg=mob.envStats().damage();
			mob2HitRound+=(((CMath.div(CMLib.dice().normalizeBy5((int)Math.round(50.0*mob2Attack/mob1Armor)),100.0))*CMath.div(mob2Dmg,2.0))+1.0)*CMath.mul(mob.envStats().speed(),1.0);
		}
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
		if(commands.size()<2)
		{
			mob.tell("Consider whom?");
			return false;
		}
		commands.removeElementAt(0);
		String targetName=CMParms.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(targetName);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("I don't see '"+targetName+"' here.");
			return false;
		}

		int relDiff=relativeLevelDiff(target,mob.getGroupMembers(new HashSet()));
		int lvlDiff=(target.envStats().level()-mob.envStats().level());
		int realDiff=(relDiff+lvlDiff)/2;

		int theDiff=2;
		if(mob.envStats().level()>20) theDiff=3;
		if(mob.envStats().level()>40) theDiff=4;
		if(mob.envStats().level()>60) theDiff=5;
		if(mob.envStats().level()>80) theDiff=6;

		int levelDiff=Math.abs(realDiff);
		if(levelDiff<theDiff)
		{
			mob.tell("The perfect match!");
			return false;
		}
		else
		if(realDiff<0)
		{
			if(realDiff>-(2*theDiff))
			{
				mob.tell(target.charStats().HeShe()+" might give you a fight.");
				return false;
			}
			else
			if(realDiff>-(3*theDiff))
			{
				mob.tell(target.charStats().HeShe()+" is hardly worth your while.");
				return false;
			}
			else
			if(realDiff>-(4*theDiff))
			{
				mob.tell(target.charStats().HeShe()+" is a pushover.");
				return false;
			}
			else
			{
				mob.tell(target.charStats().HeShe()+" is not worth the effort.");
				return false;
			}

		}
		else
		if(realDiff<(2*theDiff))
		{
			mob.tell(target.charStats().HeShe()+" looks a little tough.");
			return false;
		}
		else
		if(realDiff<(3*theDiff))
		{
			mob.tell(target.charStats().HeShe()+" is a serious threat.");
			return false;
		}
		else
		if(realDiff<(4*theDiff))
		{
			mob.tell(target.charStats().HeShe()+" will clean your clock.");
			return false;
		}
		else
		{
			mob.tell(target.charStats().HeShe()+" WILL KILL YOU DEAD!");
			return false;
		}
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}


}
