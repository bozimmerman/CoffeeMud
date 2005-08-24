package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class ManualClasses extends StdItem implements MiscMagic,ArchonOnly
{
	public String ID(){	return "ManualClasses";}
	public ManualClasses()
	{
		super();

		setName("a book");
		baseEnvStats.setWeight(1);
		setDisplayText("an roughly treated book sits here.");
		setDescription("An roughly treated book filled with mystical symbols.");
		secretIdentity="The Manual of Classes.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=5000;
		material=EnvResource.RESOURCE_PAPER;
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
					if(mob.fetchEffect("Spell_ReadMagic")!=null)
					{
						if(this.usesRemaining()<=0)
							mob.tell("The markings have been read off the parchment, and are no longer discernable.");
						else
						{
							this.setUsesRemaining(this.usesRemaining()-1);
							mob.tell("The manual glows softly, enveloping you in its wisdom.");
							CharClass lastC=null;
							CharClass thisC=null;
							for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
							{
								CharClass C=(CharClass)c.nextElement();
								if(thisC==null) thisC=C;
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
								mob.tell("You are now a "+thisC.name()+".");
								if((!mob.isMonster())&&(mob.soulMate()==null))
									CoffeeTables.bump(mob,CoffeeTables.STAT_CLASSCHANGE);
								mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,mob.name()+" undergoes a traumatic change.");
							}
						}
					}
					else
						mob.tell("The markings look magical, and are unknown to you.");
				}
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

}
