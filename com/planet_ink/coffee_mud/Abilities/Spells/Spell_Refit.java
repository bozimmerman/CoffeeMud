package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Refit extends Spell
{
	public Spell_Refit()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Refit";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;

		baseEnvStats().setLevel(4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Refit();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(!(target instanceof Armor))
		{	mob.tell(target.name()+" cannot be refitted."); return false;}

		if(!super.invoke(mob,commands, givenTarget, auto))
			return false;

		boolean success=profficiencyCheck(((mob.envStats().level()-target.envStats().level())*5),auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,
									(auto?"<T-NAME> begins to shimmer!"
										 :"^S<S-NAME> incant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				
				if(target.envStats().height()==0)
					mob.tell("Nothing happens to "+target.name()+".");
				else
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> begin(s) to magically resize itself!");
					target.baseEnvStats().setHeight(0);
				}
				target.recoverEnvStats();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
