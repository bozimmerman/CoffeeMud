package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Thief_SlipperyMind extends ThiefSkill
{
	public String ID() { return "Thief_SlipperyMind"; }
	public String name(){ return "Slippery Mind";}
	public String displayText(){return "(Slippery Mind)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"SLIPPERYMIND"};
	public String[] triggerStrings(){return triggerStrings;}
	DVector oldFactions=null;
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if(unInvoked) return false;
		if((affected!=null)&&(affected instanceof MOB)&&(ticking instanceof MOB))
        {
			if(!super.tick(ticking,tickID)) 
				return false;
			MOB mob=(MOB)affected;
			Faction F=null;
			if(oldFactions==null)
			{
				oldFactions=new DVector(2);
		        for(Enumeration e=mob.fetchFactions();e.hasMoreElements();) 
				{
		            F=CMLib.factions().getFaction((String)e.nextElement());
		            if(F!=null)
		            {
		            	oldFactions.addElement(F,Integer.valueOf(mob.fetchFaction(F.factionID())));
		            	mob.addFaction(F.factionID(),F.middle());
		            }
				}
			}
			else
			for(int f=0;f<oldFactions.size();f++)
			{
				F=(Faction)oldFactions.elementAt(f,1);
				if(mob.fetchFaction(F.factionID())!=F.middle())
					mob.addFaction(F.factionID(),F.middle());
			}
        }
		return true;
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(super.canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(msg.amISource((MOB)affected))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
				unInvoke();
			else
			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				unInvoke();
		}
	}
	
	public void unInvoke()
	{
		Environmental E=affected;
		super.unInvoke();
		if((E instanceof MOB)&&(oldFactions!=null))
		{
			if(!((MOB)E).amDead())
				((MOB)E).tell("You've lost your slippery mind concentration.");
			for(int f=0;f<oldFactions.size();f++)
				((MOB)E).addFaction(((Faction)oldFactions.elementAt(f,1)).factionID(),((Integer)oldFactions.elementAt(f,2)).intValue());
			oldFactions=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> a slippery mind.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"<T-NAME> gain(s) a slippery mind.":"<S-NAME> wink(s) and nod(s).");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> wink(s) and nod(s), but <S-IS-ARE>n't fooling anyone.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			Ability A=target.fetchEffect(ID());
			if(A!=null)
				A.tick(target,Tickable.TICKID_MOB);
		}
		return success;
	}
}