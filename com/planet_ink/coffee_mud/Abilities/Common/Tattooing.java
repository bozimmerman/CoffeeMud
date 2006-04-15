package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Tattooing extends CommonSkill
{
	public String ID() { return "Tattooing"; }
	public String name(){ return "Tattooing";}
	private static final String[] triggerStrings = {"TATTOO","TATTOOING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected String writing="";
	MOB target=null;
	public Tattooing()
	{
		super();
		displayText="You are tattooing...";
		verb="tattooing";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping)&&(target!=null))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,"<S-NAME> mess(es) up the tattoo on "+target.name()+".");
				else
				{
					commonEmote(mob,"<S-NAME> complete(s) the tattoo on "+target.name()+".");
				    target.addTattoo(writing);
				}
			}
		}
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((target==null)
			||(mob.location()!=target.location())
			||(!CMLib.flags().canBeSeenBy(target,mob)))
			{aborted=true; unInvoke(); return false;}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<3)
		{
			commonTell(mob,"You must specify whom you want to tattoo, what body part to tattoo, and what the tattoo looks like. Use 'REMOVE' as the description to remove a tattoo.");
			return false;
		}
		String whom=(String)commands.firstElement();
		commands.removeElementAt(0);
		String part=(String)commands.firstElement();
		commands.removeElementAt(0);
		String message=CMParms.combine(commands,0);
		commands.clear();
		commands.addElement(whom);
		
		int partNum=-1;
		StringBuffer allParts=new StringBuffer("");
		long[] tattoable={Item.WORN_ARMS,
		        		  Item.WORN_LEGS,
		        		  Item.WORN_HANDS,
		        		  Item.WORN_HEAD,
		        		  Item.WORN_FEET,
		        		  Item.WORN_LEFT_WRIST,
		        		  Item.WORN_RIGHT_WRIST,
		        		  Item.WORN_NECK,
		        		  Item.WORN_TORSO};
		for(int i=0;i<Item.WORN_DESCS.length;i++)
		{
		    for(int ii=0;ii<tattoable.length;ii++)
		        if(Item.WORN_CODES[i]==tattoable[ii])
		        {
				    if(Item.WORN_DESCS[i].equalsIgnoreCase(part))
				        partNum=i;
				    allParts.append(", "+CMStrings.capitalizeAndLower(Item.WORN_DESCS[i].toLowerCase()));
				    break;
			    }
		}
		if(partNum<0)
		{
		    commonTell(mob,"'"+part+"' is not a valid location.  Valid locations include: "+allParts.toString().substring(2));
		    return false;
		}
		long wornCode=Item.WORN_CODES[partNum];
		String wornName=Item.WORN_DESCS[partNum];
		
		MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.getWearPositions(wornCode)<=0)
		{
		    commonTell(mob,"That location is not available for tattooing.");
		    return false;
		}
		if(target.freeWearPositions(wornCode,(short)0)<=0)
		{
		    commonTell(mob,"That location is currently covered by something.");
		    return false;
		}
		
	    int numTattsDone=0;
	    int tatToRemove=-1;
		for(int i=0;i<target.numTattoos();i++)
		{
		    String tat=target.fetchTattoo(i);
		    if(tat.toUpperCase().startsWith(wornName.toUpperCase()+":"))
		    {
	            numTattsDone++;
	            if(tat.toUpperCase().substring(wornName.length()+1).toUpperCase().startsWith("A TATTOO OF"))
		            tatToRemove=i;
		    }
		}
		if("REMOVE".startsWith(message.toUpperCase()))
		{
		    if(tatToRemove<0)
		    {
			    commonTell(mob,"There is no tattoo there to remove.");
			    return false;
		    }
		}
		else
		if(numTattsDone>=target.getWearPositions(Item.WORN_CODES[partNum]))
		{
		    commonTell(mob,"That location is already completely decorated.");
		    return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		writing=wornName.toUpperCase()+":A tattoo of "+message;
		verb="tattooing "+target.name()+" on the "+wornName;
		displayText="You are "+verb;
		if(!profficiencyCheck(mob,0,auto)) writing="";
		int duration=30-mob.envStats().level();
		if(duration<6) duration=6;
		String str="<S-NAME> start(s) tattooing "+message+" on <T-YOUPOSS> "+wornName.toLowerCase()+".";
		if("REMOVE".startsWith(message.toUpperCase()))
		    str="<S-NAME> remove(s) the tattoo on <T-YOUPOSS> "+wornName.toLowerCase()+".";
		
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,str);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if("REMOVE".startsWith(message.toUpperCase()))
			    target.delTattoo(target.fetchTattoo(tatToRemove));
			else
			{
				beneficialAffect(mob,mob,asLevel,duration);
				Tattooing A=(Tattooing)mob.fetchEffect(ID());
				if(A!=null) A.target=target;
			}
		}
		return true;
	}
}
