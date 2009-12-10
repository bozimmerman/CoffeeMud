package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_ReqTattoo extends Property
{
	public String ID() { return "Prop_ReqTattoo"; }
	public String name(){ return "Tattoo Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	private String themsg="";
	
	public String accountForYourself()
	{
		return "Ownership restricted as follows: "+CMLib.masking().maskDesc(text());
	}
	
	public String text(){ return themsg+";"+super.text();}
	public void setMiscText(String newText)
	{
		themsg="";
		int x=newText.indexOf(";");
		if(x<0) 
			super.setMiscText(newText);
		else
		if(newText.substring(0,x).indexOf("+")>=0)
			super.setMiscText(newText);
		else
		if(newText.substring(0,x).indexOf("-")>=0)
			super.setMiscText(newText);
		else
		{
			themsg=newText.substring(0,x).trim();
			super.setMiscText(newText.substring(x+1));
		}
	}
	
	public Vector getMask(boolean[] flags)
	{
		Vector V=CMParms.parse(miscText.toUpperCase());
		String s=null;
		for(int v=V.size()-1;v>=1;v--)
		{
			s=(String)V.elementAt(v);
			if(s.startsWith("NOFOL"))
			{
				flags[0]=true;
				V.removeElementAt(v);
			}
			else
			if(s.startsWith("NOSNEAK"))
			{
				flags[1]=true;
				V.removeElementAt(v);
			}
			else
			if("+-".indexOf(s.charAt(0))<0)
			{
				V.removeElementAt(v);
				V.setElementAt(((String)V.elementAt(v-1))+" "+s,v-1);
			}
		}
		return V;
	}
	
	public boolean passesMuster(Vector mask, boolean[] flags, MOB mob)
	{
		if(mob==null) return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(flags[1]))
			return true;
		int allFlag=0;
		String s=null;
		for(int v=0;v<mask.size();v++)
		{
			s=(String)mask.elementAt(v);
			if(s.equals("+ALL"))
				allFlag=1;
			else
			if(s.equals("+NONE"))
				allFlag=0;
			else
			if(s.equals("-ALL"))
				allFlag=-1;
			else
			if(s.startsWith("+")||s.startsWith("-"))
			{
				char c=s.charAt(0);
				boolean found=((c=='+')||(c=='-'))?
					(mob.fetchTattoo(s.substring(1))!=null)
					:(mob.fetchTattoo(s)!=null);
				switch(allFlag)
				{
				case 0: // +NONE -- HAS/LACKS ALL
					if(c=='-')
					{
						if(found) 
							return false;
					}
					else
					if(!found) 
						return false;
					break;
				case 1: // +ALL -- LACKS ANY
					if(c!='+') // ----------------
					{
						if(!found)
							return true;
					}
					else
					if(found)
						return true;
					break;
				case -1: // -ALL -- HAS ANY
					if(c!='-') // ++++++++++++++++++++
					{
						if(found)
							return true;
					}
					else
					if(found)
						return false;
					break;
				}
			}
		}
		if(allFlag<0) return false; // if not returned, does not have any of them
		if(allFlag==0) return true; // none were missing, so its all good.
		if(allFlag>0) return true; // all were missing, so its all good.
		return true;
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(msg.target()!=null)
		&&(!CMLib.flags().isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			if(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			||((msg.target() instanceof Item)&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_SIT))))
			{
				boolean[] flags=new boolean[2]; 
				Vector V=getMask(flags);
				HashSet H=new HashSet();
				if(flags[0])
					H.add(msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					HashSet H2=(HashSet)H.clone();
					for(Iterator e=H2.iterator();e.hasNext();)
						((MOB)e.next()).getRideBuddies(H);
				}
				for(Iterator e=H.iterator();e.hasNext();)
				{
				    Environmental E=(Environmental)e.next();
				    if((E instanceof MOB)
					&&(passesMuster(V,flags,(MOB)E)))
						return super.okMessage(myHost,msg);
				}
				if(msg.target() instanceof Room)
					msg.source().tell(themsg.length()==0?"You have not been granted authorization to go that way.":themsg);
				else
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,affected,CMMsg.MSG_OK_ACTION,themsg.length()==0?"<O-NAME> flashes and flies out of <S-HIS-HER> hands!":themsg);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
