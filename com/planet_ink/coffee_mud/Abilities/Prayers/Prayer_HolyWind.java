package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_HolyWind extends Prayer
{
	public String ID() { return "Prayer_HolyWind"; }
	public String name(){ return "Holy Wind";}
	public String displayText(){return "(Blown Down)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int maxRange(){return 4;}
	public boolean doneTicking=false;
	public Environmental newInstance(){	return new Prayer_HolyWind();}
	public long flags(){return Ability.FLAG_MOVING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(affect.amISource(mob)))
			unInvoke();
		else
		if(affect.amISource(mob)&&(affect.sourceMinor()==Affect.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(mob.location()!=null)
			{
				FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet.");
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					ExternalPlay.standIfNecessary(mob);
				}
			}
			else
				mob.tell("You regain your feet.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell("There doesn't appear to be anyone here worth blowing around.");
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
			if(mob.location().show(mob,null,this,affectType(auto),auto?"A horrendous wind gust blows through here.":"^S<S-NAME> "+prayWord(mob)+" for the holy wind to blow through here.^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"<T-NAME> get(s) blown back!");
				if((mob.location().okAffect(mob,msg))&&(target.fetchAffect(this.ID())==null))
				{
					if((!msg.wasModified())&&(target.location()==mob.location()))
					{
						int howLong=2;
						if((mob.location().getArea().weatherType(mob.location())==Area.WEATHER_WINDY)
						||(mob.location().getArea().weatherType(mob.location())==Area.WEATHER_DUSTSTORM)
						||(mob.location().getArea().weatherType(mob.location())==Area.WEATHER_THUNDERSTORM))
							howLong=4;

						MOB victim=target.getVictim();
						if((victim!=null)&&(target.rangeToTarget()>=0))
							target.setAtRange(target.rangeToTarget()+(howLong/2));
						if(target.rangeToTarget()>target.location().maxRange())
							target.setAtRange(target.location().maxRange());
						mob.location().send(mob,msg);
						if((!Sense.isFlying(target))
						&&(Dice.rollPercentage()>(((target.charStats().getStat(CharStats.DEXTERITY)*2)+target.envStats().level()))-(5*howLong))
						&&(target.charStats().getMyRace().bodyMask()[Race.BODY_LEG]>0))
						{
							mob.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> fall(s) down!");
							doneTicking=false;
							success=maliciousAffect(mob,target,howLong,-1);
						}
						if(target.getVictim()!=null)
							target.getVictim().setAtRange(target.rangeToTarget());
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}