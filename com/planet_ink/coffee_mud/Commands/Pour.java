package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2000-2012 Bo Zimmerman

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
public class Pour extends StdCommand
{
    public Pour(){}

    private final String[] access={"POUR"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        if(commands.size()<2)
        {
            mob.tell("Pour what, into what?");
            return false;
        }
        commands.removeElementAt(0);
        Environmental fillFromThis=null;
        String thingToFillFrom=(String)commands.elementAt(0);
        fillFromThis=mob.fetchCarried(null,thingToFillFrom);
        if((fillFromThis==null)||(!CMLib.flags().canBeSeenBy(fillFromThis,mob)))
        {
            mob.tell("You don't seem to have '"+thingToFillFrom+"'.");
            return false;
        }
        commands.removeElementAt(0);

        if((commands.size()>1)&&(((String)commands.firstElement())).equalsIgnoreCase("into"))
            commands.removeElementAt(0);

        if(commands.size()<1)
        {
            mob.tell("Into what should I pour the "+thingToFillFrom+"?");
            return false;
        }

        String thingToFill=CMParms.combine(commands,0);
        Environmental fillThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFill,Wearable.FILTER_ANY);
        Item out=null;
        if((fillThis==null)&&(thingToFill.equalsIgnoreCase("out")))
        {
            out=CMClass.getItem("StdDrink");
            ((Drink)out).setLiquidHeld(999999);
            ((Drink)out).setLiquidRemaining(0);
            out.setDisplayText("");
            out.setName("out");
            mob.location().addItem(out,ItemPossessor.Expire.Resource);
            fillThis=out;
        }
        if((fillThis==null)
        ||(!CMLib.flags().canBeSeenBy(fillThis,mob)))
            mob.tell("I don't see '"+thingToFill+"' here.");
        else
        {
            CMMsg fillMsg=CMClass.getMsg(mob,fillThis,fillFromThis,CMMsg.MSG_FILL,(out==null)?"<S-NAME> pour(s) <O-NAME> into <T-NAME>.":"<S-NAME> pour(s) <O-NAME> <T-NAME>.");
            if(mob.location().okMessage(mob,fillMsg))
                mob.location().send(mob,fillMsg);
        }
        if(out!=null) out.destroy();
        return false;
    }
    public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
    public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
    public boolean canBeOrdered(){return true;}

    
}
