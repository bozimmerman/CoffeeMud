package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_ClearMoon extends Chant
{
	public String ID() { return "Chant_ClearMoon"; }
	public String name(){ return "Clear Moon";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_ClearMoon();}

	
	public void clearMoons(Environmental E)
	{
		if(E!=null)
		for(int a=E.numAffects()-1;a>=0;a--)
		{
			Ability A=E.fetchAffect(a);
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
			&&(A.name().endsWith(" Moon")))
				A.unInvoke();
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(!success)
			this.beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for a clear moon, but the magic fades.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType(auto),"^S<S-NAME> chant(s) for a clear moon.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				clearMoons(thatRoom);
				for(int i=0;i<thatRoom.numInhabitants();i++)
				{
					MOB M=thatRoom.fetchInhabitant(i);
					clearMoons(M);
				}
				for(int i=0;i<thatRoom.numItems();i++)
				{
					Item I=thatRoom.fetchItem(i);
					clearMoons(I);
				}
			}
		}

		return success;
	}
}