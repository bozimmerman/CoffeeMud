package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Dance_Basse extends Dance
{
	public String ID() { return "Dance_Basse"; }
	public String name(){ return "Basse";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().getVictim()!=target)
			&&(Dice.rollPercentage()>((msg.source().envStats().level()-target.envStats().level())*10)))
			{
				msg.source().tell("You are too much in awe of "+target.name());
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
