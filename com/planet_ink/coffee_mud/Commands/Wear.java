package com.planet_ink.coffee_mud.Commands;
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
public class Wear extends StdCommand
{
	public Wear(){}

	private String[] access={"WEAR"};
	public String[] getAccessWords(){return access;}

	public boolean wear(MOB mob, Item item, int locationIndex, boolean quiet)
	{
		String str="<S-NAME> put(s) on <T-NAME>.";
		int msgType=CMMsg.MSG_WEAR;
		if(item.rawProperLocationBitmap()==Item.WORN_HELD)
		{
			str="<S-NAME> hold(s) <T-NAME>.";
			msgType=CMMsg.MSG_HOLD;
		}
		else
		if((item.rawProperLocationBitmap()==Item.WORN_WIELD)
		||(item.rawProperLocationBitmap()==(Item.WORN_HELD|Item.WORN_WIELD)))
		{
			str="<S-NAME> wield(s) <T-NAME>.";
			msgType=CMMsg.MSG_WIELD;
		}
		else
		if(locationIndex!=0)
			str="<S-NAME> put(s) <T-NAME> on <S-HIS-HER> "+Item.WORN_DESCS[locationIndex].toLowerCase()+".";
		CMMsg newMsg=CMClass.getMsg(mob,item,null,msgType,quiet?null:str);
		newMsg.setValue(locationIndex);
		if(mob.location().okMessage(mob,newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}


	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Wear what?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.firstElement() instanceof Item)
		{
			Item wearWhat = (Item)commands.firstElement();
			boolean quietly = false;
			int wearLocationIndex = 0;
			commands.removeElementAt(0);
			if(commands.size()>0)
			{
				if(commands.firstElement() instanceof Integer)
				{
					wearLocationIndex=((Integer)commands.firstElement()).intValue();
					commands.removeElementAt(0);
				}
				if((commands.size()>0)
				&&(commands.firstElement() instanceof String)
				&&(CMParms.indexOfIgnoreCase(Item.WORN_DESCS, (String)commands.firstElement())>0))
					wearLocationIndex=CMParms.indexOfIgnoreCase(Item.WORN_DESCS, (String)commands.firstElement());
				if((commands.size()>0)
				&&(commands.lastElement() instanceof String)
				&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")))
					quietly=true;
			}
			return wear(mob,wearWhat,wearLocationIndex,quietly);
		}

		// discover if a wear location was specified
		int wearLocationIndex=0;
		for(int i=commands.size()-2;i>0;i--)
			if(((String)commands.elementAt(i)).equalsIgnoreCase("on"))
			{
				if((i<commands.size()-2)&&((String)commands.elementAt(i+1)).equalsIgnoreCase("my"))
					commands.removeElementAt(i+1);
				String possibleWearLocation = CMParms.combine(commands, i+1).toLowerCase().trim();
				int possIndex = CMParms.indexOfIgnoreCase(Item.WORN_DESCS, possibleWearLocation);
				if(possIndex<0)
					for(int w=0;w<Item.WORN_DESCS.length;w++)
						if(Item.WORN_DESCS[w].toLowerCase().endsWith(" " + possibleWearLocation))
						{
							possIndex=w;
							break;
						}
				if(possIndex>0)
				{
					wearLocationIndex=possIndex;
					while(commands.size()>i)
						commands.removeElementAt(commands.size()-1);
					break;
				}
				else
				{
					mob.tell("You can't wear anything on your '"+possibleWearLocation+"'");
					return false;
				}
				// will always break out here, one way or the other.
			}
		Vector items=CMLib.english().fetchItemList(mob,mob,null,commands,Item.WORNREQ_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		{
			// sort hold-onlys down.
			Item I=null;
			for(int i=items.size()-2;i>=0;i--)
			{
				I=(Item)items.elementAt(i);
				if(I.rawProperLocationBitmap()==Item.WORN_HELD)
				{
					items.removeElementAt(i);
					items.addElement(I);
				}
			}
			for(int i=0;i<items.size();i++)
			{
				I=(Item)items.elementAt(i);
				if((items.size()==1)||(I.canWear(mob,0)))
					wear(mob,I,wearLocationIndex,false);
			}
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
