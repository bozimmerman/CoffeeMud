package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
Copyright 2000-2005 Bo Zimmerman

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

public class AutoInvoke extends StdCommand
{
	public AutoInvoke(){}

	private String[] access={getScr("AutoInvoke","cmd")};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
	    Vector abilities=new Vector();
	    for(int a=0;a<mob.numLearnedAbilities();a++)
	    {
	        Ability A=mob.fetchAbility(a);
	        if((A!=null)
	        &&(A.isAutoInvoked())
	        &&((A.classificationCode()&Ability.ALL_CODES)!=Ability.LANGUAGE)
	        &&((A.classificationCode()&Ability.ALL_CODES)!=Ability.PROPERTY))
	            abilities.addElement(A.ID());
	    }

	    Vector effects=new Vector();
	    for(int a=0;a<mob.numEffects();a++)
	    {
	        Ability A=mob.fetchEffect(a);
	        if((A!=null)
	        &&(abilities.contains(A.ID()))
	        &&(A.isBorrowed(mob)))
	            effects.addElement(A.ID());
	    }

	    StringBuffer str=new StringBuffer(getScr("AutoInvoke","aia"));
	    for(int a=0;a<abilities.size();a++)
	    {
	        Ability A=mob.fetchAbility((String)abilities.elementAt(a));
	        if(A!=null)
	        {
		        if(effects.contains(A.ID()))
		            str.append(Util.padRight(A.Name(),20)+getScr("AutoInvoke","aia"));
		        else
		            str.append(Util.padRight(A.Name(),20)+getScr("AutoInvoke","aia"));
	        }
	    }

	    mob.tell(str.toString());
	    if(mob.session()!=null)
	    {
		    String s=mob.session().prompt(getScr("AutoInvoke","toggle"),"");
		    Ability foundA=null;
		    if(s.length()>0)
		    {
		        for(int a=0;a<abilities.size();a++)
		        {
			        Ability A=mob.fetchAbility((String)abilities.elementAt(a));
			        if((A!=null)&&(A.name().equalsIgnoreCase(s)))
			        { foundA=A; break;}
		        }
		        if(foundA==null)
		        for(int a=0;a<abilities.size();a++)
		        {
			        Ability A=mob.fetchAbility((String)abilities.elementAt(a));
			        if((A!=null)&&(EnglishParser.containsString(A.name(),s)))
			        { foundA=A; break;}
		        }
		        if(foundA==null)
		            mob.tell(getScr("AutoInvoke","terror",s));
		        else
		        if(effects.contains(foundA.ID()))
		        {
		            foundA=mob.fetchEffect(foundA.ID());
		            if(foundA!=null)
		                mob.delEffect(foundA);
		            if(mob.fetchEffect(foundA.ID())!=null)
		                mob.tell(getScr("AutoInvoke","failed",foundA.name()));
		            else
		                mob.tell(getScr("AutoInvoke","deactivate",foundA.name()));
		        }
		        else
		        {
		            foundA.autoInvocation(mob);
		            if(mob.fetchEffect(foundA.ID())!=null)
		                mob.tell(getScr("AutoInvoke","inoked",foundA.name()));
		            else
		                mob.tell(getScr("AutoInvoke","ninvoked",foundA.name()));
		        }
		    }
	    }
		return false;
	}

	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}