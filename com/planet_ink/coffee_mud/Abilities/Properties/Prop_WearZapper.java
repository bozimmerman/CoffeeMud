package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearZapper extends Property
{
	public String ID() { return "Prop_WearZapper"; }
	public String name(){ return "Restrictions to wielding/wearing/holding";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_WearZapper BOB=new Prop_WearZapper();	BOB.setMiscText(text()); return BOB;}

	public String accountForYourself()
	{
		return "Wearing restricted as follows: "+SaucerSupport.zapperDesc(miscText);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(affected==null) return false;
		if(!(affected instanceof Item)) return false;
		Item myItem=(Item)affected;

		MOB mob=affect.source();
		if(mob.location()==null)
			return true;

		if(affect.amITarget(myItem))
		switch(affect.targetMinor())
		{
		case Affect.TYP_HOLD:
			if((!SaucerSupport.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,myItem,Affect.MSG_OK_VISUAL,"<O-NAME> flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_WEAR:
			if((!SaucerSupport.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,myItem,Affect.MSG_OK_VISUAL,"<O-NAME> flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_WIELD:
			if((!SaucerSupport.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,myItem,Affect.MSG_OK_VISUAL,"<O-NAME> flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_GET:
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