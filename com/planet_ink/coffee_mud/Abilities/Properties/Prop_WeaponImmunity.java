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
	public Hashtable flags=new Hashtable();

	public String accountForYourself()
	{
		String id="Weapon Immunities for the wearer: "+text();
		return id;
	}
	public void setMiscText(String newValue)
	{
	    super.setMiscText(newValue);
	    flags=new Hashtable();
	    Vector V=Util.parse(newValue.toUpperCase());
	    Object c=null;
	    String s=null;
	    for(int v=0;v<V.size();v++)
	    {
	        s=(String)V.elementAt(v);
	        c=new Character(s.charAt(0));
	        if((s.charAt(0)=='-')||(s.charAt(0)=='+'))
	            s=s.substring(1);
	        else
	            c=new Character('+');
	        if(s.startsWith("LEVEL"))
	        {
	            c=new String(((Character)c).charValue()+" "+s.substring(5).trim());
	            s=s.substring(5).trim();
	        }
            flags.put(s,c);
	    }
	    
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
			if(flags.size()==0) return true;

			boolean immune=flags.containsKey("ALL")&&(((Character)flags.get("ALL")).charValue()=='+');
			Character foundPlusMinus=null;
			for(int i=0;i<CharStats.affectTypeMap.length;i++)
				if((CharStats.affectTypeMap[i]==msg.sourceMinor())
				&&(i!=CharStats.SAVE_MAGIC))
				{
					Vector V=Util.parse(CharStats.TRAITS[i]);
					if(((String)V.lastElement()).equals("SAVE"))
					    foundPlusMinus=(Character)flags.get(V.firstElement());
					else
					    foundPlusMinus=(Character)flags.get(V.lastElement());
					if(foundPlusMinus!=null)
					{
						if((foundPlusMinus.charValue()=='-')&&(immune))
							immune=false;
						else
						if(foundPlusMinus.charValue()!='-')
							immune=true;
						break;
					}
				}

			if((foundPlusMinus==null)&&(msg.tool() instanceof Weapon))
			{
			    foundPlusMinus=(Character)flags.get(Weapon.typeDescription[((Weapon)msg.tool()).weaponType()]);
			    foundPlusMinus=(Character)flags.get(Weapon.classifictionDescription[((Weapon)msg.tool()).weaponClassification()]);
			    foundPlusMinus=(Character)flags.get((Sense.isABonusItems(msg.tool()))?"MAGIC":"NONMAGIC");
			    foundPlusMinus=(Character)flags.get(EnvResource.RESOURCE_DESCS[((Weapon)msg.tool()).material()&EnvResource.RESOURCE_MASK]);
				if(foundPlusMinus!=null)
				{
					if((foundPlusMinus.charValue()=='-')&&(immune))
						immune=false;
					else
					if(foundPlusMinus.charValue()!='-')
						immune=true;
				}
				else
				{
				    Object O=flags.get("LEVEL");
					if((O!=null)&&(O instanceof String)&&(((String)O).length()>3))
					{
						String lvl=(String)O;
						foundPlusMinus=new Character(lvl.charAt(0));
						lvl=lvl.substring(2).trim();
						if((foundPlusMinus.charValue()=='-')&&(immune))
						{
							if(msg.tool().envStats().level()>=Util.s_int(lvl))
								immune=false;
						}
						else
						if(foundPlusMinus.charValue()!='-')
						{
							if(msg.tool().envStats().level()<Util.s_int(lvl))
								immune=true;
						}
					}
				}
			}

			if((foundPlusMinus==null)&&(msg.tool() instanceof Ability))
			{
				int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES;
				switch(classType)
				{
				case Ability.SPELL:
				case Ability.PRAYER:
				case Ability.CHANT:
				case Ability.SONG:
					{
				    	foundPlusMinus=(Character)flags.get("MAGICSKILLS");
						if(foundPlusMinus==null) foundPlusMinus=(Character)flags.get("MAGIC");
						if(foundPlusMinus!=null)
						{
							if((foundPlusMinus.charValue()=='-')&&(immune))
								immune=false;
							else
							if(foundPlusMinus.charValue()!='-')
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
