package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WallOfForce extends Spell
{
	private Item theWall=null;
	
	public Spell_WallOfForce()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wall of Force";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Wall of Force)";


		canAffectCode=Ability.CAN_ITEMS;
		canTargetCode=0;

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
		return new Spell_WallOfDarkness();
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
		&&((mob.getVictim()==invoker)||(mob==invoker))
		&&(mob.rangeToTarget()>=1)
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&((affect.targetCode()&Affect.MASK_MALICIOUS)>0))
		{
			if(((affect.tool()!=null)
				&&(affect.tool() instanceof Ability))
			||((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
				&&(affect.tool()!=null)
				&&(affect.tool() instanceof Weapon)
				&&(!((Weapon)affect.tool()).amWearingAt(Item.INVENTORY))
				&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)))
			{
				mob.tell("Malice neither escapes nor enters the wall of force.");
				if(mob.isMonster())
					ExternalPlay.remove(mob,(Weapon)affect.tool());
				return false;
			}
			if((affect.sourceMinor()==Affect.TYP_ADVANCE)
			&&((mob==invoker)||(mob.rangeToTarget()==1)))
			{
				if(mob!=invoker)
					mob.location().show(mob,null,Affect.MSG_WEAPONATTACK,"^F<S-NAME> attempt(s) to penetrate the wall of force and fail(s).^?");
				return false;
			}
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked)
		{
			if((theWall!=null)
			&&(invoker!=null)
			&&(theWall.owner()!=null)
			&&(theWall.owner() instanceof Room)
			&&(((Room)theWall.owner()).isContent(theWall)))
			{
				((Room)theWall.owner()).showHappens(Affect.MSG_OK_VISUAL,"The wall of force is gone.");
				Item wall=theWall;
				theWall=null;
				wall.destroyThis();
			}
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
				mob.tell("There is already a wall of darkness here.");
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

			FullMsg msg = new FullMsg(mob, target, this, affectType, auto?"An impenetrable wall of force appears!":"^S<S-NAME> conjur(s) up a impenetrable wall of force!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of force");
				I.setDisplayText("an impenetrable wall of force surrounds "+mob.name());
				I.setDescription("It`s tough, that's for sure.");
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