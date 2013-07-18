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


import java.io.IOException;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Drop extends StdCommand
{
	public Drop(){}

	private final String[] access={"DROP","DRO"};
	public String[] getAccessWords(){return access;}

	private final static Class[][] internalParameters=new Class[][]{{Environmental.class,Boolean.class,Boolean.class,Boolean.class}};
	
	public boolean drop(MOB mob, Environmental dropThis, boolean quiet, boolean optimize, boolean intermediate)
	{
		final Room R=mob.location();
		if(R==null) return false;
		final int msgCode=(optimize?CMMsg.MASK_OPTIMIZE:0)|(intermediate?CMMsg.MASK_INTERMSG:0)|CMMsg.MSG_DROP;
		CMMsg msg=CMClass.getMsg(mob,dropThis,null,msgCode,quiet?null:"<S-NAME> drop(s) <T-NAME>.");
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(dropThis instanceof Coins)
				((Coins)dropThis).putCoinsBack();
			if(dropThis instanceof RawMaterial)
				((RawMaterial)dropThis).rebundle();
			return true;
		}
		if(dropThis instanceof Coins)
			((Coins)dropThis).putCoinsBack();
		if(dropThis instanceof RawMaterial)
			((RawMaterial)dropThis).rebundle();
		return false;
	}

	/**
	 * This method actually performs the drop, when the given parsed
	 * set of command-line words.  
	 * 
	 * The commands list is almost always the
	 * set of strings, starting with the access word that triggered the
	 * command.  This command does have a custom API however, that allows an Item, 
	 * and two Boolean objects to be substitued for the normal command strings.
	 * 
	 * This method is not allowed to be called until the player or mob has 
	 * satisfied the actionsCost requirements and the securityCheck
	 * @see com.planet_ink.coffee_mud.Commands.interfaces.Command#actionsCost(MOB, List)
	 * @see com.planet_ink.coffee_mud.Commands.interfaces.Command#securityCheck(MOB)
	 * 
	 * @param mob the mob or player issueing the command
	 * @param commands usually the command words and parameters; a set of strings
	 * @param metaFlags flags denoting how the command is being executed
	 * @return whether the command was successfully executed.  true if it was successfully dropped, false otherwise
	 * @throws java.io.IOException usually means the player has dropped carrier
	 */
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String whatToDrop=null;
		Item container=null;
		Vector V=new Vector();

		//this is probably unnecessary and can be removed
		if((commands.size()>=3)
		&&(commands.firstElement() instanceof Item)
		&&(commands.elementAt(1) instanceof Boolean)
		&&(commands.elementAt(2) instanceof Boolean))
		{
			return drop(mob,(Item)commands.firstElement(),
						((Boolean)commands.elementAt(1)).booleanValue(),
						((Boolean)commands.elementAt(2)).booleanValue(),
						false);
		}

		if(commands.size()<2)
		{
			mob.tell("Drop what?");
			return false;
		}
		commands.removeElementAt(0);

		// uncommenting this allows dropping directly from containers
		// "drop all sack" will no longer drop all of your "sack", but will drop 
		// all of the contents of your 1.sack, leaving the sack in inventory.
		//container=CMLib.english().possibleContainer(mob,commands,true,Wearable.FILTER_UNWORNONLY);


		int maxToDrop=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToDrop<0) return false;
		
		whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
		int addendum=1;
		String addendumStr="";
		boolean onlyGoldFlag=mob.hasOnlyGoldInInventory();
		Item dropThis=CMLib.english().bestPossibleGold(mob,null,whatToDrop);
		if(dropThis!=null)
		{
			if(((Coins)dropThis).getNumberOfCoins()<CMLib.english().numPossibleGold(mob,whatToDrop+addendumStr))
				return false;
			if(CMLib.flags().canBeSeenBy(dropThis,mob))
				V.addElement(dropThis);
		}
		boolean doBugFix = true;
		if(V.size()==0)
		while(doBugFix || ((allFlag)&&(addendum<=maxToDrop)))
		{
			doBugFix=false;
			dropThis=mob.fetchItem(container,Wearable.FILTER_UNWORNONLY,whatToDrop+addendumStr);
			if((dropThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				dropThis=mob.fetchItem(null,Wearable.FILTER_WORNONLY,whatToDrop);
				if(dropThis!=null)
				{
					if((!dropThis.amWearingAt(Wearable.WORN_HELD))&&(!dropThis.amWearingAt(Wearable.WORN_WIELD)))
					{
						mob.tell("You must remove that first.");
						return false;
					}
					CMMsg newMsg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
					if(mob.location().okMessage(mob,newMsg))
						mob.location().send(mob,newMsg);
					else
						return false;
				}
			}
			if((allFlag)&&(!onlyGoldFlag)&&(dropThis instanceof Coins)&&(whatToDrop.equalsIgnoreCase("all")))
				dropThis=null;
			else
			{
				if(dropThis==null) break;
				if((CMLib.flags().canBeSeenBy(dropThis,mob)||(dropThis instanceof Light))
				&&(!V.contains(dropThis)))
					V.addElement(dropThis);
			}
			addendumStr="."+(++addendum);
		}

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
			drop(mob,(Item)V.elementAt(i),false,true,false);
		mob.location().recoverRoomStats();
		mob.location().recoverRoomStats();
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}

	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		return Boolean.valueOf(drop(mob,(Environmental)args[0],((Boolean)args[1]).booleanValue(),((Boolean)args[2]).booleanValue(),((Boolean)args[3]).booleanValue()));
	}
	
}
