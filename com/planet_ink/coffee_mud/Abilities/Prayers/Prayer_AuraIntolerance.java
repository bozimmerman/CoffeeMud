package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AuraIntolerance extends Prayer
{
	public String ID() { return "Prayer_AuraIntolerance"; }
	public String name(){ return "Aura of Intolerance";}
	public String displayText(){ return "(Intolerance Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_AuraIntolerance();	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB M=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(M!=null)&&(!M.amDead())&&(M.location()!=null))
			M.location().show(M,null,CMMsg.MSG_OK_VISUAL,"The intolerant aura around <S-NAME> fades.");
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() instanceof MOB)
		&&(msg.source().getWorshipCharID().length()>0)
		&&(!((MOB)msg.target()).getWorshipCharID().equals(msg.source().getWorshipCharID())))
		{
			if(((MOB)msg.target()).getWorshipCharID().length()>0)
				msg.setValue(msg.value()*2);
			else
				msg.setValue(msg.value()+(msg.value()/2));
		}
		return true;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		
		if(!super.tick(ticking,tickID))
			return false;
		
		Room R=((MOB)affected).location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=((MOB)affected))
			&&((M.getWorshipCharID().length()==0)
				||(((MOB)affected).getWorshipCharID().length()>0)&&(!M.getWorshipCharID().equals(((MOB)affected).getWorshipCharID()))))
			{
				if(M.getWorshipCharID().length()>0)
					MUDFight.postDamage(((MOB)affected),M,this,3,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The intolerant aura around <S-NAME> <DAMAGE> <T-NAMESELF>!");
				else
					MUDFight.postDamage(((MOB)affected),M,this,1,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The intolerant aura around <S-NAME> <DAMAGE> <T-NAMESELF>!");
			}
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("The aura of intolerance is already with you.");
			return false;
		}
		if((!auto)&&((mob.getWorshipCharID().length()==0)
					 ||(CMMap.getDeity(mob.getWorshipCharID())==null)))
		{
			mob.tell("You must worship a god to be intolerant.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the aura of intolerance.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of intolerance, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}