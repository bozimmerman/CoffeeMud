package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Wand_Advancement extends StdWand implements ArchonOnly
{
	public String ID(){	return "Wand_Advancement";}
	public Wand_Advancement()
	{
		super();

		setName("a platinum wand");
		setDisplayText("a platinum wand is here.");
		setDescription("A wand made out of platinum");
		secretIdentity="The wand of Advancement.  Hold the wand say `level up` to it.";
		this.setUsesRemaining(50);
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=20000;
		recoverEnvStats();
		secretWord="LEVEL UP";
	}


	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="LEVEL UP";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="LEVEL UP";
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if((mob.isMine(this))
			   &&(!amWearingAt(Item.INVENTORY))
			   &&(msg.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)msg.target())))
			{
				MOB target=(MOB)msg.target();
				int x=msg.targetMessage().toUpperCase().indexOf("LEVEL UP");
				if(x>=0)
				{
					if((usesRemaining()>0)&&(useTheWand(CMClass.getAbility("Falling"),mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						FullMsg msg2=new FullMsg(mob,msg.target(),null,CMMsg.MSG_HANDS,CMMsg.MSG_OK_ACTION,CMMsg.MSG_OK_ACTION,"<S-NAME> point(s) "+this.name()+" at <T-NAMESELF>, who begins to glow softly.");
						if(mob.location().okMessage(mob,msg2))
						{
							mob.location().send(mob,msg2);
							if((target.charStats().getCurrentClass().leveless())
							||(target.charStats().getMyRace().leveless())
							||(CMSecurity.isDisabled("LEVELS")))
							    mob.tell("The wand will not work on such as "+target.name()+".");
							else
							if((target.getExpNeededLevel()==Integer.MAX_VALUE)
							||(target.charStats().getCurrentClass().expless())
							||(target.charStats().getMyRace().expless()))
								target.charStats().getCurrentClass().level(target);
							else
								MUDFight.postExperience(target,null,null,target.getExpNeededLevel()+1,false);
						}

					}
				}
			}
			return;
		default:
			break;
		}
	}
}
