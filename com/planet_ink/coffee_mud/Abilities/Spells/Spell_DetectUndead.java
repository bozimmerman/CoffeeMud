package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectUndead extends Spell
{
	Room lastRoom=null;
	public Spell_DetectUndead()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Undead";
		displayText="(Detect Undead)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_SELF;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DetectUndead();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		lastRoom=null;
		super.unInvoke();
		mob.tell(mob,null,"Your senses are no longer as dark.");
	}
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if((tickID==Host.MOB_TICK)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			for(int i=0;i<lastRoom.numInhabitants();i++)
			{
				MOB mob=lastRoom.fetchInhabitant(i);
				if((mob!=null)&&(mob!=affected)&&(mob.charStats()!=null)&&(mob.charStats().getMyRace()!=null)&&(mob.charStats().getMyRace().name().equalsIgnoreCase("Undead")))
					mob.tell(mob.name()+" gives off a cold dark vibe.");
			}
		}
		return true;
	}
	


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting undead things.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"<S-NAME> gain(s) dark cold senses!":"<S-NAME> chant(s) for dark cold senses!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> open(s) <S-HER-HER> cold eyes, but the spell fizzles.");

		return success;
	}
}
