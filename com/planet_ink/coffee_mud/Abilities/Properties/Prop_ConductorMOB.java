package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ConductorMOB extends Property
{
	public String ID() { return "Prop_ConductorMOB"; }
	public String name(){ return "Conductor MOB";}
	public String displayText() {return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	Prop_ConductorMOB BOB=new Prop_ConductorMOB();	BOB.setMiscText(text());return BOB;	}
	private Vector paid=new Vector();

	public String accountForYourself()
	{
		return "is a MOB who acts as a conductor";
	}

	private int cost(){
		int amount=Util.s_int(text());
		if(amount==0) amount=10;
		return amount;
	}

	private boolean isMine(MOB mob, Rideable R)
	{
		if(mob.riding()==null) return false;
		if(mob.riding()==R) return true;
		if((R instanceof Rider)&&(((Rider)R).riding()==mob.riding()))
			return true;
		if((mob.riding() instanceof Rider)&&(((Rider)mob.riding()).riding()==R))
			return true;
		return false;
	}
	
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((myHost instanceof MOB)&&(((MOB)myHost).riding()!=null))
		{
			MOB monster=(MOB)myHost;
			MOB mob=msg.source();
			if((msg.target()!=null)
			&&(msg.target() instanceof Rideable)
			&&(msg.targetMinor()==Affect.TYP_DISMOUNT)
			&&(isMine(monster,(Rideable)msg.target())))
			{
				if(paid.contains(mob))
					paid.removeElement(mob);
			}
			else
			if(msg.amITarget(monster)
			   &&(msg.targetMinor()==Affect.TYP_GIVE)
			   &&(msg.tool() instanceof Coins))
			{
				int val=((Coins)msg.tool()).numberOfCoins();
				if(val>=cost())
				{
					ExternalPlay.quickSay(monster,mob,"Thank you! Now climb aboard!",false,false);
					if(!paid.contains(mob))
						paid.addElement(mob);
				}
			}
		}
	}
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if((myHost instanceof MOB)&&(((MOB)myHost).riding()!=null))
		{
			MOB monster=(MOB)myHost;
			MOB mob=msg.source();
			if((msg.target()!=null)
			&&(monster!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(monster,(Rideable)msg.target())))
				switch(msg.sourceMinor())
				{
				case Affect.TYP_MOUNT:
				case Affect.TYP_SIT:
				case Affect.TYP_SLEEP:
					if(!paid.contains(mob))
					{
						ExternalPlay.quickSay(monster,mob,"You'll need to give me "+cost()+" gold to board.",false,false);
						return false;
					}
					break;
				default:
					break;
				}
		}
		return true;
	}
}
