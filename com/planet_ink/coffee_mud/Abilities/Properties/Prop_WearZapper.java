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
	private Item myItem=null;
	private MOB lastMOB=null;
	private Prop_HaveZapper WA=new Prop_HaveZapper();
	public Environmental newInstance(){	Prop_WearZapper BOB=new Prop_WearZapper();	BOB.setMiscText(text()); return BOB;}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(affectedMOB!=null)
			if(affectedMOB instanceof Item)
				myItem=(Item)affectedMOB;
		super.affectEnvStats(affectedMOB,affectableStats);
	}


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(myItem==null)
			return true;

		MOB mob=affect.source();
		if(mob.location()==null)
			return true;

		if(affect.amITarget(myItem))
		switch(affect.targetMinor())
		{
		case Affect.TYP_HOLD:
			if((!WA.isOk(this,mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,myItem.name()+" flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_WEAR:
			if((!WA.isOk(this,mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,myItem.name()+" flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_WIELD:
			if((!WA.isOk(this,mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,myItem.name()+" flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_GET:
			break;
		case Affect.TYP_DROP:
			break;
		default:
			break;
		}
		return true;
	}
}