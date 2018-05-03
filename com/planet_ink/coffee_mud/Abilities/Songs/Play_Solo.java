package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Play_Solo extends Play
{
	@Override
	public String ID()
	{
		return "Play_Solo";
	}

	private final static String localizedName = CMLib.lang().L("Solo");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected boolean persistentSong()
	{
		return false;
	}

	@Override
	protected boolean skipStandardSongTick()
	{
		return true;
	}

	@Override
	protected String songOf()
	{
		return CMLib.english().startWithAorAn(name());
	}

	@Override
	protected boolean skipStandardSongInvoke()
	{
		return true;
	}

	@Override
	public boolean okMessage(Environmental E, CMMsg msg)
	{
		if(!super.okMessage(E,msg))
			return false;
		if(affected instanceof MOB)
		{
			final MOB myChar=(MOB)affected;
			if(!msg.amISource(myChar)
			&&(msg.tool()!=null)
			&&(!msg.tool().ID().equals(ID()))
			&&(msg.tool() instanceof Ability)
			&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
			{
				final MOB otherBard=msg.source();
				if(((otherBard.phyStats().level()+CMLib.dice().roll(1,30,0)+getXLEVELLevel(otherBard))>(myChar.phyStats().level()+CMLib.dice().roll(1,20,0)+getXLEVELLevel(myChar)))
				&&(otherBard.location()!=null))
				{
					if((otherBard.location().show(otherBard,myChar,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> upstage(s) <T-NAMESELF>, stopping <T-HIS-HER> solo!")))
					&&((otherBard.location()==originRoom)
							||(originRoom==null)
							||originRoom.showOthers(otherBard, myChar, null, CMMsg.MSG_OK_ACTION,L("<S-NAME> upstage(s) <T-NAMESELF>, stopping <T-HIS-HER> solo!"))))
								unplayMe(myChar,null);
				}
				else
				if(otherBard.location()!=null)
				{
					otherBard.tell(L("You can't seem to upstage @x1's solo.",myChar.name()));
					if(!invoker().curState().adjMana(-10,invoker().maxState()))
						unplayMe(myChar,null);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((!mob.isInCombat())||(CMLib.flags().domainAffects(mob.getVictim(), Ability.ACODE_SONG).size()==0))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		timeOut=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		unplayAll(mob,mob);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?L("^S@x1 begins to play!^?",songOf()):L("^S<S-NAME> begin(s) to play @x1 on @x2.^?",songOf(),instrumentName());
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str=L("^S<S-NAME> start(s) playing @x1 on @x2 again.^?",songOf(),instrumentName());

			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.elementAt(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					if(originRoom==R)
						R.send(mob,msg);
					else
						R.sendOthers(mob,msg);
					invoker=mob;
					final Play newOne=(Play)this.copyOf();

					final Vector<Ability> songsToCancel=new Vector<Ability>();
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if(M!=null)
						for(int a=0;a<M.numEffects();a++) // personal affects
						{
							final Ability A=M.fetchEffect(a);
							if((A!=null)
							&&(A.invoker()!=mob)
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
								songsToCancel.addElement(A);
						}
					}
					final int reqMana=songsToCancel.size()*10;
					if(mob.curState().getMana()<reqMana)
					{
						mob.tell(L("You needed @x1 mana to play this solo!",""+reqMana));
						return false;
					}
					mob.curState().adjMana(-reqMana,mob.maxState());
					for(int i=0;i<songsToCancel.size();i++)
					{
						final Ability A=songsToCancel.elementAt(i);
						A.unInvoke();
					}
					mob.addEffect(newOne);
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> hit(s) a foul note."));

		return success;
	}
}
