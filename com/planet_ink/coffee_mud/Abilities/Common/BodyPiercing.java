package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class BodyPiercing extends CommonSkill
{
	public String ID() { return "BodyPiercing"; }
	public String name(){ return "Body Piercing";}
	private static final String[] triggerStrings = {"BODYPIERCE","BODYPIERCING"};
	public String[] triggerStrings(){return triggerStrings;}
	private String writing="";
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
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((target==null)
			||(mob.location()!=target.location())
			||(!Sense.canBeSeenBy(target,mob)))
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
		String part=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		
		int partNum=-1;
		StringBuffer allParts=new StringBuffer("");
		long[] piercable={Item.ON_HEAD,Item.ON_EARS,Item.ON_EYES,Item.ON_TORSO};
		for(int i=0;i<Item.wornLocation.length;i++)
		{
		    for(int ii=0;ii<piercable.length;ii++)
		        if(Item.wornCodes[i]==piercable[ii])
		        {
				    if(Item.wornLocation[i].equalsIgnoreCase(part))
				        partNum=i;
				    allParts.append(", "+Util.capitalize(Item.wornLocation[i].toLowerCase()));
				    break;
			    }
		}
		if(partNum<0)
		{
		    commonTell(mob,"'"+part+"' is not a valid location.  Valid locations include: "+allParts.toString().substring(2));
		    return false;
		}
		long wornCode=Item.wornCodes[partNum];
		String wornName=Item.wornLocation[partNum];
		
		MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.getWearPositions(wornCode)<=0)
		{
		    commonTell(mob,"That location is not available for piercing.");
		    return false;
		}
		if(target.freeWearPositions(wornCode)<=0)
		{
		    commonTell(mob,"That location is currently covered by something.");
		    return false;
		}
		
	    int numTattsDone=0;
		for(int i=0;i<target.numTattoos();i++)
		{
		    String tat=mob.fetchTattoo(i);
		    if(tat.toUpperCase().startsWith(wornName.toUpperCase()+":"))
	            numTattsDone++;
		}
		if(numTattsDone>=target.getWearPositions(Item.wornCodes[partNum]))
		{
		    commonTell(mob,"That location is already completely decorated.");
		    return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(wornName.toLowerCase().endsWith("s"))
			writing=wornName.toUpperCase()+":Pierced "+wornName.toLowerCase();
		else
			writing=wornName.toUpperCase()+":A pierced "+wornName.toLowerCase();
		verb="piercing "+target.name()+" on the "+wornName;
		displayText="You are "+verb;
		if(!profficiencyCheck(mob,0,auto)) writing="";
		int duration=30-mob.envStats().level();
		if(duration<6) duration=6;
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) piercing <T-NAMESELF> on the "+wornName.toLowerCase()+".");
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

