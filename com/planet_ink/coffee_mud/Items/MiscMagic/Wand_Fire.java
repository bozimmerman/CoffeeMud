package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Wand_Fire extends StdWand
{
	public String ID(){	return "Wand_Fire";}
	public Wand_Fire()
	{
		super();

		setName("a gold wand");
		setDisplayText("a golden wand is here.");
		setDescription("A wand made out of gold, with a deep red ruby at the tip");
		secretIdentity="The wand of fire.  Responds to 'Blaze' and 'Burn'";
		this.setUsesRemaining(50);
		baseGoldValue=20000;
		baseEnvStats().setLevel(12);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
		secretWord="BLAZE, BURN";
	}


	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="BLAZE, BURN";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="BLAZE, BURN";
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
				int x=msg.targetMessage().toUpperCase().indexOf("BLAZE");
				if(x>=0)
				{
					Ability spell = CMClass.getAbility("Spell_BurningHands");
					if((usesRemaining()>0)&&(spell!=null)&&(useTheWand(spell,mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						spell.invoke(mob, target, true,envStats().level());
						return;
					}
				}
				x=msg.targetMessage().toUpperCase().indexOf("BURN");
				if(x>=0)
				{
					Ability spell = CMClass.getAbility("Spell_Fireball");
					if((usesRemaining()>4)&&(spell!=null)&&(useTheWand(spell,mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-5);
						spell.invoke(mob, target, true,envStats().level());
						return;
					}
				}
			}
			return;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}
}
