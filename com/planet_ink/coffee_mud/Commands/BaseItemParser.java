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
   Copyright 2000-2007 Bo Zimmerman

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
public class BaseItemParser extends StdCommand
{
    // mostly deprecated by the extension of util.EnglishParser
    public boolean hasOnlyGoldInInventory(MOB mob)
    {
        if(mob==null) return true;
        for(int i=0;i<mob.inventorySize();i++)
        {
            Item I=mob.fetchInventory(i);
            if(I.amWearingAt(Item.IN_INVENTORY)
            &&((I.container()==null)||(I.ultimateContainer().amWearingAt(Item.IN_INVENTORY)))
            &&(!(I instanceof Coins)))
                return false;
        }
        return true;
    }
    
    public int calculateMaxToGive(MOB mob, Vector commands, boolean breakPackages, Environmental checkWhat, boolean getOnly)
    {
        int maxToGive=Integer.MAX_VALUE;
        if((commands.size()>1)
        &&(CMLib.english().numPossibleGold(mob,CMParms.combine(commands,0))==0)
        &&(CMath.s_int((String)commands.firstElement())>0))
        {
            maxToGive=CMath.s_int((String)commands.firstElement());
            commands.setElementAt("all",0);
            if(breakPackages)
            {
                boolean throwError=false;
                if((commands.size()>2)&&("FROM".startsWith(((String)commands.elementAt(1)).toUpperCase())))
                {
                    throwError=true;
                    commands.removeElementAt(1);
                }
                String packCheckName=CMParms.combine(commands,1);
                Environmental fromWhat=null;
                if(checkWhat instanceof MOB)
                    fromWhat=mob.fetchInventory(null,packCheckName);
                else
                if(checkWhat instanceof Room)
                    fromWhat=((Room)checkWhat).fetchFromMOBRoomFavorsItems(mob,null,packCheckName,Item.WORNREQ_UNWORNONLY);
                if(fromWhat instanceof Item)
                {
                	int max=mob.maxCarry();
                	if(max>3000) max=3000;
                	if(maxToGive>max)
                	{
                		mob.tell("You can only handle "+max+" at a time.");
                		return -1;
                	}
                    Environmental toWhat=CMLib.materials().unbundle((Item)fromWhat,maxToGive);
                    if(toWhat==null)
                    {
                    	if(throwError)
                    	{
	                        mob.tell("You can't get anything from "+fromWhat.name()+".");
	                        return -1;
                    	}
                    }
                    else
                    if(getOnly&&mob.isMine(fromWhat)&&mob.isMine(toWhat))
                    {
                        mob.tell("Ok");
                        return -1;
                    }
                    else
                    if(commands.size()==1)
                        commands.addElement(toWhat.name());
                    else
                    {
                    	Object o=commands.firstElement();
                    	commands.clear();
                    	commands.addElement(o);
                    	commands.addElement(toWhat.name());
                    }
                }
                else
                if(throwError)
                {
                    mob.tell("You don't see '"+packCheckName+"' here.");
                    return -1;
                }
            }
        }
        return maxToGive;
    }
    
}
