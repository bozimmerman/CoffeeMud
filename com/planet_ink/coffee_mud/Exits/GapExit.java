package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GapExit extends StdExit
{
	public String ID(){	return "GapExit";}
	public GapExit()
	{
		super();
		name="a crevasse";
		description="Looks like you'll have to jump it.";
		displayText="";
		miscText="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;
		recoverEnvStats();
		openDelayTicks=1;
	}
	public Environmental newInstance()
	{
		return new GapExit();
	}
	
	public int mobWeight(MOB mob)
	{
		int weight=mob.baseEnvStats().weight();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(!I.amWearingAt(Item.FLOATING_NEARBY)))
				weight+=I.envStats().weight();
		}
		return weight;
	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) return false;
		MOB mob=affect.source();
		if(((affect.amITarget(this))||(affect.tool()==this))
		&&(affect.targetMinor()==Affect.TYP_ENTER)
		&&(!Sense.isInFlight(mob))
		&&(!Sense.isFalling(mob)))
		{
			int chance=(int)Math.round(Util.div(mobWeight(mob),mob.maxCarry())*(100.0-new Integer(3*mob.charStats().getStat(CharStats.STRENGTH)).doubleValue()));
			if(Dice.rollPercentage()<chance)
			{
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to jump the crevasse, but miss(es) the far ledge!");
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> fall(s)!!!!");
				ExternalPlay.postDeath(null,mob,null);
				return false;
			}
		}
		return true;
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		MOB mob=affect.source();
		if(((affect.amITarget(this))||(affect.tool()==this))
		&&(affect.targetMinor()==Affect.TYP_ENTER)
		&&(!Sense.isInFlight(mob))
		&&(!Sense.isFalling(mob)))
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> jump(s) the crevasse!");
	}
}
