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
	protected Vector mobsToKill=new Vector();

	public String accountForYourself()
	{ return "Weak and Rickity";	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		max=Util.getParmInt(newText,"max",400);
		chance=Util.getParmInt(newText,"chance",75);
		ticksDown=Util.getParmInt(newText,"down",300);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&((msg.amITarget(affected))||(msg.tool()==affected)))
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

	public int weight(MOB mob)
	{
		int weight=0;
		if(affected instanceof Room)
		{
			Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=(MOB)room.fetchInhabitant(i);
				if((M!=null)&&(M!=mob)&&(!Sense.isInFlight(M)))
					weight+=M.envStats().weight();
			}
		}
		return weight+mob.envStats().weight();
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&((msg.amITarget(affected))||(msg.tool()==affected))
		&&(!Sense.isFalling(msg.source())))
		{
			MOB mob=msg.source();
			if(Sense.isInFlight(mob)) return;
			if(bridgeIsUp)
			{
				if((weight(mob)>max)
				&&(Dice.rollPercentage()<chance))
				{
					synchronized(mobsToKill)
					{
						if(!mobsToKill.contains(mob))
						{
							mobsToKill.addElement(mob);
							if(!Sense.isFalling(mob))
							{
								Ability falling=CMClass.getAbility("Falling");
								falling.setProfficiency(0);
								falling.setAffectedOne(msg.target());
								falling.invoke(null,null,mob,true);
							}
							ExternalPlay.startTickDown(this,MudHost.TICK_SPELL_AFFECT,1);
						}
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		// get rid of flying restrictions when bridge is up
		if((affected!=null)
		&&(affected instanceof Room)
		&&(bridgeIsUp))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_SPELL_AFFECT)
		{
			if(bridgeIsUp)
			{
				synchronized(mobsToKill)
				{
					bridgeIsUp=false;
					Vector V=((Vector)mobsToKill.clone());
					mobsToKill.clear();
					if(affected instanceof Room)
					{
						Room room=(Room)affected;
						for(int i=0;i<room.numInhabitants();i++)
						{
							MOB M=room.fetchInhabitant(i);
							if((M!=null)
							&&(!Sense.isInFlight(M))
							&&(!V.contains(M)))
								V.addElement(M);
						}
					}
					for(int i=0;i<V.size();i++)
					{
						MOB mob=(MOB)V.elementAt(i);
						if((mob.location()!=null)
						&&(!Sense.isInFlight(mob)))
						{
							if((affected instanceof Room)
							&&((((Room)affected).domainType()==Room.DOMAIN_INDOORS_AIR)
							   ||(((Room)affected).domainType()==Room.DOMAIN_OUTDOORS_AIR))
							&&(((Room)affected).getRoomInDir(Directions.DOWN)!=null)
							&&(((Room)affected).getExitInDir(Directions.DOWN)!=null)
							&&(((Room)affected).getExitInDir(Directions.DOWN).isOpen()))
							{
								mob.tell("The bridge breaks under your weight!");
								if((!Sense.isFalling(mob))
								&&(mob.location()==affected))
								{
									Ability falling=CMClass.getAbility("Falling");
									falling.setProfficiency(0);
									falling.setAffectedOne(affected);
									falling.invoke(null,null,mob,true);
								}
							}
							else
							{
								mob.location().showSource(mob,null,CMMsg.MSG_OK_VISUAL,"The bridge breaks under your weight!");
								mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fall(s) to <S-HIS-HER> death!!");
								mob.location().show(mob,null,CMMsg.MSG_DEATH,null);
							}
						}
					}
					if(affected instanceof Room)
						((Room)affected).recoverEnvStats();
					ExternalPlay.deleteTick(this,MudHost.TICK_SPELL_AFFECT);
					ExternalPlay.startTickDown(this,MudHost.TICK_SPELL_AFFECT,ticksDown);
				}
			}
			else
			{
				bridgeIsUp=true;
				ExternalPlay.deleteTick(this,MudHost.TICK_SPELL_AFFECT);
				if(affected instanceof Room)
					((Room)affected).recoverEnvStats();
			}
		}
		return true;
	}
}
