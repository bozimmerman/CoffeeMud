package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SpeedBirth extends Chant
{
	public String ID() { return "Chant_SpeedBirth"; }
	public String name(){ return "Speed Birth";}
	protected int canAffectCode(){return 0;}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Chant_SpeedBirth();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Ability A=target.fetchAffect("Pregnancy");
		long start=0;
		long end=0;
		long days=0;
		long months=0;
		long remain=0;
		String rest=null;
		if(A!=null)
		{
			int x=A.text().indexOf("/");
			if(x>0)
			{
				int y=A.text().indexOf("/",x+1);
				if(y>x)
				{
					start=Util.s_long(A.text().substring(0,x));
					end=Util.s_long(A.text().substring(x+1,y));
					remain=end-System.currentTimeMillis();
					days=(remain/Host.TICK_TIME)/Host.TICKS_PER_DAY; // down to days;
					months=days/30;
					rest=A.text().substring(y);
				}
				else
					A=null;
			}
			else
				A=null;
		}

		if((!auto)&&(mob.curState().getMana()<mob.maxState().getMana()))
		{
			mob.tell("This Chant requires you to be at full mana.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		if(!auto) mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);
		if((success)&&(A!=null)&&(remain>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(remain<=20000)
				{
					mob.tell("Birth is imminent!");
					return true;
				}
				else
				if(days<1)
					remain=20000;
				else
					remain=remain/2;
				A.setMiscText((start-remain)+"/"+(end-remain)+rest);
				target.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> appear(s) even MORE pregnant!");
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}