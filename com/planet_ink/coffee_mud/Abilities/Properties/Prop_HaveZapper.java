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
		return "Ownership restricted as follows: "+ExternalPlay.zapperDesc(miscText);
	}



	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(affected==null) return false;

		MOB mob=affect.source();
		if(mob.location()==null)
			return true;

		if(affect.amITarget(affected))
		switch(affect.targetMinor())
		{
		case Affect.TYP_HOLD:
			break;
		case Affect.TYP_WEAR:
			break;
		case Affect.TYP_WIELD:
			break;
		case Affect.TYP_GET:
			if((!ExternalPlay.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,affected.name()+" flashes and flys out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_DROP:
		case Affect.TYP_THROW:
			break;
		default:
			break;
		}
		return true;
	}
}
