package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SanctifyRoom extends Prayer
{
	public String ID() { return "Prayer_SanctifyRoom"; }
	public String name(){return "Sanctify Room";}
	public String displayText(){return "(Sanctify Room)";}
	public int quality(){ return OK_OTHERS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Prayer_SanctifyRoom();}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affected==null)
			return super.okAffect(myHost,affect);

		Room R=(Room)affected;
		if((affect.sourceMinor()==Affect.TYP_GET)
		&&((text().length()==0)
		   ||((R.fetchInhabitant(text())!=null)&&(R.fetchInhabitant(text()).Name().equalsIgnoreCase(text())))))
		{
			affect.source().tell("You feel your muscles unwilling to cooperate.");
			return false;
		}
		return super.okAffect(myHost,affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=mob.location();
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to sanctify this place.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if((target instanceof Room)
				&&((ExternalPlay.doesOwnThisProperty(mob,((Room)target)))
					||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),((Room)target))))))
					target.addNonUninvokableAffect(this);
				else
					beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to sanctify this place, but <S-IS-ARE> not answered.");

		return success;
	}
}