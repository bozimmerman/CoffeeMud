package com.planet_ink.coffee_mud.Abilities.Paladin;
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

public class Paladin_Courage extends Paladin
{
	public String ID() { return "Paladin_Courage"; }
	public String name(){ return "Paladin`s Courage";}
	public Paladin_Courage()
	{
		super();
		paladinsGroup=new Vector();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((invoker==null)||(invoker.getAlignment()<650))
			return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((msg.target()!=null)
		   &&(paladinsGroup.contains(msg.target()))
		   &&(!paladinsGroup.contains(msg.source()))
		   &&(msg.target() instanceof MOB)
		   &&(msg.source()!=invoker))
		{
			if((invoker.getAlignment()>650)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&((invoker==null)||(invoker.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
			{
				String str1=msg.tool().ID().toUpperCase();
				if((str1.indexOf("SPOOK")>=0)
				||(str1.indexOf("NIGHTMARE")>=0)
				||(str1.indexOf("FEAR")>=0))
				{
					MOB mob=(MOB)msg.target();
					mob.location().showSource(mob,null,CMMsg.MSG_OK_VISUAL,"Your courage protects you from the "+msg.tool().name()+" attack.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME>'s courage protects <S-HIM-HER> from the "+msg.tool().name()+" attack.");
					return false;
				}
			}
		}
		return true;
	}
}
