package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Charge extends Play
{
	public String ID() { return "Play_Charge"; }
	public String name(){ return "Charge!";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Play_Charge();}
	protected boolean persistantSong(){return false;}
	Vector chcommands=null;
	
	protected void inpersistantAffect(MOB mob)
	{
		Ability A=CMClass.getAbility("Fighter_Charge");
		if(A!=null) A.invoke(mob,chcommands,null,true);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()==0)&&(!mob.isInCombat()))
		{
			mob.tell("Play charge at whom?");
			return false;
		}
		if(commands.size()==0)
			commands.addElement(mob.getVictim().name());
		chcommands=commands;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		return true;
	}
}
	