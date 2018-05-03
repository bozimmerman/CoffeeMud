package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Prayer_Resurrect extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_Resurrect";
	}

	private final static String localizedName = CMLib.lang().L("Resurrect");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}
	
	protected boolean canResurrectNormalMobs() 
	{ 
		return false; 
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		if (item instanceof DeadBody)
		{
			final DeadBody body=(DeadBody)item;
			if((body.getMobName()==null)||(body.getMobName().length()==0))
				return false;
			if(!body.isPlayerCorpse())
				return true;
			if(CMLib.players().playerExists(body.getMobName()))
				return true;
		}
		return false;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical body=null;
		body=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		DatabaseEngine.PlayerData nonPlayerData=null;
		boolean playerCorpse=false;
		if((body==null)&&(CMSecurity.isASysOp(mob)))
		{
			final List<PlayerData> V=CMLib.database().DBReadPlayerSectionData("HEAVEN");
			final Vector<Physical> allObjs=new Vector<Physical>();
			final Vector<DatabaseEngine.PlayerData> allDataPs=new Vector<DatabaseEngine.PlayerData>();
			if((V!=null)&&(V.size()>0))
			{
				for(int v=0;v<V.size();v++)
				{
					final DatabaseEngine.PlayerData dataP=V.get(v);
					final String data=dataP.xml();
					final PhysicalAgent obj=parseHeavenlyData(data);
					if(obj!=null)
					{
						allDataPs.addElement(dataP);
						allObjs.addElement(obj);
					}
				}
			}
			if(allObjs.size()==0)
				return false;
			final String name=CMParms.combine(commands,0);
			if(name.equalsIgnoreCase("list"))
			{
				mob.tell("^x"+CMStrings.padRight(L("Guardian"),15)
						+CMStrings.padRight(L("Child name"),45)
						+CMStrings.padRight(L("Birth date"),16)+"^?");
				for(int i=0;i<allObjs.size();i++)
				{
					body=allObjs.elementAt(i);
					final Ability age=body.fetchEffect("Age");
					mob.tell(CMStrings.padRight(allDataPs.elementAt(i).who(),15)
							+CMStrings.padRight(body.name(),45)
							+CMStrings.padRight(((age==null)?"":CMLib.time().date2String(CMath.s_long(age.text()))),16)+"\n\r"+CMStrings.padRight("",15)+body.description());
				}
				return false;
			}
			Physical P=(Physical)CMLib.english().fetchEnvironmental(allObjs,name,true);
			if(P==null)
				P=(Physical)CMLib.english().fetchEnvironmental(allObjs,name,false);
			if(P==null)
				return false;
			for(int i=0;i<allObjs.size();i++)
			{
				if(allObjs.elementAt(i)==P)
				{
					nonPlayerData=allDataPs.elementAt(i);
					body=P;
					break;
				}
			}
		}
		if(nonPlayerData==null)
		{
			if(body==null)
				return false;
			if((!supportsMending(body))
			||(((DeadBody)body).getMobName().length()==0))
			{
				mob.tell(L("You can't resurrect that."));
				return false;
			}
			playerCorpse=((DeadBody)body).isPlayerCorpse();
			if(!playerCorpse)
			{
				final Ability ageA=body.fetchEffect("Age");
				if((ageA!=null)&&(CMath.isLong(ageA.text()))&&(CMath.s_long(ageA.text())>Short.MAX_VALUE))
				{
					MOB M=null;
					for(int i=0;i<mob.location().numInhabitants();i++)
					{
						M=mob.location().fetchInhabitant(i);
						if((M!=null)&&(!M.isMonster()))
						{
							final List<PlayerData> V=CMLib.database().DBReadPlayerData(M.Name(),"HEAVEN",M.Name()+"/HEAVEN/"+ageA.text());
							if((V!=null)&&(V.size()>0))
							{
								nonPlayerData=V.get(0);
								break;
							}
						}
					}
					if(nonPlayerData==null)
					{
						mob.tell(L("You can't seem to focus on @x1's spirit.  Perhaps if loved ones were here?",body.Name()));
						return false;
					}
				}
				else
				if(!canResurrectNormalMobs())
				{
					mob.tell(L("You can't resurrect @x1.",((DeadBody)body).charStats().himher()));
					return false;
				}
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,body,this,verbalCastCode(mob,body,auto),auto?L("<T-NAME> is resurrected!"):L("^S<S-NAME> resurrect(s) <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				Room room=CMLib.map().roomLocation(body);
				if(room == null)
					room=mob.location();
				if(playerCorpse)
					success = CMLib.utensils().resurrect(mob,room, (DeadBody)body, super.getXPCOSTLevel(mob));
				else
				if((nonPlayerData!=null) && (body != null))
				{
					final String data=nonPlayerData.xml();
					final Environmental object=parseHeavenlyData(data);
					if(object==null)
						mob.location().show(mob,body,CMMsg.MSG_OK_VISUAL,L("<T-NAME> twitch(es) for a moment, but the spirit is too far gone."));
					else
					if(object instanceof Item)
					{
						body.destroy();
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 comes back to life!",object.Name()));
						mob.location().addItem((Item)object);
					}
					else
					{
						final MOB rejuvedMOB=(MOB)object;
						rejuvedMOB.recoverCharStats();
						rejuvedMOB.recoverMaxState();
						body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
						body.destroy();
						rejuvedMOB.bringToLife(mob.location(),true);
						rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> get(s) up!"));
					}
					mob.location().recoverRoomStats();
				}
				else
				if(this.canResurrectNormalMobs() 
				&& (body instanceof DeadBody) 
				&& (((DeadBody)body).getSavedMOB()!=null))
				{
					final MOB rejuvedMOB=((DeadBody)body).getSavedMOB();
					rejuvedMOB.recoverCharStats();
					rejuvedMOB.recoverMaxState();
					body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
					body.destroy();
					rejuvedMOB.bringToLife(mob.location(),true);
					rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> get(s) up!"));
				}
				else
					mob.location().show(mob,body,CMMsg.MSG_OK_VISUAL,L("<T-NAME> jerk(s) for a moment, but the spirit is too far gone."));
			}
		}
		else
			beneficialWordsFizzle(mob,body,auto?"":L("<S-NAME> attempt(s) to resurrect <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}

	public PhysicalAgent parseHeavenlyData(String data)
	{
		String classID=null;
		int ability=0;
		int x=data.indexOf('/');
		if(x>=0)
		{
			classID=data.substring(0,x);
			data=data.substring(x+1);
		}
		x=data.indexOf('/');
		if(x>=0)
		{
			ability=CMath.s_int(data.substring(0,x));
			data=data.substring(x+1);
		}
		PhysicalAgent object=CMClass.getItem(classID);
		if(object==null)
			object=CMClass.getMOB(classID);
		if(object==null)
			return null;
		object.setMiscText(data);
		object.basePhyStats().setAbility(ability);
		object.recoverPhyStats();
		return object;

	}
}
