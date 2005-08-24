package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_Tattoo extends Property
{
	public String ID() { return "Prop_Tattoo"; }
	public String name(){ return "A Tattoo";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}

	public static Vector getTattoos(MOB mob)
	{
		Vector tattos=new Vector();
		Ability A=mob.fetchAbility("Prop_Tattoo");
		if(A!=null)
			tattos=Util.parseSemicolons(A.text().toUpperCase(),true);
		else
		{
			A=mob.fetchEffect("Prop_Tattoo");
			if(A!=null)
				tattos=Util.parseSemicolons(A.text().toUpperCase(),true);
		}
		return tattos;
	}

	public void setMiscText(String text)
	{
		if(affected instanceof MOB)
		{
			MOB M=(MOB)affected;
			Vector V=Util.parseSemicolons(text,true);
			for(int v=0;v<V.size();v++)
				M.addTattoo((String)V.elementAt(v));
		}
		borrowed=true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		/*if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;

			if((msg.amITarget(mob))
			   &&(msg.targetMinor()==CMMsg.TYP_LOOK)
			   &&(text().length()>0))
			{
				Vector V=getTattoos(msg.source());
				String tattoos="";
				if(V.size()==1)
				   tattoos=(String)V.elementAt(0);
				else
				for(int v=0;v<V.size();v++)
					if(v==0)
						tattoos+=(String)V.elementAt(v);
					else
					if(v==(V.size()-1))
					   tattoos+=", and "+(String)V.elementAt(v);
					else
					   tattoos+=", "+(String)V.elementAt(v);
				if(tattoos.length()>0)
					msg.addTrailerMsg(new FullMsg(msg.source(),mob,null,CMMsg.MSG_OK_VISUAL,"<T-NAME> has the following tattoos: "+tattoos.toLowerCase(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}*/
		super.executeMsg(myHost,msg);
	}
}
