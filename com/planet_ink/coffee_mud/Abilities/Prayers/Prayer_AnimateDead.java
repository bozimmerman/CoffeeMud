package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AnimateDead extends Prayer
{
	public Prayer_AnimateDead()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Animate Dead";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(24);
		
		holyQuality=Prayer.HOLY_EVIL;
		quality=Ability.INDIFFERENT;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_AnimateDead();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget);
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
		String realName=body.rawSecretIdentity().substring(0,x);
		String description=body.rawSecretIdentity().substring(x+1);
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";

		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		mob.curState().setMana(0);


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> pray(s) over <T-NAMESELF> hungrily, calling on evil powers.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB newMOB=(MOB)CMClass.getMOB("GenUndead").newInstance();
				newMOB.setName(realName+" zombie");
				newMOB.setDescription(description);
				newMOB.setDisplayText("");
				newMOB.baseEnvStats().setLevel(body.envStats().level());
				newMOB.baseCharStats().setStat(CharStats.STRENGTH,25);
				newMOB.baseCharStats().setStat(CharStats.DEXTERITY,3);
				newMOB.baseEnvStats().setAttackAdjustment(50);
				newMOB.baseEnvStats().setDamage(30);
				newMOB.setAlignment(0);
				newMOB.baseState().setHitPoints(50);
				newMOB.baseState().setMovement(30);
				newMOB.baseState().setMana(0);
				newMOB.recoverCharStats();
				newMOB.recoverEnvStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.text();
				newMOB.bringToLife(mob.location());
				int it=0;
				while(it<newMOB.location().numItems())
				{
					Item item=newMOB.location().fetchItem(it);
					if((item!=null)&&(item.location()==body))
					{
						FullMsg msg2=new FullMsg(newMOB,body,item,Affect.MSG_GET,null);
						newMOB.location().send(newMOB,msg2);
						FullMsg msg3=new FullMsg(newMOB,item,null,Affect.MSG_GET,null);
						newMOB.location().send(newMOB,msg3);
						it=0;
					}
					else
						it++;
				}
				body.destroyThis();
				mob.location().show(newMOB,null,Affect.MSG_OK_VISUAL,"<S-NAME> begin(s) to rise!");
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for dark powers, but fail(s) miserably.");


		// return whether it worked
		return success;
	}
}