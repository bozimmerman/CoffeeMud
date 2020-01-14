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
   Copyright 2001-2020 Bo Zimmerman

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
public class Song_RestoreMusic extends Song
{

	@Override
	public String ID()
	{
		return "Song_RestoreMusic";
	}

	private final static String localizedName = CMLib.lang().L("Restore Music");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int overrideMana()
	{
		return 50;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	protected boolean skipStandardSongInvoke()
	{
		return true;
	}

	protected boolean mindAttack()
	{
		return abstractQuality() == Ability.QUALITY_MALICIOUS;
	}

	protected boolean skipStandardSongTick()
	{
		return true;
	}

	protected boolean skipSimpleStandardSongTickToo()
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SONG|Ability.DOMAIN_SINGING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item target=getTarget(mob,null,givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if((!(target instanceof Scroll))
		||(!(target instanceof MiscMagic)))
		{
			mob.tell(L("You can't restore that."));
			return false;
		}

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell(L("You can't sing!"));
			return false;
		}

		if(((Scroll)target).usesRemaining()>((Scroll)target).getSpells().size())
		{
			mob.tell(L("That can not be enhanced any further."));
			return false;
		}

		if(((Scroll)target).getSpells().size()==0)
		{
			mob.tell(L("That has nothing on it to restore."));
			return false;
		}
		boolean arcane=false;
		for(final Ability A : ((Scroll)target).getSpells())
		{
			if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
				arcane=true;
		}
		if(!arcane)
		{
			mob.tell(L("The markings on @x1 cannot be restored by this song.",target.Name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> sing(s) to <T-NAMESELF>, turning a magical phrase.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("The music notations on <T-NAME> become more definite!"));
				((Scroll)target).setUsesRemaining(((Scroll)target).usesRemaining()+1);
			}

		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> sing(s) to <T-NAMESELF>, uttering a magical phrase, and looking very frustrated."));

		// return whether it worked
		return success;
	}
}
