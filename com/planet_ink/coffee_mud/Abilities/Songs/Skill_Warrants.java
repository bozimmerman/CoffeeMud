package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Skill_Warrants extends BardSkill
{
	public String ID() { return "Skill_Warrants"; }
	public String name(){ return "Warrants";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"WARRANTS"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL; }
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        LegalBehavior B=null;
		if(mob.location()!=null) B=CMLib.law().getLegalBehavior(mob.location());
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,(-25+mob.charStats().getStat(CharStats.STAT_CHARISMA)+(2*getXLEVELLevel(mob))),auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Vector V=new Vector();
				if(B!=null)
                    V=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),(MOB)null);
				if(V.size()==0)
				{
					mob.tell("No one is wanted for anything here.");
					return false;
				}
				StringBuffer buf=new StringBuffer("");
				buf.append(CMStrings.padRight("Name",14)+" "+CMStrings.padRight("Victim",14)+" "+CMStrings.padRight("Witness",14)+" Crime\n\r");
				for(int v=0;v<V.size();v++)
				{
                    LegalWarrant W=(LegalWarrant)V.elementAt(v);
                    buf.append(CMStrings.padRight(W.criminal().Name(),14)+" ");
					buf.append(CMStrings.padRight(W.victim()!=null?W.victim().Name():"N/A",14)+" ");
					buf.append(CMStrings.padRight(W.witness()!=null?W.witness().Name():"N/A",14)+" ");
					buf.append(CMLib.coffeeFilter().fullOutFilter(mob.session(),mob,W.criminal(),W.victim(),null,W.crime(),false)+"\n\r");
				}
				if(!mob.isMonster()) mob.session().rawPrintln(buf.toString());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to gather warrant information, but fail(s).");

		return success;
	}

}
