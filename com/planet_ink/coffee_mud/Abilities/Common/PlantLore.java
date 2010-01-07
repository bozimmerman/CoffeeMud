package com.planet_ink.coffee_mud.Abilities.Common;
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
public class PlantLore extends CommonSkill
{
	public String ID() { return "PlantLore"; }
	public String name(){ return "Plant Lore";}
	private static final String[] triggerStrings = {"PLANTLORE","PSPECULATE"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_NATURELORE; }

	protected boolean success=false;
	public PlantLore()
	{
		super();
		displayText="You are observing plant growth...";
		verb="observing plant growths";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(success==false)
				{
					StringBuffer str=new StringBuffer("Your growth observation attempt failed.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!helping))
			{
				MOB mob=(MOB)affected;
				Room room=mob.location();
				if((success)&&(!aborted)&&(room!=null))
				{
					if((room.domainType()&Room.INDOORS)==0)
					{
						StringBuffer str=new StringBuffer("");
						Vector V=new Vector();
						TrackingLibrary.TrackingFlags flags;
						flags = new TrackingLibrary.TrackingFlags()
								.add(TrackingLibrary.TrackingFlag.OPENONLY)
								.add(TrackingLibrary.TrackingFlag.AREAONLY)
								.add(TrackingLibrary.TrackingFlag.NOAIR);
						CMLib.tracking().getRadiantRooms(room,V,flags,null,2+(getXLEVELLevel(mob)/2),null);
						for(int v=0;v<V.size();v++)
						{
							Room R=(Room)V.elementAt(v);
							int material=R.myResource()&RawMaterial.MATERIAL_MASK;
							int resource=R.myResource()&RawMaterial.RESOURCE_MASK;
							if(!RawMaterial.CODES.IS_VALID(resource))
								continue;
							if((material!=RawMaterial.MATERIAL_VEGETATION)
							&&(resource!=RawMaterial.RESOURCE_COTTON)
							&&(resource!=RawMaterial.RESOURCE_HEMP)
							&&(resource!=RawMaterial.RESOURCE_SAP)
							&&(material!=RawMaterial.MATERIAL_WOODEN))
								continue;
							String resourceStr=RawMaterial.CODES.NAME(resource);
							if(R==room)
								str.append("You think this spot would be good for "+resourceStr.toLowerCase()+".\n\r");
							else
							{
								int isAdjacent=-1;
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
								{
									Room room2=room.getRoomInDir(d);
									if(room2==R) isAdjacent=d;
								}
								if(isAdjacent>=0)
									str.append("There looks like "+resourceStr.toLowerCase()+" "+Directions.getInDirectionName(isAdjacent)+".\n\r");
								else
								{
									int d=CMLib.tracking().radiatesFromDir(R,V);
									if(d>=0)
									{
										d=Directions.getOpDirectionCode(d);
										str.append("There looks like "+resourceStr.toLowerCase()+" far "+Directions.getInDirectionName(d)+".\n\r");
									}
								}

							}
						}
						commonTell(mob,str.toString());
					}
					else
						commonTell(mob,"You don't find any good plant life around here.");
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb="observing plant growth";
		success=false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
			success=true;
		int duration=getDuration(45,mob,1,5);
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) observing the growth in this area.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
