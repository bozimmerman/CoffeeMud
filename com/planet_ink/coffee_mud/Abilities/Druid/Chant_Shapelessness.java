package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Shapelessness extends Chant
{
	public String ID() { return "Chant_Shapelessness"; }
	public String name(){ return "Shapelessness";}
	public String displayText(){ return "(Shapelessness)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public Environmental newInstance(){	return new Chant_Shapelessness();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> return(s) to material form.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
	}

	public int mobWeight(MOB mob)
	{
		int weight=mob.baseWeight();;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(!I.amWearingAt(Item.FLOATING_NEARBY)))
				weight+=I.envStats().weight();
		}
		return weight;
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==Affect.TYP_GET)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Item)
		&&(((msg.tool()==null)||(msg.tool() instanceof MOB))))
		{
			MOB mob=msg.source();
			if((msg.target().envStats().weight()>(mob.maxCarry()-mobWeight(mob)))&&(!mob.isMine(msg.target())))
			{
				mob.tell(msg.target().name()+" is too heavy.");
				return false;
			}
		}
		return super.okAffect(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchAffect(ID())!=null)
		{
			target.tell("You are already shapeless.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant that <T-NAME> be given a shapeless form.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> shimmer(s) and become(s) ethereal!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a new shape, but nothing happens.");


		// return whether it worked
		return success;
	}
}
