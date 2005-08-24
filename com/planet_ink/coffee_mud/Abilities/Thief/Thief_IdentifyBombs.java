package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_IdentifyBombs extends ThiefSkill
{
	public String ID() { return "Thief_IdentifyBombs"; }
	public String name(){ return "Identify Bombs";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected Room lastRoom=null;

	public String trapCheck(Environmental E)
	{
		if(E!=null)
		{
		    Trap T=CoffeeUtensils.fetchMyTrap(E);
			if((T!=null)&&(T.isABomb()))
			{
				if(Dice.rollPercentage()==1)
				{
					helpProfficiency((MOB)affected);
					affected.recoverEnvStats();
				}
				return E.name()+" is a bomb.\n\r";
			}
		}
		return "";
	}

	public String trapHere(MOB mob, Environmental E)
	{
		StringBuffer msg=new StringBuffer("");
		if(E==null) return msg.toString();
		if((E instanceof Room)&&(Sense.canBeSeenBy(E,mob)))
			msg.append(trapCheck(mob.location()));
		else
		if((E instanceof Container)&&(Sense.canBeSeenBy(E,mob)))
		{
			Container C=(Container)E;
			Vector V=C.getContents();
			for(int v=0;v<V.size();v++)
				if(trapCheck((Item)V.elementAt(v)).length()>0)
				{
					if(Dice.rollPercentage()==1)
					{
						helpProfficiency((MOB)affected);
						affected.recoverEnvStats();
					}
					msg.append(C.name()+" contains a bomb.");
				}
		}
		else
		if((E instanceof Item)&&(Sense.canBeSeenBy(E,mob)))
			msg.append(trapCheck(E));
		else
		if((E instanceof Exit)&&(Sense.canBeSeenBy(E,mob)))
		{
			Room room=mob.location();
			if(room!=null)
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				if(room.getExitInDir(d)==E)
				{
					Exit E2=room.getReverseExit(d);
					Room R2=room.getRoomInDir(d);
					msg.append(trapCheck(E));
					msg.append(trapCheck(E2));
					msg.append(trapCheck(R2));
					break;
				}
			}
		}
		else
		if((E instanceof MOB)&&(Sense.canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				if(trapCheck(I).length()>0)
				{
					if(Dice.rollPercentage()==1)
					{
						helpProfficiency((MOB)affected);
						affected.recoverEnvStats();
					}
					return E.name()+" is carrying a bomb.";
				}
			}
			if(CoffeeUtensils.getShopKeeper((MOB)E)!=null)
			{
				Vector V=CoffeeUtensils.getShopKeeper((MOB)E).getUniqueStoreInventory();
				for(int v=0;v<V.size();v++)
				{
					Environmental E2=(Environmental)V.elementAt(v);
					if(E2 instanceof Item)
						if(trapCheck(E2).length()>0)
						{
							if(Dice.rollPercentage()==1)
							{
								helpProfficiency((MOB)affected);
								affected.recoverEnvStats();
							}
							return E.name()+" has a bomb in stock.";
						}
				}
			}
		}
		return msg.toString();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.target()!=null)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_LOOK))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				String str=trapHere((MOB)affected,msg.target());
				if(str.length()>0)
					((MOB)affected).tell(str);
			}
			else
			if((msg.target()!=null)
			&&(trapHere((MOB)affected,msg.target()).length()>0)
			&&(msg.source()!=msg.target()))
			{
				FullMsg msg2=new FullMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}
}
