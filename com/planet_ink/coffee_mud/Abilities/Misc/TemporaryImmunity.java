package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class TemporaryImmunity extends StdAbility
{
	public String ID() { return "TemporaryImmunity"; }
	public String name(){ return "Temporary Immunity";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public boolean canBeUninvoked(){return true;}
	public boolean isAutoInvoked(){return true;}
	public final static long IMMUNITY_TIME=Tickable.TIME_MILIS_PER_MUDHOUR*60;
    protected DVector set=new DVector(2);

    public TemporaryImmunity()
    {
        super();

        tickDown = 10;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB)
		&&((--tickDown)==0))
		{
			tickDown=10;
			makeLongLasting();
			for(int s=set.size()-1;s>=0;s--)
			{
				Long L=(Long)set.elementAt(s,2);
				if((System.currentTimeMillis()-L.longValue())>IMMUNITY_TIME)
					set.removeElementAt(s);
			}

			if(set.size()==0){ unInvoke(); return false;}
		}
		return super.tick(ticking,tickID);
	}

	public String text()
	{
		if(set.size()==0) return "";
		StringBuffer str=new StringBuffer("");
		for(int s=0;s<set.size();s++)
			str.append(((String)set.elementAt(s,1))+"/"+((Long)set.elementAt(s,2)).longValue()+";");
		return str.toString();
	}

	public void setMiscText(String str)
	{
		if(str.startsWith("+"))
		{
			str=str.substring(1);
			if(set.indexOf(str)>=0)
				set.setElementAt(set.indexOf(str),2,Long.valueOf(System.currentTimeMillis()));
			else
				set.addElement(str,Long.valueOf(System.currentTimeMillis()));
		}
		else
		{
			set.clear();
			Vector V=CMParms.parseSemicolons(str,true);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				int x=s.indexOf("/");
				if(x>0)
					set.addElement(s.substring(0,x),Long.valueOf(CMath.s_long(s.substring(x+1))));
			}
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(!mob.amDead())
		&&(msg.tool() instanceof Ability)
		&&(set.contains(msg.tool().ID())))
		{
            if(msg.source()!=msg.target())
    			mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+msg.tool().name()+".");
			return false;
		}
		return true;
	}
}
