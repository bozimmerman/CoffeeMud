package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_LinkedHealth extends Prayer
{
	public String ID() { return "Prayer_LinkedHealth"; }
	public String name(){ return "Linked Health";}
	public String displayText(){ return "(Linked Health)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	MOB buddy=null;
	public Environmental newInstance(){	return new Prayer_LinkedHealth();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(buddy!=null)
			{
				mob.tell("Your health is no longer linked with "+buddy.name()+".");
				Ability A=buddy.fetchEffect(ID());
				if(A!=null) A.unInvoke();
			}
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			if((msg.tool()==null)||(!msg.tool().ID().equals(ID())))
			{
				int recovery=(int)Math.round(Util.div((msg.value()),2.0));
				msg.setValue(recovery);
				MUDFight.postDamage(msg.source(),buddy,this,recovery,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_BURSTING,"<T-NAME> absorb(s) damage from the harm to "+msg.target().name()+".");
			}
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob.fetchEffect(ID())!=null)
		{
			mob.tell("Your health is already linked with someones!");
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target.name()+"'s health is already linked with someones!");
			return false;
		}

		if(!mob.getGroupMembers(new HashSet()).contains(target))
		{
			mob.tell(target.name()+" is not in your group.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" that <S-HIS-HER> health be linked with <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<S-NAME> and <T-NAME> are linked in health.");
				buddy=mob;
				beneficialAffect(mob,target,0);
				buddy=target;
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for a link with <T-NAME>, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}