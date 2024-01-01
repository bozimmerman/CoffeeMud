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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
   Copyright 2023-2024 Bo Zimmerman

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
public class Spell_Majesty extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Majesty";
	}

	private final static String localizedName = CMLib.lang().L("Majesty");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Majestic Aura)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	protected String				fname	= "King";
	protected String				ftitle	= "@x1, King of @x2";
	protected CompiledZMask			mask	= null;
	protected AtomicBoolean			norc	= new AtomicBoolean(false);
	protected LimitedTreeSet<MOB>	noboth	= new LimitedTreeSet<MOB>(10000,100,false);

	@Override
	protected int getTicksBetweenCasts()
	{
		final long duration = CMProps.getTicksPerMudHour() * CMLib.time().globalClock().getHoursInDay();
		return (int)duration;
	}

	protected void addTitle()
	{
		final Physical P = affected;
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			final PlayerStats p = mob.playerStats();
			if(p != null)
			{
				if(!p.getTitles().contains(ftitle))
					p.delTitle(ftitle);
				p.addTitle(ftitle);
			}
		}
	}

	protected void removeTitle()
	{
		final Physical P = affected;
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			final PlayerStats p = mob.playerStats();
			if(p != null)
				p.delTitle(ftitle);
		}
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			removeTitle();
			String name = mob.Name();
			if (mob.phyStats().newName() != null)
				name = mob.phyStats().newName();
			String title;
			if(mob.charStats().reproductiveCode() == 'M')
				title = "King";
			else
			if(mob.charStats().reproductiveCode() == 'F')
				title = "Queen";
			else
				title = "Royal";
			fname = title + " " + name;
			final String pluralRaceName=CMLib.english().makePlural(mob.charStats().getMyRace().name());
			final String maskStr = "-RACE +"+mob.charStats().getMyRace().ID();
			mask = CMLib.masking().maskCompile(maskStr);
			ftitle = mob.Name()+", "+title+" of "+pluralRaceName;
			addTitle();
		}
	}

	public void doGreet(final MOB meM, final MOB M)
	{
		CMLib.threads().scheduleRunnable(new Runnable()
		{
			final MOB mob=M;
			final MOB kingM = meM;
			public void run()
			{
				mob.enqueCommand(new XVector<String>("KNEEL ",kingM.Name()), 0,0);
				switch(CMLib.dice().roll(1, 5, 0))
				{
				case 1:
					mob.enqueCommand(new XVector<String>("SAYTO ",kingM.Name(),L("Hail @x1!",fname)), 0,0);
					break;
				case 2:
					mob.enqueCommand(new XVector<String>("SAYTO ",kingM.Name(),L("All Hail @x1!",fname)), 0,0);
					break;
				case 3:
					mob.enqueCommand(new XVector<String>("SAYTO ",kingM.Name(),L("Hail @x1!",ftitle)), 0,0);
					break;
				case 4:
					mob.enqueCommand(new XVector<String>("SAYTO ",kingM.Name(),L("Your Majesty.")), 0,0);
					break;
				}
				noboth.add(mob);
			}
		},1000);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		final Physical P = affected;
		if(!(P instanceof MOB))
			return;
		final MOB mob=(MOB)P;
		if(msg.amISource(mob))
		{
			if(msg.sourceMinor()==CMMsg.TYP_QUIT)
				removeTitle();
			else
			if(msg.sourceMinor()==CMMsg.TYP_LIFE)
				addTitle();
		}
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&(!noboth.contains(msg.source())))
		{
			final Set<MOB> grp = mob.getGroupMembers(new HashSet<MOB>());
			if(msg.source()==mob)
			{
				final Room R = (Room)msg.target();
				for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
				{
					final MOB M = m.nextElement();
					if((M!=null)
					&&(M!=mob)
					&&(!grp.contains(M))
					&&(CMLib.masking().maskCheck(mask, M, true)))
						doGreet(mob,M);
				}
			}
			else
			if((msg.target()==mob.location())
			&&(!grp.contains(msg.source()))
			&&(CMLib.masking().maskCheck(mask, msg.source(), true)))
				doGreet(mob,msg.source());
			// kneel and hail and so forth
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.source()==affected)
		{
			final MOB mob=msg.source();
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null))
			{
				msg.setSourceMessage(CMStrings.replaceWord(msg.sourceMessage(), "I", "We"));
				msg.setSourceMessage(CMStrings.replaceWord(msg.sourceMessage(), "my", "our"));
				msg.setSourceMessage(CMStrings.replaceWord(msg.sourceMessage(), "mine", "ours"));
				msg.setSourceMessage(CMStrings.replaceWord(msg.sourceMessage(), "me", "us"));
				if(msg.targetMessage()!=null)
				{
					msg.setTargetMessage(CMStrings.replaceWord(msg.targetMessage(), "I", "We"));
					msg.setTargetMessage(CMStrings.replaceWord(msg.targetMessage(), "my", "our"));
					msg.setTargetMessage(CMStrings.replaceWord(msg.targetMessage(), "mine", "ours"));
					msg.setTargetMessage(CMStrings.replaceWord(msg.targetMessage(), "me", "us"));
				}
				if(msg.othersMessage()!=null)
				{
					msg.setOthersMessage(CMStrings.replaceWord(msg.othersMessage(), "I", "We"));
					msg.setOthersMessage(CMStrings.replaceWord(msg.othersMessage(), "my", "our"));
					msg.setOthersMessage(CMStrings.replaceWord(msg.othersMessage(), "mine", "ours"));
					msg.setOthersMessage(CMStrings.replaceWord(msg.othersMessage(), "me", "us"));
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
			&&(mob.isPlayer())
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0)
			&&("oO".indexOf(msg.targetMessage().charAt(0))>=0))
			{
				final List<String> ml=CMParms.parse(msg.targetMessage());
				if((ml.size()>1)
				&&("ORDER".startsWith(ml.get(0).toUpperCase()))
				&&(ml.size()>2))
				{
					ml.remove(0);
					String whomToOrder=ml.get(0);
					final List<MOB> V=new ArrayList<MOB>();
					boolean allFlag=whomToOrder.equalsIgnoreCase("all");
					if (whomToOrder.toUpperCase().startsWith("ALL."))
					{
						allFlag = true;
						whomToOrder = "ALL " + whomToOrder.substring(4);
					}
					if (whomToOrder.toUpperCase().endsWith(".ALL"))
					{
						allFlag = true;
						whomToOrder = "ALL " + whomToOrder.substring(0, whomToOrder.length() - 4);
					}
					int addendum=1;
					String addendumStr="";
					boolean doBugFix = true;
					while(doBugFix || allFlag)
					{
						doBugFix=false;
						final MOB target=mob.location().fetchInhabitant(whomToOrder+addendumStr);
						if(target==null)
							break;
						if((CMLib.flags().canBeSeenBy(target,mob))
						&&(target!=mob)
						&&(!V.contains(target)))
							V.add(target);
						addendumStr="."+(++addendum);
					}
					if(V.size()==0)
						return true; // don't matter
					for(final MOB M : V)
					{
						if(!CMLib.masking().maskCheck(mask, M, true))
							return true;
					}
					final PlayerStats p = mob.playerStats();
					final double oldActions = mob.actions();
					final String oldSecFlags = (p==null)?null:p.getSetSecurityFlags(null);
					try
					{
						boolean proceed = false;
						synchronized(norc)
						{
							if(!norc.get())
							{
								norc.set(true);
								proceed=true;
							}
						}
						if(proceed)
						{

							final List<String> fl=CMParms.parse(msg.targetMessage());
							mob.clearCommandQueue();
							mob.setActions(mob.phyStats().speed());
							if(p!=null)
								p.getSetSecurityFlags(oldSecFlags+";ORDER");
							mob.prequeCommand(fl,0,0);
							mob.dequeCommand();
						}
					}
					finally
					{
						mob.setActions(oldActions);
						if(p!=null)
							p.getSetSecurityFlags(oldSecFlags);
						norc.set(false);
					}
					return false;
				}
			}
		}
		if(msg.target() == affected)
		{
			if(msg.targetMinor()==CMMsg.TYP_LEGALWARRANT)
			{
				msg.source().tell(L("@x1 is above the law.",affected.name(msg.source())));
				return false;
			}
			else
			if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
			&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)))
			{
				final MOB target=(MOB)msg.target();
				if((!target.isInCombat())
				&&(msg.source().getVictim()!=target)
				&&(msg.source().location()==target.location()))
				{
					msg.source().tell(L("You are too much in awe of @x1",target.name(msg.source())));
					if(target.getVictim()==msg.source())
					{
						target.makePeace(true);
						target.setVictim(null);
					}
					return false;
				}
			}
		}
		return true;
	}
	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		//affectableStats.setName(fname);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			removeTitle();
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fade(s) back into serfdom."));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?("^S<S-NAME> attain(s) a majestic aura.^?"):L("^S<S-NAME> cast(s) a spell on <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				final long duration = CMProps.getTicksPerMudHour() * CMLib.time().globalClock().getHoursInDay();
				beneficialAffect(mob,target,asLevel,(int)duration);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> speak(s) majestically to <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
