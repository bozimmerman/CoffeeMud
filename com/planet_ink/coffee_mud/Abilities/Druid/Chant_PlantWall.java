package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantWall extends Chant
{
	public String ID() { return "Chant_PlantWall"; }
	public String name(){return "Plant Wall";}
	public String displayText(){return "(Plant Wall)";}
	public int maxRange(){return 10;}
	public int minRange(){return 1;}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){return new Chant_PlantWall();}

	private int amountRemaining=0;
	private Item theWall=null;
	private String deathNotice="";

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		MOB mob=affect.source();

		if((invoker!=null)
		&&(mob.isInCombat())
		&&(mob!=invoker)
		&&(mob.getVictim()==invoker))
		{
			if((affect.targetMinor()==Affect.TYP_WEAPONATTACK)
			&&(mob.rangeToTarget()>0)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon)
			&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			&&(affect.tool().maxRange()>0))
			{
				mob.location().show(mob,null,Affect.MSG_WEAPONATTACK,"^F<S-NAME> fire(s) at the plant wall with "+affect.tool().name()+".^?");
				amountRemaining-=mob.envStats().damage();
				if(amountRemaining<0)
				{
					deathNotice="The plant wall is destroyed!";
					((Item)affected).destroyThis();
				}
				return false;
			}
			else
			if((mob.rangeToTarget()==1)&&(affect.sourceMinor()==Affect.TYP_ADVANCE))
			{
				Item w=mob.fetchWieldedItem();
				if(w==null) w=mob.myNaturalWeapon();
				if(w==null) return false;
				mob.location().show(mob,null,Affect.MSG_WEAPONATTACK,"^F<S-NAME> hack(s) at the wall of stone with "+w.name()+".^?");
				amountRemaining-=mob.envStats().damage();
				if(amountRemaining<0)
				{
					deathNotice="The plant wall is destroyed!";
					((Item)affected).destroyThis();
				}
				return false;
			}
			else
			if((mob.rangeToTarget()>0)
			&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(affect.tool().maxRange()>0))
			{
				mob.location().show(mob,null,affect.tool(),Affect.MSG_OK_VISUAL,"^FThe plant wall absorbs <O-NAME> from <S-NAME>.^?");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
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
				((Room)theWall.owner()).show(invoker,null,Affect.MSG_OK_VISUAL,deathNotice);
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
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell("You really should be in ranged combat to use this chant.");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I.fetchAffect(ID())!=null))
			{
				mob.tell("There is already a plant wall here.");
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

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),auto?"A plant wall appears!":"^S<S-NAME> chant(s) for a plant wall!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				amountRemaining=mob.baseState().getHitPoints()/6;
				Item I=CMClass.getItem("GenItem");
				I.setName("a plant wall");
				I.setDisplayText("a writhing plant wall has grown here");
				I.setDescription("The wall is thick and stringy.");
				I.setMaterial(EnvResource.RESOURCE_GREENS);
				I.setGettable(false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				deathNotice="The plant wall withers away!";
				beneficialAffect(mob,I,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}