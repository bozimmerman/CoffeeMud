package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CorpseEater extends ActiveTicker
{
	public String ID(){return "CorpseEater";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public CorpseEater()
	{
		minTicks=5; maxTicks=20; chance=75;
		tickReset();
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
						MOB mob2=makeMOBfromCorpse((DeadBody)I,null);
						if(!MUDZapper.zapperCheck(getParms(),mob2))
							continue;
					}
					if(I instanceof Container)
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