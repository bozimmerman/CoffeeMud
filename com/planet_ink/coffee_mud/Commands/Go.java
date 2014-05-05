package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Go extends StdCommand
{
	public Go(){}

	private final String[] access=_i(new String[]{"GO","WALK"});
	@Override public String[] getAccessWords(){return access;}

	protected Command stander=null;
	protected Vector ifneccvec=null;
	public void standIfNecessary(MOB mob, int metaFlags)
		throws java.io.IOException
	{
		if((ifneccvec==null)||(ifneccvec.size()!=2))
		{
			ifneccvec=new Vector();
			ifneccvec.addElement("STAND");
			ifneccvec.addElement("IFNECESSARY");
		}
		if(stander==null) stander=CMClass.getCommand("Stand");
		if((stander!=null)&&(ifneccvec!=null))
			stander.execute(mob,ifneccvec,metaFlags);
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		standIfNecessary(mob,metaFlags);
		if((commands.size()>3)
		&&(commands.firstElement() instanceof Integer))
		{
			return CMLib.tracking().walk(mob,
						((Integer)commands.elementAt(0)).intValue(),
						((Boolean)commands.elementAt(1)).booleanValue(),
						((Boolean)commands.elementAt(2)).booleanValue(),
						((Boolean)commands.elementAt(3)).booleanValue(),false);

		}
		final String whereStr=CMParms.combine(commands,1);
		final Room R=mob.location();
		if(R==null) return false;

		final boolean inAShip =(R instanceof SpaceShip)||(R.getArea() instanceof SpaceShip);
		final String validDirs = inAShip?Directions.SHIP_NAMES_LIST() : Directions.NAMES_LIST();

		int direction=-1;
		if(whereStr.equalsIgnoreCase("OUT"))
		{
			if(!CMath.bset(R.domainType(),Room.INDOORS))
			{
				mob.tell(_("You aren't indoors."));
				return false;
			}

			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if((R.getExitInDir(d)!=null)
				&&(R.getRoomInDir(d)!=null)
				&&(!CMath.bset(R.getRoomInDir(d).domainType(),Room.INDOORS)))
				{
					if(direction>=0)
					{
						mob.tell(_("Which way out?  Try @x1.",validDirs));
						return false;
					}
					direction=d;
				}
			}
			if(direction<0)
			{
				mob.tell(_("There is no direct way out of this place.  Try a direction."));
				return false;
			}
		}
		if(direction<0)
		{
			if(mob.isMonster())
				direction=Directions.getGoodDirectionCode(whereStr);
			else
				direction=(inAShip)?Directions.getGoodShipDirectionCode(whereStr):Directions.getGoodCompassDirectionCode(whereStr);
		}
		if(direction<0)
		{
			final Environmental E=R.fetchFromRoomFavorItems(null,whereStr);
			if(E instanceof Rideable)
			{
				final Command C=CMClass.getCommand("Enter");
				return C.execute(mob,commands,metaFlags);
			}
			if(E instanceof Exit)
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if(R.getExitInDir(d)==E)
					{ direction=d; break;}
			}
		}
		final String doing=(String)commands.elementAt(0);
		if(direction>=0)
			CMLib.tracking().walk(mob,direction,false,false,false,false);
		else
		{
			boolean doneAnything=false;
			for(int v=1;v<commands.size();v++)
			{
				int num=1;
				String s=(String)commands.elementAt(v);
				if(CMath.s_int(s)>0)
				{
					num=CMath.s_int(s);
					v++;
					if(v<commands.size())
						s=(String)commands.elementAt(v);
				}
				else
				if((s.length()>0) && (Character.isDigit(s.charAt(0))))
				{
					int x=1;
					while((x<s.length()-1)&&(Character.isDigit(s.charAt(x))))
						x++;
					num=CMath.s_int(s.substring(0,x));
					s=s.substring(x);
				}

				if(mob.isMonster())
					direction=Directions.getGoodDirectionCode(s);
				else
					direction=(inAShip)?Directions.getGoodShipDirectionCode(s):Directions.getGoodCompassDirectionCode(s);
				if(direction>=0)
				{
					doneAnything=true;
					for(int i=0;i<num;i++)
					{
						if(mob.isMonster())
						{
							if(!CMLib.tracking().walk(mob,direction,false,false,false,false))
								return false;
						}
						else
						{
							final Vector V=new Vector();
							V.addElement(doing);
							V.addElement(inAShip?Directions.getShipDirectionName(direction):Directions.getDirectionName(direction));
							mob.enqueCommand(V,metaFlags,0);
						}
					}
				}
				else
					break;
			}
			if(!doneAnything)
				mob.tell(_("@x1 which direction?\n\rTry @x2.",CMStrings.capitalizeAndLower(doing),validDirs.toLowerCase()));
		}
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		double cost=CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),100.0);
		if((mob!=null)&&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTORUN)))
			cost /= 4.0;
		return CMProps.getActionCost(ID(), cost);
	}

	@Override public boolean canBeOrdered(){return true;}
}
