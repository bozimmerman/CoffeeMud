package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Spell_MassDisintegrate extends Spell
{
	public String ID() { return "Spell_MassDisintegrate"; }
	public String name(){return "Mass Disintegrate";}
	public int maxRange(){return 2;}
	public int quality(){return MALICIOUS;};
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	public int overrideMana(){return 200;}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()<0))
		{
			if(mob.location().numItems()==0)
			{
				mob.tell("There doesn't appear to be anyone here worth disintgrating.");
				return false;
			}
			else
				h=new HashSet();
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int avgLevel=0;
		for(Iterator e=h.iterator();e.hasNext();)
		{
			MOB mob2=(MOB)e.next();
			avgLevel+=mob2.envStats().level();
		}
		if(h.size()>0)
			avgLevel=avgLevel/h.size();
		
		int levelDiff=avgLevel-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		
		boolean success=false;
		success=profficiencyCheck(mob,-(avgLevel*25),auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"Something is happening!":"^S<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a trecherous spell!^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();
				if((target.envStats().level()/avgLevel)<2)
				{
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							if(target.curState().getHitPoints()>0)
								MUDFight.postDamage(mob,target,this,target.curState().getHitPoints()*10,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,("^SThe spell <DAMAGE> <T-NAME>!^?")+CommonStrings.msp("spelldam2.wav",40));
							if(!target.amDead())
								return false;
						}
					}
				}
			}
			mob.location().recoverRoomStats();
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
				if((!(I instanceof DeadBody))
				||(!((DeadBody)I).playerCorpse())
				||(((DeadBody)I).mobName().equals(mob.Name())))
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" disintegrates!");
					I.destroy();
				}
			}
			mob.location().recoverRoomStats();
		}
		else
			maliciousFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a treacherous but fizzled spell!");

		return success;
	}
}
