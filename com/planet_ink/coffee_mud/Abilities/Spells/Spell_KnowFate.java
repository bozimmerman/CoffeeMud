package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2011-2018 Bo Zimmerman

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

public class Spell_KnowFate extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_KnowFate";
	}

	private final static String localizedName = CMLib.lang().L("Know Fate");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> concentrate(s) on <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				String[] aliasNames=new String[0];
				if(mob.playerStats()!=null)
					aliasNames=mob.playerStats().getAliasNames();
				final List<List<String>> combatV=new LinkedList<List<String>>();
				for (final String aliasName : aliasNames)
				{
					final String alias=mob.playerStats().getAlias(aliasName);
					if(alias.length()>0)
					{
						final List<String> all_stuff=CMParms.parseSquiggleDelimited(alias,true);
						  for(final String stuff : all_stuff)
						  {
							final Vector<String> preCommands=CMParms.parse(stuff);
						  	final List<String> THIS_CMDS=new Vector<String>(preCommands.size());
						  	combatV.add(THIS_CMDS);
							for(int v=preCommands.size()-1;v>=0;v--)
								THIS_CMDS.add(0,preCommands.elementAt(v));
						  }
					}
				 }

				int iwin=0;
				int hewin=0;
				long ihp=0;
				long hehp=0;
				int draws=0;

				final Session fakeS=(Session)CMClass.getCommon("FakeSession");
				fakeS.initializeSession(null,Thread.currentThread().getThreadGroup().getName(),"MEMORY");
				for(int tries=0;tries<20;tries++)
				{
					final MOB newMOB=(MOB)mob.copyOf();
					final MOB newVictiM=(MOB)target.copyOf();
					final Room arenaR=CMClass.getLocale("StdRoom");
					arenaR.setArea(mob.location().getArea());
					newMOB.setSession(fakeS);
					arenaR.bringMobHere(newMOB,false);
					arenaR.bringMobHere(newVictiM,false);
					newMOB.setVictim(newVictiM);
					newVictiM.setVictim(newMOB);
					newMOB.setStartRoom(null);
					newVictiM.setStartRoom(null);

					int motionlessTries=10;
					while((!newMOB.amDead())
					&&(!newVictiM.amDead())
					&&(!newMOB.amDestroyed())
					&&(!newVictiM.amDestroyed()))
					{
						if(newMOB.commandQueSize()==0)
						{
							for(final List<String> cmd : combatV)
								newMOB.enqueCommand(cmd, 0, 0);
						}
						final int nowHp=newMOB.curState().getHitPoints();
						final int hisHp=newVictiM.curState().getHitPoints();
						try
						{
							newMOB.setVictim(newVictiM);
							newVictiM.setVictim(newMOB);
							CMLib.commands().postStand(newMOB,true);
							CMLib.commands().postStand(newVictiM,true);
							newMOB.tick(newMOB,Tickable.TICKID_MOB);
							newVictiM.tick(newVictiM,Tickable.TICKID_MOB);
						}
						catch(final Exception t)
						{
							Log.errOut("Spell_KnowFate",t);
						}
						final int nowHp2=newMOB.curState().getHitPoints();
						final int hisHp2=newVictiM.curState().getHitPoints();
						if((nowHp==nowHp2)&&(hisHp==hisHp2))
						{
							if(--motionlessTries==0)
								break;
						}
						else
							motionlessTries=10;
					}

					if((newMOB.amDead())||(newMOB.amDestroyed()))
					{
						hewin++;
						hehp+=newMOB.curState().getHitPoints();
					}
					else
					if((newVictiM.amDead())||(newVictiM.amDestroyed()))
					{
						iwin++;
						ihp+=newMOB.curState().getHitPoints();
					}
					else
						draws++;
					newMOB.destroy();
					newVictiM.destroy();
					arenaR.setArea(null);
					arenaR.destroy();
					fakeS.onlyPrint("--------------------------------------------\n\r");
				}
				String addendum="";
				if(draws>0)
					addendum=" with "+draws+" draws.";
				if(iwin>hewin)
					mob.tell(L("@x1% of the time, you defeat @x2 with @x3 hit points left@x4.",""+iwin,target.charStats().himher(),""+(ihp/iwin),addendum));
				else
				if(hewin>iwin)
					mob.tell(L("@x1% of the time you die, and @x2 still has @x3 hit points left@x4.",""+hewin,target.charStats().himher(),""+(hehp/hewin),addendum));
				else
				if(iwin>0)
					mob.tell(L("Half of the time, you defeat @x1 with @x2 hit points left@x3.",target.charStats().himher(),""+(ihp/iwin),addendum));
				else
					mob.tell(L("You can't hurt each other .. there were @x1% draws.",""+(draws*5)));
				//Log.debugOut(fakeS.afkMessage());
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> concentrate(s) on <T-NAMESELF>, but look(s) frustrated."));

		// return whether it worked
		return success;
	}
}
