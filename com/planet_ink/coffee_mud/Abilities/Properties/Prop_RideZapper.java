package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RideZapper extends Property
{
	public String ID() { return "Prop_RideZapper"; }
	public String name(){ return "Restrictions to riding";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	Prop_RideZapper BOB=new Prop_RideZapper();	BOB.setMiscText(text()); return BOB;}

	public String accountForYourself()
	{
		return "Mounting restricted as follows: "+SaucerSupport.zapperDesc(miscText);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(affected==null) return true;
		if(!(affected instanceof Rideable)) return true;

		MOB mob=affect.source();
		if(mob.location()==null) return true;

		if(affect.amITarget(affected))
		switch(affect.targetMinor())
		{
		case Affect.TYP_SIT:
		case Affect.TYP_SLEEP:
		case Affect.TYP_MOUNT:
		case Affect.TYP_ENTER:
			if((!SaucerSupport.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,affected,Affect.MSG_OK_VISUAL,"<O-NAME> zaps <S-NAME>, making <S-HIS-HER> jump up!");
				return false;
			}
			break;
		}
		return true;
	}
}