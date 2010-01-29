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
public class Put extends StdCommand
{
	public Put(){}

	private String[] access={"PUT","PU","P"};
	public String[] getAccessWords(){return access;}

	public void putout(MOB mob, Vector commands, boolean quiet)
	{
		if(commands.size()<3)
		{
			mob.tell("Put out what?");
			return;
		}
		commands.removeElementAt(1);
		commands.removeElementAt(0);

		Vector items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
		{
			Item I=(Item)items.elementAt(i);
			if((items.size()==1)||(I instanceof Light))
			{
				CMMsg msg=CMClass.getMsg(mob,I,null,CMMsg.MSG_EXTINGUISH,quiet?null:"<S-NAME> put(s) out <T-NAME>.");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
		}
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Put what where?");
			return false;
		}

		if(((String)commands.lastElement()).equalsIgnoreCase("on"))
		{
			commands.removeElementAt(commands.size()-1);
			Command C=CMClass.getCommand("Wear");
			if(C!=null) C.execute(mob,commands,metaFlags);
			return false;
		}
		
		if(commands.size()>=4)
		{
			String s=CMParms.combine(commands, 0).toLowerCase();
	        Wearable.CODES codes = Wearable.CODES.instance();
			for(int i=1;i<codes.total();i++)
				if(s.endsWith(" on "+codes.name(i).toLowerCase())||s.endsWith(" on my "+codes.name(i).toLowerCase()))
				{
					Command C=CMClass.getCommand("Wear");
					if(C!=null) C.execute(mob,commands,metaFlags);
					return false;
				}
		}

		if(((String)commands.elementAt(1)).equalsIgnoreCase("on"))
		{
			commands.removeElementAt(1);
			Command C=CMClass.getCommand("Wear");
			if(C!=null) C.execute(mob,commands,metaFlags);
			return false;
		}

		if(((String)commands.elementAt(1)).equalsIgnoreCase("out"))
		{
			putout(mob,commands,false);
			return false;
		}

		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("Where should I put the "+(String)commands.elementAt(0));
			return false;
		}

		Environmental container=CMLib.english().possibleContainer(mob,commands,false,Wearable.FILTER_ANY);
		if((container==null)||(!CMLib.flags().canBeSeenBy(container,mob)))
		{
			mob.tell("I don't see a "+(String)commands.lastElement()+" here.");
			return false;
		}

		int maxToPut=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
        if(maxToPut<0) return false;
        
		String thingToPut=CMParms.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToPut.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(4);}
		if(thingToPut.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);}
        boolean onlyGoldFlag=mob.hasOnlyGoldInInventory();
        Item putThis=CMLib.english().bestPossibleGold(mob,null,thingToPut);
        if(putThis!=null)
        {
            if(((Coins)putThis).getNumberOfCoins()<CMLib.english().numPossibleGold(mob,thingToPut))
                return false;
            if(CMLib.flags().canBeSeenBy(putThis,mob))
                V.addElement(putThis);
        }
		boolean doBugFix = true;
        if(V.size()==0)
		while(doBugFix || ((allFlag)&&(addendum<=maxToPut)))
		{
			doBugFix=false;
			putThis=mob.fetchCarried(null,thingToPut+addendumStr);
            if((allFlag)&&(!onlyGoldFlag)&&(putThis instanceof Coins)&&(thingToPut.equalsIgnoreCase("ALL")))
                putThis=null;
            else
            {
    			if(putThis==null) break;
    			if((CMLib.flags().canBeSeenBy(putThis,mob))
    			&&(!V.contains(putThis)))
    				V.addElement(putThis);
            }
			addendumStr="."+(++addendum);
		}

		if(V.contains(container))
			V.remove(container);

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			putThis=(Item)V.elementAt(i);
			String putWord=(container instanceof Rideable)?((Rideable)container).putString(mob):"in";
			CMMsg putMsg=CMClass.getMsg(mob,container,putThis,CMMsg.MASK_OPTIMIZE|CMMsg.MSG_PUT,"<S-NAME> put(s) <O-NAME> "+putWord+" <T-NAME>.");
			if(mob.location().okMessage(mob,putMsg))
				mob.location().send(mob,putMsg);
			if(putThis instanceof Coins)
				((Coins)putThis).putCoinsBack();
			if(putThis instanceof RawMaterial)
				((RawMaterial)putThis).rebundle();
		}
		mob.location().recoverRoomStats();
		mob.location().recoverRoomStats();
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
