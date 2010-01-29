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
public class Draw extends Get
{
	public Draw(){}

	private String[] access={"DRAW"};
	public String[] getAccessWords(){return access;}

	public Vector getSheaths(MOB mob)
	{
		Vector sheaths=new Vector();
		if(mob!=null)
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(!I.amWearingAt(Wearable.IN_INVENTORY))
			&&(I instanceof Container)
			&&(((Container)I).capacity()>0)
			&&(((Container)I).containTypes()!=Container.CONTAIN_ANYTHING))
			{
				Vector contents=((Container)I).getContents();
				for(int c=0;c<contents.size();c++)
					if(contents.elementAt(c) instanceof Weapon)
					{
						sheaths.addElement(I);
						break;
					}
			}
		}
		return sheaths;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean quiet=false;
		boolean noerrors=false;
		boolean ifNecessary=false;
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("IFNECESSARY")))
		{
			quiet=true;
			noerrors=true;
			commands.removeElementAt(commands.size()-1);
			if((commands.size()>0)
			&&(((String)commands.lastElement()).equalsIgnoreCase("HELD")))
			{
				commands.removeElementAt(commands.size()-1);
				if(mob.fetchFirstWornItem(Wearable.WORN_HELD)!=null)
					return false;
			}
			else
			if(mob.fetchWieldedItem()!=null)
				return false;
		}
		else
		{
			if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")))
			{
				commands.removeElementAt(commands.size()-1);
				quiet=true;
			}
			if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("IFNECESSARY")))
			{
				ifNecessary=true;
				commands.removeElementAt(commands.size()-1);
				noerrors=true;
			}
		}

		boolean allFlag=false;
		Vector containers=new Vector();
		String containerName="";
		String whatToGet="";
		int c=0;
		Vector sheaths=getSheaths(mob);
		if(commands.size()>0)
			commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(sheaths.size()>0)
				containerName=((Item)sheaths.elementAt(0)).name();
			else
				containerName="a weapon";
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I instanceof Weapon)
				   &&(I.container()!=null)
				   &&(sheaths.contains(I.container())))
				{
					containers.addElement(I.container());
					whatToGet=I.name();
					break;
				}
			}
			if(whatToGet.length()==0)
				for(int i=0;i<mob.inventorySize();i++)
				{
					Item I=mob.fetchInventory(i);
					if(I instanceof Weapon)
					{
						whatToGet=I.name();
						break;
					}
				}
		}
		else
		{
			containerName=(String)commands.lastElement();
			commands.insertElementAt("all",0);
			containers=CMLib.english().possibleContainers(mob,commands,Wearable.FILTER_WORNONLY,true);
			if(containers.size()==0) containers=sheaths;
			whatToGet=CMParms.combine(commands,0);
			allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
			if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
			if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		}
		boolean doneSomething=false;
		while((c<containers.size())||(containers.size()==0))
		{
			Vector V=new Vector();
			Item container=null;
			if(containers.size()>0) container=(Item)containers.elementAt(c++);
			int addendum=1;
			String addendumStr="";
			boolean doBugFix = true;
			while(doBugFix || allFlag)
			{
				doBugFix=false;
				Environmental getThis=null;
				if((container!=null)&&(mob.isMine(container)))
				   getThis=mob.fetchInventory(container,whatToGet+addendumStr);
				if(getThis==null) break;
				if(getThis instanceof Weapon)
					V.addElement(getThis);
				addendumStr="."+(++addendum);
			}

			for(int i=0;i<V.size();i++)
			{
				Item getThis=(Item)V.elementAt(i);
				long wearCode=0;
				if(container!=null)	wearCode=container.rawWornCode();
				if((ifNecessary)
				&&(mob.freeWearPositions(Wearable.WORN_WIELD,(short)0,(short)0)==0)
				&&(mob.freeWearPositions(Wearable.WORN_HELD,(short)0,(short)0)==0))
					break;
				if(get(mob,container,getThis,quiet,"draw",false))
				{
					if(getThis.container()==null)
					{
						if(mob.freeWearPositions(Wearable.WORN_WIELD,(short)0,(short)0)==0)
						{
							CMMsg newMsg=CMClass.getMsg(mob,getThis,null,CMMsg.MSG_HOLD,null);
							if(mob.location().okMessage(mob,newMsg))
								mob.location().send(mob,newMsg);
						}
						else
						{
							CMMsg newMsg=CMClass.getMsg(mob,getThis,null,CMMsg.MSG_WIELD,null);
							if(mob.location().okMessage(mob,newMsg))
								mob.location().send(mob,newMsg);
						}
					}
				}
				if(container!=null)	container.setRawWornCode(wearCode);
				doneSomething=true;
			}

			if(containers.size()==0) break;
		}
		if((!doneSomething)&&(!noerrors))
		{
			if(containers.size()>0)
			{
				Item container=(Item)containers.elementAt(0);
				if(((Container)container).isOpen())
					mob.tell("You don't see that in "+container.name()+".");
				else
					mob.tell(container.name()+" is closed.");
			}
			else
				mob.tell("You don't see "+containerName+" here.");
		}
		return false;
	}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
