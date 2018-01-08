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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
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

public class Spell_Torture extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Torture";
	}

	private final static String localizedName = CMLib.lang().L("Torture");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(being tortured)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if((canBeUninvoked())
		&&(!mob.amDead())
		&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> manage(s) to survive the torture."));
	}

	public void cryOut(MOB mob)
	{
		if((text().length()>0)&&(!text().equalsIgnoreCase("HITONLY")))
		{
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if(S.mob()!=null)
					S.println(mob,null,null,text());
			}
			setMiscText("");
			return;
		}
		int roll=CMLib.dice().roll(1,16,0);
		boolean someoneelse=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			final MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&(!M.isMonster())&&(M!=mob))
				someoneelse=true;
		}
		if(!someoneelse)
			roll=CMLib.dice().roll(1,10,0);
		else
		switch(roll)
		{
		case 11:
			if(mob.getLiegeID().length()>0)
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				L("<S-NAME> admits that @x1 is <S-HIS-HER> liege.",mob.getLiegeID()));
			else
				roll=CMLib.dice().roll(1,10,0);
			break;
		case 12:
		{
			if(!mob.clans().iterator().hasNext())
				roll=CMLib.dice().roll(1,10,0);
			else
			{
				int numClans=0;
				for(@SuppressWarnings("unused") final Pair<Clan,Integer> p : mob.clans())
					numClans++;
				int clanNum=CMLib.dice().roll(1,numClans,-1);
				Clan C=null;
				for(final Pair<Clan,Integer> p : mob.clans())
				{
					if(clanNum==0)
					{
						C=p.first;
						break;
					}
					else
						clanNum--;
				}
				if(C==null)
					roll=CMLib.dice().roll(1,10,0);
				else
				{
					final List<MemberRecord> V=C.getMemberList();
					if(V.size()>0)
					{
						final String name=V.get(CMLib.dice().roll(1,V.size(),-1)).name;
						if(name.equals(mob.Name()))
							roll=CMLib.dice().roll(1,10,0);
						else
							mob.location().show(mob,null,CMMsg.MSG_SPEAK,
							L("<S-NAME> mutters that @x1 is a part of his clan, called @x2.",name,C.getName()));
					}
				}
			}
			break;
		}
		case 13:
			if(!mob.clans().iterator().hasNext())
				roll=CMLib.dice().roll(1,10,0);
			else
			{
				int numClans=0;
				for(@SuppressWarnings("unused") final Pair<Clan,Integer> p : mob.clans())
					numClans++;
				int clanNum=CMLib.dice().roll(1,numClans,-1);
				Clan C=null;
				for(final Pair<Clan,Integer> p : mob.clans())
				{
					if(clanNum==0)
					{
						C=p.first;
						break;
					}
					else
						clanNum--;
				}
				if(C==null)
					roll=CMLib.dice().roll(1,10,0);
				else
					mob.location().show(mob,null,CMMsg.MSG_SPEAK,
					L("<S-NAME> mutters that @x1 has @x2 experience points.",C.getName(),""+C.getExp()));
			}
			break;
		case 14:
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE)
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless())
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				L("<S-NAME> mutters that <S-HE-SHE> scored @x1 experience points.",""+mob.getExperience()));
			break;
		case 15:
		{
			final StringBuffer str=new StringBuffer("");
			final Command C=CMClass.getCommand("Affect");
			try
			{
				str.append(C.executeInternal(mob,0,mob).toString());
			}
			catch(final Exception e)
			{
			}
			mob.location().show(mob,null,CMMsg.MSG_SPEAK,
			L("<S-NAME> says OK! I am affected by:\n\r@x1",str.toString()));
			break;
		}
		case 16:
			if(mob.numAllAbilities()<1)
				roll=CMLib.dice().roll(1,10,0);
			else
			{
			   final Ability A=mob.fetchRandomAbility();
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				L("<S-NAME> admit(s) that <S-HE-SHE> knows @x1 at @x2%.",A.name(),""+A.proficiency()));
			}
			break;
		}

		switch(roll)
		{
		case 1:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> struggle(s) against the pain.")); break;
		case 2:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> scream(s) in horror!")); break;
		case 3:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> beg(s) for mercy.")); break;
		case 4:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> grab(s) <S-HIS-HER> head and cr(ys).")); break;
		case 5:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> whimper(s).")); break;
		case 6:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> look(s) terrified!")); break;
		case 7:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> shake(s) in pain from <S-HIS-HER> head to <S-HIS-HER> feet.")); break;
		case 8:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> gasp(s) for air.")); break;
		case 9:    mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> shiver(s) in pain.")); break;
		case 10:mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			L("<S-NAME> cr(ys) in anticipation of pain!")); break;
		}
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.amITarget(affected))
		&&(text().equalsIgnoreCase("HITONLY"))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.value()>0))
			cryOut((MOB)affected);
		super.executeMsg(host,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!text().equalsIgnoreCase("HITONLY"))
		&&(affected instanceof MOB))
			cryOut((MOB)affected);
		return super.tick(ticking,tickID);
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
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> whisper(s) a torturous spell to <T-NAMESELF>.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))||(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
					maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> whisper(s) a torturous spell to <T-NAMESELF>, but the spell fades."));

		// return whether it worked
		return success;
	}
}

