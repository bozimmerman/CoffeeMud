package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class Chant_ResuscitateCompanion extends Chant implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Chant_ResuscitateCompanion";
	}

	private final static String localizedName = CMLib.lang().L("Resuscitate Companion");

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
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}
	
	private final List<WeakReference<DeadBody>> companionMobs=new LinkedList<WeakReference<DeadBody>>();

	private boolean isCompanionBody(final DeadBody body)
	{
		for(final Iterator<WeakReference<DeadBody>> m=companionMobs.iterator();m.hasNext();)
		{
			final WeakReference<DeadBody> wM=m.next();
			if(wM.get()==body)
				return true;
		}
		return false;
	}
	
	@Override
	public boolean supportsMending(Physical item)
	{
		return (item instanceof DeadBody)
				&&(((DeadBody)item).getSavedMOB()!=null)
				&&(!((DeadBody)item).isPlayerCorpse())
				&&(CMLib.flags().isAnimalIntelligence(((DeadBody)item).getSavedMOB()));
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(affected instanceof MOB)
		{
			final MOB myChar=(MOB)affected;
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(msg.source().isMonster())
			&&(CMLib.flags().isAnimalIntelligence(msg.source()))
			&&(msg.source().amFollowing()==myChar))
			{
				final Chant_ResuscitateCompanion A=(Chant_ResuscitateCompanion)myChar.fetchAbility(ID());
				final MOB aniM=msg.source();
				final Room room=myChar.location();
				if(A!=null)
				{
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							if((room!=null)
							&&(!aniM.amDestroyed())
							&&(aniM.amDead()))
							{
								for(final Iterator<WeakReference<DeadBody>> m=companionMobs.iterator();m.hasNext();)
								{
									final WeakReference<DeadBody> wM=m.next();
									if(wM.get()==null)
										m.remove();
								}
								for(int i=room.numItems()-1;i>=0;i--)
								{
									final Item I=room.getItem(i);
									if((I instanceof DeadBody)
									&&(((DeadBody)I).getMobName().equals(aniM.Name()))
									&&(!isCompanionBody((DeadBody)I)))
									{
										final List<WeakReference<DeadBody>> companionMobs=A.companionMobs;
										while(companionMobs.size()>10)
											companionMobs.remove(companionMobs.iterator().next());
										companionMobs.add(new WeakReference<DeadBody>((DeadBody)I));
									}
								}
							}
						}
					});
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical body=null;
		body=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(body==null) 
			return false;
		if((!(body instanceof DeadBody))
		||(((DeadBody)body).getMobName().length()==0)
		||(((DeadBody)body).getSavedMOB()==null))
		{
			mob.tell(L("@x1 can not be resuscitated.",body.Name()));
			return false;
		}
		boolean playerCorpse=((DeadBody)body).isPlayerCorpse();
		if(playerCorpse)
		{
			mob.tell(L("You can't resuscitate @x1.",((DeadBody)body).charStats().himher()));
			return false;
		}
		if(!isCompanionBody((DeadBody)body))
		{
			mob.tell(L("@x1 was either not your companion, or you were not present at the time of death.",((DeadBody)body).getMobName()));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("You can't do that while in combat!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,body,this,verbalCastCode(mob,body,auto),auto?L("<T-NAME> is resuscitated!"):L("^S<S-NAME> resuscitate(s) <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				if(playerCorpse)
					success = CMLib.utensils().resurrect(mob,mob.location(), (DeadBody)body, super.getXPCOSTLevel(mob));
				else
				{
					final MOB rejuvedMOB=((DeadBody)body).getSavedMOB();
					for(Iterator<WeakReference<DeadBody>> m=companionMobs.iterator();m.hasNext();)
					{
						WeakReference<DeadBody> wM=m.next();
						if(wM.get()==body)
							m.remove();
					}
					rejuvedMOB.recoverCharStats();
					rejuvedMOB.recoverMaxState();
					body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
					body.destroy();
					rejuvedMOB.bringToLife(mob.location(),true);
					rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> get(s) up!"));
				}
			}
		}
		else
			beneficialWordsFizzle(mob,body,auto?"":L("<S-NAME> attempt(s) to resuscitate <T-NAMESELF>, but nothing happens."));
		// return whether it worked
		return success;
	}
}
