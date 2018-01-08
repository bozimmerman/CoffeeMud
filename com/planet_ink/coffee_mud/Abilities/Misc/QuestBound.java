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
import java.util.List;

/*
   Copyright 2007-2018 Bo Zimmerman

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
	public boolean canTarget(int can_code)
	{
		return false;
	}

	@Override
	public boolean canAffect(int can_code)
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
	public void setAbilityCode(int newCode)
	{
	}

	@Override
	public int adjustedLevel(MOB mob, int asLevel)
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
	public ExpertiseLibrary.SkillCost getTrainingCost(MOB mob)
	{
		return CMLib.expertises().createNewSkillCost(ExpertiseLibrary.CostType.TRAIN, Double.valueOf(1.0));
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public void setName(String newName)
	{
	}

	@Override
	public void setDescription(String newDescription)
	{
	}

	@Override
	public void setDisplayText(String newDisplayText)
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
	public void setImage(String newImage)
	{
	}

	@Override
	public MOB invoker()
	{
		return null;
	}

	@Override
	public void setInvoker(MOB mob)
	{
	}

	public static final String[]	empty	= {};

	@Override
	public String[] triggerStrings()
	{
		return empty;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel)
	{
		return false;
	}

	@Override
	public boolean invoke(MOB mob, Physical target, boolean auto, int asLevel)
	{
		return false;
	}

	@Override
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		return true;
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
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
	public boolean canBeTaughtBy(MOB teacher, MOB student)
	{
		return false;
	}

	@Override
	public boolean canBePracticedBy(MOB teacher, MOB student)
	{
		return false;
	}

	@Override
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		return false;
	}

	@Override
	public void teach(MOB teacher, MOB student)
	{
	}

	@Override
	public void practice(MOB teacher, MOB student)
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
	public void setExpirationDate(long time)
	{
	}

	@Override
	public void startTickDown(MOB invokerMOB, Physical affected, int tickTime)
	{
		if (affected.fetchEffect(ID()) == null)
			affected.addEffect(this);
	}

	@Override
	public int proficiency()
	{
		return 0;
	}

	@Override
	public void setProficiency(int newProficiency)
	{
	}

	@Override
	public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto)
	{
		return false;
	}

	@Override
	public void helpProficiency(MOB mob, int adjustment)
	{
	}

	@Override
	public Physical affecting()
	{
		return affected;
	}

	@Override
	public void setAffectedOne(Physical P)
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
	public int castingQuality(MOB invoker, Physical target)
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
		return false;
	}

	@Override
	public void setSavable(boolean truefalse)
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

	// protected void finalize(){
	// CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed for
	// mem & perf

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
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for (int i = 0; i < CODES.length; i++)
		{
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
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
	public void setStat(String code, String val)
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
	public boolean sameAs(Environmental E)
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

	private void cloneFix(Ability E)
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
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected String	questID	= "";

	@Override
	public void setMiscText(String newMiscText)
	{
		questID = newMiscText;
	}

	@Override
	public String text()
	{
		return questID;
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public boolean appropriateToMyFactions(MOB mob)
	{
		return true;
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public String requirements(MOB mob)
	{
		return "";
	}

	@Override
	public boolean canAffect(Physical P)
	{
		return false;
	}

	@Override
	public boolean canTarget(Physical P)
	{
		return false;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
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

	private void resetQuest(int reason)
	{
		if(text().length()>0)
		{
			Quest theQ=null;
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				final Quest Q=CMLib.quests().fetchQuest(q);
				if((Q!=null)&&(""+Q).equals(text()))
				{
					theQ=Q;
					break;
				}
			}
			if((theQ==null)||(!theQ.running()))
				affected.delEffect(this);
			else
			{
				Log.sysOut("QuestBound",CMMsg.TYPE_DESCS[reason]+" message for "+(affected==null?"null":affected.name())+" caused "+theQ.name()+" to reset.");
				theQ.resetQuest(5);
			}
		}
		else
			affected.delEffect(this);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
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
	public int[] usageCost(MOB mob, boolean ignoreCostOverride)
	{
		return cost;
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}
}
