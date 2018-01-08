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

public class Spell_Claireaudience extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Claireaudience";
	}

	private final static String localizedName = CMLib.lang().L("Claireaudience");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	public static final DVector scries=new DVector(2);

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if(canBeUninvoked())
			scries.removeElement(mob);
		if((canBeUninvoked())&&(invoker!=null))
			invoker.tell(L("The sounds of '@x1' fade.",mob.name(invoker)));
		super.unInvoke();

	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(invoker!=null)
		&&(invoker.location()!=((MOB)affected).location())
		&&(msg.othersMessage()!=null))
			invoker.executeMsg(invoker,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			MOB M=null;
			int tries=0;
			while(((++tries)<100)&&(M==null))
			{
				final Room R=CMLib.map().getRandomRoom();
				if(R.numInhabitants()>0)
					M=R.fetchRandomInhabitant();
				if((M!=null)&&(M.name().equals(mob.name())))
					M=null;
			}
			if(M!=null)
				commands.add(M.Name());
		}
		if(commands.size()<1)
		{
			final StringBuffer scryList=new StringBuffer("");
			for(int e=0;e<scries.size();e++)
			{
				if(scries.elementAt(e,2)==mob)
					scryList.append(((e>0)?", ":"")+((MOB)scries.elementAt(e,1)).name());
			}
			if(scryList.length()>0)
				mob.tell(L("Cast on or revoke from whom?  You currently have @x1 on the following: @x2.",name(),scryList.toString()));
			else
				mob.tell(L("Cast on whom?"));
			return false;
		}
		final String mobName=CMParms.combine(commands,0).trim().toUpperCase();
		MOB target=null;
		if(givenTarget instanceof MOB)
			target=(MOB)givenTarget;
		if(target!=null)
			target=mob.location().fetchInhabitant(mobName);
		if(target==null)
		{
			try
			{
				List<MOB> targets=CMLib.map().findInhabitantsFavorExact(mob.location().getArea().getProperMap(), mob, mobName, false, 10);
				if(targets.size()==0)
					targets=CMLib.map().findInhabitantsFavorExact(CMLib.map().rooms(), mob, mobName, false, 10);
				if(targets.size()>0)
					target=targets.get(CMLib.dice().roll(1,targets.size(),-1));
			}
			catch(final NoSuchElementException nse)
			{
			}
		}
		if(target instanceof Deity)
			target=null;
		Room newRoom=mob.location();
		if(target!=null)
			newRoom=target.location();
		else
		{
			mob.tell(L("You can't seem to focus on '@x1'.",mobName));
			return false;
		}

		if(mob==target)
		{
			mob.tell(L("You can't cast this on yourself!"));
			return false;
		}

		final Ability A=target.fetchEffect(ID());
		if((A!=null)&&(A.invoker()==mob))
		{
			A.unInvoke();
			return true;
		}
		else
		if((A!=null)||(scries.contains(target)))
		{
			mob.tell(L("You can't seem to focus on '@x1'.",mobName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invoke(s) claireaudience, calling '@x1'.^?",mobName));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location())
					newRoom.send(target,msg2);
				scries.addElement(target,mob);
				beneficialAffect(mob,target,asLevel,0);
			}

		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to invoke claireaudience, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
