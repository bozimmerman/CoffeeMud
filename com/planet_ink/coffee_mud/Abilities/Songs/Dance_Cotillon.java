package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Cotillon extends Dance
{
	public String ID() { return "Dance_Cotillon"; }
	public String name(){ return "Cotillon";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Cotillon();}
	protected String danceOf(){return name()+" Dance";}
	protected MOB whichLast=null;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((referenceDance!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).isInCombat()))
		{
			MOB M=(MOB)affected;
			MOB V=M.getVictim().getVictim();
			MOB oldLast=whichLast;
			
			whichLast=((Dance_Cotillon)referenceDance).whichLast;
			if((whichLast==null)&&(V!=M))
			{
				if(M.location().show(M,null,M.getVictim(),Affect.MSG_NOISYMOVEMENT,"<S-NAME> dance(s) into <O-YOUPOSS> way."))
				{
					((Dance_Cotillon)referenceDance).whichLast=M;
					M.getVictim().setVictim(M);
				}
			}
			else
			if((whichLast==M)&&(V==M))
				((Dance_Cotillon)referenceDance).whichLast=null;
			else
			if((whichLast==oldLast)&&(V==oldLast))
				((Dance_Cotillon)referenceDance).whichLast=null;
			else
			if(whichLast==null)
				((Dance_Cotillon)referenceDance).whichLast=V;
		}
		return true;
	}
	
}