package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Skill_WildernessLore extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_WildernessLore";
	}

	private final static String	localizedName	= CMLib.lang().L("Wilderness Lore");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "WILDERNESSLORE", "WLORE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_NATURELORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean report=false;
		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("REPORT")))
		{
			commands.remove(commands.size()-1);
			report=true;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> take(s) a quick look at the terrain and feel(s) quite confused."));
			return false;
		}
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_HANDS,L("<S-NAME> take(s) a quick look at the terrain."));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			switch(room.domainType())
			{
			case Room.DOMAIN_INDOORS_METAL:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a metal structure.")));
				else
					mob.tell(L("You are in a metal structure."));
				break;
			case Room.DOMAIN_OUTDOORS_SPACEPORT:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a space port.")));
				else
					mob.tell(L("You are at a space port."));
				break;
			case Room.DOMAIN_OUTDOORS_CITY:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a city street.")));
				else
					mob.tell(L("You are on a city street."));
				break;
			case Room.DOMAIN_OUTDOORS_WOODS:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a forest.")));
				else
					mob.tell(L("You are in a forest."));
				break;
			case Room.DOMAIN_OUTDOORS_ROCKS:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a rocky plain.")));
				else
					mob.tell(L("You are on a rocky plain."));
				break;
			case Room.DOMAIN_OUTDOORS_PLAINS:
				if(report)
					CMLib.commands().postSay(mob, (L("This is the plains.")));
				else
					mob.tell(L("You are on the plains."));
				break;
			case Room.DOMAIN_OUTDOORS_UNDERWATER:
				if(report)
					CMLib.commands().postSay(mob, (L("This is under the water.")));
				else
					mob.tell(L("You are under the water."));
				break;
			case Room.DOMAIN_OUTDOORS_AIR:
				if(report)
					CMLib.commands().postSay(mob, (L("This is up in the air.")));
				else
					mob.tell(L("You are up in the air."));
				break;
			case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a water surface.")));
				else
					mob.tell(L("You are on the surface of the water."));
				break;
			case Room.DOMAIN_OUTDOORS_JUNGLE:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a jungle.")));
				else
					mob.tell(L("You are in a jungle."));
				break;
			case Room.DOMAIN_OUTDOORS_SEAPORT:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a seaport.")));
				else
					mob.tell(L("You are at a seaport."));
				break;
			case Room.DOMAIN_OUTDOORS_SWAMP:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a swamp.")));
				else
					mob.tell(L("You are in a swamp."));
				break;
			case Room.DOMAIN_OUTDOORS_DESERT:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a desert.")));
				else
					mob.tell(L("You are in a desert."));
				break;
			case Room.DOMAIN_OUTDOORS_HILLS:
				if(report)
					CMLib.commands().postSay(mob, (L("This is the hills.")));
				else
					mob.tell(L("You are in the hills."));
				break;
			case Room.DOMAIN_OUTDOORS_MOUNTAINS:
				if(report)
					CMLib.commands().postSay(mob, (L("This is the mountains.")));
				else
					mob.tell(L("You are on a mountain."));
				break;
			case Room.DOMAIN_INDOORS_STONE:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a stone enclosure.")));
				else
					mob.tell(L("You are in a stone structure."));
				break;
			case Room.DOMAIN_INDOORS_WOOD:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a wooden enclosure.")));
				else
					mob.tell(L("You are in a wooden structure."));
				break;
			case Room.DOMAIN_INDOORS_CAVE:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a cave.")));
				else
					mob.tell(L("You are in a cave."));
				break;
			case Room.DOMAIN_INDOORS_MAGIC:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a magical place.")));
				else
					mob.tell(L("You are in a magical place."));
				break;
			case Room.DOMAIN_INDOORS_UNDERWATER:
				if(report)
					CMLib.commands().postSay(mob, (L("This is an under water enclosure.")));
				else
					mob.tell(L("You are under the water."));
				break;
			case Room.DOMAIN_INDOORS_AIR:
				if(report)
					CMLib.commands().postSay(mob, (L("This is a large enclosed space.")));
				else
					mob.tell(L("You are up in a large indoor space."));
				break;
			case Room.DOMAIN_INDOORS_WATERSURFACE:
				if(report)
					CMLib.commands().postSay(mob, (L("This is an enclosed water surface.")));
				else
					mob.tell(L("You are inside, on the surface of the water."));
				break;
			}
			final int derivedClimate=room.getClimateType();
			if(derivedClimate!=Places.CLIMASK_NORMAL)
			{
				final StringBuffer str=new StringBuffer(L("It is unusually "));
				final List<String> conditions=new Vector<String>();
				if(CMath.bset(derivedClimate, Places.CLIMASK_WET))
					conditions.add("wet");
				if(CMath.bset(derivedClimate, Places.CLIMASK_HOT))
					conditions.add("hot");
				if(CMath.bset(derivedClimate, Places.CLIMASK_DRY))
					conditions.add("dry");
				if(CMath.bset(derivedClimate, Places.CLIMASK_COLD))
					conditions.add("cold");
				if(CMath.bset(derivedClimate, Places.CLIMASK_WINDY))
					conditions.add("windy");
				str.append(CMLib.english().toEnglishStringList(conditions.toArray(new String[0])));
				str.append(L(" here."));
				if(report)
					CMLib.commands().postSay(mob, str.toString());
				else
					mob.tell(str.toString());
			}
			int xlvl = super.getXLEVELLevel(mob);
			if((xlvl > 0)&&(room.resourceChoices()!=null)&&(room.resourceChoices().size()>0))
			{
				List<String> rscNames=new ArrayList<String>();
				int maxRscs=xlvl;
				if(room.resourceChoices().size()<maxRscs)
					maxRscs=room.resourceChoices().size();
				int[] chances=new int[room.resourceChoices().size()];
				String[] chancestr=new String[room.resourceChoices().size()];
				if(xlvl >= 5)
				{
					long total=0;
					for(int i=0;i<room.resourceChoices().size();i++)
					{
						int rscCode=room.resourceChoices().get(i).intValue();
						chances[i]=RawMaterial.CODES.FREQUENCY(rscCode);
						total+=chances[i];
					}
					for(int i=0;i<room.resourceChoices().size();i++)
					{
						double chancePct = CMath.div(chances[i],(int)total)*100.0;
						if(chancePct < 1)
							chancestr[i] = ""+CMath.div(Math.round(chancePct*100.0),100.0);
						else
							chancestr[i] = ""+(int)Math.round(chancePct);
					}
				}
				List<Integer> unused=new XVector<Integer>(room.resourceChoices());
				for(int i=0;i<xlvl && unused.size()>0;i++)
				{
					int rscIndex=CMLib.dice().roll(1, unused.size(), -1);
					int rscChoice=unused.remove(rscIndex).intValue();
					String resourceName = CMLib.materials().getResourceDesc(rscChoice).toLowerCase();
					if(!resourceName.endsWith("s"))
						resourceName = CMLib.english().makePlural(resourceName);
					if(xlvl >=5)
						rscNames.add(resourceName +" ("+chancestr[i]+"%)");
					else
						rscNames.add(resourceName);
				}
				String list=CMLib.english().toEnglishStringList(rscNames);
				if(report)
					CMLib.commands().postSay(mob, L("This is the sort of place that one might find @x1.",list));
				else
					mob.tell(L("This is the sort of place that one might find @x1.",list));
			}
		}
		else
			mob.location().show(mob,null,this,CMMsg.MSG_HANDS,L("<S-NAME> take(s) a quick look around, but get(s) confused."));
		return success;
	}

}
