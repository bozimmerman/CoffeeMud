package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_IdentifyBombs extends ThiefSkill
{
	public String ID() { return "Thief_IdentifyBombs"; }
	public String name(){ return "Identify Bombs";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected Room lastRoom=null;
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DETRAP;}

	public String trapCheck(Environmental E)
	{
		if(E!=null)
		{
		    Trap T=CMLib.utensils().fetchMyTrap(E);
			if((T!=null)&&(T.isABomb()))
			{
				if(CMLib.dice().rollPercentage()==1)
				{
					helpProficiency((MOB)affected);
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
		if((E instanceof Room)&&(CMLib.flags().canBeSeenBy(E,mob)))
			msg.append(trapCheck(mob.location()));
		else
		if((E instanceof Container)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			Container C=(Container)E;
			Vector V=C.getContents();
			for(int v=0;v<V.size();v++)
				if(trapCheck((Item)V.elementAt(v)).length()>0)
				{
					if(CMLib.dice().rollPercentage()==1)
					{
						helpProficiency((MOB)affected);
						affected.recoverEnvStats();
					}
					msg.append(C.name()+" contains a bomb.");
				}
		}
		else
		if((E instanceof Item)&&(CMLib.flags().canBeSeenBy(E,mob)))
			msg.append(trapCheck(E));
		else
		if((E instanceof Exit)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			Room room=mob.location();
			if(room!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
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
		if((E instanceof MOB)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				if(trapCheck(I).length()>0)
				{
					if(CMLib.dice().rollPercentage()==1)
					{
						helpProficiency((MOB)affected);
						affected.recoverEnvStats();
					}
					return E.name()+" is carrying a bomb.";
				}
			}
			if(CMLib.coffeeShops().getShopKeeper(E)!=null)
			{
				Vector V=CMLib.coffeeShops().getShopKeeper(E).getShop().getStoreInventory();
				for(int v=0;v<V.size();v++)
				{
					Environmental E2=(Environmental)V.elementAt(v);
					if(E2 instanceof Item)
						if(trapCheck(E2).length()>0)
						{
							if(CMLib.dice().rollPercentage()==1)
							{
								helpProficiency((MOB)affected);
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
		&&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
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
				CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}
}
