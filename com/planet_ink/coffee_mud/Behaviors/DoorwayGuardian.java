package com.planet_ink.coffee_mud.Behaviors;
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
public class DoorwayGuardian extends StdBehavior
{
	public String ID(){return "DoorwayGuardian";}

	private boolean nosneak=false;
	private boolean always=false;
	private String mask=null;
	private static final String DEFAULT_MESSAGE="<S-NAME> won't let <T-NAME> through there.";
	private String message=DEFAULT_MESSAGE;
	Vector<Integer> dirs=new Vector<Integer>();

	public void setParms(String parms)
	{
		super.setParms(parms);
		int x=parms.indexOf(';');
		if(x>=0)
		{
			message=parms.substring(x+1);
			parms=parms.substring(0,x);
		}
		else
			message=DEFAULT_MESSAGE;
		Vector V=CMParms.parse(parms);
		nosneak=false;
		always=false;
		mask=null;
		dirs.clear();
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("NOSNEAK"))
			{
				nosneak=true;
				V.removeElementAt(v);
			}
			else
			if(s.equalsIgnoreCase("ALWAYS"))
			{
				always=true;
				V.removeElementAt(v);
			}
			else
			{
				int dir=Directions.getGoodDirectionCode(s);
				if(dir>=0) 
				{
					dirs.addElement(Integer.valueOf(dir));
					V.removeElementAt(v);
				}
			}
		}
		if(V.size()>0)
			mask=CMParms.combineWithQuotes(V,0);
	}
	
	public Exit[] getParmExits(MOB monster)
	{
		if(monster==null) return null;
		if(monster.location()==null) return null;
		if(getParms().length()==0) return null;
		Room room=monster.location();
		if(dirs!=null)
		for(Enumeration<Integer> dirE=dirs.elements();dirE.hasMoreElements();)
		{
			int dir=dirE.nextElement().intValue();
			if(room.getExitInDir(dir)!=null)
			{
				Exit[] exits={room.getExitInDir(dir),room.getReverseExit(dir)};
				return exits;
			}
		}
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit E=room.getExitInDir(d);
			if((E!=null)&&(E.hasADoor()))
			{
				Exit[] exits={E,room.getReverseExit(d)};
				return exits;
			}
		}
		return null;
	}


	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if(!super.okMessage(oking,msg)) return false;
		MOB mob=msg.source();
		if(always)
		{
			if(!canActAtAll(oking)) return true;
		}
		else
		if(!canFreelyBehaveNormal(oking)) 
			return true;
		MOB monster=(MOB)oking;
		if((mob.location()==monster.location())
		&&(mob!=monster)
		&&(msg.target()!=null)
		&&(!BrotherHelper.isBrother(mob,monster,false))
        &&(CMLib.flags().canSenseMoving(mob,monster)||nosneak))
		{
			if(msg.target() instanceof Exit)
			{
				Exit exit=(Exit)msg.target();
				Exit texit[]=getParmExits(monster);
				if((texit!=null)
				&&(texit[0]!=exit)
				&&(texit[1]!=exit))
					return true;

				if((msg.targetMinor()!=CMMsg.TYP_CLOSE)
				&&(msg.targetMinor()!=CMMsg.TYP_LOCK)
				&&((mask==null)||CMLib.masking().maskCheck(mask,mob,false)))
				{
					CMMsg msgs=CMClass.getMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,message);
					if(monster.location().okMessage(monster,msgs))
					{
						monster.location().send(monster,msgs);
						return false;
					}
				}
			}
			else
			if((msg.tool()!=null)
			&&(msg.target() instanceof Room)
			&&(msg.tool() instanceof Exit)
			&&((mask==null)||CMLib.masking().maskCheck(mask,mob,false)))
			{
				Exit exit=(Exit)msg.tool();
				Exit texit[]=getParmExits(monster);
				if((texit!=null)
				&&(texit[0]!=exit)
				&&(texit[1]!=exit))
					return true;

				CMMsg msgs=CMClass.getMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,message);
				if(monster.location().okMessage(monster,msgs))
				{
					monster.location().send(monster,msgs);
					return false;
				}
			}
		}
		return true;
	}
}
