package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_HaveZapper extends Property
{
	private Item myItem=null;
	private MOB lastMOB=null;

	public Prop_HaveZapper()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Restrictions to ownership";
	}

	public Environmental newInstance()
	{
		Prop_HaveZapper BOB=new Prop_HaveZapper();
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

	public boolean isOk(Ability me, MOB mob)
	{
		if(mob==null) return true;
		if(mob.charStats()==null) return true;
		if(mob.charStats().getMyClass()==null) return true;
		if(mob.charStats().getMyRace()==null) return true;
		String mobClass=mob.charStats().getMyClass().name().toUpperCase().substring(0,3);
		String mobRace=mob.charStats().getMyRace().name().toUpperCase().substring(0,3);
		String mobAlign=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase().substring(0,3);
		String text=me.text().toUpperCase();

		// do class first
		if(text.indexOf("-CLAS")>=0)
		{
			if(text.indexOf("+"+mobClass)<0)
				return false;
		}
		else
		{
			if(text.indexOf("-"+mobClass)>=0)
				return false;
		}

		// now race
		if(text.indexOf("-RACE")>=0)
		{
			if(text.indexOf("+"+mobRace)<0)
				return false;
		}
		else
		{
			if(text.indexOf("-"+mobRace)>=0)
				return false;

		}

		// and now alignments
		if(text.indexOf("-ALIG")>=0)
		{

			if(text.indexOf("+"+mobAlign)<0)
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
		//String allowed="";
		//String disallowed="";

		for(int c=0;c<CMClass.charClasses.size();c++)
		{

		}
		return "";
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
