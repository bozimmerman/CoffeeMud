package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Allergies extends StdAbility
{
	public String ID() { return "Allergies"; }
	public String name(){ return "Allergies";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public int classificationCode(){return Ability.PROPERTY;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private Vector resourceAllergies=new Vector();
	private Vector raceAllergies=new Vector();
	private int allergicCheckDown=0;
	
	public void setMiscText(String newText)
	{
	    super.setMiscText(newText);
	    resourceAllergies.clear();
	    raceAllergies.clear();
	    Vector V=Util.parse(newText.toUpperCase().trim());
	    for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
	        if(V.contains(EnvResource.RESOURCE_DESCS[i]))
	            resourceAllergies.addElement(new Integer(EnvResource.RESOURCE_DATA[i][0]));
	    Race R=null;
        for(Enumeration r=CMClass.races();r.hasMoreElements();)
        {
            R=(Race)r.nextElement();
            if(V.contains(R.ID().toUpperCase()))
                raceAllergies.addElement(R);
        }
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if(!super.tick(ticking,tickID))
	        return false;
	    if((++allergicCheckDown)>10)
	    {
	        
	    }
	    return true;
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&(((String)commands.firstElement()).equals(givenTarget.name())))
				commands.removeElementAt(0);
			choice=Util.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=Util.combine(commands,1);
			while(commands.size()>1)
			    commands.removeElementAt(1);
		}
		MOB target=getTarget(mob,commands,givenTarget);
		
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null) return false;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			Vector allChoices=new Vector();
		    for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		        allChoices.addElement(EnvResource.RESOURCE_DESCS[i]);
		    Race R=null;
	        for(Enumeration r=CMClass.races();r.hasMoreElements();)
	        {
	            R=(Race)r.nextElement();
	            allChoices.addElement(R.ID().toUpperCase());
	        }
	        String allergies="";
	        if((choice.length()>0)&&(allChoices.contains(choice.toUpperCase())))
                allergies=choice.toUpperCase();
	        else
	        for(int i=0;i<allChoices.size();i++)
	            if((Dice.roll(1,allChoices.size(),0)==1)&&(!(((String)allChoices.elementAt(i)).equalsIgnoreCase(mob.charStats().getMyRace().ID().toUpperCase()))))
	                allergies+=" "+(String)allChoices.elementAt(i);
	        if(allergies.length()==0) return false;
	        
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(target.location().okMessage(target,msg))
			{
			    target.location().send(target,msg);
			    Ability A=(Ability)copyOf();
			    A.setMiscText(allergies.trim());
			    target.addNonUninvokableEffect(A);
			}
		}
        return success;
	}
}
