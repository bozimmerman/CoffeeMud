package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WallOfStone extends Spell
{

	private int amountRemaining=0;
	private Item theWall=null;
	private String deathNotice="";

	public Spell_WallOfStone()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wall of Stone";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Wall of Stone)";


		quality=Ability.INDIFFERENT;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=1;
		maxRange=3;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WallOfStone();
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
		&&(mob.rangeToTarget()==1))
		{
			if(affect.sourceMinor()==Affect.TYP_ADVANCE)
			{
				Item w=mob.fetchWieldedItem();
				if(w==null) w=mob.myNaturalWeapon();
				if(w==null) return false;
				mob.location().show(mob,null,Affect.MSG_WEAPONATTACK,"<S-NAME> hack(s) at the wall of stone with "+w.name()+".");
				amountRemaining-=mob.envStats().damage();
				if(amountRemaining<0) 
				{
					deathNotice="The wall of stone is destroyed!";
					((Item)affected).destroyThis();
				}
				return false;
			}
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
			((Room)theWall.myOwner()).show(invoker,null,Affect.MSG_OK_VISUAL,deathNotice);
			Item wall=theWall;
			theWall=null;
			wall.destroyThis();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You really should be in ranged combat to cast this.");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I.fetchAffect(ID())!=null))
			{
				mob.tell("There is already a wall of stone here.");
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

			FullMsg msg = new FullMsg(mob, target, this, affectType, auto?"A mighty wall of stone appears!":"<S-NAME> chant(s) and conjur(s) up a mighty wall of stone!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				amountRemaining=mob.baseState().getHitPoints()/6;
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of stone");
				I.setDisplayText("a mighty wall of stone has been erected here");
				I.setDescription("The bricks are sold and sturdy.");
				I.setMaterial(Item.ROCK);
				I.setGettable(false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				deathNotice="The wall of stone vanishes!";
				beneficialAffect(mob,I,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but the conjuration fizzles.");

		// return whether it worked
		return success;
	}
}