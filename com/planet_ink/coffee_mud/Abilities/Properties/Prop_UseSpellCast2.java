package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_UseSpellCast2 extends Prop_UseSpellCast
{
	public String ID() { return "Prop_UseSpellCast2"; }
	public String name(){ return "Casting spells when used";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(processing) return;
		processing=true;

		if(affected==null) return;
		Item myItem=(Item)affected;
		if(myItem.owner()==null) return;
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_DRINK:
			if((myItem instanceof Drink)
			&&(msg.amITarget(myItem)))
				addMeIfNeccessary(msg.source(),msg.source());
			break;
		case CMMsg.TYP_EAT:
			if((myItem instanceof Food)
			&&(msg.amITarget(myItem)))
				addMeIfNeccessary(msg.source(),msg.source());
			break;
		case CMMsg.TYP_GET:
			if((!(myItem instanceof Drink))
			  &&(!(myItem instanceof Food))
			  &&(msg.amITarget(myItem)))
				addMeIfNeccessary(msg.source(),msg.source());
			break;
		}
		processing=false;
	}
}
