package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Distraction extends Song
{
	public String ID() { return "Song_Distraction"; }
	public String name(){ return "Distraction";}
	public String displayText(){ return "(Song of Distraction)";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Distraction();}
	
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			&&(mob.isInCombat())
			&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+50))
			&&((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOVE))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> appear(s) distracted by the singing.");
				return false;
			}
		}
		return super.okAffect(affect);
	}

}
