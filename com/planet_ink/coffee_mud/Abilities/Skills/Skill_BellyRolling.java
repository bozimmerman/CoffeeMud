package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_BellyRolling extends StdAbility
{
	public String ID() { return "Skill_BellyRolling"; }
	public String name(){ return "Belly Rolling";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private boolean doneThisRound=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public Environmental newInstance(){	return new Skill_Parry();}

