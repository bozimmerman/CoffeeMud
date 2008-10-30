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
   Copyright 2000-2008 Bo Zimmerman

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
public class BodyPiercing extends CommonSkill
{
	public String ID() { return "BodyPiercing"; }
	public String name(){ return "Body Piercing";}
	private static final String[] triggerStrings = {"BODYPIERCE","BODYPIERCING"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ARTISTIC; }
	protected String writing="";
	MOB target=null;
	public BodyPiercing()
	{
		super();
		displayText="You are piercing...";
		verb="piercing";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping)&&(target!=null))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,"<S-NAME> mess(es) up the piercing on "+target.name()+".");
				else
				{
					commonEmote(mob,"<S-NAME> complete(s) the piercing on "+target.name()+".");
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
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify whom you want to pierce, and what body part to pierce.");
			return false;
		}
		String name=(String)commands.firstElement();
		String part=CMParms.combine(commands,1);
		
		MOB target=super.getTarget(mob,CMParms.makeVector(name),givenTarget);
		if(target==null) return false;
		
		int partNum=-1;
		StringBuffer allParts=new StringBuffer("");
		String[][] piercables={{"lip", "nose"},
				 			   {"ears","left ear","right ear"},
							   {"eyebrows"},
							   {"nipples","belly button"}};
		long[] piercable={Item.WORN_HEAD,
		        		  Item.WORN_EARS,
		        		  Item.WORN_EYES,
		        		  Item.WORN_TORSO};
		String fullPartName=null;
		for(int i=0;i<Item.WORN_CODES.length;i++)
		{
		    for(int ii=0;ii<piercable.length;ii++)
		        if(Item.WORN_CODES[i]==piercable[ii])
		        {
				    for(int iii=0;iii<piercables[ii].length;iii++)
				    {
				        if(piercables[ii][iii].startsWith(part.toLowerCase()))
				        {    partNum=i; fullPartName=piercables[ii][iii];}
					    allParts.append(", "+CMStrings.capitalizeAndLower(piercables[ii][iii]));
				    }
				    break;
			    }
		}
		if(partNum<0)
		{
		    commonTell(mob,"'"+part+"' is not a valid location.  Valid locations include: "+allParts.toString().substring(2));
		    return false;
		}
		long wornCode=Item.WORN_CODES[partNum];
		String wornName=fullPartName;

		if((target.getWearPositions(wornCode)<=0)
		||(target.freeWearPositions(wornCode,(short)(Short.MIN_VALUE+1),(short)0)<=0))
		{
		    commonTell(mob,"That location is not available for piercing.");
		    return false;
		}
		
	    int numTattsDone=0;
		for(int i=0;i<target.numTattoos();i++)
		{
		    String tat=target.fetchTattoo(i);
		    if(tat.toUpperCase().startsWith(wornName.toUpperCase()+":"))
	            numTattsDone++;
		}
		if(numTattsDone>=target.getWearPositions(Item.WORN_CODES[partNum]))
		{
		    commonTell(mob,"That location is already decorated.");
		    return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(wornName.toLowerCase().endsWith("s"))
			writing=Item.WORN_DESCS[partNum].toUpperCase()+":Pierced "+wornName.toLowerCase();
		else
			writing=Item.WORN_DESCS[partNum].toUpperCase()+":A pierced "+wornName.toLowerCase();
		verb="piercing "+target.name()+" on the "+wornName;
		displayText="You are "+verb;
		if(!proficiencyCheck(mob,0,auto)) writing="";
		int duration=getDuration(30,mob,1,6);
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) piercing <T-NAMESELF> on the "+wornName.toLowerCase()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
			BodyPiercing A=(BodyPiercing)mob.fetchEffect(ID());
			if(A!=null) A.target=target;
		}
		return true;
	}
}

