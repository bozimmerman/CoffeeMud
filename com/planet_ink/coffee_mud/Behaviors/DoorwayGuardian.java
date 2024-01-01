package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Pattern;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class DoorwayGuardian extends StdBehavior
{
	@Override
	public String ID()
	{
		return "DoorwayGuardian";
	}

	private boolean nosneak=false;
	private boolean always=false;
	private MaskingLibrary.CompiledZMask mask=null;
	private static final String DEFAULT_MESSAGE="<S-NAME> won't let <T-NAME> through there.";
	//private static final String CHALLENGE_SAY="Halt! Who goes there?!";
	private String message=DEFAULT_MESSAGE;
	//private String challenge=CHALLENGE_SAY;
	Vector<Integer> dirs=new Vector<Integer>();
	private volatile Pair<Room,Set<Exit>> pexits = null;

	@Override
	public String accountForYourself()
	{
		return "doorway guarding";
	}

	@Override
	public void setParms(String parms)
	{
		super.setParms(parms);
		final String[] parts=parms.split("(?<!\\\\)" + Pattern.quote(";"),3);
		if(parts.length>1)
		{
			message=CMStrings.replaceAll(parts[1],"\\;",";");
			//if(parts.length>2)
			//	challenge=CMStrings.replaceAll(parts[2],"\\;",";");
			parms=CMStrings.replaceAll(parts[0],"\\;",";");
		}
		else
		{
			message=DEFAULT_MESSAGE;
			//challenge=CHALLENGE_SAY;
		}
		final Vector<String> V=CMParms.parse(parms);
		nosneak=false;
		always=false;
		mask=null;
		dirs.clear();
		for(int v=V.size()-1;v>=0;v--)
		{
			final String s=V.elementAt(v);
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
				final int dir=CMLib.directions().getGoodDirectionCode(s);
				if(dir>=0)
				{
					dirs.addElement(Integer.valueOf(dir));
					V.removeElementAt(v);
				}
			}
		}
		if(V.size()>0)
			mask=CMLib.masking().maskCompile(CMParms.combineQuoted(V,0));
	}

	public Set<Exit> getParmExits(final MOB monster)
	{
		if(monster==null)
			return null;
		final Room room=monster.location();
		if(room==null)
			return null;
		Pair<Room, Set<Exit>> p1 = this.pexits;
		if((p1 == null)||(p1.first != room))
		{
			synchronized(this)
			{
				p1 = this.pexits;
				if((p1 == null)||(p1.first != room))
				{
					final Set<Exit> exits = new HashSet<Exit>();
					if((dirs!=null) && (dirs.size()>0))
					{
						for (final Integer integer : dirs)
						{
							final int dir=integer.intValue();
							final Exit E1 = room.getExitInDir(dir);
							if(E1!=null)
							{
								exits.add(E1);
								final Exit E2 = room.getReverseExit(dir);
								if(E2 != null)
									exits.add(E2);
							}
						}
					}
					else
					{
						for(int dir=Directions.NUM_DIRECTIONS()-1;dir>=0;dir--)
						{
							final Exit E1=room.getExitInDir(dir);
							if(E1!=null)
							{
								exits.add(E1);
								final Exit E2 = room.getReverseExit(dir);
								if(E2 != null)
									exits.add(E2);
							}
						}
					}
					this.pexits = new Pair<Room,Set<Exit>>(room,exits);
				}
			}
		}
		final Pair<Room, Set<Exit>> p3 = this.pexits;
		return (p3 == null) ? null : p3.second;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		final MOB mob=msg.source();
		if(always)
		{
			if(!canActAtAll(host))
				return true;
		}
		else
		if(!canFreelyBehaveNormal(host))
			return true;
		final MOB monster=(MOB)host;
		if((mob.location()==monster.location())
		&&(mob!=monster)
		&&(msg.target()!=null)
		&&(!BrotherHelper.isBrother(mob,monster,false))
		&&(CMLib.flags().canSenseEnteringLeaving(mob,monster)||nosneak))
		{
			if(msg.target() instanceof Exit)
			{
				final Exit exit=(Exit)msg.target();
				final Set<Exit> texit=getParmExits(monster);
				if((texit!=null)&&(!texit.contains(exit)))
					return true;

				if((msg.targetMinor()!=CMMsg.TYP_CLOSE)
				&&(msg.targetMinor()!=CMMsg.TYP_LOCK)
				&&((mask==null)||CMLib.masking().maskCheck(mask,mob,false)))
				{
					final CMMsg msgs=CMClass.getMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,message);
					if(monster.location().okMessage(monster,msgs))
					{
						monster.location().send(monster,msgs);
						return false;
					}
				}
			}
			else
			if((msg.target() instanceof Room)
			&&(msg.tool() instanceof Exit)
			&&((mask==null)||CMLib.masking().maskCheck(mask,mob,false)))
			{
				final Exit exit=(Exit)msg.tool();
				final Set<Exit> texit=getParmExits(monster);
				if((texit!=null)&&(!texit.contains(exit)))
					return true;

				final CMMsg msgs=CMClass.getMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,message);
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
