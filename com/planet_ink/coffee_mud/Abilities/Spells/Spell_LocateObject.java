package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_LocateObject extends Spell
{
	public String ID() { return "Spell_LocateObject"; }
	public String name(){return "Locate Object";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Locate what?");
			return false;
		}

		int minLevel=Integer.MIN_VALUE;
		int maxLevel=Integer.MAX_VALUE;
		String s=(String)commands.lastElement();
		boolean levelAdjust=false;
		while((commands.size()>1)
		&&((Util.s_int(s)>0)
			||(s.startsWith(">"))
			||(s.startsWith("<"))))
			{
				levelAdjust=true;
				boolean lt=true;
				if(s.startsWith(">"))
				{
					lt=false;
					s=s.substring(1);
				}
				else
				if(s.startsWith("<"))
					s=s.substring(1);
				int levelFind=Util.s_int(s);

				if(lt)
					maxLevel=levelFind;
				else
					minLevel=levelFind;

				commands.removeElementAt(commands.size()-1);
				if(commands.size()>1)
					s=(String)commands.lastElement();
				else
					s="";
			}
		String what=Util.combine(commands,0);

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a divination, shouting '"+what+"'^?.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Vector itemsFound=new Vector();
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();

					if(!Sense.canAccess(mob,room)) continue;

					Environmental item=room.fetchItem(null,what);
					if((item!=null)&&(Sense.canBeLocated((Item)item)))
					{
						String str=item.name()+" is in a place called '"+room.roomTitle()+"'.";
						itemsFound.addElement(str);
					}
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if(inhab==null) break;

						item=inhab.fetchInventory(what);
						if((item==null)&&(CoffeeUtensils.getShopKeeper(inhab)!=null))
							item=CoffeeUtensils.getShopKeeper(inhab).getStock(what,mob);
						if((item!=null)
						&&(item instanceof Item)
						&&((Sense.canBeLocated((Item)item)))
						&&(item.envStats().level()>minLevel)
						&&(item.envStats().level()<maxLevel))
						{
							String str=item.name()+((!levelAdjust)?"":("("+item.envStats().level()+")"))+" is being carried by "+inhab.name()+" in a place called '"+room.roomTitle()+"'.";
							itemsFound.addElement(str);
						}
					}
				}
				if(itemsFound.size()==0)
					mob.tell("There doesn't seem to be anything in the wide world called '"+what+"'.");
				else
					mob.tell((String)itemsFound.elementAt(Dice.roll(1,itemsFound.size(),-1)));
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> invoke(s) a divination, shouting '"+what+"', but there is no answer.");


		// return whether it worked
		return success;
	}
}
