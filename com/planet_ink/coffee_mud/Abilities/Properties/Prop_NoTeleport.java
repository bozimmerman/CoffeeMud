package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class Prop_NoTeleport extends Property
{
	public String ID() { return "Prop_NoTeleport"; }
	public String name(){ return "Teleport INTO Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.source()!=null)
		&&(msg.source().location()!=null)
		&&(msg.sourceMinor()!=CMMsg.TYP_LEAVE))
		{
			boolean summon=Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
			boolean teleport=Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
			boolean shere=(msg.source().location()==affected)
						||((affected instanceof Area)&&(((Area)affected).inMetroArea(msg.source().location().getArea())));
			if(((!shere)&&(!summon)&&(teleport))
			   ||((shere)&&(summon)))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
				return false;
			}
		}
		return true;
	}
}
