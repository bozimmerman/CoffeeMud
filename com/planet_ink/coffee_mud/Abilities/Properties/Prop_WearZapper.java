package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearZapper extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;
	private Prop_HaveZapper WA=new Prop_HaveZapper();

	public Prop_WearZapper()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Restrictions to wielding/wearing/holding";
		canAffectCode=Ability.CAN_ITEMS;
	}

	public Environmental newInstance()
	{
		Prop_WearZapper BOB=new Prop_WearZapper();
		BOB.setMiscText(text());
		return BOB;
	}

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