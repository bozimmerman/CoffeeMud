package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WeakBridge extends Property
{
	public String ID() { return "Prop_WeakBridge"; }
	public String name(){ return "Weak Rickity Bridge";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Prop_WeakBridge();}
	
	protected boolean bridgeIsUp=true;
	protected int max=400;
	protected int chance=75;
	protected int ticksDown=100;
	
	public String accountForYourself()
	{ return "Weak and Rickity";	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		max=getParmVal(newText,"max",400);
		chance=getParmVal(newText,"chance",75);
		ticksDown=getParmVal(newText,"down",300);
	}
	
	public boolean okAffect(Affect msg)
	{
		if((msg.targetMinor()==Affect.TYP_ENTER)
		||(msg.targetMinor()==Affect.TYP_LEAVE))
		{
			MOB mob=msg.source();
			if(Sense.isInFlight(mob)) return true;
			if(!bridgeIsUp)
			{
				mob.tell("The bridge appears to be out.");
				return false;
			}
		}
		return true;
	}
	public void affect(Affect msg)
	{
		if((msg.targetMinor()==Affect.TYP_ENTER)
		||(msg.targetMinor()==Affect.TYP_LEAVE))
		{
			MOB mob=msg.source();
			if(Sense.isInFlight(mob)) return;
			if(bridgeIsUp)
			{
				if((mob.envStats().weight()>max)
				&&(Dice.rollPercentage()<chance))
				{
					bridgeIsUp=false;
					ExternalPlay.startTickDown(this,Host.SPELL_AFFECT,ticksDown);
					affected.recoverEnvStats();
					msg.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"The bridge breaks under <S-NAMEPOSS> weight!"));
					if((affected instanceof Room)
					&&((((Room)affected).domainType()==Room.DOMAIN_INDOORS_AIR)
					   ||(((Room)affected).domainType()==Room.DOMAIN_OUTDOORS_AIR))
					&&(((Room)affected).getRoomInDir(Directions.DOWN)!=null)
					&&(((Room)affected).getExitInDir(Directions.DOWN)!=null)
					&&(((Room)affected).getExitInDir(Directions.DOWN).isOpen()))
						((Room)affected).recoverRoomStats();
					else
					if(affected instanceof Exit)
						msg.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_DEATH,"<S-NAME> fall(s) to <S-HIS-HER> death!!"));
					else
					if(affected instanceof Room)
					for(int i=0;i<((Room)affected).numInhabitants();i++)
					{
						MOB M=((Room)affected).fetchInhabitant(i);
						if((M!=null)&&(!Sense.isInFlight(M)))
							msg.addTrailerMsg(new FullMsg(M,null,Affect.MSG_DEATH,"<S-NAME> fall(s) to <S-HIS-HER> death!!"));
					}
				}
			}
		}
		super.affect(msg);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		// get rid of flying restrictions when bridge is up
		if((affected!=null)
		&&(affected instanceof Room)
		&&(bridgeIsUp))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}
	public boolean tick(int tickID)
	{
		if(tickID==Host.SPELL_AFFECT)
		{
			bridgeIsUp=true;
			ExternalPlay.deleteTick(this,Host.SPELL_AFFECT);
		}
		return true;
	}
}
