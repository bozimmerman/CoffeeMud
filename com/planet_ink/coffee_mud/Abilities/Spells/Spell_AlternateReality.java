package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_AlternateReality extends Spell
{
	public String ID() { return "Spell_AlternateReality"; }
	public String name(){return "Alternate Reality";}
	public String displayText(){return "(Alternate Reality spell)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_AlternateReality();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your reality returns to normal.");
	}

	public boolean okAffect(Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&((affect.amISource((MOB)affected)))
		&&(affect.target()!=null)
		&&(invoker()!=null))
		{
			Hashtable H=invoker().getGroupMembers(new Hashtable());
			if(H.contains(affect.target()))
			{
				affect.source().tell("But you are on "+invoker().name()+"'s side!");
				if(invoker().getVictim()!=affected)
					((MOB)affected).setVictim(invoker().getVictim());
				return false;
			}
		}
		return super.okAffect(affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if(target.getVictim()!=mob)
		{
			mob.tell("But "+target.charStats().heshe()+" isn't fighting you!");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) to <T-NAME>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0));
					if(success)
					{
						Room R=target.location();
						R.show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> change(s) sides!");
						target.makePeace();
						if(mob.getVictim()==target)
							mob.setVictim(null);
						Hashtable H=mob.getGroupMembers(new Hashtable());
						if(!H.contains(mob))H.put(mob,mob);
						Vector badGuys=new Vector();
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)&&(M!=mob)&&(M!=target))
							{
								if(!H.contains(M))
								{
									if(M.getVictim()==mob)
									{
										badGuys.clear();
										badGuys.addElement(M);
										break;
									}
									badGuys.addElement(M);
								}
								else
								if(M.getVictim()==target)
									M.setVictim(null);
							}
						}
						if(badGuys.size()>0)
							target.setVictim((MOB)badGuys.elementAt(Dice.roll(1,badGuys.size(),-1)));
						if(mob.getVictim()==null)
							mob.setVictim((MOB)badGuys.elementAt(Dice.roll(1,badGuys.size(),-1)));
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) to <T-NAME>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
