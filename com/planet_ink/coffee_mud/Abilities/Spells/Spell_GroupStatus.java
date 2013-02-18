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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_GroupStatus extends Spell
{
	public String ID() { return "Spell_GroupStatus"; }
	public String name(){return "Group Status";}
	public String displayText(){return "(Group Status)";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	protected List<Pair<MOB,Ability>> groupMembers=null;
	
	protected HashSet<String> reporteds=new HashSet<String>();
	protected HashSet<String> affects=new HashSet<String>();

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			MOB mob=(MOB)ticking;
			if(((MOB)ticking)==invoker())
			{
				if(groupMembers==null)
				{
					groupMembers=new SLinkedList<Pair<MOB,Ability>>();
					Set<MOB> grp=mob.getGroupMembers(new TreeSet<MOB>());
					for(MOB M : grp)
					{
						Pair<MOB,Ability> P=new Pair<MOB,Ability>(M,null);
						groupMembers.add(P);
					}
				}
				for(Pair<MOB,Ability> P : groupMembers)
				{
					if(P.second==null)
					{
						P.second=CMClass.getAbility(ID());
						if(P.second!=null)
						{
							P.second.setName(text());
							P.second.setStat("TICKDOWN", Integer.toString(tickDown));
							P.first.addEffect(P.second);
							P.second.setInvoker(mob);
						}
					}
				}
			}
			else
			if(invoker()!=null)
			{
				if((mob.curState().getHitPoints()<5)
				||(mob.curState().getHitPoints()<mob.getWimpHitPoint())
				||(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints())<=0.15))
				{
					if(!reporteds.contains("LOWHITPOINTS"))
					{
						invoker().tell(mob.Name()+" is low on hit points.");
						reporteds.add("LOWHITPOINTS");
					}
				}
				else
					reporteds.remove("LOWHITPOINTS");
				
				
				for(final Iterator<String> i=affects.iterator();i.hasNext();)
				{
					final String affectName=i.next();
					if(mob.fetchEffect(affectName)==null)
						i.remove();
				}
				for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A.abstractQuality() == Ability.QUALITY_MALICIOUS)
					&&(!affects.contains(A.ID())))
					{
						affects.add(A.ID());
						invoker().tell(mob.Name()+" is now affected by "+A.name()+".");
					}
				}
			}
		}
		return true;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(affected != invoker()))
		{
			if(!reporteds.contains("DEATH"))
			{
				invoker().tell(affected.Name()+" is dying.");
				reporteds.add("DEATH");
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		MOB mob=(MOB)affected;

		if(canBeUninvoked())
		{
			if(groupMembers!=null)
				for(final Pair<MOB,Ability> Gs : groupMembers)
				{
					final Ability A=Gs.second;
					if((A!=null)&&(A.invoker()==mob))
						A.unInvoke();
				}
			groupMembers=null;
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already knowledgable about <S-HIS-HER> group.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> point(s) at <S-HIS-HER> group members and knowingly cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <S-HIS-HER> group members speak(s) knowingly, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
