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
   Copyright 2000-2006 Bo Zimmerman

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
public class Empty extends Drop
{
	public Empty(){}

	private String[] access={getScr("Empty","cmd1"),getScr("Empty","cmd2")};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatToDrop=null;
		Environmental target=mob;
		Vector V=new Vector();
		if(commands.size()<2)
		{
			mob.tell(getScr("Empty","emptywhere"));
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()>1)
		{
			String s=(String)commands.lastElement();
			if(s.equalsIgnoreCase(getScr("Empty","here"))) target=mob.location();
			else
			if(s.equalsIgnoreCase(getScr("Empty","me"))) target=mob;
			else
			if(s.equalsIgnoreCase(getScr("Empty","self"))) target=mob;
			else
			if(getScr("Empty","inventory").startsWith(s.toUpperCase())) target=mob;
			else
			if(s.equalsIgnoreCase(getScr("Empty","floor"))) target=mob.location();
			else
			if(s.equalsIgnoreCase(getScr("Empty","ground"))) target=mob.location();
			else
			{
				target=CMLib.english().possibleContainer(mob,commands,false,Item.WORNREQ_UNWORNONLY);
				if(target==null) 
					target=mob.location().fetchFromRoomFavorItems(null,s,Item.WORNREQ_UNWORNONLY);
				else
					commands.addElement(getScr("Empty","delme"));
			}
			if(target!=null)
				commands.removeElementAt(commands.size()-1);
		}
		
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(getScr("Empty","where"));
			return false;
		}

        int maxToDrop=super.calculateMaxToGive(mob,commands,true,mob);
        if(maxToDrop<0) return false;

		whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase(getScr("Empty","all")):false;
		if(whatToDrop.toUpperCase().startsWith(getScr("Empty","alldot"))){ allFlag=true; whatToDrop=getScr("Empty","allup")+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(getScr("Empty","dotall"))){ allFlag=true; whatToDrop=getScr("Empty","allup")+whatToDrop.substring(0,whatToDrop.length()-4);}
		int addendum=1;
		String addendumStr="";
		Drink drink=null;
		do
		{
			Item dropThis=mob.fetchCarried(null,whatToDrop+addendumStr);
			if((dropThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				dropThis=mob.fetchWornItem(whatToDrop);
				if((dropThis!=null)&&(dropThis instanceof Container))
				{
					if((!dropThis.amWearingAt(Item.WORN_HELD))&&(!dropThis.amWearingAt(Item.WORN_WIELD)))
					{
						mob.tell(getScr("Empty","removefirst"));
						return false;
					}
					CMMsg newMsg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
					if(mob.location().okMessage(mob,newMsg))
						mob.location().send(mob,newMsg);
					else
						return false;
				}
			}
			if(dropThis==null) break;
			if(dropThis instanceof Drink)
				drink=(Drink)dropThis;
			if((CMLib.flags().canBeSeenBy(dropThis,mob))
			&&(dropThis instanceof Container)
			&&(!V.contains(dropThis)))
				V.addElement(dropThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDrop));

		String str=getScr("Empty","emptysmsg");
		if(target instanceof Room) str+=getScr("Empty","msghere");
		else
		if(target instanceof MOB) str+=".";
		else str+=getScr("Empty","msginto")+target.Name()+".";
		
		if((V.size()==0)&&(drink!=null))
		{
			mob.tell(drink.name()+getScr("Empty","mustpour"));
			return false;
		}
		
		if(V.size()==0)
			mob.tell(getScr("Empty","nocarry"));
		else
		if((V.size()==1)&&(V.firstElement()==target))
			mob.tell(getScr("Empty","noself"));
		else
		if((V.size()==1)&&(V.firstElement() instanceof Drink)&&(!((Drink)V.firstElement()).containsDrink()))
			mob.tell(mob,(Drink)V.firstElement(),null,getScr("Empty","emptyalready"));
		else
		for(int v=0;v<V.size();v++)
		{
			Container C=(Container)V.elementAt(v);
			if(C==target) continue;
			Vector V2=C.getContents();
            
            boolean skipMessage=false;
            if((C instanceof Drink)&&(((Drink)C).containsDrink()))
            {
                if(target instanceof Drink)
                {
                    Command C2=CMClass.getCommand("Pour");
                    C2.execute(mob,CMParms.makeVector(getScr("Empty","cmdpour"),"$"+C.Name()+"$","$"+target.Name()+"$"));
                    skipMessage=true;
                }
                else
                {
                    ((Drink)C).setLiquidRemaining(0);
                    if(((Drink)C).disappearsAfterDrinking())
                        C.destroy();
                }
            }
			CMMsg msg=CMClass.getMsg(mob,C,CMMsg.MSG_QUIETMOVEMENT,str);
            Room R=mob.location();
			if(skipMessage||(R.okMessage(mob,msg)))
			{
                if(!skipMessage) R.send(mob,msg);
				for(int v2=0;v2<V2.size();v2++)
				{
					Item I=(Item)V2.elementAt(v2);
					if(I instanceof Coins) ((Coins)I).setContainer(null);
					if(((I.container()==null)||(Get.get(mob,C,I,true,null,true)))
					&&(I.container()==null))
					{
						if(target instanceof Room)
							drop(mob,I,true,true);
						else
						if(target instanceof Container)
						{
							CMMsg putMsg=CMClass.getMsg(mob,target,I,CMMsg.MASK_OPTIMIZE|CMMsg.MSG_PUT,null);
							if(R.okMessage(mob,putMsg))
								R.send(mob,putMsg);
						}
						if(I instanceof Coins)
							((Coins)I).putCoinsBack();
						if(I instanceof RawMaterial)
							((RawMaterial)I).rebundle();
					}
				}
			}
		}
		mob.location().recoverRoomStats();
		mob.location().recoverRoomStats();
		return false;
	}
    public double combatActionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
