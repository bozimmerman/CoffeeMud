package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_PeaceMaker extends Property
{
	public String ID() { return "Prop_PeaceMaker"; }
	public String name(){ return "Strike Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public String accountForYourself()
	{ return "Peace Maker";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.source()!=null)
			&&(msg.target()!=null)
			&&(msg.source()!=affected)
			&&(msg.source()!=msg.target()))
			{
				if(affected instanceof MOB)
				{
					MOB mob=(MOB)affected;
					if((Sense.aliveAwakeMobile(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().length()>0)
						{
							Vector V=Util.parseSemicolons(text(),true);
							t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
						}
						CommonMsgs.say(mob,msg.source(),t,false,false);
					}
					else
						return super.okMessage(myHost,msg);
				}
				else
				{
					String t="You feel too peaceful here.";
					if(text().length()>0)
					{
						Vector V=Util.parseSemicolons(text(),true);
						t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
					}
					msg.source().tell(t);
				}
				if(msg.source().getVictim()!=null)
					msg.source().getVictim().makePeace();
				msg.source().makePeace();
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
