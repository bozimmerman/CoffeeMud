package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Harmony extends Play
{
	public String ID() { return "Play_Harmony"; }
	public String name(){ return "Harmony";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Play_Harmony();}
	protected boolean persistantSong(){return false;}
	
	protected void inpersistantAffect(MOB mob)
	{
		if(mob.getVictim()!=null)
			mob.getVictim().makePeace();
		mob.makePeace();
	}
}
	