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
   Copyright 2000-2006 Bo Zimmerman

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
public class Thief_UndergroundConnections extends ThiefSkill
{
	public String ID() { return "Thief_UndergroundConnections"; }
	public String name(){ return "Underground Connections";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"UNDERGROUND"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	Vector pathOut=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		
		return super.tick(ticking,tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.isInCombat())
		{
			mob.tell(target,null,null,"Not while <S-NAME> <S-IS-ARE> fighting.");
			return false;
		}
		Area A=CMLib.map().areaLocation(target);
		int other=0;
		int streets=0;
		int buildings=0;
		Room R=null;
		for(Enumeration e=A.getCompleteMap();e.hasMoreElements();)
		{
			R=(Room)e.nextElement();
			if(R.roomID().length()==0) continue;
			if(R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
				streets++;
			else
			if((R.domainType()==Room.DOMAIN_INDOORS_METAL)
			||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
			||(R.domainType()==Room.DOMAIN_INDOORS_WOOD))
				buildings++;
			else
				other++;
		}
		if((CMLib.utensils().getLegalBehavior(A)==null)
		&&((streets<(other/2))||((streets+buildings)<other))
		&&(!auto))
		{
			mob.tell("You can only use this skill in cities.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":"<S-NAME> contact(s) <S-HIS-HER> underground connections here.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> can't seem to contact <S-HIS-HER> underground connections here.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			Thief_UndergroundConnections underA=(Thief_UndergroundConnections)target.fetchEffect(ID());
			if(underA!=null)
			{
				Vector trail=new Vector();
				trail=CMLib.tracking().getRadiantRooms(mob.location(),false,false,true,true,true,30);
				
			}
		}
		return success;
	}
}