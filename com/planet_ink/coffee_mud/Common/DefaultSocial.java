package com.planet_ink.coffee_mud.Common;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

// requires nothing to load
/*
   Copyright 2005-2020 Bo Zimmerman

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

public class DefaultSocial implements Social
{
	protected String	socialFullID;
	protected String	socialFullTail;
	protected String	socialBaseName;
	protected String	socialTarget;
	protected boolean	isTargetable;
	protected String	socialArg;
	protected String	sourceMsg;
	protected String	othersSeeMsg;
	protected String	targetSeesMsg;
	protected String	failedTargetMsg;
	protected String	soundFile		= "";
	protected int		sourceCode		= CMMsg.MSG_OK_ACTION;
	protected int		othersCode		= CMMsg.MSG_OK_ACTION;
	protected int		targetCode		= CMMsg.MSG_OK_ACTION;

	@Override
	public String ID()
	{
		return "DefaultSocial";
	}

	@Override
	public String name()
	{
		return socialFullID;
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public String baseName()
	{
		return socialBaseName;
	}

	@Override
	public String tailName()
	{
		return socialFullTail;
	}

	@Override
	public boolean isTargetable()
	{
		return isTargetable;
	}

	@Override
	public String targetName()
	{
		return socialTarget;
	}

	@Override
	public String argumentName()
	{
		return socialArg;
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public void setName(final String newName)
	{
		socialFullID = newName.toUpperCase().trim();
		final int x = newName.indexOf(' ');
		if(x>0)
		{
			socialBaseName=newName.substring(0, x);
			socialFullTail=newName.substring(x+1).trim();
			final int y=newName.indexOf(' ',x+1);
			if(y>x)
			{
				socialTarget=newName.substring(x+1,y).trim();
				socialArg=newName.substring(y+1).trim();
			}
			else
			{
				socialTarget=newName.substring(x+1);
				socialArg="";
			}
			isTargetable = socialTarget.endsWith("-NAME>");
		}
		else
		{
			socialBaseName=newName;
			socialTarget="";
			isTargetable=false;
			socialFullTail="";
			socialArg="";
		}
	}

	@Override
	public String getSourceMessage()
	{
		return sourceMsg;
	}

	@Override
	public String getOthersMessage()
	{
		return othersSeeMsg;
	}

	@Override
	public String getTargetMessage()
	{
		return targetSeesMsg;
	}

	@Override
	public String getFailedTargetMessage()
	{
		return failedTargetMsg;
	}

	@Override
	public int getSourceCode()
	{
		return sourceCode;
	}

	@Override
	public int getOthersCode()
	{
		return othersCode;
	}

	@Override
	public int getTargetCode()
	{
		return targetCode;
	}

	@Override
	public void setSourceMessage(final String str)
	{
		sourceMsg = str;
	}

	@Override
	public void setOthersMessage(final String str)
	{
		othersSeeMsg = str;
	}

	@Override
	public void setTargetMessage(final String str)
	{
		targetSeesMsg = str;
	}

	@Override
	public void setFailedMessage(final String str)
	{
		failedTargetMsg = str;
	}

	@Override
	public void setSourceCode(final int code)
	{
		sourceCode = code;
	}

	@Override
	public void setOthersCode(final int code)
	{
		othersCode = code;
	}

	@Override
	public void setTargetCode(final int code)
	{
		targetCode = code;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public String getSoundFile()
	{
		return soundFile;
	}

	@Override
	public void setSoundFile(final String newFile)
	{
		soundFile = newFile;
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
	public boolean targetable(final Environmental E)
	{
		if (E == null)
			return isTargetable;
		if (E instanceof MOB)
			return isTargetable && targetName().equals("<T-NAME>");
		if ((E instanceof Item) && (((Item) E).container() == null))
		{
			final Item I = (Item) E;
			if (I.owner() instanceof Room)
			{
				if((I.displayText().length()==0)
				&&(!CMLib.flags().isGettable(I))
				&&((targetCode==CMMsg.MSG_NOISYMOVEMENT)||(targetCode==CMMsg.MSG_HANDS)))
					return false; // added so that touch didn't work on wallpaper
				return targetName().equals("<I-NAME>");
			}
			if (I.owner() instanceof MOB)
			{
				if (I.amWearingAt(Wearable.IN_INVENTORY))
					return targetName().equals("<V-NAME>");
				else
					return targetName().equals("<E-NAME>");
			}
		}
		return false;
	}

	protected boolean awardRPXP(final MOB mob)
	{
		final PlayerStats pStats=mob.playerStats();
		if(pStats != null)
		{
			if(System.currentTimeMillis() >= pStats.getLastRolePlayXPTime() + CMProps.getIntVar(CMProps.Int.RP_AWARD_DELAY))
			{
				pStats.setLastRolePlayXPTime(System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical target, final boolean auto)
	{
		if(mob == null)
			return false;
		final Room R = mob.location();
		if(R== null)
			return false;

		String targetStr = "";
		String restArg = "";
		if ((commands.size() > 1)
		&& (!commands.get(1).equalsIgnoreCase("SELF"))
		&& (!commands.get(1).equalsIgnoreCase("ALL")))
		{
			targetStr = commands.get(1);
			if (commands.size() > 2)
				restArg=commands.get(2);
		}

		Physical targetE = target;
		if (targetE == null)
		{
			targetE = R.fetchFromMOBRoomFavorsMOBs(mob, null, targetStr, Wearable.FILTER_ANY);
			if ((targetE != null) && (!CMLib.flags().canBeSeenBy(targetE, mob)))
				targetE = null;
			else
			if ((targetE != null) && (!targetable(targetE)))
			{
				final Social S = CMLib.socials().fetchSocial(baseName(), targetE, restArg, true);
				if (S != null)
					return S.invoke(mob, commands, targetE, auto);
			}
		}

		final String mspFile = ((soundFile != null) && (soundFile.length() > 0)) ? CMLib.protocol().msp(soundFile, 10) : "";

		String srcMsg = getSourceMessage();
		if ((srcMsg != null) && (srcMsg.trim().length() == 0))
			srcMsg = null;

		String othMsg = getOthersMessage();
		if ((othMsg != null) && (othMsg.trim().length() == 0))
			othMsg = null;

		String tgtMsg = getTargetMessage();
		if ((tgtMsg != null) && (tgtMsg.trim().length() == 0))
			tgtMsg = null;

		String failMsg = getFailedTargetMessage();
		if ((failMsg != null) && (failMsg.trim().length() == 0))
			failMsg = null;

		if (((targetE == null) && (targetable(null)))
		|| ((targetE != null) && (!targetable(targetE))))
		{
			final CMMsg msg = CMClass.getMsg(mob, null, this,
					(auto ? CMMsg.MASK_ALWAYS : 0) | getSourceCode(), failMsg,
					CMMsg.NO_EFFECT, null,
					CMMsg.NO_EFFECT, null);
			if (R.okMessage(mob, msg))
			{
				R.send(mob, msg);
				if(mob.isPlayer())
				{
					CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_SOCUSE);
					CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.SOCIALUSE, 1, this);
				}
			}
		}
		else
		if (targetE == null)
		{
			final CMMsg msg = CMClass.getMsg(mob, null, this,
					(auto ? CMMsg.MASK_ALWAYS : 0) | getSourceCode(), (srcMsg == null) ? null : srcMsg + mspFile,
					CMMsg.NO_EFFECT, null,
					getOthersCode(), (othMsg == null) ? null : othMsg + mspFile);
			if (R.okMessage(mob, msg))
			{
				R.send(mob, msg);
				if(mob.isPlayer())
				{
					CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_SOCUSE);
					CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.SOCIALUSE, 1, this);
				}
			}
		}
		else
		{
			final CMMsg msg = CMClass.getMsg(mob, targetE, this,
					(auto ? CMMsg.MASK_ALWAYS : 0) | getSourceCode(), (srcMsg == null) ? null : srcMsg + mspFile,
					getTargetCode(), (tgtMsg == null) ? null : tgtMsg + mspFile,
					getOthersCode(), (othMsg == null) ? null : othMsg + mspFile);
			if (R.okMessage(mob, msg))
			{
				R.send(mob, msg);
				if(mob.isPlayer())
				{
					CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_SOCUSE);
					CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.SOCIALUSE, 1, this);
				}
				if (targetE instanceof MOB)
				{
					final MOB tmob = (MOB) targetE;
					if(mob.isPlayer())
					{
						if(tmob.isPlayer())
						{
							if((CMProps.getIntVar(CMProps.Int.RP_SOCIAL_PC)!=0)&&(awardRPXP(mob)))
								CMLib.leveler().postRPExperience(mob, tmob, "", CMProps.getIntVar(CMProps.Int.RP_SOCIAL_PC), false);
						}
						else
						{
							if((CMProps.getIntVar(CMProps.Int.RP_SOCIAL_NPC)!=0)&&(awardRPXP(mob)))
								CMLib.leveler().postRPExperience(mob, tmob, "", CMProps.getIntVar(CMProps.Int.RP_SOCIAL_NPC), false);
						}
					}

					if ((name().toUpperCase().startsWith("SMILE"))
					&& (mob.charStats().getStat(CharStats.STAT_CHARISMA) >= 16)
					&& (mob.charStats().getMyRace().ID().equals(tmob.charStats().getMyRace().ID()))
					&& (CMLib.dice().rollPercentage() == 1)
					&& (mob.charStats().getStat(CharStats.STAT_GENDER) != ('N'))
					&& (tmob.charStats().getStat(CharStats.STAT_GENDER) != ('N'))
					&& (mob.charStats().getStat(CharStats.STAT_GENDER) != tmob.charStats().getStat(CharStats.STAT_GENDER))
					&& (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						final Ability A = CMClass.getAbility("Disease_Smiles");
						if ((A != null) && (targetE.fetchEffect(A.ID()) == null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							A.invoke(tmob, tmob, true, 0);
					}
				}
				else
				{
					if((CMProps.getIntVar(CMProps.Int.RP_SOCIAL_OTH)!=0)&&(awardRPXP(mob)))
						CMLib.leveler().postRPExperience(mob, null, "", CMProps.getIntVar(CMProps.Int.RP_SOCIAL_OTH), false);
				}
			}
		}
		return true;
	}

	@Override
	public CMMsg makeChannelMsg(final MOB mob, final int channelInt, final String channelName, final List<String> commands, final boolean makeTarget)
	{
		final String channelColor = "^Q";
		final String str = makeTarget ? "" : (channelColor + "^<CHANNEL \"" + channelName + "\"^>[" + channelName + "] ");
		final String end = makeTarget ? "" : "^</CHANNEL^>^N^.";
		return makeMessage(mob, str, end, CMMsg.MASK_CHANNEL, CMMsg.MASK_CHANNEL | (CMMsg.TYP_CHANNEL + channelInt), commands, channelName, makeTarget);
	}

	@Override
	public CMMsg makeMessage(final MOB mob, final String str, final String end, final int srcMask, final int fullCode, final List<String> commands, final String I3channelName, final boolean makeTarget)
	{
		String targetStr = "";
		if ((commands.size() > 1)
		&& (!commands.get(1).equalsIgnoreCase("SELF"))
		&& (!commands.get(1).equalsIgnoreCase("ALL")))
			targetStr = commands.get(1);

		Environmental target = null;
		if (targetStr.length() > 0)
		{
			String targetMud = "";
			if (targetStr.indexOf('@') > 0)
				targetMud = targetStr.substring(targetStr.indexOf('@') + 1);
			else
			{
				target = CMLib.players().getPlayerAllHosts(targetStr); // socials come from anywhere on the map
				if ((target == null) && (!makeTarget))
				{
					final MOB possTarget = CMLib.catalog().getCatalogMob(targetStr);
					if (possTarget != null)
					{
						final CatalogLibrary.CataData data = CMLib.catalog().getCatalogData(possTarget);
						if (data != null)
							target = data.getLiveReference();
					}
				}
			}
			if (((target == null) && (makeTarget))
			|| ((targetMud.length() > 0)
				&& (I3channelName != null)
				&& (CMLib.intermud().i3online())
				&& (CMLib.intermud().isI3channel(I3channelName))))
			{
				target = CMClass.getFactoryMOB();
				target.setName(targetStr);
				((MOB) target).setLocation(CMLib.map().getRandomRoom());
			}
			else
			if ((target != null)
			&& (!CMLib.flags().isInTheGame(target, true)))
				target = null;

			if ((target != null)
			&& (target instanceof Physical)
			&& (!CMLib.flags().isSeeable((Physical) target)))
				target = null;
		}

		String mspFile = ((soundFile != null) && (soundFile.length() > 0)) ? CMLib.protocol().msp(soundFile, 10) : "";
		if (end.length() == 0)
			mspFile = "";

		int targetCode = fullCode;
		int otherCode = fullCode;
		int srcCode = srcMask | getSourceCode();

		String You_see = getSourceMessage();
		if ((You_see != null)
		&& (You_see.trim().length() == 0))
		{
			You_see = null;
			srcCode = CMMsg.NO_EFFECT;
		}
		else
			You_see = str + You_see + end + mspFile;

		String Third_party_sees = getOthersMessage();
		if ((Third_party_sees != null)
		&& (Third_party_sees.trim().length() == 0))
		{
			Third_party_sees = null;
			otherCode = CMMsg.NO_EFFECT;
		}
		else
			Third_party_sees = str + Third_party_sees + end + mspFile;

		String Target_sees = getTargetMessage();
		if ((Target_sees != null)
		&& (Target_sees.trim().length() == 0))
		{
			Target_sees = null;
			targetCode = CMMsg.NO_EFFECT;
		}
		else
			Target_sees = str + Target_sees + end + mspFile;

		String See_when_no_target = getFailedTargetMessage();
		if ((See_when_no_target != null)
		&& (See_when_no_target.trim().length() == 0))
			See_when_no_target = null;
		else
			See_when_no_target = str + See_when_no_target + end;

		CMMsg msg = null;
		if (((target == null) && (targetable(null)))
		|| ((target != null) && (!targetable(target))))
			msg = CMClass.getMsg(mob, null, this, srcCode, See_when_no_target, CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null);
		else
		if (target == null)
			msg = CMClass.getMsg(mob, null, this, srcCode, You_see, CMMsg.NO_EFFECT, null, otherCode, Third_party_sees);
		else
			msg = CMClass.getMsg(mob, target, this, srcCode, You_see, targetCode, Target_sees, otherCode, Third_party_sees);
		return msg;
	}

	@Override
	public String description()
	{
		return "";
	}

	@Override
	public void setDescription(final String str)
	{
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public void setDisplayText(final String str)
	{
	}

	@Override
	public CMObject newInstance()
	{
		return new DefaultSocial();
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected boolean	amDestroyed	= false;

	@Override
	public void destroy()
	{
		amDestroyed = true;
	}

	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	@Override
	public boolean isSavable()
	{
		return true;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES	= { "CLASS", "NAME" };

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
			return name();
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
			setName(val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if (!(E instanceof Social))
			return false;
		final String name = socialFullID.toUpperCase().trim();
		if (!(((Social) E).name().toUpperCase().equals(name.trim())))
			return false;
		if (((sourceMsg == null) != (((Social) E).getSourceMessage() == null))
		|| ((sourceMsg != null) && (!sourceMsg.equals(((Social) E).getSourceMessage()))))
			return false;
		if (this.sourceCode != ((Social) E).getSourceCode())
			return false;
		if (this.targetCode != ((Social) E).getTargetCode())
			return false;
		if (this.othersCode != ((Social) E).getOthersCode())
			return false;
		if (((othersSeeMsg == null) != (((Social) E).getOthersMessage() == null))
		|| ((othersSeeMsg != null) && (!othersSeeMsg.equals(((Social) E).getOthersMessage()))))
			return false;
		if (((targetSeesMsg == null) != (((Social) E).getTargetMessage() == null))
		|| ((targetSeesMsg != null) && (!targetSeesMsg.equals(((Social) E).getTargetMessage()))))
			return false;
		if (((failedTargetMsg == null) != (((Social) E).getFailedTargetMessage() == null))
		|| ((failedTargetMsg != null) && (!failedTargetMsg.equals(((Social) E).getFailedTargetMessage()))))
			return false;
		if (((soundFile == null) != (((Social) E).getSoundFile() == null))
		|| ((soundFile != null) && (!soundFile.equals(((Social) E).getSoundFile()))))
			return false;
		return true;
	}

	protected void cloneFix(final Social E)
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultSocial E = (DefaultSocial) this.clone();
			E.cloneFix(this);
			return E;

		}
		catch (final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
	}

	@Override
	public String text()
	{
		return "";
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
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
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return true;
	}

	@Override
	public int maxRange()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int minRange()
	{
		return 0;
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
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSocialActionCost(baseName());
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSocialCombatActionCost(baseName());
	}

	@Override
	public double checkedActionsCost(final MOB mob, final List<String> cmds)
	{
		if (mob != null)
			return mob.isInCombat() ? combatActionsCost(mob, cmds) : actionsCost(mob, cmds);
		return actionsCost(mob, cmds);
	}
}
