package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Spell_MassDisintegrate extends Spell
{
	@Override public String ID() { return "Spell_MassDisintegrate"; }
	@Override public String name(){return "Mass Disintegrate";}
	@Override public int maxRange(){return adjustedMaxInvokerRange(2);}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	@Override public int overrideMana(){return 200;}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()<0))
		{
			if(mob.location().numItems()==0)
			{
				mob.tell("There doesn't appear to be anyone here worth disintgrating.");
				return false;
			}
			h=new HashSet();
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int avgLevel=0;
		for (final Object element : h)
		{
			final MOB mob2=(MOB)element;
			avgLevel+=mob2.phyStats().level();
		}
		if(h.size()>0)
			avgLevel=avgLevel/h.size();

		int levelDiff=avgLevel-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;

		boolean success=false;
		success=proficiencyCheck(mob,-(levelDiff*25),auto);

		if(success)
		{
			if(avgLevel <= 0)
				avgLevel = 1;
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"Something is happening!":"^S<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a trecherous spell!^?"))
				for (final Object element : h)
				{
					final MOB target=(MOB)element;
					if((target.phyStats().level()/avgLevel)<2)
					{
						final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							if(msg.value()<=0)
							{
								if(target.curState().getHitPoints()>0)
									CMLib.combat().postDamage(mob,target,this,target.curState().getHitPoints()*100,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,("^SThe spell <DAMAGE> <T-NAME>!^?")+CMLib.protocol().msp("spelldam2.wav",40));
							}
						}
					}
				}
			mob.location().recoverRoomStats();
			final Vector V=new Vector();
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				final Item I=mob.location().getItem(i);
				if((I!=null)&&(I.container()==null))
				{
					final List<DeadBody> DBs=CMLib.utensils().getDeadBodies(I);
					boolean ok=true;
					for(final DeadBody DB : DBs)
					{
						if(DB.playerCorpse()
						&&(!((DeadBody)I).mobName().equals(mob.Name())))
							ok=false;
					}
					if(ok) V.addElement(I);
				}
			}
			for(int i=0;i<V.size();i++)
			{
				final Item I=(Item)V.elementAt(i);
				if((!(I instanceof DeadBody))
				||(!((DeadBody)I).playerCorpse())
				||(((DeadBody)I).mobName().equals(mob.Name())))
				{
					final CMMsg msg=CMClass.getMsg(mob,I,this,verbalCastCode(mob,I,auto),I.name()+" disintegrates!");
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
							I.destroy();
					}
				}
			}
			mob.location().recoverRoomStats();
		}
		else
			maliciousFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and utter(s) a treacherous but fizzled spell!");

		return success;
	}
}
