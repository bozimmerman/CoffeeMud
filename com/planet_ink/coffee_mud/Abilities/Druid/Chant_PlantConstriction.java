package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantConstriction extends Chant
{
	public String ID() { return "Chant_PlantConstriction"; }
	public String name(){return "Plant Constriction";}
	public String displayText(){return "(Plant Constriction)";}
	public int maxRange(){return 10;}
	public int minRange(){return 0;}
	public int quality(){ return MALICIOUS;}
	public boolean bubbleAffect(){return true;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){return new Chant_PlantConstriction();}

	public void unInvoke()
	{
		Item I=null;
		if(affected instanceof Item)
			I=(Item)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(I!=null)&&(I.owner() instanceof MOB)
		&&(!I.amWearingAt(Item.INVENTORY)))
		{
			MOB mob=(MOB)I.owner();
			if((mob.location()!=null)
			&&(!mob.amDead())
			&&(mob.location().isInhabitant(mob)))
			{
				mob.tell(I.name()+" loosens its grip on you and falls off.");
				I.setRawWornCode(0);
				mob.location().bringItemHere(I,Item.REFUSE_PLAYER_DROP);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		Item I=null;
		if(affected instanceof Item)
			I=(Item)affected;
		if((canBeUninvoked())&&(I!=null)&&(I.owner() instanceof MOB)
		&&(I.amWearingAt(Item.ON_LEGS)||I.amWearingAt(Item.ON_ARMS)))
		{
			MOB mob=(MOB)I.owner();
			if((mob.location()!=null)
			&&(!mob.amDead())
			&&(mob.isMonster())
			&&(mob.location().isInhabitant(mob)))
				CommonMsgs.remove(mob,I,false);
		}
		return super.tick(ticking,tickID);
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg)) return false;
		if((msg.targetMinor()==CMMsg.TYP_REMOVE)
		&&(msg.target()==affected)
		&&(affected instanceof Item)
		&&(((Item)affected).amWearingAt(Item.ON_LEGS)||((Item)affected).amWearingAt(Item.ON_ARMS)))
		{
			if(Dice.rollPercentage()>(msg.source().charStats().getStat(CharStats.STRENGTH)*4))
			{
				msg.source().location().show(msg.source(),affected,CMMsg.MSG_OK_VISUAL,"<S-NAME> struggle(s) to remove <T-NAME> and fail(s).");
				return false;
			}
		}
		return true;
	}
	
	public void affectEnvStats(Environmental aff, EnvStats affectableStats)
	{
		if((aff instanceof MOB)&&(affected instanceof Item)
		&&(((MOB)aff).isMine(affected))
		&&((Item)affected).amWearingAt(Item.ON_ARMS))
			affectableStats.setSpeed(affectableStats.speed()/2.0);
	}
	
	public void affectCharState(MOB aff, CharState affectableState)
	{
		if((affected instanceof Item)
		&&(aff.isMine(affected))
		&&((Item)affected).amWearingAt(Item.ON_LEGS))
			affectableState.setMovement(affectableState.getMovement()/2);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(myPlant==null)
		{
			if(auto)
				myPlant=Chant_SummonPlants.buildPlant(mob,mob.location());
			else
			{
				mob.tell("There doesn't appear to be any of your plants here to choke with.");
				return false;
			}
		}
		Vector positionChoices=new Vector();
		if(target.getWearPositions(Item.ON_ARMS)>0)
			positionChoices.addElement(new Long(Item.ON_ARMS));
		if(target.getWearPositions(Item.ON_LEGS)>0)
			positionChoices.addElement(new Long(Item.ON_LEGS));
		if(positionChoices.size()==0)
		{
			if(!auto)
				mob.tell("Ummm, "+target.name()+" doesn't have arms or legs to constrict...");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),auto?"":"^S<S-NAME> chant(s) at <T-NAME> while pointing at "+myPlant.name()+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.giveItem(myPlant);
				Long II=(Long)positionChoices.elementAt(Dice.roll(1,positionChoices.size(),-1));
				myPlant.setRawWornCode(II.longValue());
				if(II.longValue()==Item.ON_ARMS)
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,myPlant.name()+" jumps up and wraps itself around <S-YOUPOSS> arms!");
				else
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,myPlant.name()+" jumps up and wraps itself around <S-YOUPOSS> legs!");
				beneficialAffect(mob,myPlant,20);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, but the magic fizzles.");

		// return whether it worked
		return success;
	}
}