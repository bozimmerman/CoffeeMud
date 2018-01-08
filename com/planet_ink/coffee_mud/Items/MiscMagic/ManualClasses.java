package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
@SuppressWarnings("rawtypes")
public class ManualClasses extends StdItem implements MiscMagic,ArchonOnly
{
	@Override
	public String ID()
	{
		return "ManualClasses";
	}

	public ManualClasses()
	{
		super();

		setName("a book");
		basePhyStats.setWeight(1);
		setDisplayText("an roughly treated book sits here.");
		setDescription("An roughly treated book filled with mystical symbols.");
		secretIdentity="The Manual of Classes.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=5000;
		material=RawMaterial.RESOURCE_PAPER;
		recoverPhyStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
				if(mob.isMine(this))
				{
					if(mob.fetchEffect("Spell_ReadMagic")!=null)
					{
						if(this.usesRemaining()<=0)
							mob.tell(L("The markings have been read off the parchment, and are no longer discernable."));
						else
						{
							this.setUsesRemaining(this.usesRemaining()-1);
							mob.tell(L("The manual glows softly, enveloping you in its wisdom."));
							CharClass lastC=null;
							CharClass thisC=null;
							for(final Enumeration c=CMClass.charClasses();c.hasMoreElements();)
							{
								final CharClass C=(CharClass)c.nextElement();
								if(thisC==null)
									thisC=C;
								if((lastC!=null)&&(thisC==mob.charStats().getCurrentClass()))
								{
									thisC=C;
									break;
								}
								lastC=C;
							}
							if((thisC!=null)&&(!(thisC.ID().equals("Archon"))))
							{
								mob.charStats().setCurrentClass(thisC);
								if((!mob.isMonster())&&(mob.soulMate()==null))
									CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_CLASSCHANGE);
								mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 undergoes a traumatic change.",mob.name()));
								mob.tell(L("You are now a @x1.",thisC.name(mob.charStats().getClassLevel(thisC))));
							}
						}
					}
					else
						mob.tell(L("The markings look magical, and are unknown to you."));
				}
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

}
