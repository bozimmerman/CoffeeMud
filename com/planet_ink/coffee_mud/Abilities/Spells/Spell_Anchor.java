package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Anchor extends Spell
{
	public String ID() { return "Spell_Anchor"; }
	public String name(){return "Anchor";}
	public String displayText(){return "(Anchor)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Anchor();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

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
			mob.tell("Your anchor has been lifted.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected==null)	return true;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&((affected==null)
			||((affected instanceof Item)&&(!((Item)affected).amWearingAt(Item.INVENTORY))&&(msg.amITarget(((Item)affected).owner())))
			||((affected instanceof MOB)&&(msg.amITarget((MOB)affected))))
		&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_MOVING)
		   ||Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		{
			Room roomS=null;
			Room roomD=null;
			if((msg.target()!=null)&&(msg.target() instanceof MOB))
				roomD=((MOB)msg.target()).location();
			else
			if((msg.target()!=null)&&(msg.target() instanceof Item))
			{
				Item I=(Item)msg.target();
				if((I.owner()!=null)&&(I.owner() instanceof MOB))
					roomD=((MOB)((Item)msg.target()).owner()).location();
				else
				if((I.owner()!=null)&&(I.owner() instanceof Room))
					roomD=(Room)((Item)msg.target()).owner();
			}
			else
			if((msg.target()!=null)&&(msg.target() instanceof Room))
				roomD=(Room)msg.target();

			if((msg.source()!=null)&&(msg.source().location()!=null))
				roomS=msg.source().location();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			if(roomD!=null)
				roomD.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"An magical anchoring field envelopes <T-NAME>!":"^S<S-NAME> invoke(s) an anchoring field of protection around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an anchoring field, but fail(s).");

		return success;
	}
}
