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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().getVictim()!=target)
			&&(Dice.rollPercentage()>((msg.source().envStats().level()-target.envStats().level())*10)))
			{
				msg.source().tell("You are too much in awe of "+target.name());
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}