package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WallOfFire extends Spell
{
	private Item theWall=null;
	private String deathNotice="";

	public Spell_WallOfFire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wall of Fire";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Wall of Fire)";


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
		return new Spell_WallOfFire();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null))
			{
				Room room=invoker.location();
				if(!invoker.location().isContent(theWall))
					unInvoke();
				else
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mob=room.fetchInhabitant(m);
					if((mob!=null)
					&&(mob!=invoker)
					&&(mob.isInCombat())
					&&(mob.getVictim()==invoker)
					&&(mob.rangeToTarget()==1))
					{
						int damage = Dice.roll((int)Math.round(new Integer(invoker.envStats().level()).doubleValue()/4.0),6,1);
						ExternalPlay.postDamage(invoker,mob,this,damage,Affect.ACT_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The wall of fire flares and <DAMAGE> <T-NAME>!");
					}
				}
			}
		}
		return super.tick(tickID);
	}
	
	
	public void unInvoke()
	{
		super.unInvoke();
		if((theWall!=null)
		&&(invoker!=null)
		&&(theWall.owner()!=null)
		&&(theWall.owner() instanceof Room)
		&&(((Room)theWall.owner()).isContent(theWall)))
		{
			((Room)theWall.owner()).show(invoker,null,Affect.MSG_OK_VISUAL,deathNotice);
			Item wall=theWall;
			theWall=null;
			wall.destroyThis();
		}
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
				mob.tell("There is already a wall of fire here.");
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

			FullMsg msg = new FullMsg(mob, target, this, affectType, auto?"A blazing wall of fire appears!":"<S-NAME> chant(s) and conjur(s) up a blazing wall of fire!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of fire");
				I.setDisplayText("a blazing wall of fire is burning here");
				I.setDescription("The flames are high and hot.");
				I.setMaterial(EnvResource.RESOURCE_NOTHING);
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_LIGHT);
				I.setGettable(false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				deathNotice="The wall of fire flickers...";
				beneficialAffect(mob,I,20);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but the conjuration fizzles.");

		// return whether it worked
		return success;
	}
}