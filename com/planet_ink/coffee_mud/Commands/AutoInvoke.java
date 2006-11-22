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
	        &&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)
	        &&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PROPERTY))
	            abilities.addElement(A.ID());
	    }

	    Vector effects=new Vector();
	    for(int a=0;a<mob.numEffects();a++)
	    {
	        Ability A=mob.fetchEffect(a);
	        if((A!=null)
	        &&(abilities.contains(A.ID()))
	        &&(!A.savable()))
	            effects.addElement(A.ID());
	    }

	    StringBuffer str=new StringBuffer(getScr("AutoInvoke","aia"));
	    for(int a=0;a<abilities.size();a++)
	    {
	        Ability A=mob.fetchAbility((String)abilities.elementAt(a));
	        if(A!=null)
	        {
		        if(effects.contains(A.ID()))
		            str.append(CMStrings.padRight(A.Name(),20)+getScr("AutoInvoke","aia"));
		        else
		            str.append(CMStrings.padRight(A.Name(),20)+getScr("AutoInvoke","aia"));
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
			        if((A!=null)&&(CMLib.english().containsString(A.name(),s)))
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

	
	public boolean canBeOrdered(){return true;}
	
}