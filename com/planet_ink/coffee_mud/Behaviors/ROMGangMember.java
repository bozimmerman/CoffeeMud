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

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class ROMGangMember extends StdBehavior
{
	@Override
	public String ID()
	{
		return "ROMGangMember";
	}

	int tickTock=5;
	String gangName="";
	String[] messages=new String[0];

	@Override
	public String accountForYourself()
	{
		return "gang membership";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		List<String> list=CMParms.parseSemicolons(newParms, true);
		messages=new String[0];
		if(list.size()==0)
			gangName=CMLib.dice().rollPercentage()>50?"Red":"Blue";
		else
		{
			gangName=list.remove(0);
			messages=list.toArray(messages);
		}
	}
	
	public void pickAFight(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer))
			return;
		final Room R=observer.location();
		if(R==null)
			return;
		if(R.numPCInhabitants()==0)
			return;

		MOB victim=null;
		String vicParms="";
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB inhab=R.fetchInhabitant(i);
			if((inhab!=null)
			&&((inhab.isMonster())||(CMLib.clans().findCommonRivalrousClans(inhab,observer).size()==0)))
			{
				for(final Enumeration<Behavior> e=inhab.behaviors();e.hasMoreElements();)
				{
					final Behavior B=e.nextElement();
					if(B.ID().equals(ID())&&(!((ROMGangMember)B).gangName.equals(gangName)))
					{
						victim=inhab;
						vicParms=((ROMGangMember)B).gangName;
					}
					else
					if((B.ID().indexOf("GoodGuardian")>=0)||(B.ID().indexOf("Patrolman")>=0))
						return;
				}
			}
		}

		if(victim==null)
			return;
		Item weapon=observer.fetchWieldedItem();
		if(weapon==null)
			weapon=observer.getNaturalWeapon();

		/* say something, then raise hell */
		if(messages.length>0)
		{
			R.show(observer,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) '"+messages[CMLib.dice().roll(1,messages.length,-1)]+"'.^?");
		}
		else
		{
			switch (CMLib.dice().roll(1,7,-1))
			{
			case 0:
				R.show(observer,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> yell(s) 'I've been looking for you, punk!'^?"));
				break;
			case 1:
				R.show(observer,victim,CMMsg.MSG_NOISYMOVEMENT,L("With a scream of rage, <S-NAME> attack(s) <T-NAME>."));
				break;
			case 2:
				R.show(observer,victim,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'What's slimy @x1 trash like you doing around here?'^?",vicParms));
				break;
			case 3:
				R.show(observer,victim,CMMsg.MSG_SPEAK,L("^T<S-NAME> crack(s) <S-HIS-HER> knuckles and say(s) 'Do ya feel lucky?'^?"));
				break;
			case 4:
				R.show(observer,victim,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'There's no cops to save you this time!'^?"));
				break;
			case 5:
				R.show(observer,victim,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'Time to join your brother, spud.'^?"));
				break;
			case 6:
				R.show(observer,victim,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'Let's rock.'^?"));
				break;
			}
		}

		CMLib.combat().postAttack(observer,victim,weapon);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		final MOB mob=(MOB)ticking;
		tickTock--;
		if(tickTock<=0)
		{
			tickTock=CMLib.dice().roll(1,10,0);
			pickAFight(mob);
		}
		return true;
	}
}
