package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Rockthought extends Chant
{
	public String ID() { return "Chant_Rockthought"; }
	public String name(){ return "Rockthought";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_Rockthought();}
	CMMsg stubb=null;
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(stubb==null)
		&&(msg.amISource((MOB)affected))
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL)
		&&(msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0)))
			stubb=msg;
		super.executeMsg(host,msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)
		&&(stubb!=null)
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).location().okMessage(affected,stubb)))
			((MOB)affected).location().send((MOB)affected,stubb);
		return super.tick(ticking,tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					stubb=null;
					success=maliciousAffect(mob,target,20,CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0));
					if(success)
					{
						if(target.isInCombat()) target.makePeace();
						target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> look(s) stubborn.");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}