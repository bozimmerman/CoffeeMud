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
	private Item myItem=null;
	private MOB lastMOB=null;
	public Environmental newInstance(){	Prop_HaveZapper BOB=new Prop_HaveZapper();	BOB.setMiscText(text());return BOB;}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(affectedMOB!=null)
			if(affectedMOB instanceof Item)
				myItem=(Item)affectedMOB;
		super.affectEnvStats(affectedMOB,affectableStats);
	}

	public boolean isOk(Ability me, MOB mob)
	{
		if(mob==null) return true;
		if(mob.charStats()==null) return true;
		String mobClass=mob.charStats().getCurrentClass().name().toUpperCase().substring(0,3);
		String mobBaseClass=mob.charStats().getCurrentClass().baseClass().toUpperCase().substring(0,3);
		String mobRace=mob.charStats().getMyRace().name().toUpperCase().substring(0,3);
		String mobAlign=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase().substring(0,3);
		String text=me.text().toUpperCase();

		// do class first
		int x=text.indexOf("-CLAS");
		if(x>=0)
		{
			if(text.indexOf("+"+mobClass)<x)
				return false;
		}
		else
		{
			if(text.indexOf("-"+mobClass)>=0)
				return false;
		}

		// now base class
		x=text.indexOf("-BASECLAS");
		if(x>=0)
		{
			if(text.indexOf("+"+mobBaseClass)<x)
				return false;
		}
		else
		{
			if(text.indexOf("-"+mobBaseClass)>=0)
				return false;
		}

		// now race
		x=text.indexOf("-RACE");
		if(x>=0)
		{
			if(text.indexOf("+"+mobRace)<x)
				return false;
		}
		else
		{
			if(text.indexOf("-"+mobRace)>=0)
				return false;

		}

		// and now alignments
		x=text.indexOf("-ALIG");
		if(x>=0)
		{

			if(text.indexOf("+"+mobAlign)<x)
				return false;
		}
		else
		{
			if(text.indexOf("-"+mobAlign)>=0)
				return false;

		}

		return true;
	}

	public String accountForYourself()
	{
		return "Restricted as follows: "+miscText;
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
			break;
		case Affect.TYP_WEAR:
			break;
		case Affect.TYP_WIELD:
			break;
		case Affect.TYP_GET:
			if((!isOk(this,mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,myItem.name()+" flashes and flys out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case Affect.TYP_DROP:
			break;
		default:
			break;
		}
		return true;
	}
}
