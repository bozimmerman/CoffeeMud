package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Prayer_BirdsEye extends Prayer
{
	public String ID() { return "Prayer_BirdsEye"; }
	public String name(){ return "Birds Eye";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for a birds eye view.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("BardMap");
				if(I!=null)
				{
					Vector set=new Vector();
					TrackingLibrary.TrackingFlags flags;
					flags = new TrackingLibrary.TrackingFlags()
							.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
							.add(TrackingLibrary.TrackingFlag.NOAIR);
					CMLib.tracking().getRadiantRooms(mob.location(),set,flags,null,2,null);
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<set.size();i++)
						str.append(CMLib.map().getExtendedRoomID((Room)set.elementAt(i))+";");
					I.setReadableText(str.toString());
					I.setName("");
					I.baseEnvStats().setDisposition(EnvStats.IS_GLOWING);
					msg=CMClass.getMsg(mob,I,CMMsg.MSG_READ,"");
					mob.addInventory(I);
					mob.location().send(mob,msg);
					I.destroy();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a birds eye view, but fail(s).");

		return success;
	}
}
