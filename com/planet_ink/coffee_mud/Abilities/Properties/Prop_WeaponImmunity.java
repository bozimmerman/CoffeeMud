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
public class Prop_WeaponImmunity extends Property
{
	public String ID() { return "Prop_WeaponImmunity"; }
	public String name(){ return "Weapon Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}

	public String accountForYourself()
	{
		String id="Weapon Immunities for the wearer: "+text();
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
			&&(!((Item)affected).amWearingAt(Item.INVENTORY))
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof MOB))
				M=(MOB)((Item)affected).owner();
			if(M==null) return true;
			if(!msg.amITarget(M)) return true;
			if(msg.tool()==null) return true;

			String text=text().toUpperCase();
			boolean immune=text.indexOf("+ALL")>=0;
			int x=-1;
			for(int i=0;i<CharStats.affectTypeMap.length;i++)
				if((CharStats.affectTypeMap[i]==msg.sourceMinor())
				&&(i!=CharStats.SAVE_MAGIC))
				{
					Vector V=Util.parse(CharStats.TRAITS[i]);
					if(((String)V.lastElement()).equals("SAVE"))
						x=text.indexOf((String)V.firstElement());
					else
						x=text.indexOf((String)V.lastElement());
					if(x>0)
					{
						if((text.charAt(x-1)=='-')&&(immune))
							immune=false;
						else
						if(text.charAt(x-1)!='-')
							immune=true;
						break;
					}
				}

			if((x<0)&&(msg.tool() instanceof Weapon))
			{
				x=text.indexOf(Weapon.typeDescription[((Weapon)msg.tool()).weaponType()]);
				if(x<0)x=(Sense.isABonusItems(msg.tool()))?text.indexOf("MAGIC"):-1;
				if(x<0)x=text.indexOf(EnvResource.RESOURCE_DESCS[((Weapon)msg.tool()).material()&EnvResource.RESOURCE_MASK]);
				if(x>0)
				{
					if((text.charAt(x-1)=='-')&&(immune))
						immune=false;
					else
					if(text.charAt(x-1)!='-')
						immune=true;
				}
				else
				{
					x=text.indexOf("LEVEL");
					if(x>0)
					{
						String lvl=text.substring(x+5);
						if(lvl.indexOf(" ")>=0)
							lvl=lvl.substring(lvl.indexOf(" "));
						if((text.charAt(x-1)=='-')&&(immune))
						{
							if(msg.tool().envStats().level()>=Util.s_int(lvl))
								immune=false;
						}
						else
						if(text.charAt(x-1)!='-')
						{
							if(msg.tool().envStats().level()<Util.s_int(lvl))
								immune=true;
						}
					}
				}
			}

			if((x<0)&&(msg.tool() instanceof Ability))
			{
				int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES;
				switch(classType)
				{
				case Ability.SPELL:
				case Ability.PRAYER:
				case Ability.CHANT:
				case Ability.SONG:
					{
						x=text.indexOf("MAGIC");
						if(x>0)
						{
							if((text.charAt(x-1)=='-')&&(immune))
								immune=false;
							else
							if(text.charAt(x-1)!='-')
								immune=true;
						}
					}
					break;
				default:
					break;
				}
			}
			if(immune) msg.setValue(0);
		}
		return true;
	}
}
