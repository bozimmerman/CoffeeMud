package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Countertracking extends ThiefSkill
{
	public String ID() { return "Thief_Countertracking"; }
	public String name(){ return "Counter-Tracking";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((!msg.amISource(mob))
		&&(msg.target()==mob)
		&&(msg.tool() instanceof Ability)
		&&(profficiencyCheck(mob,0,false))
		&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRACKING)))
		{
			msg.source().tell("You can't get a bead on him.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
