package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WallOfAir extends Spell
{
	public String ID() { return "Spell_WallOfAir"; }
	public String name(){return "Wall of Air";}
	public String displayText(){return "(Wall of Air)";}
	public int maxRange(){return 10;}
	public int minRange(){return 1;}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_WallOfAir();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	private Item theWall=null;

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
			ExternalPlay.postWeaponDamage(M,mob,(Weapon)affect.tool(),true);
			M.setLocation(null);
			M.setVictim(null);
			if(mob.isMonster())
				ExternalPlay.remove(mob,(Item)affect.tool(),false);
			return false;
		}

		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((theWall!=null)
			&&(invoker!=null)
			&&(theWall.owner()!=null)
			&&(theWall.owner() instanceof Room)
			&&(((Room)theWall.owner()).isContent(theWall)))
			{
				((Room)theWall.owner()).showHappens(Affect.MSG_OK_VISUAL,"The wall of air dissipates.");
				Item wall=theWall;
				theWall=null;
				wall.destroyThis();
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null)
			   &&(!invoker.location().isContent(theWall)))
				unInvoke();
		}
		return super.tick(ticking,tickID);
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

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),auto?"An swirling wall of air appears!":"^S<S-NAME> conjur(s) up a swirling wall of air!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of air");
				I.setDisplayText("");
				I.setDescription("The air is swirling dangerously.");
				I.setMaterial(EnvResource.RESOURCE_NOTHING);
				I.setGettable(false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				beneficialAffect(mob,I,10);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}