package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_LoveSong extends Play
{
	public String ID() { return "Play_LoveSong"; }
	public String name(){ return "Love Song";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String songOf(){return "a "+name();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			Vector choices=new Vector();
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB M=mob.location().fetchInhabitant(i);
				if((M!=null)
				&&(M!=mob)
				&&(Sense.canBeSeenBy(M,mob))
				&&(M.charStats().getStat(CharStats.GENDER)!=mob.charStats().getStat(CharStats.GENDER))
				&&(M.charStats().getStat(CharStats.GENDER)!=(int)'N')
				&&(M.charStats().getSave(CharStats.CHARISMA)>14))
					choices.addElement(M);
			}
			if(choices.size()>0)
			{
				MOB M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
				try{
				if(Dice.rollPercentage()==1)
				{
					Item I=mob.fetchFirstWornItem(Item.ON_WAIST);
					if(I!=null)	CommonMsgs.remove(mob,I,false);
					I=mob.fetchFirstWornItem(Item.ON_LEGS);
					if(I!=null)	CommonMsgs.remove(mob,I,false);
					mob.doCommand(Util.parse("MATE "+M.name()));
				}
				else
				if(Dice.rollPercentage()>10)
					switch(Dice.roll(1,5,0))
					{
					case 1:
						mob.tell("You feel strange urgings towards "+M.name()+".");
						break;
					case 2:
						mob.tell("You have strong happy feelings towards "+M.name()+".");
						break;
					case 3:
						mob.tell("You feel very appreciative of "+M.name()+".");
						break;
					case 4:
						mob.tell("You feel very close to "+M.name()+".");
						break;
					case 5:
						mob.tell("You feel lovingly towards "+M.name()+".");
						break;
					}
				}catch(Exception e){}
			}
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+6);
	}
}
