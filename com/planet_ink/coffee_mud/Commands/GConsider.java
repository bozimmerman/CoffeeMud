package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public GConsider(){}

	private String[] access={"GCONSIDER","GCOS","GCO"};
	public String[] getAccessWords(){return access;}

	public int relativeLevelDiff(MOB mob1, HashSet mobs)
	{
		if((mob1==null)||(mobs==null)) return 0;
	    MOB mob2=(MOB)mobs.iterator().next();
	    while(mob2.amFollowing()!=null)
	        mob2=mob2.amFollowing();
	    
		int mob2Armor=mob2.adjustedArmor();
		int mob1Armor=mob1.adjustedArmor();
		int mob1Attack=mob1.adjustedAttackBonus(mob2);
		int mob1Dmg=mob1.envStats().damage();
		int mob2Hp=mob2.baseState().getHitPoints();
		int mob1Hp=mob1.baseState().getHitPoints();

		double mob2HitRound=0.0;
		for(Iterator i=mobs.iterator();i.hasNext();)
		{
		    MOB mob=(MOB)i.next();
			int mob2Attack=mob.adjustedAttackBonus(mob1);
			int mob2Dmg=mob.envStats().damage();
			mob2HitRound+=(((Util.div(Dice.normalizeBy5((mob2Attack+mob1Armor)),100.0))*Util.div(mob2Dmg,2.0))+1.0)*Util.mul(mob.envStats().speed(),1.0);
		}
		double mob1HitRound=(((Util.div(Dice.normalizeBy5((mob1Attack+mob2Armor)),100.0))*Util.div(mob1Dmg,2.0))+1.0)*Util.mul(mob1.envStats().speed(),1.0);
		double mob2SurvivalRounds=Util.div(mob2Hp,mob1HitRound);
		double mob1SurvivalRounds=Util.div(mob1Hp,mob2HitRound);

		//int levelDiff=(int)Math.round(Util.div((mob1SurvivalRounds-mob2SurvivalRounds),1));
		double levelDiff=mob1SurvivalRounds-mob2SurvivalRounds;
		int levelDiffed=(int)Math.round(Math.sqrt(Math.abs(levelDiff)));

		return levelDiffed*(levelDiff<0.0?-1:1);
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Consider whom?");
			return false;
		}
		commands.removeElementAt(0);
		String targetName=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(targetName);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
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
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
