package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AnimateGhost extends Prayer
{
	public String ID() { return "Prayer_AnimateGhost"; }
	public String name(){ return "Animate Ghost";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_AnimateGhost();}

	public static void makeGhostFrom(Room R, DeadBody body, MOB mob, int level)
	{
		int x=body.rawSecretIdentity().indexOf("/");
		String description=body.rawSecretIdentity().substring(x+1);
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";
		
		MOB newMOB=(MOB)CMClass.getMOB("GenUndead");
		newMOB.setName((mob==null)?"a poltergeist":"a ghost");
		newMOB.setDescription(description);
		newMOB.setDisplayText(newMOB.Name()+" is here");
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseCharStats().setStat(CharStats.GENDER,body.charStats().getStat(CharStats.GENDER));
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		newMOB.baseCharStats().setBodyPartStrAfterRace(body.charStats().getBodyPartStr());
		Ability P=CMClass.getAbility("Prop_StatTrainer");
		if(P!=null)
		{
			P.setMiscText("NOTEACH STR=2 INT=10 WIS=10 CON=10 DEX=35 CHA=2");
			newMOB.addNonUninvokableEffect(P);
		}
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setAttackAdjustment(10);
		newMOB.baseEnvStats().setDisposition(EnvStats.IS_FLYING|((mob==null)?EnvStats.IS_INVISIBLE:0));
		newMOB.baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK|EnvStats.CAN_SEE_INVISIBLE);
		newMOB.baseEnvStats().setDamage(4);
		newMOB.setAlignment(0);
		newMOB.baseState().setHitPoints(10*newMOB.baseEnvStats().level());
		newMOB.baseState().setMovement(newMOB.baseCharStats().getCurrentClass().getLevelMove(newMOB));
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseState().setMana(100);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		Ability A=CMClass.getAbility("Immunities");
		if(A!=null){
			A.setMiscText("all");
			newMOB.addNonUninvokableEffect(A);
		}
		Behavior B=CMClass.getBehavior("Aggressive");
		if((B!=null)&&(mob!=null)){ B.setParms("+NAMES \"-"+mob.Name()+"\"");}
		if(B!=null) newMOB.addBehavior(B);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		if(mob==null)
		{
			B=CMClass.getBehavior("Thiefness");
			if(B!=null) newMOB.addBehavior(B);
		}
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(R,true);
		newMOB.setMoney(0);
		R.showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		int it=0;
		while(it<newMOB.location().numItems())
		{
			Item item=newMOB.location().fetchItem(it);
			if((item!=null)&&(item.container()==body))
			{
				FullMsg msg2=new FullMsg(newMOB,body,item,CMMsg.MSG_GET,null);
				newMOB.location().send(newMOB,msg2);
				FullMsg msg4=new FullMsg(newMOB,item,null,CMMsg.MSG_GET,null);
				newMOB.location().send(newMOB,msg4);
				FullMsg msg3=new FullMsg(newMOB,item,null,CMMsg.MSG_WEAR,null);
				newMOB.location().send(newMOB,msg3);
				if(!newMOB.isMine(item))
					it++;
				else
					it=0;
			}
			else
				it++;
		}
		body.destroy();
		R.show(newMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> begin(s) to rise!");
		R.recoverRoomStats();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(target==mob)
		{
			mob.tell(target.name()+" doesn't look dead yet.");
			return false;
		}
		if((!(target instanceof DeadBody))
		||(((DeadBody)target).rawSecretIdentity().indexOf("FAKE")>=0))
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
		if(body.baseEnvStats().level()<15)
		{
			mob.tell("This creature is too weak to create a ghost from.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF> as a ghost.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				makeGhostFrom(mob.location(),body,mob,14);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF>, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}