package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MirrorImage extends Spell
{
	public String ID() { return "Spell_MirrorImage"; }
	public String name(){return "Mirror Image";}
	public String displayText(){return "(Mirror Image spell)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_MirrorImage();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	private	Random randomizer = new Random(System.currentTimeMillis());
	private int numberOfImages = 0;
	private boolean notAgain=false;


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((affect.amITarget(mob))&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK))
		{
			if(invoker()!=null)
			{
				if(numberOfImages <= 0)
				{
					unInvoke();
					return true;
				}
				int numberOfTargets = numberOfImages + 1;
				if(randomizer.nextInt() % numberOfTargets == 0)
				{
					mob.location().show(mob,affect.source(),Affect.MSG_OK_ACTION,"<T-NAME> attack(s) a mirrored image!");
					numberOfImages--;
					return false;
				}
			}
		}
		return true;
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		if(notAgain) return;

		MOB mob=(MOB)affected;
		if(affect.amISource(mob))
		{
			if((
				(Util.bset(affect.othersCode(),Affect.MASK_EYES))
				||(Util.bset(affect.othersCode(),Affect.MASK_MOVE))
				||(Util.bset(affect.othersCode(),Affect.MASK_MOUTH))
				||(Util.bset(affect.othersCode(),Affect.MASK_HANDS)))
			&&(affect.othersMessage()!=null)
			&&((affect.targetCode()&Affect.MASK_HURT)==0)
			&&(affect.othersMessage().length()>0))
			{
				notAgain=true;
				if(numberOfImages<=0)
					unInvoke();
				else
				for(int x=0;x<numberOfImages;x++)
					affect.addTrailerMsg(new FullMsg(mob,affect.target(),Affect.MSG_OK_VISUAL,affect.othersMessage()));
			}
		}
		notAgain=false;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 10);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			numberOfImages=0;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your mirror images fade away.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already have mirror images.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			numberOfImages = Dice.roll(1,(int)(Math.round(Util.div(adjustedLevel(mob),3.0))),2);
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A spell forms around":"^S<S-NAME> incant(s) the reflective spell of")+" <T-NAME>, and suddenly " + numberOfImages + " copies appear.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
		{
			numberOfImages = 0;
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) reflectively, but nothing more happens.");
		}
		// return whether it worked
		return success;
	}
}
