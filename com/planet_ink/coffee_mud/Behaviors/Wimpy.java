package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Wimpy extends StdBehavior
{
	public String ID(){return "Wimpy";}
	protected int tickWait=0;
	protected int tickDown=0;
	protected boolean veryWimpy=false;
	
	public Behavior newInstance()
	{
		return new Wimpy();
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		return false;
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=getParmVal(newParms,"delay",0);
		tickDown=tickWait;
		veryWimpy=getParmVal(newParms,"very",0)==1;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return true;
		if(((--tickDown)<0)&&(ticking instanceof MOB))
		{
			tickDown=tickWait;
			MOB monster=(MOB)ticking;
			if(monster.location()!=null)
			for(int m=0;m<monster.location().numInhabitants();m++)
			{
				MOB M=(MOB)monster.location().fetchInhabitant(m);
				if((M!=null)&&(M!=monster)&&(SaucerSupport.zapperCheck(getParms(),monster)))
				{
					if((monster.getVictim()==M)||(M.getVictim()==monster))
					{
						ExternalPlay.flee(monster,"");
						return true;
					}
					else
					if((veryWimpy)&&(!monster.isInCombat()))
					{
						Room oldRoom=monster.location();
						Behavior B=null;
						for(int b=0;b<monster.numBehaviors();b++)
						{
							B=monster.fetchBehavior(b);
							if((B!=null)&&(Util.bset(B.flags(),Behavior.FLAG_MOBILITY)))
							{
								int tries=0;
								while(((++tries)<100)&&(oldRoom==monster.location()))
									B.tick(monster,Host.MOB_TICK);
								if(oldRoom!=monster.location())
									return true;
							}
						}
						if(oldRoom==monster)
							SaucerSupport.beMobile(monster,false,false,false,false,null);
					}
				}
			}
		}
		return true;
	}
}
