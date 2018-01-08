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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_Knock extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Knock";
	}

	private final static String localizedName = CMLib.lang().L("Knock");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Knock Spell)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS|CAN_EXITS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room R=givenTarget==null?mob.location():CMLib.map().roomLocation(givenTarget);
		if(R==null)
			R=mob.location();
		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			int theDir=-1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Exit E=R.getExitInDir(d);
				if((E!=null)
				&&(!E.isOpen()))
				{
					theDir=d;
					break;
				}
			}
			if(theDir>=0)
				commands.add(CMLib.directions().getDirectionName(theDir));
		}

		final String whatToOpen=CMParms.combine(commands,0);
		Physical openThis=givenTarget;
		final int dirCode=CMLib.directions().getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=R.getExitInDir(dirCode);
		if(openThis==null)
			openThis=getTarget(mob,R,givenTarget,commands,Wearable.FILTER_ANY);
		if(openThis==null)
			return false;

		if(openThis instanceof Exit)
		{
			if(((Exit)openThis).isOpen())
			{
				mob.tell(L("That's already open!"));
				return false;
			}
		}
		else
		if(openThis instanceof Container)
		{
			if(((Container)openThis).isOpen())
			{
				mob.tell(L("That's already open!"));
				return false;
			}
		}
		else
		{
			mob.tell(L("You can't cast knock on @x1!",openThis.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=openThis.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		final boolean success=proficiencyCheck(mob,-(levelDiff*25),auto);
		if(!success)
			beneficialVisualFizzle(mob,openThis,auto?L("Nothing happens to @x1.",openThis.name()):L("<S-NAME> point(s) at @x1 and shout(s) incoherently, but nothing happens.",openThis.name()));
		else
		{
			CMMsg msg=CMClass.getMsg(mob,openThis,this,somanticCastCode(mob,openThis,auto),(auto?L("@x1 begin(s) to glow!",openThis.name()):L("^S<S-NAME> point(s) at <T-NAMESELF>.^?"))+CMLib.protocol().msp("knock.wav",10));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				for(int a=0;a<openThis.numEffects();a++)
				{
					final Ability A=openThis.fetchEffect(a);
					if((A!=null)&&(A.ID().equalsIgnoreCase("Spell_WizardLock")))
					{
						final String txt=A.text().trim();
						int level=(A.invoker()!=null)?A.invoker().phyStats().level():0;
						if(txt.length()>0)
						{
							if(CMath.isInteger(txt))
								level=CMath.s_int(txt);
							else
							{
								final int x=txt.indexOf(' ');
								if((x>0)&&(CMath.isInteger(txt.substring(0,x))))
									level=CMath.s_int(txt.substring(0,x));
							}
						}
						if(level<(mob.phyStats().level()+3+(2*getXLEVELLevel(mob))))
						{
							A.unInvoke();
							R.show(mob,null,openThis,CMMsg.MSG_OK_VISUAL,L("A spell around <O-NAME> seems to fade."));
							break;
						}
					}
				}
				msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_UNLOCK,null);
				CMLib.utensils().roomAffectFully(msg,R,dirCode);
				msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_OPEN,L("<T-NAME> opens."));
				CMLib.utensils().roomAffectFully(msg,R,dirCode);
			}
		}

		return success;
	}
}
