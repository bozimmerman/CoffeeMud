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
public class Thief_Graffiti extends ThiefSkill
{
	public String ID() { return "Thief_Graffiti"; }
	public String name(){ return "Graffiti";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"GRAFFITI"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String str=CMParms.combine(commands,0);
		if(str.length()==0)
		{
			mob.tell("What would you like to write here?");
			return false;
		}
		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;

		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		   &&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD)
		   &&(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE))
		{
			mob.tell("You can't put graffiti here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
        if(levelDiff<0) levelDiff=0;
        levelDiff*=5;
		boolean success=proficiencyCheck(mob,-levelDiff,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> write(s) graffiti here.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenWallpaper");
				I.setName("Graffiti");
				CMLib.flags().setReadable(I,true);
				I.recoverEnvStats();
				I.setReadableText(str);
				switch(CMLib.dice().roll(1,6,0))
				{
				case 1:
					I.setDescription("Someone has scribbed some graffiti here.  Try reading it.");
					break;
				case 2:
					I.setDescription("A cryptic message has been written on the walls.  Try reading it.");
					break;
				case 3:
					I.setDescription("Someone wrote a message here to read.");
					break;
				case 4:
					I.setDescription("A strange message is written here.  Read it.");
					break;
				case 5:
					I.setDescription("This graffiti looks like it is in "+mob.name()+" handwriting.  Read it!");
					break;
				case 6:
					I.setDescription("The wall is covered in graffiti.  You might want to read it.");
					break;
				}
				mob.location().addItem(I);
				I.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to write graffiti here, but fails.");
		return success;
	}
}
