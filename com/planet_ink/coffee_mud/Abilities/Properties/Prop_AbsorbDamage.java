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
public class Prop_AbsorbDamage extends Property
{
	public String ID() { return "Prop_AbsorbDamage"; }
	public String name(){ return "Absorb Damage";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}

	public String accountForYourself()
	{
		String id="Absorbs damage of the following amount and types: "+text();
		return id;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)&&(msg.value()>0))
		{
			MOB M=null;
			if(affected instanceof MOB)
				M=(MOB)affected;
			else
			if((affected instanceof Item)
			&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY))
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof MOB))
				M=(MOB)((Item)affected).owner();
			if(M==null) return true;
			if(!msg.amITarget(M)) return true;

			String text=text().toUpperCase();

			int immune=text.indexOf("+ALL");
			int x=-1;
			for(int i : CharStats.CODES.SAVING_THROWS())
				if((CharStats.CODES.CMMSGMAP(i)==msg.sourceMinor())
				&&((msg.tool()==null)||(i!=CharStats.STAT_SAVE_MAGIC)))
				{
					x=text.indexOf(CharStats.CODES.NAME(i));
					if(x>0)
					{
						if((text.charAt(x-1)=='-')&&(immune>=0))
							immune=-1;
						else
						if(text.charAt(x-1)!='-')
							immune=x;
					}
				}

			if((x<0)&&(msg.tool() instanceof Weapon))
			{
				x=text.indexOf(Weapon.TYPE_DESCS[((Weapon)msg.tool()).weaponType()]);
				if(x<0) x=(CMLib.flags().isABonusItems(msg.tool()))?text.indexOf("MAGIC"):-1;
				if(x<0) x=text.indexOf(RawMaterial.CODES.NAME(((Weapon)msg.tool()).material()));
				if(x>0)
				{
					if((text.charAt(x-1)=='-')&&(immune>=0))
						immune=-1;
					else
					if(text.charAt(x-1)!='-')
						immune=x;
				}
				else
				{
					x=text.indexOf("LEVEL");
					if(x>0)
					{
						String lvl=text.substring(x+5);
						if(lvl.indexOf(" ")>=0)
							lvl=lvl.substring(lvl.indexOf(" "));
						if((text.charAt(x-1)=='-')&&(immune>=0))
						{
							if(msg.tool().envStats().level()>=CMath.s_int(lvl))
								immune=-1;
						}
						else
						if(text.charAt(x-1)!='-')
						{
							if(msg.tool().envStats().level()<CMath.s_int(lvl))
								immune=x;
						}
					}
				}
			}

			if((x<0)&&(msg.tool() instanceof Ability))
			{
				int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES;
				switch(classType)
				{
				case Ability.ACODE_SPELL:
				case Ability.ACODE_PRAYER:
				case Ability.ACODE_CHANT:
				case Ability.ACODE_SONG:
					{
						x=text.indexOf("MAGIC");
						if(x>0)
						{
							if((text.charAt(x-1)=='-')&&(immune>=0))
								immune=-1;
							else
							if(text.charAt(x-1)!='-')
								immune=x;
						}
					}
					break;
				default:
					break;
				}
			}
			if(immune>0)
			{
				int lastNumber=-1;
				x=0;
				while(x<immune)
				{
					if(Character.isDigit(text.charAt(x))&&((x==0)||(!Character.isDigit(text.charAt(x-1)))))
					   lastNumber=x;
					x++;
				}
				if(lastNumber>=0)
				{
					text=text.substring(lastNumber,immune).trim();
					x=text.indexOf(" ");
					if(x>0) text=text.substring(0,x).trim();
					if(text.endsWith("%"))
						msg.setValue(msg.value()-(int)Math.round(CMath.mul(msg.value(),CMath.div(CMath.s_int(text.substring(0,text.length()-1)),100.0))));
					else
						msg.setValue(msg.value()-CMath.s_int(text));
					if(msg.value()<0) msg.setValue(0);
				}
			}
		}
		return true;
	}
}
