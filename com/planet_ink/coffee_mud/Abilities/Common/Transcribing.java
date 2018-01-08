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
public class Transcribing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Transcribing";
	}

	private final static String	localizedName	= CMLib.lang().L("Transcribing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TRANSCRIBING", "TRANSCRIBE", "BCOPY" });

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

	protected Item		foundI	= null;
	protected Item		targetI	= null;
	protected String	pageNum	= "";
	
	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Transcribing()
	{
		super();
		displayText=L("You are transcribing a book...");
		verb=L("copying");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if((foundI==null)||(targetI==null))
					commonTell(mob,L("You mess up your transcribing."));
				else
				{
					MOB factM=CMClass.getFactoryMOB(mob.Name(), mob.phyStats().level(), mob.location());
					try
					{
						String tmsg="";
						final CMMsg rmsg=CMClass.getMsg(mob,foundI,this,CMMsg.TYP_READ,null,pageNum,null);
						foundI.executeMsg(foundI, rmsg);
						for(CMMsg m2 : rmsg.trailerMsgs())
						{
							if((m2.source()==mob)
							&&(m2.target()==foundI)
							&&(m2.targetMessage().length()>0)
							&&(m2.sourceMinor()==CMMsg.TYP_WASREAD))
								tmsg+=m2.targetMessage();
						}
						if((foundI instanceof Recipe)
						&&(targetI instanceof Recipe)
						&&(CMClass.getAbilityPrototype(((Recipe)targetI).getCommonSkillID())==null))
							((Recipe)targetI).setCommonSkillID(((Recipe)foundI).getCommonSkillID());
							
						final CMMsg msg=CMClass.getMsg(mob,targetI,this,CMMsg.TYP_WRITE,
								L("<S-NAME> transcribe(s) from @x1 into <T-NAME>.",foundI.name(mob)),
								tmsg,
								L("<S-NAME> transcribe(s) from @x1 into <T-NAME>.",foundI.name(mob)));
						if(mob.location().okMessage(mob,msg))
							mob.location().send(mob,msg);
					}
					finally
					{
						factM.destroy();
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean error(final MOB mob)
	{
		commonTell(mob,L("You must specify what book to transcribe, what to transcribe to, and the optional page/chapter number to edit."));
		return false;
	}

	protected Item getBrandedItem(final MOB mob, final String itemName, boolean from)
	{
		Item I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemName);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
			I=mob.location().findItem(null, itemName);
		if((I!=null)&&(CMLib.flags().canBeSeenBy(I,mob)))
		{
			/*
			final Set<MOB> followers=mob.getGroupMembers(new TreeSet<MOB>());
			boolean ok=false;
			for(final MOB M : followers)
			{
				if(I.secretIdentity().indexOf(getBrand(M))>=0)
					ok=true;
			}
			if(!ok)
			{
				if(from)
					commonTell(mob,L("You aren't allowed to copy from '@x1'.",I.name(mob)));
				else
					commonTell(mob,L("You aren't allowed to copy to '@x1'.",I.name(mob)));
				return null;
			}
			*/
		}
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",itemName));
			return null;
		}
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
		{
			if(from)
				commonTell(mob,L("You can't transcribe something like @x1.",I.name(mob)));
			else
				commonTell(mob,L("You can't transcribe onto something like @x1.",I.name(mob)));
			return null;
		}
		if((!CMLib.flags().isReadable(I))
		||(I instanceof Scroll))
		{
			commonTell(mob,L("@x1 isn't even readable!",CMStrings.capitalizeAndLower(I.name(mob))));
			return null;
		}
		
		if(!I.isGeneric())
		{
			commonTell(mob,L("You aren't able to transcribe @x1.",I.name(mob)));
			return null;
		}
		return I;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
			return error(mob);
		foundI = null;
		targetI = null;
		pageNum="";
		if((commands.size()>2)&&(CMath.isInteger(commands.get(commands.size()-1))))
			pageNum=commands.remove(commands.size()-1);
		String copyFromName = commands.get(0);
		String copyToName = CMParms.combine(commands,1);
		Item copyFromI=this.getBrandedItem(mob, copyFromName, true);
		if(copyFromI == null)
			return false;
		Item copyToI=this.getBrandedItem(mob, copyToName, false);
		if(copyToI == null)
			return false;
		if((copyToI instanceof Recipe)
		&&(((Recipe)copyToI).getTotalRecipePages() <= ((Recipe)copyToI).getRecipeCodeLines().length))
		{
			commonTell(mob,L("@x1 is full.",copyToI.name(mob)));
			return false;
		}
		if((copyToI instanceof Recipe) != (copyFromI instanceof Recipe))
		{
			commonTell(mob,L("@x1 can not be copied to @x2.",copyFromI.name(mob),copyToI.name(mob)));
			return false;
		}
		if((copyFromI instanceof Recipe)
		&&(copyToI instanceof Recipe)
		&&(CMClass.getAbilityPrototype(((Recipe)copyFromI).getCommonSkillID())!=null)
		&&(CMClass.getAbilityPrototype(((Recipe)copyToI).getCommonSkillID())!=null)
		&&(CMClass.getAbilityPrototype(((Recipe)copyToI).getCommonSkillID())!=CMClass.getAbilityPrototype(((Recipe)copyFromI).getCommonSkillID())))
		{
			commonTell(mob,L("@x1 can not be copied to @x2, as it would break up the recipe types.",copyFromI.name(mob),copyToI.name(mob)));
			return false;
		}
		final Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTell(mob,L("You must know how to write."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		foundI=copyFromI;
		targetI=copyToI;
		verb=L("transcribing @x1 into @x2",foundI.name(),targetI.name());
		displayText=L("You are @x1",verb);
		if((!proficiencyCheck(mob,0,auto))||(!write.proficiencyCheck(mob,0,auto)))
			foundI = null;
		final int duration=getDuration(30,mob,1,1);
		final CMMsg msg=CMClass.getMsg(mob,copyFromI,this,getActivityMessageType(),
				L("<S-NAME> start(s) transcribing <T-NAME> into @x1.",targetI.name()),
				pageNum,
				L("<S-NAME> start(s) transcribing <T-NAME> into @x1.",targetI.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(duration==1)
			{
				Transcribing B=(Transcribing)beneficialAffect(mob,mob,asLevel,duration);
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
