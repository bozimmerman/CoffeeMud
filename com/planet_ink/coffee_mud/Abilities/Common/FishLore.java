package com.planet_ink.coffee_mud.Abilities.Common;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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
public class FishLore extends CommonSkill
{
	@Override
	public String ID()
	{
		return "FishLore";
	}

	private final static String	localizedName	= CMLib.lang().L("Fish Lore");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"FISHLORE","FSPECULATE"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_NATURELORE;
	}

	protected boolean success=false;
	protected Room fishRoom=null;
	public FishLore()
	{
		super();
		displayText=L("You are finding fish...");
		verb=L("finding");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if ((affected != null) 
		&& (affected instanceof MOB) 
		&& (tickID == Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(success==false)
				{
					final StringBuffer str=new StringBuffer(L("Your fish finding attempt failed.\n\r"));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room room=fishRoom;
				if((success)&&(!aborted)&&(room!=null))
				{
					final StringBuffer str=new StringBuffer("");
					int resource=room.myResource()&RawMaterial.RESOURCE_MASK;
					if(RawMaterial.CODES.IS_VALID(resource))
					{
						if(CMParms.contains(RawMaterial.CODES.FISHES(),room.myResource()))
						{
							final String resourceStr=RawMaterial.CODES.NAME(room.myResource());
							str.append(L("You think this spot would be good for @x1.\n\r",resourceStr.toLowerCase()));
						}
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							final Room room2=room.getRoomInDir(d);
							if((room2!=null)
							&&(room.getExitInDir(d)!=null)
							&&(room.getExitInDir(d).isOpen()))
							{
								resource=room2.myResource()&RawMaterial.RESOURCE_MASK;
								if(RawMaterial.CODES.IS_VALID(resource) && CMParms.contains(RawMaterial.CODES.FISHES(),room2.myResource()))
								{
									final String resourceStr=RawMaterial.CODES.NAME(room2.myResource());
									str.append(L("There looks like @x1 @x2.\n\r",resourceStr.toLowerCase(),CMLib.directions().getInDirectionName(d)));
								}
							}
						}
						commonTell(mob,str.toString());
					}
					if(str.length()==0)
						commonTell(mob,L("You don't find any good fishing spots around here."));
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(super.checkStop(mob, commands) || (R == null))
			return true;
		verb=L("finding fish");
		success=false;
		fishRoom=null;
		
		if((R.domainType()&Room.INDOORS)>0)
		{
			commonTell(mob,L("You can't do this indoors!"));
			return false;
		}
		if((mob.riding()!=null)&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER))
			fishRoom=R;
		else
		if(CMLib.flags().isWateryRoom(R))
			fishRoom=R;
		else
		if((R.getArea() instanceof BoardableShip)
		&&((R.domainType()&Room.INDOORS)==0))
			fishRoom=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
		
		if(fishRoom==null)
		{
			this.commonTell(mob, L("You need to be on the water, or in a boat to use this skill."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
			success=true;
		final int duration=getDuration(45,mob,1,10);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) finding fish in this area."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
