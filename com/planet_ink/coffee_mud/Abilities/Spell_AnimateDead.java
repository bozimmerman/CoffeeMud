package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_AnimateDead extends Spell
	implements NecromancyDevotion
{
	public Spell_AnimateDead()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Animate Dead";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(24);

		addQualifyingClass(new Mage().ID(),24);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_AnimateDead();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("You must specify a dead body to cast this on.");
			return false;
		}
		Environmental target=mob.location().fetchFromRoom(null,(String)commands.elementAt(0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
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
		int x=body.secretIdentity().indexOf("/");

		if((body.secretIdentity().length()==0)||(x<=0))
		{
			mob.tell("You can't animate that.");
			return false;
		}
		String realName=body.secretIdentity().substring(0,x);
		String description=body.secretIdentity().substring(x+1);
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";

		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;
		mob.curState().setMana(0);


		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) over <T-NAME> hungrily.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.SOUND_WORDS,"<T-NAME> begin(s) to rise!");
				GenUndead newMOB=new GenUndead();
				newMOB.setName(realName+" zombie");
				newMOB.setDescription(description);
				newMOB.setDisplayText("");
				int i=0;
				while(i<mob.location().numItems())
				{
					Item thisItem=mob.location().fetchItem(i);
					if(thisItem.location()!=body)
						i++;
					else
					{
						mob.location().delItem(thisItem);
						newMOB.addInventory(thisItem);
					}
				}
				newMOB.baseEnvStats().setLevel(3);
				newMOB.baseCharStats().setStrength(25);
				newMOB.baseCharStats().setDexterity(3);
				newMOB.baseEnvStats().setAttackAdjustment(50);
				newMOB.baseEnvStats().setDamage(30);
				newMOB.setAlignment(0);
				newMOB.maxState().setHitPoints(50);
				newMOB.maxState().setMovement(30);
				newMOB.maxState().setMana(0);
				newMOB.recoverCharStats();
				newMOB.recoverMaxState();
				newMOB.recoverEnvStats();
				newMOB.setFollowing(mob);
				newMOB.bringToLife(mob.location());
				body.destroyThis();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");


		// return whether it worked
		return success;
	}
}