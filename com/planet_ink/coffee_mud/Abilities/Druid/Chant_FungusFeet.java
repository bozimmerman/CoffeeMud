package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_FungusFeet extends Chant implements DiseaseAffect
{
	public String ID() { return "Chant_FungusFeet"; }
	public String name(){ return "Fungus Feet";}
	public String displayText(){return "(Fungus Feet)";}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Chant_FungusFeet();}
	public int abilityCode(){return 0;}
	int plagueDown=8;
	double drawups=1.0;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if((--plagueDown)<=0)
		{
			MOB mob=(MOB)affected;
			plagueDown=10;
			if(invoker==null) invoker=mob;
			drawups+=.1;
			if(drawups>=3.1)
			{
				if((mob.location()!=null)&&(Sense.isInTheGame(mob)))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOU-POSS> feet rot off!");
					Ability A=CMClass.getAbility("Amputation");
					if(A!=null)
					{
						while(A.invoke(mob,Util.parse("foot"),mob,true));
						mob.recoverCharStats();
						mob.recoverEnvStats();
						mob.recoverMaxState();
					}
					unInvoke();
				}
			}
			else
				MUDFight.postDamage(invoker,mob,this,1,CMMsg.TYP_DISEASE,-1,"<T-NAME> feel(s) the fungus between <T-HIS-HER> toes eating <T-HIS-HER> feet away!");
		}
		return true;
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected==null) return;
		affectableState.setMovement((int)Math.round(Util.div(affectableState.getMovement(),drawups)));
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead())&&(mob.getWearPositions(Item.ON_FEET)>0))
			{
				Ability A=mob.fetchEffect("TemporaryImmunity");
				if(A==null)
				{
					A=CMClass.getAbility("TemporaryImmunity");
					A.setBorrowed(mob,true);
					A.makeLongLasting();
					mob.addEffect(A);
					A.makeLongLasting();
				}
				A.setMiscText("+"+ID());
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The fungus on <S-YOUPOSS> feet dies and falls off.");
			}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if(target.charStats().getBodyPart(Race.BODY_FOOT)==0)
		{
			mob.tell(target.name()+" has no feet!");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) at <T-YOUPOSS> feet!^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					invoker=mob;
					maliciousAffect(mob,target,Integer.MAX_VALUE/2,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"A fungus sprouts up between <S-YOUPOSS> toes!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-YOUPOSS> feet, but nothing happens.");


		// return whether it worked
		return success;
	}
}