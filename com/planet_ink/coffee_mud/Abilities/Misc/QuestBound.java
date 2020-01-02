package com.planet_ink.coffee_mud.Abilities.Misc;
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/*
   Copyright 2007-2020 Bo Zimmerman

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
public class QuestBound implements Ability
{
	@Override
	public String ID()
	{
		return "QuestBound";
	}

	@Override
	public String name()
	{
		return "QuestBound";
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public String description()
	{
		return "";
	}

	@Override
	public String displayText()
	{
		return "";
	}

	protected Physical	affected	= null;
	protected boolean	keyPlayer	= false;

	@Override
	public boolean canTarget(final int can_code)
	{
		return false;
	}

	@Override
	public boolean canAffect(final int can_code)
	{
		return false;
	}

	@Override
	public double castingTime(final MOB mob, final List<String> cmds)
	{
		return 0.0;
	}

	@Override
	public double combatCastingTime(final MOB mob, final List<String> cmds)
	{
		return 0.0;
	}

	@Override
	public double checkedCastingCost(final MOB mob, final List<String> cmds)
	{
		return 0.0;
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
	}

	@Override
	public int adjustedLevel(final MOB mob, final int asLevel)
	{
		return -1;
	}

	@Override
	public boolean bubbleAffect()
	{
		return false;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public int usageType()
	{
		return 0;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public ExpertiseLibrary.SkillCost getTrainingCost(final MOB mob)
	{
		return CMLib.expertises().createNewSkillCost(ExpertiseLibrary.CostType.TRAIN, Double.valueOf(1.0));
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public void setName(final String newName)
	{
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
	}

	@Override
	public String image()
	{
		return "";
	}

	@Override
	public String rawImage()
	{
		return "";
	}

	@Override
	public void setImage(final String newImage)
	{
	}

	@Override
	public MOB invoker()
	{
		return null;
	}

	@Override
	public void setInvoker(final MOB mob)
	{
	}

	public static final String[]	empty	= {};

	@Override
	public String[] triggerStrings()
	{
		return empty;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical target, final boolean auto, final int asLevel)
	{
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final Physical target, final boolean auto, final int asLevel)
	{
		return false;
	}

	@Override
	public boolean preInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int secondsElapsed, final double actionsRemaining)
	{
		return true;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		return false;
	}

	@Override
	public void unInvoke()
	{
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean isNowAnAutoEffect()
	{
		return true;
	}

	@Override
	public List<String> externalFiles()
	{
		return null;
	}

	@Override
	public boolean canBeTaughtBy(final MOB teacher, final MOB student)
	{
		return false;
	}

	@Override
	public boolean canBePracticedBy(final MOB teacher, final MOB student)
	{
		return false;
	}

	@Override
	public boolean canBeLearnedBy(final MOB teacher, final MOB student)
	{
		return false;
	}

	@Override
	public void teach(final MOB teacher, final MOB student)
	{
	}

	@Override
	public void unlearn(final MOB student)
	{
	}

	@Override
	public void practice(final MOB teacher, final MOB student)
	{
	}

	@Override
	public int maxRange()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int minRange()
	{
		return Integer.MIN_VALUE;
	}

	@Override
	public long expirationDate()
	{
		return 0;
	}

	@Override
	public void setExpirationDate(final long time)
	{
	}

	@Override
	public void startTickDown(final MOB invokerMOB, final Physical affected, final int tickTime)
	{
		if ((affected!=null)&&(affected.fetchEffect(ID()) == null))
			affected.addEffect(this);
	}

	@Override
	public int proficiency()
	{
		return 0;
	}

	@Override
	public void setProficiency(final int newProficiency)
	{
	}

	@Override
	public boolean proficiencyCheck(final MOB mob, final int adjustment, final boolean auto)
	{
		return false;
	}

	@Override
	public void helpProficiency(final MOB mob, final int adjustment)
	{
	}

	@Override
	public Physical affecting()
	{
		return affected;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		affected = P;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int castingQuality(final MOB invoker, final Physical target)
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isSavable()
	{
		return text().equals("*") && (!amDestroyed);
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
	}

	protected boolean	amDestroyed	= false;

	@Override
	public void destroy()
	{
		amDestroyed = true;
		affected = null;
	}

	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	/*
	protected void finalize()
	{
		CMClass.unbumpCounter(this, CMClass.CMObjectType.ABILITY);
	}// removed for mem & perf
	*/

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new QuestBound();
	}

	public QuestBound()
	{
		super();
		// CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed for
		// mem & perf
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES	= { "CLASS", "TEXT", "KEY" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		for (int i = 0; i < CODES.length; i++)
		{
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return text();
		case 2:
			return "" + keyPlayer;
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setMiscText(val);
			break;
		case 2:
			keyPlayer = CMath.s_bool(val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if (!(E instanceof QuestBound))
			return false;
		for (int i = 0; i < CODES.length; i++)
		{
			if (!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}

	private void cloneFix(final Ability E)
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final QuestBound E = (QuestBound) this.clone();
			// CMClass.bumpCounter(E,CMClass.CMObjectType.ABILITY);//removed for
			// mem & perf
			E.cloneFix(this);
			return E;

		}
		catch (final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected Set<String>	questIDs = new STreeSet<String>();

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText.length()==0)
			return;
		for(final String id : CMParms.parseAny(newMiscText,';', true))
		{
			if(!questIDs.contains(id))
				questIDs.add(id);
		}
	}

	@Override
	public String text()
	{
		return CMParms.combineWith(questIDs, ';');
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public boolean appropriateToMyFactions(final MOB mob)
	{
		return true;
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public String requirements(final MOB mob)
	{
		return "";
	}

	@Override
	public boolean canAffect(final Physical P)
	{
		return false;
	}

	@Override
	public boolean canTarget(final Physical P)
	{
		return false;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(questIDs.contains("*"))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_CLOAKED);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((amDestroyed())||(affected==null))
			return true;
		if((msg.targetMinor()==CMMsg.TYP_SHUTDOWN)
		||((msg.targetMinor()==CMMsg.TYP_EXPIRE)
			&&(msg.target()!=null)
			&&((msg.target() instanceof Room)
				||(msg.target()==affected)
				||((affected instanceof Item)&&(((Item)affected).owner()==msg.target()))))
		||(msg.targetMinor()==CMMsg.TYP_ROOMRESET)
		||(keyPlayer
		   &&(msg.source()==affected)
		   &&(msg.sourceMinor()==CMMsg.TYP_DEATH)))
			resetQuest(msg.targetMinor());
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((amDestroyed())||(affected==null))
			return;
		if((msg.targetMinor()==CMMsg.TYP_SHUTDOWN)
		||((msg.targetMinor()==CMMsg.TYP_EXPIRE)
			&&(msg.target()!=null)
			&&((msg.target() instanceof Room)
				||(msg.target()==affected)
				||((affected instanceof Item)&&(((Item)affected).owner()==msg.target()))))
		||(msg.targetMinor()==CMMsg.TYP_ROOMRESET)
		||(keyPlayer
			&&(msg.source()==affected)
			&&(msg.sourceMinor()==CMMsg.TYP_DEATH)))
			resetQuest(msg.targetMinor());
	}

	private void resetQuest(final int reason)
	{
		final boolean star=questIDs.contains("*");
		if(questIDs.size()>(star?1:0))
		{
			for(int q=CMLib.quests().numQuests()-1;q>=0;q--)
			{
				final Quest Q=CMLib.quests().fetchQuest(q);
				if((Q!=null)
				&&(questIDs.contains(""+Q)))
				{
					Log.sysOut("QuestBound",CMMsg.TYPE_DESCS[reason]+" message for "+(affected==null?"null":affected.name())+" caused "+Q.name()+" to reset.");
					Q.resetQuest(5);
					break;
				}
			}
			questIDs.clear();
			if(star)
				questIDs.add("*");
		}
		if((questIDs.size()==0)&&(affected!=null))
			affected.delEffect(this);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((keyPlayer)
		&&(ticking instanceof MOB)
		&&(((MOB)ticking).amDead() || ((MOB)ticking).amDestroyed()))
		{
			resetQuest(CMMsg.TYP_DEATH);
			return false;
		}
		return true;
	}

	@Override
	public void makeLongLasting()
	{
	}

	@Override
	public void makeNonUninvokable()
	{
	}

	private static final int[]	cost	= new int[3];

	@Override
	public int[] usageCost(final MOB mob, final boolean ignoreCostOverride)
	{
		return cost;
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}
}
