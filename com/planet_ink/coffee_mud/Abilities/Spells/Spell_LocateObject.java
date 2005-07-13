package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_LocateObject extends Spell
{
	public String ID() { return "Spell_LocateObject"; }
	public String name(){return "Locate Object";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
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

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a divination, shouting '"+what+"'^?.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Vector itemsFound=new Vector();
				HashSet areas=new HashSet();
				HashSet areasTried=new HashSet();
				Area A=null;
				int numAreas=(int)Math.round(Util.mul(CMMap.numAreas(),0.90))+1;
				if(numAreas>CMMap.numAreas()) numAreas=CMMap.numAreas();
				int tries=numAreas*numAreas;
				while((areas.size()<numAreas)&&(((--tries)>0)))
				{
				    A=CMMap.getRandomArea();
				    if((A!=null)&&(!areasTried.contains(A)))
				    {
				        areasTried.add(A);
				        if(Sense.canAccess(mob,A))
				            areas.add(A);
				        else
				            numAreas--;
				    }
				}
				MOB inhab=null;
				Environmental item=null;
				Room room=null;
				for(Iterator a=areas.iterator();a.hasNext();)
				{
				    A=(Area)a.next();
					for(Enumeration r=A.getProperMap();r.hasMoreElements();)
					{
					    room=(Room)r.nextElement();
	
						if(!Sense.canAccess(mob,room)) continue;
	
						item=room.fetchItem(null,what);
						if((item!=null)&&(Sense.canBeLocated((Item)item)))
						{
							String str=item.name()+" is in a place called '"+room.roomTitle()+"'.";
							itemsFound.addElement(str);
						}
						for(int i=0;i<room.numInhabitants();i++)
						{
							inhab=room.fetchInhabitant(i);
							if(inhab==null) break;
	
							item=inhab.fetchInventory(what);
							if((item==null)&&(CoffeeUtensils.getShopKeeper(inhab)!=null))
								item=CoffeeUtensils.getShopKeeper(inhab).getStock(what,mob);
							if((item instanceof Item)
							&&((Sense.canBeLocated((Item)item)))
							&&(item.envStats().level()>minLevel)
							&&(item.envStats().level()<maxLevel))
							{
								String str=item.name()+((!levelAdjust)?"":("("+item.envStats().level()+")"))+" is being carried by "+inhab.name()+" in a place called '"+room.roomTitle()+"'.";
								itemsFound.addElement(str);
								break;
							}
						}
						if(itemsFound.size()>0) break;
					}
					if(itemsFound.size()>0) break;
				}
				if(itemsFound.size()==0)
					mob.tell("Your magic fails to focus on anything called '"+what+"'.");
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
