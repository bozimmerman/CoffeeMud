package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MassDisintegrate extends Spell
{
	public String ID() { return "Spell_MassDisintegrate"; }
	public String name(){return "Mass Disintegrate";}
	public int maxRange(){return 2;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_MassDisintegrate();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if((h==null)||(h.size()<0))
		{
			if(mob.location().numItems()==0)
			{
				mob.tell("There doesn't appear to be anyone here worth disintgrating.");
				return false;
			}
			else
				h=new Hashtable();
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int avgLevel=0;
		for(Enumeration e=h.elements();e.hasMoreElements();)
		{
			MOB mob2=(MOB)e.nextElement();
			avgLevel+=mob2.envStats().level();
		}
		if(h.size()>0)
			avgLevel=avgLevel/h.size();
		
		boolean success=false;
		success=profficiencyCheck(-(avgLevel*2),auto);

		if(success)
		{
			mob.location().show(mob,null,affectType(auto),auto?"Something is happening!":"^S<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a trecherous spell!^?");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						if(((MOB)target).curState().getHitPoints()>0)
							ExternalPlay.postDamage(mob,(MOB)target,this,(((MOB)target).curState().getHitPoints()*10),Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe spell <DAMAGE> <T-NAME>!^?");
						if(!((MOB)target).amDead())
							return false;
						mob.location().recoverRoomStats();
					}
				}
			}
			Vector V=new Vector();
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				Item I=mob.location().fetchItem(i);
				if((I!=null)&&(I.container()==null))
					V.addElement(I);
			}
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				mob.location().showHappens(Affect.MSG_OK_VISUAL,I.name()+" disintegrates!");
				I.destroyThis();
			}
		}
		else
			maliciousFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a treacherous but fizzled spell!");

		return success;
	}
}
