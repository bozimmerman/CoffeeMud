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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

// requires nothing to load
/*
   Copyright 2005-2023 Bo Zimmerman

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
	protected String		 socialFullID;
	protected String		 socialFullTail;
	protected String		 socialBaseName;
	protected String		 socialTarget;
	protected boolean		 isTargetable;
	protected String		 socialArg;
	protected String		 sourceMsg;
	protected String		 othersSeeMsg;
	protected String		 targetSeesMsg;
	protected String		 failedTargetMsg;
	protected String		 soundFile	= "";
	protected String		 zapperMask	= "";
	protected CompiledZMask	 zMask		= null;
	protected int			 sourceCode	= CMMsg.MSG_OK_ACTION;
	protected int			 othersCode	= CMMsg.MSG_OK_ACTION;
	protected int			 targetCode	= CMMsg.MSG_OK_ACTION;
	protected Set<SocialFlag>flags		= new SHashSet<SocialFlag>();

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
	public String getTargetDesc()
	{
		final String targ = this.socialTarget.toUpperCase().trim();
		if(targ.equals("<T-NAME>"))
			return ("MOB Target "+this.socialArg).toString();
		if(targ.equals("<I-NAME>"))
			return ("Room Item Target "+this.socialArg).toString();
		if(targ.equals("<V-NAME>"))
			return ("Inventory Target "+this.socialArg).toString();
		if(targ.equals("<E-NAME>"))
			return ("Equipment Target "+this.socialArg).toString();
		return this.socialFullTail;
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
	public boolean meetsCriteriaToUse(final MOB mob)
	{
		return (this.zMask == null) || (mob==null) || CMLib.masking().maskCheck(zMask, mob, false);
	}

	@Override
	public void setCriteriaZappermask(final String mask)
	{
		this.zMask=null;
		this.zapperMask = "";
		if((mask!=null)
		&&(mask.trim().length()>0))
		{
			this.zapperMask=mask.trim();
			zMask = CMLib.masking().maskCompile(this.zapperMask);
		}
	}

	@Override
	public String getCriteriaZappermask()
	{
		return this.zapperMask != null ? this.zapperMask : "";
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical target, final boolean auto)
	{
		if(mob == null)
			return false;

		boolean confirmed=false;
		if(getFlags().contains(SocialFlag.CONFIRM)
		&&(commands.size()>0)
		&&(commands.get(commands.size()-1).equalsIgnoreCase("CONFIRMED")))
		{
			commands.remove(commands.size()-1);
			confirmed=true;
		}

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

		Physical targetP = target;
		if (targetP == null)
		{
			final Room R = mob.location();
			if(R== null)
				return false;
			int ctr = 1;
			targetP = R.fetchFromMOBRoomFavorsMOBs(mob, null, targetStr, Wearable.FILTER_ANY);
			while ((targetP != null)
			&& (!CMLib.flags().canBeSeenBy(targetP, mob))
			&&(targetStr.indexOf('.')<0))
				targetP = R.fetchFromMOBRoomFavorsMOBs(mob, null, targetStr+"."+(++ctr), Wearable.FILTER_ANY);
			if ((targetP != null) && (!targetable(targetP)))
			{
				final Social S = CMLib.socials().fetchSocial(baseName(), targetP, restArg, true);
				if((S != null)
				&& (S.meetsCriteriaToUse(mob)))
				{
					if(confirmed)
						commands.add("CONFIRMED");
					return S.invoke(mob, commands, targetP, auto);
				}
			}
		}

		if(getFlags().contains(SocialFlag.CONFIRM)
		&&(!confirmed)
		&&(!mob.isMonster()))
		{
			final Session sess=mob.session();
			sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"Y",0)
			{
				@Override
				public void showPrompt()
				{
					sess.promptPrint(L("\n\rAre you sure (Y/n)? "));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					if (this.input.equals("Y"))
					{
						if(confirmed)
							commands.add("CONFIRMED");
						invoke(mob, commands, target, auto);
					}
				}
			});
			return true;
		}
		if(getFlags().contains(SocialFlag.TARG_CONFIRM)
		&&(targetP instanceof MOB)
		&&(!((MOB)targetP).isMonster()))
		{
			final Session sess=((MOB)targetP).session();
			final Physical fP=targetP;
			sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"Y",0)
			{
				final Physical tarP=fP;
				@Override
				public void showPrompt()
				{
					sess.promptPrint(L("\n\r@x1 want(s) to @x2 you.  Is this OK (Y/n)? ",mob.name(),baseName().toLowerCase()));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					if (this.input.equals("Y"))
						invokeIntern(mob, commands, tarP, auto);
					else
						mob.tell(L("@x1 forbid(s) you to @x2.",tarP.name(),baseName().toLowerCase()));
				}
			});
			return true;
		}
		return invokeIntern(mob, commands, targetP, auto);
	}

	protected boolean invokeIntern(final MOB mob, final List<String> commands, final Physical targetP, final boolean auto)
	{
		final Room R = mob.location();
		if(R== null)
			return false;

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

		if (((targetP == null) && (targetable(null)))
		|| ((targetP != null) && (!targetable(targetP))))
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
		if (targetP == null)
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
			final CMMsg msg = CMClass.getMsg(mob, targetP, this,
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
				if (targetP instanceof MOB)
				{
					final MOB tmob = (MOB) targetP;
					if(mob.isPlayer())
					{
						if(tmob.isPlayer())
						{
							if((CMProps.getIntVar(CMProps.Int.RP_SOCIAL_PC)!=0)&&(awardRPXP(mob)))
								CMLib.leveler().postRPExperience(mob, "SOCIAL:"+socialFullID, tmob, "", CMProps.getIntVar(CMProps.Int.RP_SOCIAL_PC), false);
						}
						else
						{
							if((CMProps.getIntVar(CMProps.Int.RP_SOCIAL_NPC)!=0)&&(awardRPXP(mob)))
								CMLib.leveler().postRPExperience(mob, "SOCIAL:"+socialFullID, tmob, "", CMProps.getIntVar(CMProps.Int.RP_SOCIAL_NPC), false);
						}
					}

					if ((name().toUpperCase().startsWith("SMILE"))
					&& (mob.charStats().getStat(CharStats.STAT_CHARISMA) >= 16)
					&& (mob.charStats().getMyRace().ID().equals(tmob.charStats().getMyRace().ID()))
					&& (CMLib.dice().rollPercentage() == 1)
					&& (mob.charStats().reproductiveCode() != ('N'))
					&& (tmob.charStats().reproductiveCode() != ('N'))
					&& (mob.charStats().getStat(CharStats.STAT_GENDER) != tmob.charStats().getStat(CharStats.STAT_GENDER))
					&& (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						final Ability A = CMClass.getAbility("Disease_Smiles");
						if ((A != null) && (targetP.fetchEffect(A.ID()) == null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							A.invoke(tmob, tmob, true, 0);
					}
				}
				else
				{
					if((CMProps.getIntVar(CMProps.Int.RP_SOCIAL_OTH)!=0)&&(awardRPXP(mob)))
						CMLib.leveler().postRPExperience(mob, "SOCIAL:"+socialFullID, null, "", CMProps.getIntVar(CMProps.Int.RP_SOCIAL_OTH), false);
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

		String youSee = getSourceMessage();
		if ((youSee != null)
		&& (youSee.trim().length() == 0))
		{
			youSee = null;
			srcCode = CMMsg.NO_EFFECT;
		}
		else
			youSee = str + youSee + end + mspFile;

		String thirdPartySees = getOthersMessage();
		if ((thirdPartySees != null)
		&& (thirdPartySees.trim().length() == 0))
		{
			thirdPartySees = null;
			otherCode = CMMsg.NO_EFFECT;
		}
		else
			thirdPartySees = str + thirdPartySees + end + mspFile;

		CMMsg msg = null;
		if (((target == null) && (targetable(null)))
		|| ((target != null) && (!targetable(target))))
		{
			String seeWhenNoTarget = getFailedTargetMessage();
			if ((seeWhenNoTarget == null)
			|| (seeWhenNoTarget.trim().length() == 0))
				seeWhenNoTarget = null;
			else
				seeWhenNoTarget = str + seeWhenNoTarget + end;
			msg = CMClass.getMsg(mob, null, this, srcCode, seeWhenNoTarget, CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null);
		}
		else
		if (target == null)
			msg = CMClass.getMsg(mob, null, this, srcCode, youSee, CMMsg.NO_EFFECT, null, otherCode, thirdPartySees);
		else
		{
			String targetSees = getTargetMessage();
			if ((targetSees != null)
			&& (targetSees.trim().length() == 0))
			{
				targetSees = null;
				targetCode = CMMsg.NO_EFFECT;
			}
			else
				targetSees = str + targetSees + end + mspFile;
			msg = CMClass.getMsg(mob, target, this, srcCode, youSee, targetCode, targetSees, otherCode, thirdPartySees);
		}
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
	public String getEncodedLine()
	{
		final StringBuilder buf = new StringBuilder("");
		switch(getSourceCode())
		{
		case CMMsg.MSG_SPEAK:
			buf.append('w');
			break;
		case CMMsg.MSG_HANDS:
			buf.append('m');
			break;
		case CMMsg.MSG_NOISE:
			buf.append('s');
			break;
		case CMMsg.MSG_NOISYMOVEMENT:
			buf.append('o');
			break;
		case CMMsg.MSG_QUIETMOVEMENT:
		case CMMsg.MSG_SUBTLEMOVEMENT:
			buf.append('q');
			break;
		default:
			buf.append(' ');
			break;
		}
		switch(getTargetCode())
		{
		case CMMsg.MSG_HANDS:
			buf.append('t');
			break;
		case CMMsg.MSG_NOISE:
			buf.append('s');
			break;
		case CMMsg.MSG_SPEAK:
			buf.append('w');
			break;
		case CMMsg.MSG_NOISYMOVEMENT:
			buf.append('v');
			break;
		case CMMsg.MSG_QUIETMOVEMENT:
		case CMMsg.MSG_SUBTLEMOVEMENT:
			buf.append('q');
			break;
		case CMMsg.MSG_OK_VISUAL:
			buf.append('o');
			break;
		default:
			buf.append(' ');
			break;
		}
		final String[] stuff=new String[] {
			name(),
			getSourceMessage(),
			getOthersMessage(),
			getTargetMessage(),
			getFailedTargetMessage(),
			getSoundFile(),
			getCriteriaZappermask(),
			CMParms.toListString(getFlags())
		};
		buf.append('\t');
		for (final String element : stuff)
		{
			if(element==null)
				buf.append("\t");
			else
				buf.append(element+"\t");
		}
		buf.setCharAt(buf.length()-1,'\r');
		buf.append('\n');
		return buf.toString();
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
		if (flags.size() != ((Social)E).getFlags().size())
			return false;
		if(flags.size()>0)
		{
			if(!flags.containsAll(((Social)E).getFlags()))
				return false;
			if(!((Social)E).getFlags().containsAll(flags))
				return false;
		}

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

	@Override
	public Set<SocialFlag> getFlags()
	{
		return flags;
	}
}
