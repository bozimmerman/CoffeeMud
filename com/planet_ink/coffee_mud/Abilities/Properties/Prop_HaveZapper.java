package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_HaveZapper extends Property
{
	public String ID() { return "Prop_HaveZapper"; }
	public String name(){ return "Restrictions to ownership";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_HaveZapper BOB=new Prop_HaveZapper();	BOB.setMiscText(text());return BOB;}

	public String accountForYourself()
	{
		return "Ownership restricted as follows: "+SaucerSupport.zapperDesc(miscText);
	}



	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null) return false;

		MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(affected))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			break;
		case CMMsg.TYP_WEAR:
			break;
		case CMMsg.TYP_WIELD:
			break;
		case CMMsg.TYP_GET:
			if((!SaucerSupport.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,affected,CMMsg.MSG_OK_ACTION,"<O-NAME> flashes and flies out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case CMMsg.TYP_DROP:
		case CMMsg.TYP_THROW:
			break;
		default:
			break;
		}
		return true;
	}
}
