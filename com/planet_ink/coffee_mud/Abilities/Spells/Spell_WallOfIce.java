package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WallOfIce extends Spell
{
	public String ID() { return "Spell_WallOfIce"; }
	public String name(){return "Wall of Ice";}
	public String displayText(){return "(Wall of Ice)";}
	public int maxRange(){return 10;}
	public int minRange(){return 1;}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_WallOfIce();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	private int amountRemaining=0;
	private Item theWall=null;
	private String deathNotice="";

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
				Room room=mob.location();
				room.show(mob,null,Affect.MSG_WEAPONATTACK,"^F<S-NAME> hack(s) at the wall of ice with "+w.name()+".^?");
				amountRemaining-=mob.envStats().damage();
				if(amountRemaining<0)
				{
					deathNotice="The wall of ice shatters!!!";
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB M=room.fetchInhabitant(i);
						if((M.isInCombat())
						&&(M.getVictim()==invoker)
						&&(M.rangeToTarget()>0)
						&&(M.rangeToTarget()<3)
						&&(!M.amDead()))
							ExternalPlay.postDamage(invoker,M,this,Dice.roll(M.envStats().level()/2,6,0),Affect.MSG_OK_VISUAL,Weapon.TYPE_PIERCING,"A shard of ice <DAMAGE> <T-NAME>!");
					}
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
				mob.tell("There is already a wall of ice here.");
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

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),auto?"A mighty wall of ice appears!":"^S<S-NAME> conjur(s) up a mighty wall of ice!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				amountRemaining=20;
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of ice");
				I.setDisplayText("a mighty wall of ice has been erected here");
				I.setDescription("The ice is crystal clear.");
				I.setMaterial(EnvResource.RESOURCE_GLASS);
				I.setGettable(false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				deathNotice="The wall of ice melts.";
				beneficialAffect(mob,I,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}