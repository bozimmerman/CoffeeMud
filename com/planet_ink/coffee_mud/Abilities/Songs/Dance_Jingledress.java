package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Jingledress extends Dance
{
	public String ID() { return "Dance_Jingledress"; }
	public String name(){ return "Jingledress";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Jingledress();}
	protected String danceOf(){return name()+" Dance";}
	public long flags(){return Ability.FLAG_HEALING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if(invoker()!=null)
		{
			int healing=Dice.roll(2,adjustedLevel(invoker()),4);
			MUDFight.postHealing(invoker(),mob,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
		}
		return true;
	}


}