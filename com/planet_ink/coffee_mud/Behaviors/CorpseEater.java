package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class CorpseEater extends ActiveTicker
{
	public String ID(){return "CorpseEater";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
    private boolean EatItems=false;
	public CorpseEater()
	{
		minTicks=5; maxTicks=20; chance=75;
		tickReset();
	}

    public void setParms(String newParms) 
	{
        super.setParms(newParms);
        EatItems=(newParms.toUpperCase().indexOf("EATITEMS") > 0);
    }


	public static MOB makeMOBfromCorpse(DeadBody corpse, String type)
	{
		if((type==null)||(type.length()==0))
			type="StdMOB";
		MOB mob=CMClass.getMOB(type);
		if(corpse!=null)
		{
			mob.setName(corpse.name());
			mob.setDisplayText(corpse.displayText());
			mob.setDescription(corpse.description());
			mob.setBaseCharStats(corpse.charStats().cloneCharStats());
			mob.setBaseEnvStats(corpse.baseEnvStats().cloneStats());
			mob.recoverCharStats();
			mob.recoverEnvStats();
			int level=mob.baseEnvStats().level();
			mob.baseState().setHitPoints(Dice.rollHP(level,mob.baseEnvStats().ability()));
			mob.baseState().setMana(mob.baseCharStats().getCurrentClass().getLevelMana(mob));
			mob.baseState().setMovement(mob.baseCharStats().getCurrentClass().getLevelMove(mob));
			mob.recoverMaxState();
			mob.resetToMaxState();
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		return mob;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return true;
			for(int i=0;i<thisRoom.numItems();i++)
			{
				Item I=thisRoom.fetchItem(i);
				if((I!=null)&&(I instanceof DeadBody)&&(Sense.canBeSeenBy(I,mob)||Sense.canSmell(mob)))
				{
					if(getParms().length()>0)
					{
                        if(((DeadBody)I).playerCorpse())
                        {
                            if(getParms().toUpperCase().indexOf("-PLAYER")>=0)
                                continue;
                        }
                        else
                        if((getParms().toUpperCase().indexOf("-NPC")>=0)
                        ||(getParms().toUpperCase().indexOf("-MOB")>=0))
                            continue;
                        MOB mob2=makeMOBfromCorpse((DeadBody)I,null);
						if(!MUDZapper.zapperCheck(getParms(),mob2))
                        {
                            mob2.destroy();
							continue;
                        }
                        mob2.destroy();
					}
					if((I instanceof Container)&&(!EatItems))
						((Container)I).emptyPlease();
					thisRoom.show(mob,null,I,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> eat(s) <O-NAME>.");
					I.destroy();
					return true;
				}
			}
		}
		return true;
	}
}
