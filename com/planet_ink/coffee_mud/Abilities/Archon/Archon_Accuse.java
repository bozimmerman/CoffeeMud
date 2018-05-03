package com.planet_ink.coffee_mud.Abilities.Archon;
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
   Copyright 2015-2018 Bo Zimmerman

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

public class Archon_Accuse extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Accuse";
	}

	private final static String localizedName = CMLib.lang().L("Accuse");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings = I(new String[] { "ACCUSE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean announce=false;
		if(commands.size()>0)
		{
			if((commands.get(commands.size()-1)).equals("!"))
			{
				commands.remove(commands.size()-1);
				announce=true;
			}
		}
		if((commands.size()<2)||(commands.get(0).equals("?")))
		{
			mob.tell(L("Accuse who of what? Add a ! to the end to announce, or a number to the end to set punishment degree."));
			return false;
		}

		Vector<String> origCommands = new XVector<String>(commands);
		Vector<String> nameVec=new XVector<String>(commands.get(0));
		commands.remove(0);
		
		final MOB target=getTargetAnywhere(mob,nameVec,givenTarget,true);
		if(target==null)
			return false;
		
		int punishmentLevel = Law.PUNISHMENT_JAIL1;
		if(CMath.isInteger(commands.get(commands.size()-1)))
		{
			int x=CMath.s_int(commands.get(commands.size()-1));
			if((x<0)||(x>Law.PUNISHMENT_HIGHEST))
			{
				mob.tell(L("The punishment level given is invalid: @x1",""+x));
				return false;
			}
			punishmentLevel = x;
			commands.remove(commands.size()-1);
		}
		
		if((commands.size()<2)||(commands.get(0).equals("?")))
		{
			mob.tell(L("Accuse who of what? Add a ! to the end to announce, or a number to the end to set punishment degree."));
			return false;
		}
		
		LegalBehavior B=null;
		Area A2=null;
		Room room=target.location();
		if(room==null)
			return false;
		B=CMLib.law().getLegalBehavior(room);
		A2=CMLib.law().getLegalObject(room);
		if(B==null)
		{
			room=target.getStartRoom();
			if(room==null)
				return false;
			B=CMLib.law().getLegalBehavior(room);
			A2=CMLib.law().getLegalObject(room);
			if(B==null)
			{
				room=mob.location();
				if(room==null)
					return false;
				B=CMLib.law().getLegalBehavior(room);
				A2=CMLib.law().getLegalObject(room);
			}
		}
		if(B==null)
		{
			mob.tell(mob,target,null,L("<T-NAME> is not currently connectable to a legal area."));
			return false;
		}
		
		String crimeDesc = CMParms.combine(commands);
		if(crimeDesc.toLowerCase().startsWith("of "))
			crimeDesc=crimeDesc.substring(3);
		
		if(!super.invoke(mob,origCommands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			Map<MOB,MOB> oldFightState = super.saveCombatState(mob, true);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
									(auto || (!announce))?L("<T-NAME> <T-IS-ARE> accused of @x1!",crimeDesc):
										 L("^F**<S-NAME> accuse(s) <T-NAMESELF> of @x1!^?",crimeDesc));
			CMLib.color().fixSourceFightColor(msg);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				final String crime=crimeDesc;
				final String desc=L("No one should never be caught @x1!",crimeDesc);
				final String crimeLocs="";
				final String crimeFlags="!witness";
				final String sentence=Law.PUNISHMENT_DESCS[punishmentLevel];
				
				B.addWarrant(A2,target,null,crimeLocs,crimeFlags,crime,sentence,desc);
				
				super.restoreCombatState(oldFightState);
				if(announce)
				{
					final Command C=CMClass.getCommand("Announce");
					try
					{
						C.execute(mob,new XVector<String>("ANNOUNCE",L("@x1 is accused of @x2!",target.name(),crimeDesc)),MUDCmdProcessor.METAFLAG_FORCED);
					}
					catch (final Exception e)
					{
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to accuse <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
