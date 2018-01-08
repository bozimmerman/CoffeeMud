package com.planet_ink.coffee_mud.Commands;
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

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Flee extends Go
{
	public Flee()
	{
	}

	private final String[] access = I(new String[] { "FLEE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		String direction="";
		if(commands.size()>1)
			direction=CMParms.combine(commands,1);
		if(mob==null)
			return false;
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!mob.isMonster())||(mob.amFollowing()!=null))
		{
			if(!mob.isInCombat())
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You can only flee while in combat."));
				return false;
			}
		}

		boolean XPloss=true;
		final MOB fighting=mob.getVictim();
		if(fighting!=null)
		{
			final Set<MOB> H=CMLib.combat().allCombatants(mob);
			for (final Object element : H)
			{
				final MOB M=(MOB)element;
				if(CMLib.flags().isAliveAwakeMobileUnbound(M,true))
				{
					XPloss=true;
					break;
				}
				XPloss=false;
			}
		}

		if((!XPloss)&&(direction.length()==0))
		{
			mob.tell(L("You stop fighting."));
			direction="NOWHERE";
		}

		int directionCode=-1;
		if(!direction.equals("NOWHERE"))
		{
			if(direction.length()==0)
			{
				final Vector<Integer> directions=new Vector<Integer>();
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Exit thisExit=R.getExitInDir(d);
					final Room thisRoom=R.getRoomInDir(d);
					if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
						directions.add(Integer.valueOf(d));
				}
				// up is last resort
				if(directions.size()>1)
					directions.removeElement(Integer.valueOf(Directions.UP));
				if(directions.size()>0)
				{
					directionCode=directions.get(CMLib.dice().roll(1,directions.size(),-1)).intValue();
					direction=CMLib.directions().getDirectionName(directionCode);
				}
			}
			else
				directionCode=CMLib.directions().getGoodDirectionCode(direction);
			if(directionCode<0)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Flee where?!"));
				return false;
			}
		}
		
		if(CMLib.flags().isFalling(mob))
		{
			int fallDir = CMLib.flags().getFallingDirection(mob);
			if((fallDir >=0) && (R.rawDoors()[fallDir]!=null) && (R.getExitInDir(fallDir)!=null))
				directionCode = fallDir;
		}
		
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(CMLib.tracking().walk(mob,directionCode,true,false,false))))
		{
			mob.makePeace(false);
			if(XPloss&&(fighting!=null))
			{
				final String whatToDo=CMProps.getVar(CMProps.Str.PLAYERFLEE);
				if((whatToDo==null)||(whatToDo.length()==0))
					return false;
				final int[] expLost={10+((mob.phyStats().level()-fighting.phyStats().level()))*5};
				if(expLost[0]<10)
					expLost[0]=10;
				final String[] cmds=CMParms.toStringArray(CMParms.parseCommas(whatToDo,true));
				CMLib.combat().handleCombatLossConsequences(mob,fighting,cmds,expLost,"You lose @x1 experience points for withdrawing.");
				final double pctHPremaining=CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints());
				if(expLost[0]>0)
				{
					final int gainedExperience=(int)Math.round(CMath.mul(expLost[0],1.0-pctHPremaining))/4;
					if((fighting!=mob)
					&&(gainedExperience>0)
					&&((mob.session()==null)
					   ||(fighting.session()==null)
					   ||(!mob.session().getAddress().equals(fighting.session().getAddress()))))
							CMLib.leveler().postExperience(fighting,null,null,gainedExperience,false);
				}
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
