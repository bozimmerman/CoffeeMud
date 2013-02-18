package com.planet_ink.coffee_mud.Abilities.SuperPowers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2013 Bo Zimmerman

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
public class Power_TrapSense extends SuperPower
{
	public String ID() { return "Power_TrapSense"; }
	public String name(){return "Trap Sense";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected  int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	Room lastRoom=null;

	public String trapCheck(Physical P)
	{
		if(P!=null)
		if(CMLib.utensils().fetchMyTrap(P)!=null)
			return P.name()+" is trapped.\n\r";
		return "";
	}

	public String trapHere(MOB mob, Physical P)
	{
		StringBuffer msg=new StringBuffer("");
		if(P==null) return msg.toString();
		if((P instanceof Room)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			msg.append(trapCheck(P));
			Room R=(Room)P;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(R.getExitInDir(d)==P)
				{
					Exit E2=R.getReverseExit(d);
					msg.append(trapHere(mob,P));
					msg.append(trapHere(mob,E2));
					break;
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.getItem(i);
				if((I!=null)&&(I.container()==null))
					msg.append(trapHere(mob,I));
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M!=mob))
					msg.append(trapHere(mob,M));
			}
		}
		else
		if((P instanceof Container)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			Container C=(Container)P;
			List<Item> V=C.getContents();
			for(int v=0;v<V.size();v++)
				if(trapCheck((Item)V.get(v)).length()>0)
					msg.append(C.name()+" contains something trapped.\n");
		}
		else
		if((P instanceof Item)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(P));
		else
		if((P instanceof Exit)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(P));
		else
		if((P instanceof MOB)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			for(int i=0;i<((MOB)P).numItems();i++)
			{
				Item I=((MOB)P).getItem(i);
				if(trapCheck(I).length()>0)
					return P.name()+" is carrying something trapped.\n";
			}
			ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(P);
			if(SK!=null)
			{
				for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					Environmental E2=(Environmental)i.next();
					if(E2 instanceof Item)
						if(trapCheck((Item)E2).length()>0)
							return P.name()+" has something trapped in stock.\n";
				}
			}
		}
		return msg.toString();
	}

	public void messageTo(MOB mob)
	{
		String here=trapHere(mob,mob.location());
		if(here.length()>0)
			mob.tell(here);
		else
		{
			String last="";
			String dirs="";
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Room R=mob.location().getRoomInDir(d);
				Exit E=mob.location().getExitInDir(d);
				if((R!=null)&&(E!=null)&&(trapHere(mob,R).length()>0))
				{
					if(last.length()>0)
						dirs+=", "+last;
					last=Directions.getFromDirectionName(d);
				}
			}
			if((dirs.length()==0)&&(last.length()>0))
				mob.tell("You sense a trap to "+last+".");
			else
			if((dirs.length()>2)&&(last.length()>0))
				mob.tell("You sense a trap to "+dirs.substring(2)+", and "+last+".");
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}
}
