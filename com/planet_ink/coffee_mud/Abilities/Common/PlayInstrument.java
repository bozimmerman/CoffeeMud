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

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class PlayInstrument extends CommonSkill implements Wand.WandUsage
{
	@Override
	public String ID()
	{
		return "PlayInstrument";
	}

	private final static String	localizedName	= CMLib.lang().L("Play Instrument");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PLAY", "PLAYINSTRUMENT", "PLAYSONG"});

	@Override
	public String supportedResourceString()
	{
		return "MISC";
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public PlayInstrument()
	{
		super();
		displayText=L("You are playing...");
		verb=L("playing");
	}

	protected volatile String				lastInstrType	= "";
	protected volatile MusicalInstrument	instrument		= null;

	@Override
	public int getEnchantType()
	{
		return Ability.ACODE_SONG;
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	@Override
	protected String getAlmostDoneMessage()
	{
		return null;
	}

	@Override
	protected String getYouContinueMessage()
	{
		final String sound=(playSound!=null)?CMLib.protocol().msp(playSound,10):"";
		return L("<S-NAME> continue(s) @x1.@x2",verb,sound);
	}

	@Override
	protected String getOthersContinueMessage()
	{
		final String sound=(playSound!=null)?CMLib.protocol().msp(playSound,10):"";
		return L("<S-NAME> continue(s) @x1.@x2",verb,sound);
	}

	protected Map<String,int[]> proficiencies = new Hashtable<String,int[]>();

	@Override
	public void setMiscText(final String text)
	{
		proficiencies.clear();
		final Map<String,String> map= CMParms.parseStrictEQParms(text);
		for(final String key : map.keySet())
			proficiencies.put(key, new int[] {CMath.s_int(map.get(key))});
	}

	@Override
	public String text()
	{
		proficiency();
		final Map<String,String> strMap=new HashMap<String,String>();
		for(final String key : proficiencies.keySet())
			strMap.put(key, ""+proficiencies.get(key)[0]);
		return CMParms.toEqListString(strMap);
	}

	@Override
	public int proficiency()
	{
		final MusicalInstrument instrument = this.instrument;
		if(instrument != null)
		{
			final String typeName = instrument.getInstrumentTypeName();
			final int[] profSave;
			if(proficiencies.size()==0)
			{
				profSave = new int[] {super.proficiency};
				proficiencies.put(typeName, profSave);
			}
			else
			if(!proficiencies.containsKey(typeName))
			{
				profSave = new int[] {0};
				proficiencies.put(typeName, profSave);
			}
			else
				profSave = proficiencies.get(typeName);
			if(lastInstrType.equals(typeName))
				profSave[0] = super.proficiency();
			else
			{
				super.proficiency = profSave[0];
				lastInstrType = typeName;
			}
		}
		return super.proficiency();
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if((affected instanceof MOB)&&(instrument != null))
			affectableStats.addAmbiance("playing "+instrument.name());
		super.affectPhyStats(affected, affectableStats);
	}

	public static boolean usingInstrument(final MusicalInstrument I, final MOB mob)
	{
		if((I==null)||(mob==null))
			return false;
		if(I instanceof Rideable)
		{
			return (((Rideable)I).amRiding(mob)
					&&(mob.fetchFirstWornItem(Wearable.WORN_WIELD)==null)
					&&(mob.fetchHeldItem()==null));
		}
		return mob.isMine(I)&&(!I.amWearingAt(Wearable.IN_INVENTORY));
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final Physical affected=this.affected;
		if(!(affected instanceof MOB))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			final MOB mob=(MOB)affected;
			if(!usingInstrument(this.instrument,mob))
			{
				aborted=true;
				unInvoke();
				return false;
			}
			this.activityRoom = mob.location();
		}
		return super.tick(ticking, tickID);
	}

	protected MusicalInstrument getInstrumentPlayed(final MOB mob)
	{
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if((I!=null)
			&&(I instanceof MusicalInstrument)
			&&(I.container()==null)
			&&(usingInstrument((MusicalInstrument)I,mob)))
			{
				return (MusicalInstrument) I;
			}
		}
		if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
			return (MusicalInstrument)mob.riding();
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
		{
			final MusicalInstrument target=this.getInstrumentPlayed(mob);
			if((target != null)
			&&(usingInstrument(target, mob))
			&&(mob.fetchEffect(ID())==null))
			{
				final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),null,CMParms.combineQuoted(commands,0),null);
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob, msg);
			}
			return true;
		}

		if((commands.size()>0)&&(commands.get(0).equalsIgnoreCase("LIST")))
		{
			final StringBuilder str = new StringBuilder(L("You have some musical proficiency with: "));
			if(proficiencies.size()==0)
				str.append(L("Nothing!"));
			else
			{
				for(final String s : proficiencies.keySet())
				{
					str.append(CMStrings.capitalizeAndLower(s)+" ("+proficiencies.get(s)[0]+"%), ");
				}
				str.delete(str.length()-2,str.length());
			}
			commonTell(mob,str.toString());
			return false;
		}

		if(mob.fetchEffect(ID())!=null)
		{
			commonTell(mob,L("You are already playing an instrument.  Use PLAYINSTRUMENT STOP to stop."));
			return false;
		}

		MusicalInstrument target=null;
		if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
		{
			if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
			{
				commonTell(mob,L("You need to free your hands to play @x1.",mob.riding().name()));
				return false;
			}
			target=(MusicalInstrument)mob.riding();
		}
		if(target==null)
			target=this.getInstrumentPlayed(mob);
		if(target==null)
		{
			commonTell(mob,L("You need an instrument to play one!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		this.instrument=target; // necc for the proficiency checks
		verb=L("playing @x1",target.name());
		displayText=L("You are @x1",verb);
		if(!proficiencyCheck(mob,0,auto))
		{
			if(proficiency() < 25)
			{
				verb=L("making foul noises with @x1",target.name());
				displayText=L("You are @x1",verb);
			}
			else
			if(proficiency() < 50)
			{
				verb=L("trying to play @x1 and failing",target.name());
				displayText=L("You are @x1",verb);
			}
			else
			if(proficiency() < 75)
			{
				verb=L("playing @x1, but terribly",target.name());
				displayText=L("You are @x1",verb);
			}
			else
			{
				verb=L("playing @x1 pretty badly",target.name());
				displayText=L("You are @x1",verb);
			}
		}
		else
		if(proficiency() == 100)
		{
			verb=L("playing @x1 beautifully",target.name());
			displayText=L("You are @x1",verb);
		}

		final int duration=30;//getDuration(30,mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),
				L("<S-NAME> start(s) playing <T-NAME>."),CMParms.combineQuoted(commands,0),L("<S-NAME> start(s) playing <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			instrument = target;
			mob.location().send(mob,msg);
			final PlayInstrument pA = (PlayInstrument)beneficialAffect(mob,mob,asLevel,duration);
			if(pA != null)
			{
				pA.proficiencies = this.proficiencies;
				pA.lastInstrType = this.lastInstrType;
				pA.instrument = this.instrument;
			}
		}
		return true;
	}
}
