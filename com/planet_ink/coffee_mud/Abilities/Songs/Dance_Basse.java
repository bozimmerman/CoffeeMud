package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Basse extends Dance
{
	public String ID() { return "Dance_Basse"; }
	public String name(){ return "Basse";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Basse();}
	protected String danceOf(){return name()+" Dance";}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&((affect.amITarget(affected))))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())
			&&(affect.source().getVictim()!=target)
			&&(Dice.rollPercentage()>((affect.source().envStats().level()-target.envStats().level())*10)))
			{
				affect.source().tell("You are too much in awe of "+target.name());
				if(target.getVictim()==affect.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}