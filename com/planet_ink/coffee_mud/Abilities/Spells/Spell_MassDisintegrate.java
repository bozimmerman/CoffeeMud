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
		Hashtable h=properTargets(mob,givenTarget,auto);
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
			if(mob.location().show(mob,null,this,affectType(auto),auto?"Something is happening!":"^S<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a trecherous spell!^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						if(((MOB)target).curState().getHitPoints()>0)
							MUDFight.postDamage(mob,(MOB)target,this,(((MOB)target).curState().getHitPoints()*10),CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,("^SThe spell <DAMAGE> <T-NAME>!^?")+CommonStrings.msp("spelldam2.wav",40));
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
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" disintegrates!");
				I.destroy();
			}
		}
		else
			maliciousFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a treacherous but fizzled spell!");

		return success;
	}
}
