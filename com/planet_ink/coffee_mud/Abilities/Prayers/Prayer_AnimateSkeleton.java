package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AnimateSkeleton extends Prayer
{
	public String ID() { return "Prayer_AnimateSkeleton"; }
	public String name(){ return "Animate Skeleton";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_AnimateSkeleton();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(target==mob)
		{
			mob.tell(target.name()+" doesn't look dead yet.");
			return false;
		}
		if(!(target instanceof DeadBody))
		{
			mob.tell("You can't animate that.");
			return false;
		}

		DeadBody body=(DeadBody)target;
		int x=body.rawSecretIdentity().indexOf("/");

		if((body.rawSecretIdentity().length()==0)||(x<=0))
		{
			mob.tell("You can't animate that.");
			return false;
		}
		String description=body.rawSecretIdentity().substring(x+1);
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF> as a skeleton.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB newMOB=(MOB)CMClass.getMOB("GenUndead");
				newMOB.setName("a skeleton");
				newMOB.setDescription(description);
				newMOB.setDisplayText("a skeleton is here");
				newMOB.baseEnvStats().setLevel(1);
				newMOB.baseCharStats().setStat(CharStats.GENDER,body.charStats().getStat(CharStats.GENDER));
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Skeleton"));
				newMOB.baseCharStats().setBodyPartStrAfterRace(body.charStats().getBodyPartStr());
				Ability P=CMClass.getAbility("Prop_StatTrainer");
				if(P!=null)
				{
					P.setMiscText("NOTEACH STR=16 INT=10 WIS=10 CON=10 DEX=15 CHA=2");
					newMOB.addNonUninvokableEffect(P);
				}
				newMOB.recoverCharStats();
				newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
				newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
				newMOB.setAlignment(0);
				newMOB.baseState().setHitPoints(15*newMOB.baseEnvStats().level());
				newMOB.baseState().setMovement(newMOB.baseCharStats().getCurrentClass().getLevelMove(newMOB));
				newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
				newMOB.baseState().setMana(0);
				Behavior B=CMClass.getBehavior("Aggressive");
				if(B!=null){ B.setParms("+NAMES \"-"+mob.Name()+"\""); newMOB.addBehavior(B);}
				newMOB.recoverCharStats();
				newMOB.recoverEnvStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.text();
				newMOB.bringToLife(mob.location(),true);
				newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				int it=0;
				while(it<newMOB.location().numItems())
				{
					Item item=newMOB.location().fetchItem(it);
					if((item!=null)&&(item.container()==body))
					{
						FullMsg msg2=new FullMsg(newMOB,body,item,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg2);
						FullMsg msg3=new FullMsg(newMOB,item,null,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg3);
						it=0;
					}
					else
						it++;
				}
				body.destroy();
				mob.location().show(newMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> begin(s) to rise!");
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF>, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}