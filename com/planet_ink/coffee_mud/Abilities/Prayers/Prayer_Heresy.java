package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Heresy extends Prayer
{
	public String ID() { return "Prayer_Heresy"; }
	public String name(){return "Heresy";}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return 100;}
	public Environmental newInstance(){	return new Prayer_Heresy();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Vector Bs=Sense.flaggedBehaviors(mob.location().getArea(),Behavior.FLAG_LEGALBEHAVIOR);
		Behavior B=null;
		if((Bs!=null)&&(Bs.size()>0)) B=(Behavior)Bs.firstElement();
		
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		MOB oldVictim=mob.getVictim();
		if((success)&&(B!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> accuse(s) <T-NAMESELF> of heresy"+againstTheGods(mob)+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					MOB D=null;
					if(mob.getWorshipCharID().length()>0) D=CMMap.getDeity(mob.getWorshipCharID());
					if(D==null)
					{
						D=CMClass.getMOB("StdMOB");
						D.setName("the gods");
					}
					Vector V=new Vector();
					V.addElement(new Integer(Law.MOD_ADDWARRANT));
					V.addElement(D);//victim first
					V.addElement("");//crime locs
					V.addElement("");// crime flags
					V.addElement("heresy against <T-NAMESELF>");//the crime
					int low=CMAble.lowestQualifyingLevel(ID());
					int me=CMAble.qualifyingClassLevel(mob,this);
					int lvl=(low-me)/5;
					if(lvl>Law.ACTION_HIGHEST) lvl=Law.ACTION_HIGHEST;
					V.addElement(Law.ACTION_DESCS[lvl]);//sentence
					V.addElement("Angering "+D.name()+" will bring doom upon us all!");
					B.modifyBehavior(mob.location().getArea(),target,V);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> accuse(s) <T-NAMESELF> of heresy"+againstTheGods(mob)+", but nothing happens.");
		mob.setVictim(oldVictim);
		if(oldVictim==null) mob.makePeace();

		// return whether it worked
		return success;
	}
}