package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonMarker extends Spell
{
	public String ID() { return "Spell_SummonMarker"; }
	public String name(){return "Summon Marker";}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Spell_SummonMarker();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public void unInvoke()
	{
		
		if((canBeUninvoked())&&(invoker()!=null)&&(affected!=null)&&(affected instanceof Room))
			invoker().tell("Your marker in '"+((Room)affected).displayText()+"' dissipates.");
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		for(Iterator r=CMMap.rooms();r.hasNext();)
		{
			Room R=(Room)r.next();
			if(R!=null)
			for(int a=0;a<R.numAffects();a++)
			{
				Ability A=R.fetchAffect(a);
				if((A!=null)
				   &&(A.ID().equals(ID()))
				   &&(A.invoker()==mob))
				{
					A.unInvoke();
					break;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"":"^S<S-NAME> summon(s) <S-HIS-HER> marker energy to this place!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,mob.location(),Affect.MSG_OK_VISUAL,"The spot <S-NAME> pointed to glows for brief moment.");
				beneficialAffect(mob,mob.location(),(adjustedLevel(mob)*240)+450);
			}
			
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon <S-HIS-HER> marker energy, but fail(s).");


		// return whether it worked
		return success;
	}
}