package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WallOfAir extends Spell
{
	private Item theWall=null;
	
	public Spell_WallOfAir()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wall of Air";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Wall of Air)";


		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=1;
		maxRange=10;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WallOfAir();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		MOB mob=affect.source();

		if((invoker!=null)
		&&(mob.isInCombat())
		&&(mob.getVictim()==invoker)
		&&(mob.rangeToTarget()>=1)
		&&(affect.amITarget(invoker))
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(!((Weapon)affect.tool()).amWearingAt(Item.INVENTORY))
		&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED))
		{
			mob.location().show(mob,invoker,Affect.MSG_OK_VISUAL,"<S-NAME> fire(s) "+affect.tool().name()+" at <T-NAME>.  The missile enters the wall of air.");
			MOB M=CMClass.getMOB("StdMOB");
			M.setLocation(mob.location());
			M.setName("The wall of air");
			M.setVictim(mob);
			M.setAtRange(mob.rangeToTarget());
			ExternalPlay.strike(M,mob,(Weapon)affect.tool(),true);
			M.setLocation(null);
			M.setVictim(null);
			if(mob.isMonster())
				ExternalPlay.remove(mob,(Item)affect.tool());
			return false;
		}
			
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		super.unInvoke();
		if((theWall!=null)
		&&(invoker!=null)
		&&(theWall.myOwner()!=null)
		&&(theWall.myOwner() instanceof Room)
		&&(((Room)theWall.myOwner()).isContent(theWall)))
		{
			((Room)theWall.myOwner()).show(invoker,null,Affect.MSG_OK_VISUAL,"The wall of air dissipates.");
			Item wall=theWall;
			theWall=null;
			wall.destroyThis();
		}
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null)
			   &&(!invoker.location().isContent(theWall)))
				unInvoke();
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell("You really should be in ranged combat to cast this.");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I.fetchAffect(ID())!=null))
			{
				mob.tell("There is already a wall of air here.");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType, auto?"An swirling wall of air appears!":"<S-NAME> chant(s) and conjur(s) up a swirling wall of air!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of air");
				I.setDisplayText("");
				I.setDescription("The air is swirling dangerously.");
				I.setMaterial(Item.GLASS);
				I.setGettable(false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				beneficialAffect(mob,I,10);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but the conjuration fizzles.");

		// return whether it worked
		return success;
	}
}