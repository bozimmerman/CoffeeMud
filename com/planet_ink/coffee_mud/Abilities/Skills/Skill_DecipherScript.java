package com.planet_ink.coffee_mud.Abilities.Skills;
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

public class Skill_DecipherScript extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_DecipherScript";
	}

	private final static String	localizedName	= CMLib.lang().L("Decipher Script");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedDisplay = CMLib.lang().L("(Decipher Script)");

	@Override
	public String displayText()
	{
		return localizedDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DECIPHER" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public int overrideMana()
	{
		return 0;
	}
	
	protected Item decryptI=null;
	protected ItemPossessor possessorI=null;
	protected boolean success=false;
	protected String page="";
	protected int tickUp=0;

	protected boolean confirmSuccess()
	{
		final Physical P=affected;
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			if((decryptI==null)
			||(decryptI.amDestroyed())
			||(decryptI.owner()!=possessorI)
			||(!CMLib.flags().canBeSeenBy(decryptI, mob)))
			{
				success = false;
				return false;
			}
		}
		else
		{
			success = false;
			return false;
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID == Tickable.TICKID_MOB)
		{
			tickUp++;
			if(!confirmSuccess())
			{
				unInvoke();
				return false;
			}
			final Physical P=affected;
			if(P instanceof MOB)
			{
				final MOB mob=(MOB)P;
				if(tickDown==4)
					mob.location().show(mob,decryptI,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> <S-IS-ARE> almost done decrypting <T-NAME>."));
				else
				if((tickUp%4)==0)
				{
					final int total=tickUp+tickDown;
					final int pct=(int)Math.round(CMath.div(tickUp,total)*100.0);
					mob.location().show(mob,decryptI,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> continue(s) decrypting <T-NAME> (@x1% completed).",""+pct),null,L("<S-NAME> continue(s) decrypting <T-NAME>."));
				}
			}
		}
		return true;
	}
	
	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		confirmSuccess();
		super.unInvoke();
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			if(!success)
			{
				mob.tell(L("You've failed to figure out the encryption on @x1.",decryptI.name(mob)));
			}
			else
			{
				mob.tell(L("You've completed the decryption of @x1.",decryptI.name(mob)));
				boolean killEncrypto=false;
				Ability encryptA=mob.fetchEffect("Encrypto");
				if(encryptA==null)
				{
					encryptA=decryptI.fetchEffect("Encrypto");
					if(encryptA!=null)
					{
						killEncrypto=true;
						encryptA=(Ability)encryptA.copyOf();
						encryptA.setProficiency(proficiency());
						mob.addNonUninvokableEffect(encryptA);
					}
				}
				try
				{
					final CMMsg newMsg=CMClass.getMsg(mob,decryptI,null,CMMsg.MSG_READ,null,CMMsg.MSG_READ,page,CMMsg.MSG_READ,null);
					if(mob.location().okMessage(mob,newMsg))
						mob.location().send(mob,newMsg);
				}
				finally
				{
					if(killEncrypto)
					{
						encryptA=mob.fetchEffect("Encrypto");
						if(encryptA!=null)
							mob.delEffect(encryptA);
					}
				}
				
			}
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
		{
			mob.tell(L("You are too stupid to try this."));
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell(L("What would you like to decipher?"));
			return false;
		}
		String page="";
		if((commands.size()>1)&&(CMath.isInteger(commands.get(commands.size()-1))))
			page=commands.remove(commands.size()-1);
		String name=CMParms.combine(commands);
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,name);
		if(target==null)
		{
			target=mob.location().findItem(null,name);
			if((target!=null)&&(CMLib.flags().isGettable(target)))
			{
				mob.tell(L("You don't have that."));
				return false;
			}
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(0))));
			return false;
		}

		final Item item=target;
		
		if((item instanceof Scroll)||(!CMLib.flags().isReadable(item)))
		{
			mob.tell(L("You can't decipher that."));
			return false;
		}

		Ability encryptA=item.fetchEffect("Encrypto");
		if(encryptA==null)
		{
			Language L=null;
			for(Enumeration<Ability> a= item.effects();a.hasMoreElements();)
			{
				Ability A=a.nextElement();
				if(A instanceof Language)
					L=(Language)A;
			}
			if(L==null)
				mob.tell(L("That doesn't appear to be encrypted."));
			else
				mob.tell(L("That doesn't appear to be encrypted, just written in another language."));
			
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int level = encryptA.abilityCode();
		int adjustment = 0;
		if(level > 0)
		{
			adjustment = adjustedLevel(mob,asLevel) - level;
			if(adjustment < 0)
				adjustment *= 10;
			else
				adjustment = 0;
		}
		
		final boolean success=proficiencyCheck(mob,adjustment,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> begin(s) decrypting <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Skill_DecipherScript A=(Skill_DecipherScript)this.beneficialAffect(mob, mob, asLevel, 20);
			if(A != null)
			{
				A.success = success;
				A.decryptI=target;
				A.possessorI=target.owner();
				A.page=page;
				A.tickUp=0;
			}
		}
		return success;
	}

}
