package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_AvoidTraps extends ThiefSkill
{
	public String ID() { return "Thief_AvoidTraps"; }
	public String name(){ return "Avoid Traps";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Thief_AvoidTraps();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_TRAPS,affectableStats.getStat(CharStats.SAVE_TRAPS)+profficiency());
	}
}
