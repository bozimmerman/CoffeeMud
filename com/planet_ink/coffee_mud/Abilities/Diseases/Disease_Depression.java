package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Disease_Depression extends Disease
{
	public String ID() { return "Disease_Depression"; }
	public String name(){ return "Depression";}
	public String displayText(){ return "(Depression)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 4;}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return 20;}
	protected String DISEASE_DONE(){return "You feel better.";}
	protected String DISEASE_START(){return "^G<S-NAME> seem(s) depressed.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> moap(s).";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof MOB)
		{
			if(msg.source()!=affected)
				return true;
			if(msg.source().location()==null)
				return true;
		    MOB mob=(MOB)affected;
			if(((msg.amITarget(mob))||(msg.amISource(mob)))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>")))
			{
			    mob.tell("You don't really feel like doing it right now.");
			    return false;
			}
		}
		return true;
	}
	
	public void affectEnvStats(Environmental E, EnvStats stats)
	{
	    super.affectEnvStats(E,stats);
	    stats.setAttackAdjustment(stats.attackAdjustment()-10);
	}
	
	public void affectChatStats(MOB E, CharStats stats)
	{
	    super.affectCharStats(E,stats);
	    stats.setStat(CharStats.SAVE_JUSTICE,stats.getStat(CharStats.SAVE_JUSTICE)-20);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if(Dice.rollPercentage()==1)
			mob.tell("You are hungry.");
		if(mob.isInCombat()
		&&(Dice.rollPercentage()<10))
		{
		    mob.tell("Whats the point in fighting, really?");
		    mob.makePeace();
		}
		else
		if((!mob.isInCombat())
		&&(mob.session()!=null)
		&&(mob.session().getIdleMillis()>10000)
        &&((Dice.rollPercentage()==1)||(Sense.isSitting(mob))))
        {
		    Command C=CMClass.getCommand("Sleep");
		    try{C.execute(mob,Util.makeVector("Sleep"));}catch(Exception e){}
        }
		if(mob.curState().getFatigue()<CharState.FATIGUED_MILLIS)
			mob.curState().setFatigue(CharState.FATIGUED_MILLIS);
		return true;
	}

}

