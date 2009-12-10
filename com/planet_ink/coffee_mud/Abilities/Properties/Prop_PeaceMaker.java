package com.planet_ink.coffee_mud.Abilities.Properties;
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
@SuppressWarnings("unchecked")
public class Prop_PeaceMaker extends Property
{
	public String ID() { return "Prop_PeaceMaker"; }
	public String name(){ return "Strike Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public String accountForYourself()
	{ return "Peace Maker";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.source()!=null)
			&&(msg.target()!=null)
			&&(msg.source()!=affected)
			&&(msg.source()!=msg.target()))
			{
				if(affected instanceof MOB)
				{
					MOB mob=(MOB)affected;
					if((CMLib.flags().aliveAwakeMobileUnbound(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().length()>0)
						{
							Vector V=CMParms.parseSemicolons(text(),true);
							t=(String)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
						}
						CMLib.commands().postSay(mob,msg.source(),t,false,false);
					}
					else
						return super.okMessage(myHost,msg);
				}
				else
				{
					String t="You feel too peaceful here.";
					if(text().length()>0)
					{
						Vector V=CMParms.parseSemicolons(text(),true);
						t=(String)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
					}
					msg.source().tell(t);
				}
				MOB victim=msg.source().getVictim();
				if(victim!=null) victim.makePeace();
				msg.source().makePeace();
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
