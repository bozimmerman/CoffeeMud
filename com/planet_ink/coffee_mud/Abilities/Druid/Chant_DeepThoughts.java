package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Chant_DeepThoughts extends Chant
{
	public String ID() { return "Chant_DeepThoughts"; }
	public String name(){ return "Deep Thoughts";}
	public String displayText(){return "(Thinking deep thoughts)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	long lastTime=0;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> end(s) <S-HIS-HER> deep thinking.");
				else
					mob.tell("You stop thinking deeply.");
			}
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.tool()!=this)
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
		&&((CMath.bset(msg.sourceCode(),CMMsg.MASK_MOVE))
				||(CMath.bset(msg.sourceCode(),CMMsg.MASK_HANDS))
				||(CMath.bset(msg.sourceCode(),CMMsg.MASK_MOUTH))
				||(CMath.bset(msg.sourceCode(),CMMsg.MASK_EYES))))
			unInvoke();
		return;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;

		if(tickID!=Tickable.TICKID_MOB) return true;
		if(!mob.isInCombat())
		{
			if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
			{
				unInvoke();
				return false;
			}
			if((System.currentTimeMillis()-lastTime)<60000) 
				return true;
			if(!proficiencyCheck(null,0,false)) 
				return true;
			lastTime=System.currentTimeMillis();
			Room room=mob.location();
			int myAlignment=mob.fetchFaction(CMLib.factions().AlignID());
			int total=CMLib.factions().getTotal(CMLib.factions().AlignID());
			int ratePct=(int)Math.round(CMath.mul(total,.01));
			if(CMLib.factions().getAlignPurity(myAlignment,Faction.ALIGN_INDIFF)<99)
			{
				if(CMLib.factions().getAlignPurity(myAlignment,Faction.ALIGN_EVIL)<CMLib.factions().getAlignPurity(myAlignment,Faction.ALIGN_GOOD))
					CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), ratePct);
				else
					CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), -ratePct);
				switch(CMLib.dice().roll(1,10,0))
				{
				case 0: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> contemplate(s) the great depths."); break;
				case 1: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> listen(s) to the wisdom of the stones."); break;
				case 2: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) at a single stone."); break;
				case 3: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> mentally embrace(s) the earth."); break;
				case 4: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> inhale(s) wisdom and dust."); break;
				case 5: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> watch(es) the unchanging stones and feel(s) their history."); break;
				case 6: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> become(s) one with the earth."); break;
				case 7: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> feel(s) the inner warmth of the great deep."); break;
				case 8: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> expunge(s) <S-HIS-HER> unnatural thoughts."); break;
				case 9: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> find(s) clarity in the stones."); break;
				}
			}
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("You can't think deeply while in combat!");
			return false;
		}
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("You must be in a cave for this chant to work.");
			return false;
		}
		
		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),"<S-NAME> grow(s) very still and begin(s) to think deep thoughts...");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE-1000);
				helpProficiency(mob);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> grow(s) very still, but lose(s) concentration.");

		// return whether it worked
		return success;
	}
}
