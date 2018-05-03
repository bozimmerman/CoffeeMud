package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2017-2018 Bo Zimmerman

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

public class Gait extends StdAbility
{
	@Override
	public String ID()
	{
		return "Gait";
	}

	private final static String	localizedName	= CMLib.lang().L("Mood");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		if(stateWord == null)
			return stateWord;
		return "("+stateWord+")";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected String arriveWord = null;
	protected String leaveWord	= null;
	protected String stateWord	= null;
	
	@Override
	public void affectCharStats(MOB affectableMob, CharStats affectableStats)
	{
		affectableStats.setArriveLeaveStr(arriveWord, leaveWord);
		super.affectCharStats(affectableMob, affectableStats);
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		arriveWord = CMParms.getParmStr(newMiscText, "ARRIVE", "");
		if((arriveWord != null)&&(arriveWord.trim().length()==0))
			arriveWord=null;
		leaveWord = CMParms.getParmStr(newMiscText, "LEAVE", "");
		if((leaveWord != null)&&(leaveWord.trim().length()==0))
			leaveWord=null;
		
		stateWord = CMParms.getParmStr(newMiscText, "STATE", "");
		if((stateWord != null)&&(stateWord.trim().length()==0))
			stateWord=null;
	}

	public String[][] getGaits()
	{
		final Object[][][] set = CMProps.getListFileGrid(CMProps.ListFile.GAIT_LIST);
		
		final String[][] fset = new String[set.length][4];
		for(int x=0;x<set.length;x++)
		{
			for(int y=0;y<set[x].length;y++)
				fset[x][y]=set[x][y][0].toString();
		}
		return fset;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String entered=CMParms.combine(commands,0);
		final String origEntered=CMParms.combine(commands,0);
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		Gait gaitA=(Gait)target.fetchEffect(ID());
		boolean add=false;
		if(gaitA==null)
		{
			add=true;
			gaitA=(Gait)copyOf();
		}
		
		if(entered.trim().length()==0)
		{
			String gaitName=L("Normal");
			if((gaitA!=null)&&(gaitA.stateWord!=null)&&(gaitA.stateWord.length()>0))
				gaitName=CMLib.english().startWithAorAn(gaitName.toLowerCase());
			mob.tell(L("You are currently using @x1 gait.",gaitName));
			return false;
		}
		final String[][] gaits = getGaits();
		if(entered.equalsIgnoreCase("RANDOM"))
		{
			final int rand=CMLib.dice().roll(1,gaits.length+3,-1);
			if(rand>=gaits.length)
				entered="NORMAL";
			else
				entered=gaits[rand][0];
		}
		String choice[]=null;
		if(entered.equalsIgnoreCase("NORMAL"))
			choice=new String[]{"NORMAL",null,null,null};
		else
		{
			for (final String[] element : gaits)
			{
				if(element[0].equalsIgnoreCase(entered))
				{
					choice=element;
				}
			}
		}
		if((choice==null)&&(entered.length()>0)&&(Character.isLetter(entered.charAt(0))))
		{
			if("NORMAL".startsWith(entered.toUpperCase()))
				choice=new String[]{"NORMAL",null,null,null};
			else
			{
				for (final String[] element : gaits)
				{
					if(element[0].startsWith(entered.toUpperCase()))
					{
						choice=element;
				}
				}
			}
		}
		if((choice==null)||(entered.equalsIgnoreCase("list")))
		{
			String choices=", NORMAL";
			for (final String[] element : gaits)
				choices+=", "+element[0];
			if(entered.equalsIgnoreCase("LIST"))
				mob.tell(L("Gait choices include: @x1",choices.substring(2)));
			else
				mob.tell(L("'@x1' is not a known gait. Choices include: @x2",entered,choices.substring(2)));
			return false;
		}
		
		if(((gaitA.stateWord!=null)&&(gaitA.stateWord.equalsIgnoreCase(choice[1])))
		||((gaitA.stateWord==null)&&(choice[1]==null)))
		{
			if(origEntered.equalsIgnoreCase("RANDOM"))
				return false;
			mob.tell(L("You are already in @x1 mood.",CMLib.english().startWithAorAn(choice[0].toLowerCase())));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String gaitMsgStr;
			if(choice[1]==null)
				gaitMsgStr=L("<T-NAME> start(s) walking normally.");
			else
				gaitMsgStr=L("<T-NAME> start(s) @x1.",choice[1].toLowerCase());
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,gaitMsgStr);
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					if(choice[0].equalsIgnoreCase("NORMAL"))
					{
						target.delEffect(gaitA);
					}
					else
					{
						if(add)
							target.addNonUninvokableEffect(gaitA);
						
						gaitA.setMiscText("ARRIVE=\""+choice[2]+"\" LEAVE=\""+choice[3]+"\" STATE=\""+choice[1]+"\"");
					}
					target.recoverPhyStats();
					target.location().recoverRoomStats();
				}
			}
		}
		return success;
	}
}
