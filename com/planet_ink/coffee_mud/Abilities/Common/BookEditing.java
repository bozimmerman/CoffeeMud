package com.planet_ink.coffee_mud.Abilities.Common;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2017-2025 Bo Zimmerman

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
public class BookEditing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "BookEditing";
	}

	private final static String	localizedName	= CMLib.lang().L("Book Editing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BOOKEDITING", "BOOKEDIT", "BEDIT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected Item		found	= null;
	protected String	pageNum	= "";

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public BookEditing()
	{
		super();
		displayText=L("You are editing a book...");
		verb=L("editing");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(found==null)
					commonTelL(mob,"You mess up your book editing.");
				else
				if((pageNum!=null)&&(pageNum.startsWith("DELETE ")))
				{
					final CMMsg msg=CMClass.getMsg(mob,found,this,CMMsg.TYP_REWRITE,
							L("<S-NAME> edit(s) <T-NAME>."),pageNum,L("<S-NAME> edit(s) <T-NAME>."));
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						verb="";
					}
				}
				else
				{
					final CMMsg msg=CMClass.getMsg(mob,found,this,CMMsg.TYP_REWRITE,
							L("<S-NAME> start(s) editing <T-NAME>."),pageNum,L("<S-NAME> start(s) editing <T-NAME>."));
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						verb="";
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean error(final MOB mob)
	{
		commonTelL(mob,"You must specify what book to edit, and the optional page/chapter number to edit.");
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
			return error(mob);
		found = null;
		pageNum="";
		if((commands.size()>1)
		&&(CMath.isInteger(commands.get(commands.size()-1))))
		{
			pageNum=commands.remove(commands.size()-1);
			if(commands.get(commands.size()-1).equalsIgnoreCase("DELETE"))
			{
				commands.remove(commands.size()-1);
				pageNum="DELETE "+pageNum;
			}
		}
		final String itemName = CMParms.combine(commands);
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemName);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			target=mob.location().findItem(null, itemName);
		if((target!=null)&&(CMLib.flags().canBeSeenBy(target,mob)))
		{
			/*
			final Set<MOB> followers=mob.getGroupMembers(new XTreeSet<MOB>());
			boolean ok=false;
			for(final MOB M : followers)
			{
				if(target.secretIdentity().indexOf(getBrand(M))>=0)
					ok=true;
			}
			if(!ok)
			{
				commonTelL(mob,"You aren't allowed to work on '@x1'.",itemName);
				return false;
			}
			*/
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTelL(mob,"You don't seem to have a '@x1'.",itemName);
			return false;
		}


		final Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTelL(mob,"You must know how to write.");
			return false;
		}

		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER))
		&&(((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		&&(target.material()!=RawMaterial.RESOURCE_SILK)
		&&(target.material()!=RawMaterial.RESOURCE_HIDE))
		{
			commonTelL(mob,"You can't edit something like that.");
			return false;
		}

		if(!CMLib.flags().isReadable(target))
		{
			commonTelL(mob,"That's not even readable!");
			return false;
		}

		if((target instanceof Recipes)
		&&((pageNum.length()==0)||(!pageNum.startsWith("DELETE "))))
		{
			commonTelL(mob,"You can't edit that with this skill, but only delete pages.");
			return false;
		}

		if(!target.isGeneric())
		{
			commonTelL(mob,"You aren't able to give that a name.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("editing @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		if((!proficiencyCheck(mob,0,auto))||(!write.proficiencyCheck(mob,0,auto)))
			found = null;
		final int duration=getDuration(30,mob,1,1);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),
				L("<S-NAME> prepare(s) to edit <T-NAME>."),pageNum,L("<S-NAME> prepare(s) to edit <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(duration==1)
			{
				final BookEditing B=(BookEditing)beneficialAffect(mob,mob,asLevel,duration);
				if(B!=null)
				{
					B.tickDown=0;
					B.tickUp=0;
					B.unInvoke();
				}
			}
			else
				beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
