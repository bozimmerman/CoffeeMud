package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_TicketTaker extends Property
{
	public String ID() { return "Prop_TicketTaker"; }
	public String name(){ return "Ticket Taker";}
	public String displayText() {return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_TicketTaker BOB=new Prop_TicketTaker();	BOB.setMiscText(text());return BOB;	}

	public String accountForYourself()
	{
		return "one who acts as a ticket taker";
	}

	private int cost(){
		int amount=Util.s_int(text());
		if(amount==0) amount=10;
		return amount;
	}

	private boolean isMine(Environmental host, Rideable R)
	{
		if(host instanceof Rider)
		{
			Rider mob=(Rider)host;
			if(R==mob) return true;
			if(mob.riding()==null) return false;
			if(mob.riding()==R) return true;
			if((R instanceof Rider)&&(((Rider)R).riding()==mob.riding()))
				return true;
			if((mob.riding() instanceof Rider)&&(((Rider)mob.riding()).riding()==R))
				return true;
		}
		else
		if(host instanceof Rideable)
			return host==R;
		return false;
	}
	
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if(((myHost instanceof Rider)&&(((Rider)myHost).riding()!=null))
		   ||(myHost instanceof Rideable))
		{
			MOB mob=msg.source();
			if((msg.target()!=null)
			&&(myHost!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(myHost,(Rideable)msg.target())))
			{
				switch(msg.sourceMinor())
				{
				case Affect.TYP_MOUNT:
				case Affect.TYP_SIT:
				case Affect.TYP_ENTER:
				case Affect.TYP_SLEEP:
					if(mob.getMoney()>=cost())
					{
						mob.location().show(mob,myHost,Affect.MSG_NOISYMOVEMENT,"<S-NAME> give(s) "+cost()+" gold to <T-NAME>.");
						mob.setMoney(mob.getMoney()-cost());
					}
				}
			}
		}
	}
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if(((myHost instanceof Rider)&&(((Rider)myHost).riding()!=null))
		   ||(myHost instanceof Rideable))
		{
			MOB mob=msg.source();
			if((msg.target()!=null)
			&&(myHost!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(myHost,(Rideable)msg.target())))
				switch(msg.sourceMinor())
				{
				case Affect.TYP_MOUNT:
				case Affect.TYP_SIT:
				case Affect.TYP_ENTER:
				case Affect.TYP_SLEEP:
					if(mob.getMoney()<cost())
					{
						if(myHost instanceof MOB)
							ExternalPlay.quickSay((MOB)myHost,mob,"You'll need "+cost()+" gold to board.",false,false);
						else
							mob.tell("You'll need "+cost()+" gold to board.");
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
