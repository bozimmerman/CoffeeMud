package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Serenity extends Song
{
	public String ID() { return "Song_Serenity"; }
	public String name(){ return "Serenity";}
	public String displayText(){ return "(Song of Serenity)";}
	public int quality(){ return MALICIOUS;}
	protected boolean mindAttack(){return true;}
	public Environmental newInstance(){	return new Song_Serenity();	}

	public boolean okAffect(Affect msg)
	{
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;
		if(!Sense.canBeHeardBy(invoker,affected)) return true;

		if((Util.bset(msg.targetCode(),Affect.MASK_MALICIOUS))
		   &&(msg.amISource((MOB)affected))
		   &&(msg.target()!=null)
		   &&(affected!=msg.target()))
		{
			msg.source().makePeace();
			msg.source().tell("You feel too peaceful to fight.");
			return false;
		}
		return true;
	}
}
