package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WizInvis extends Property
{
	public String ID() { return "Prop_WizInvis"; }
	public String displayText() {return "(Wizard Invisibility)";}
	public String name(){ return "Wizard Invisibility";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	boolean disabled=false;
	public Environmental newInstance(){	return new Prop_WizInvis();}

	public String accountForYourself()
	{ return "Wizard Invisibile";	}


	public boolean canBeUninvoked(){return true;}
	public boolean isAnAutoEffect(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_CLIMBING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
		if(!Sense.canBreathe(affected))
			affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_NOT_BREATHE);
		if(Sense.isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_SLEEPING);
		if(Sense.isSitting(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_SITTING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affected.curState().setHunger(affected.maxState().getHunger());
		affected.curState().setThirst(affected.maxState().getThirst());
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(affected==null) return;
		Environmental being=affected;

		if(this.canBeUninvoked())
		{
			being.delAffect(this);
			if(being instanceof Room)
				((Room)being).recoverRoomStats();
			else
			if(being instanceof MOB)
			{
				if(((MOB)being).location()!=null)
					((MOB)being).location().recoverRoomStats();
				else
				{
					being.recoverEnvStats();
					((MOB)being).recoverCharStats();
					((MOB)being).recoverMaxState();
				}
			}
			else
				being.recoverEnvStats();
			mob.tell("You begin to fade back into view.");
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)&&(affect.amITarget(affected))&&(affected!=null)&&(!disabled)))
		{
			affect.source().tell("Ah, leave "+affected.displayName()+" alone.");
			if(affected instanceof MOB)
				((MOB)affected).makePeace();
			return false;
		}
		else
		if((affected!=null)&&(affected instanceof MOB)&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))&&(affect.amISource((MOB)affected)))
			disabled=true;
		return super.okAffect(myHost,affect);
	}
}
