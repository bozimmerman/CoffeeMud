package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummoningWard extends Spell
{
	public String ID() { return "Spell_SummoningWard"; }
	public String name(){return "Summoning Ward";}
	public String displayText(){return "(Summoning Ward)";}
	public int quality(){ return OK_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	public Environmental newInstance(){	return new Spell_SummoningWard();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your summoning ward dissipates.");

		super.unInvoke();

	}


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affected==null)
			return super.okAffect(myHost,affect);

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((affect.amITarget(mob))
			&&(!affect.amISource(mob))
			&&(mob.location()!=affect.source().location())
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_SUMMONING))
			&&(!mob.amDead()))
			{
				affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				return false;
			}
		}
		else
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(Util.bset(((Ability)affect.tool()).flags(),Ability.FLAG_SUMMONING)))
			{
				if((affect.source().location()!=null)&&(affect.source().location()!=R))
					affect.source().location().showHappens(Affect.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				R.showHappens(Affect.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=null;
		if(commands.size()>0)
		{
			String s=Util.combine(commands,0);
			if(s.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
				target=mob.location();
			else
			if(CoffeeUtensils.containsString(mob.location().ID(),s)
			||CoffeeUtensils.containsString(mob.location().name(),s)
			||CoffeeUtensils.containsString(mob.location().displayText(),s))
				target=mob.location();
		}
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> seem(s) magically protected.":"^S<S-NAME> invoke(s) a summoning ward upon <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target instanceof Room)
				&&((ExternalPlay.doesOwnThisProperty(mob,((Room)target)))
					||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),((Room)target))))))
					target.addNonUninvokableAffect(this);
				else
					beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a summoning ward, but fail(s).");

		return success;
	}
}
