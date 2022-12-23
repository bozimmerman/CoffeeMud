package com.planet_ink.coffee_mud.Abilities.Diseases;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class Disease_Hatred extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Hatred";
	}

	private final static String localizedName = CMLib.lang().L("Hatred");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Hatred)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	protected static final String[] moodTypes = new String[] { "ANGRY", "RUDE", "MEAN" };
	protected static final String[] socTypes = new String[] {
		"ACCUSE", "COUGH", "CRITICIZE", "EYE", "FART", "FLARE", "FLICK", "FROWN", "FUME",
		"GLARE", "GROWL", "GRUMBLE", "MUTTER", "NARROW", "SHOO", "SNAP", "SNARL", "SNICKER",
		"TEASE", "THREATEN", "TOUT", "TSK", "VOODOO"
	};
	protected String whatDesc = "something";
	protected final Map<String,CompiledZMask> whats = new SHashtable<String,CompiledZMask>();
	protected Ability mood = null;

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public void setMiscText(String miscText)
	{
		final boolean plus = miscText.startsWith("+");
		final boolean minus = miscText.startsWith("<");
		if(plus||minus)
			miscText = miscText.substring(1);
		else
		{
			super.setMiscText(miscText);
			whats.clear();
			whatDesc="nothing";
		}
		final Map<String,String> parms = CMParms.parseEQParmsLow(miscText);
		if(minus)
		{
			for(final String key : parms.keySet())
				whats.remove(key.toUpperCase().trim());
		}
		else
		{
			for(final String key : parms.keySet())
				whats.put(key.toUpperCase().trim(), CMLib.masking().getPreCompiledMask(parms.get(key)));
		}
		if(whats.size()>0)
			whatDesc = CMLib.english().toEnglishStringList(whats.keySet());
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 1;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 980;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return CMLib.dice().roll(1, 10, 10);
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You feel more rational.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> feel(s) hatred.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	protected final static Set<Integer> shopCmds = new XHashSet<Integer>(new Integer[] {
		Integer.valueOf(CMMsg.TYP_BUY),
		Integer.valueOf(CMMsg.TYP_BID),
		Integer.valueOf(CMMsg.TYP_SELL),
		Integer.valueOf(CMMsg.TYP_LIST),
		Integer.valueOf(CMMsg.TYP_VALUE),
		Integer.valueOf(CMMsg.TYP_DEPOSIT),
		Integer.valueOf(CMMsg.TYP_WITHDRAW),
		Integer.valueOf(CMMsg.TYP_BORROW),
		Integer.valueOf(CMMsg.TYP_VIEW)
	});

	protected boolean doIHate(final MOB M)
	{
		for(final CompiledZMask mask : whats.values())
		{
			if(CMLib.masking().maskCheck(mask, M, false))
				return true;
		}
		return false;
	}

	protected boolean doIHate(final MOB srcM, final Room R)
	{
		if((R==null)||(srcM==null))
			return false;
		final Set<MOB> grp = srcM.getGroupMembers(new HashSet<MOB>());
		boolean any=false;
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M = m.nextElement();
			if((M != null)
			&&(!grp.contains(M)))
			{
				if(!doIHate(M))
					return false;
				else
					any=true;
			}
		}
		return any;
	}
	protected String whatIHateAbout(final MOB M)
	{
		for(final String what : whats.keySet())
		{
			final CompiledZMask mask = whats.get(what);
			if(CMLib.masking().maskCheck(mask, M, false))
				return what.toLowerCase();
		}
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.source()==affected)
		{
			if(msg.target() instanceof MOB)
			{
				if((shopCmds.contains(Integer.valueOf(msg.targetMinor())))
				&&(doIHate((MOB)msg.target())))
				{
					msg.source().tell(msg.source(),msg.target(),null,
							L("Your hatred of @x1 prevents you from dealing with <T-NAME>.",
							whatIHateAbout((MOB)msg.target())));
					return false;
				}
				else
				if(msg.targetMinor()==CMMsg.TYP_FOLLOW)
				{
					boolean verboten = doIHate((MOB)msg.target());
					for(final MOB M : ((MOB)msg.target()).getGroupMembers(new HashSet<MOB>()))
					{
						if(doIHate(M))
							verboten = true;
					}
					if(verboten)
					{
						msg.source().tell(msg.source(),msg.target(),null,
								L("Your hatred of @x1 prevents you from grouping with <T-NAME>.",
								whatIHateAbout((MOB)msg.target())));
					}
					return false;
				}
			}
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null))
			{
				boolean doit;
				if(msg.target() instanceof MOB)
					doit=doIHate((MOB)msg.target());
				else
					doit=doIHate(msg.source(), msg.source().location());
				if(doit)
				{
					if(mood == null)
						mood = CMClass.getAbility("Mood");
					mood.setAffectedOne(affected);
					final String newStr = moodTypes[CMLib.dice().roll(1, moodTypes.length, -1)];
					if(!mood.text().equals(newStr))
						mood.setMiscText(newStr);
					if(!mood.okMessage(msg.source(), msg))
						return false;
				}
			}
		}
		else
		if(msg.target()==affected)
		{
			if((shopCmds.contains(Integer.valueOf(msg.targetMinor())))
			&&(doIHate(msg.source())))
			{
				msg.source().tell(msg.source(),msg.target(),null,
						L("<T-YOUPOSS> hatred of you prevents <T-HIM-HER> from dealing with you."));
				return false;
			}
			else
			if(msg.targetMinor()==CMMsg.TYP_FOLLOW)
			{
				boolean verboten = doIHate(msg.source());
				MOB whoIHate = msg.source();
				for(final MOB M : msg.source().getGroupMembers(new HashSet<MOB>()))
				{
					if(doIHate(M))
					{
						verboten = true;
						whoIHate = M;
					}
				}
				if(verboten)
				{
					msg.source().tell(msg.source(),msg.target(),null,
							L("<S-YOUPOSS> hatred of @x1 prevents <T-HIM-HER> from grouping with you.",
									whatIHateAbout(whoIHate)));
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)
		||(!(affected instanceof MOB)))
			return true;

		final MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null)
			diseaser=mob;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			final Room R = CMLib.map().roomLocation(mob);
			if(R!=null)
			{
				MOB targetM=null;
				for(final Enumeration<MOB> e = R.inhabitants();e.hasMoreElements();)
				{
					final MOB M = e.nextElement();
					if((M != null)
					&& (M != mob)
					&& (doIHate(M)))
					{
						targetM = M;
						break;
					}
				}
				if(targetM != null)
				{
					Social S = null;
					int tries = 0;
					String socWord = "";
					while((++tries<20)&&(S == null))
					{
						socWord = socTypes[CMLib.dice().roll(1, socTypes.length, -1)];
						final String socID = socWord +" <T-NAME>";
						S = CMLib.socials().fetchSocial(socID, true);
					}
					if(S != null)
						S.invoke(mob, new XVector<String>(socWord,targetM.name()), targetM, false);
				}
			}
			return true;
		}
		return true;
	}

}

