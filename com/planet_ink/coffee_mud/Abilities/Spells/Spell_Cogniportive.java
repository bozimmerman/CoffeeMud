package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Cogniportive extends Spell
{
	public String ID() { return "Spell_Cogniportive"; }
	public String name(){return "Cogniportive";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Cogniportive();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null)
		{
			String str=Util.combine(commands,0).toUpperCase();
			if(str.equals("MONEY")||str.equals("GOLD")||str.equals("COINS"))
				mob.tell("You can't cast this spell on your own coins.");
			return false;
		}

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already cogniportive!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> glow(s) softly!");
				beneficialAffect(mob,target,100);
				Ability A=target.fetchAffect(ID());
				if(A!=null){
					A.makeLongLasting();
					A.makeNonUninvokable();
				}
				target.recoverEnvStats();
				mob.recoverEnvStats();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}