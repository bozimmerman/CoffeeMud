package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
public class ManualArchon extends StdItem implements MiscMagic,ArchonOnly
{
	public String ID(){	return "ManualArchon";}
	public ManualArchon()
	{
		super();

		setName("an ornately decorated book");
		baseEnvStats.setWeight(1);
		setDisplayText("an ornately decorated book has definitely been left behind by someone.");
		setDescription("A book covered with mystical symbols, inside and out.");
		secretIdentity="The Manual of the Archons.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=50000;
		material=RawMaterial.RESOURCE_PAPER;
		recoverEnvStats();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
				if(mob.isMine(this))
				{
					mob.tell("The manual glows softly, enveloping you in its magical energy.");
					Session session=mob.session();
					CharClass newClass=CMClass.getCharClass("Archon");
					if((session!=null)&&(newClass!=null))
					{
						mob.setSession(null);

						for(int i : CharStats.CODES.BASE())
							mob.baseCharStats().setStat(i,25);
						if((!mob.isMonster())&&(mob.soulMate()==null))
							CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_CLASSCHANGE);
						mob.recoverCharStats();
						if((!mob.charStats().getCurrentClass().leveless())
                        &&(!mob.charStats().isLevelCapped(mob.charStats().getCurrentClass()))
						&&(!mob.charStats().getMyRace().leveless())
						&&(!CMSecurity.isDisabled("LEVELS")))
						while(mob.baseEnvStats().level()<100)
						{
							int oldLevel = mob.baseEnvStats().level();
							if((mob.getExpNeededLevel()==Integer.MAX_VALUE)
							||(mob.charStats().getCurrentClass().expless())
							||(mob.charStats().getMyRace().expless()))
								CMLib.leveler().level(mob);
							else
								CMLib.leveler().postExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
							if(mob.baseEnvStats().level()==oldLevel)
								break;
						}
						mob.baseCharStats().setCurrentClass(newClass);
						mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),30);
						mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+30);
						mob.setExperience(mob.getExpNextLevel());
						mob.recoverCharStats();
						mob.recoverEnvStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
						mob.charStats().getCurrentClass().startCharacter(mob,true,false);
						CMLib.utensils().outfit(mob,mob.charStats().getCurrentClass().outfit(mob));
						mob.setSession(session);
						CMLib.database().DBUpdatePlayer(mob);
					}
				}
				mob.tell("The book vanishes out of your hands.");
				destroy();
				msg.source().location().recoverRoomStats();
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

}
