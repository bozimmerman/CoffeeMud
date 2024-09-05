package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2024 Bo Zimmerman

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
public class Mood extends StdAbility
{
	@Override
	public String ID()
	{
		return "Mood";
	}

	private final static String	localizedName	= CMLib.lang().L("Mood");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return (mood == null) ? "" : "(In " + CMLib.english().startWithAorAn(mood.name().toLowerCase()) + " mood)";
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
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected enum MoodType
	{
		FORMAL("+ADJCHA 17","^Bformal","formally"),
		POLITE("+ADJCHA 13","^Bpolite","politely"),
		HAPPY("","^Yhappy","happily"),
		HAPPYSILLY("","^Yhappy","happily"),
		SAD("","^Csad","sadly"),
		ANGRY("","^rangry","angrily"),
		RUDE("","^grude","rudely"),
		MEAN("","^rmean","meanly"),
		PROUD("","^bproud","proudly"),
		GRUMPY("","^Ggrumpy","grumpily"),
		EXCITED("","^Wexcited","excitedly"),
		SCARED("","^yscared","scaredly"),
		LONELY("","^Clonely","lonely"),
		REFLECTIVE("","^Creflective","reflectively"),
		SILLY("","^psilly","sillily"),
		APATHETIC("","^kapathetic", "apathetically")
		;
		final String statChanges;
		final String adj;
		final String adv;

		private MoodType(final String statChanges, final String adj, final String adv)
		{
			this.statChanges=statChanges;
			this.adj=adj;
			this.adv=adv;
		}

	}

	protected MoodType		mood	= null;
	protected volatile int	counter	= 0;
	protected Object		lastOne	= null;
	protected CMMsg			lastMsg	= null;

	public static final String[] BOAST_CHANNELS=
	{
		"BOAST","GRATZ","ANNOUNCE","GOSSIP","OOC","CHAT"
	};

	public final static String[] uglyPhrases=
	{
		"orc-brain",
		"jerk",
		"dork",
		"dim-wit",
		"excremental waste",
		"squeegy",
		"ding-dong",
		"female-dog",
		"smelly dork",
		"geek",
		"illegitimate offspring",
		"gluteus maximus cavity",
		"uncle copulator",
		"ugly yokle",
		"brainless goop",
		"stupid noodle",
		"stupid ugly-bottom",
		"pig-dog",
		"son of a silly person",
		"silly K...kanigget",
		"empty-headed animal",
		"food trough wiper",
		"perfidious mousedropping hoarder",
		"son of a window-dresser",
		"brightly-colored, mealy-templed, cranberry-smeller",
		"electric donkey-bottom biter",
		"bed-wetting type",
		"tiny-brained wiper of other people`s bottoms"
	};

	public static String[] sillySocials = new String[] {
		"ADJUST","AGREE","AIR","ANGELIC","ANTICIPATE","APPLAUD","BABBLE","BARK","BAT",
		"BEAM","BEARHUG","BECKON","BEER","BEG","BIRD","BITE","BKISS","BLINK","BLUSH",
		"BOAST","BOGGLE","BONK","BONK","BONK","BONK","BOUNCE","BOW","CACKLE","CHARGE",
		"CHEER","CHEST","CHORTLE","CHUCKLE","CLAP","COLLAPSE","COMB","COMMANDO","CONFUSED",
		"CONSPIRE","CONTEMPLATE","COUGH","COZY","CURTSEY","DANCE","DISCODANCE","DOH",
		"DROOL","DUCK","EARPLUG","EMBRACE","EYEBROW","FART","FIDGET","FLASH","FLEX",
		"FLIRT","FLUTTER","FOOTSIE","FPALM","FROLICK","GIGGLE","GOOSE","GREET","GROUPHUG",
		"HICCUP","HIGHFIVE","HOMEWORK","HOP","HOWL","HUSH","INNOCENT","JIG","KISS","LAUGH",
		"LAUGH","LAUGH","LICK","MOO","MOON","MOSH","NIBBLE","NOOGIE","NTWIST","NUDGE",
		"NUZZLE","PANT","PAT","PATPAT","PECK","PET","PIE","PILLOW","PINCH","POINT","POKE",
		"POUNCE","PRANCE","PRAY","PURR","RASPBERRY","ROAR","ROFL","ROFL","ROFL","ROFL",
		"ROFLMAO","ROFLMAO","ROFLMAO","ROLL","ROLLOVER","SERENADE","SHAKE","SILLY","SILLY",
		"SILLY","SLOBBER","SLOWDANCE","SMILE","SMILE","SMIRK","SMIRK","SNOWBALL","SPIN",
		"SQUEEL","SQUIRM","SSMILE","STRUT","SWEET","SWOON","TEASE","TEASE","THANK","THANK",
		"THANK","TICKLE","TICKLE","TONGUE","TWERK","TWIDDLE","WHEW","WIGGY","WIGGY","WIGGY",
		"WINK","WINK","WONDER","WORM","YEEHAW","ZERBERT"
	};

	public static String[] happySillySocials = new String[] {
		"AGREE","ANGELIC","APPLAUD","BEAM","BEARHUG","BEER","BKISS","BOW","CHEER",
		"CHUCKLE","CLAP","COZY","CURTSEY","DROOL","EMBRACE","EYEBROW",
		"FLIRT","FLUTTER","GIGGLE","GREET","GROUPHUG","HIGHFIVE","JIG",
		"LAUGH","PAT","PECK","PET","ROFL","ROFLMAO","SERENADE","SHAKE","SLOBBER",
		"SMILE","SMILE","SMILE","SMILE","SQUEEL","SSMILE","SSMILE","THANK","THANK",
		"THANK","TICKLE"
	};

	@Override
	public void setMiscText(String newText)
	{
		// this checks the input, and allows us to get mood
		// lists without having the code in front of us.
		if(newText.length()>0)
		{
			mood=null;
			if(CMath.isInteger(newText))
			{
				final int x=CMath.s_int(newText);
				if((x>=0)&&(x<MoodType.values().length))
				{
					mood=MoodType.values()[x];
					newText=mood.name();
				}
			}
			else
			if(newText.equalsIgnoreCase("random"))
			{
				mood = MoodType.values()[CMLib.dice().roll(1, MoodType.values().length, -1)];
				newText=mood.name();
			}
			else
			{
				mood=(MoodType)CMath.s_valueOf(MoodType.class, newText);
				if(mood == null)
				{
					for(int i=0;i<MoodType.values().length;i++)
					{
						if(MoodType.values()[i].name().equalsIgnoreCase(newText))
							mood=MoodType.values()[i];
					}
					if(mood == null)
						newText="";
					else
						newText=mood.name();
				}
			}
		}
		super.setMiscText(newText);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(mood == null)
		{
			if(affected != null)
				affected.delEffect(this);
			else
			if(ticking instanceof Physical)
				((Physical)ticking).delEffect(this);
			return true;
		}
		if((!(affected instanceof MOB))
		||(CMLib.flags().isAliveAwakeMobileUnbound((MOB)affected, true)))
		{
			if(((MOB)affected).phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD))
				return true;
			switch(mood)
			{
			case SILLY: // silly
			{
				final Physical affected=this.affected;
				if(affected instanceof MOB)
				{
					if(counter<=0)
						counter=CMLib.dice().roll(1, 55, 5);
					else
					if(--counter<=1)
					{
						counter=2;
						final int sillySocialIndex=CMLib.dice().roll(1, sillySocials.length, -1);
						final String socialName = sillySocials[sillySocialIndex];
						final Social social = CMLib.socials().fetchSocial(socialName, true);
						if(social != null)
						{
							counter=CMLib.dice().roll(1, 55, 5);
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=(MOB)affected;
								@Override
								public void run()
								{
									mob.enqueCommand(new XVector<String>(socialName), 0, 0);
								}
							}, 500);
						}
					}
				}
				break;
			}
			case APATHETIC: // apathetic
			{
				final Physical affected=this.affected;
				if(affected instanceof MOB)
				{
					if(counter<=0)
						counter=CMLib.dice().roll(1, 35, 15);
					else
					if(--counter<=1)
					{
						counter=CMLib.dice().roll(1, 55, 15);
						final Social social = CMLib.socials().fetchSocial("SIGH", true);
						if(social != null)
						{
							counter=CMLib.dice().roll(1, 35, 15);
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=(MOB)affected;
								@Override
								public void run()
								{
									mob.enqueCommand(new XVector<String>("SIGH"), 0, 0);
								}
							}, 500);
						}
					}
				}
				break;
			}
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		if((mood != null)
		&&(mood.adj.length()>0)
		&&(!stats.isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD))
		&&(!affected.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
			stats.addAmbiance(mood.adj.toLowerCase()+"^?");
	}

	private String changeSay(final String msg, final String to)
	{
		if(msg==null)
			return null;
		final int x=msg.indexOf('\'');
		if(x<0)
			return msg;
		final int y=msg.indexOf(L("say(s)"));
		if((y>=0)&&(y<x))
			return msg.substring(0,y)+to+msg.substring(y+6);
		return msg;
	}

	private void changeAllSays(final CMMsg msg, final String to)
	{
		msg.setSourceMessage(changeSay(msg.sourceMessage(),to));
		msg.setTargetMessage(changeSay(msg.targetMessage(),to));
		msg.setOthersMessage(changeSay(msg.othersMessage(),to));
	}

	public MOB target(final MOB mob, final Environmental target)
	{
		if(target instanceof MOB)
			return (MOB)target;
		if(mob==null)
			return null;
		final Room R=mob.location();
		if(R==null)
			return null;
		if(R.numInhabitants()==1)
			return null;
		if(R.numInhabitants()==2)
		for(int r=0;r<R.numInhabitants();r++)
		{
			if(R.fetchInhabitant(r)!=mob)
				return R.fetchInhabitant(r);
		}
		if((lastOne instanceof MOB)&&(R.isInhabitant((MOB)lastOne)))
			return (MOB)lastOne;
		final List<MOB> players=new ArrayList<MOB>(R.numInhabitants());
		final ArrayList<MOB> mobs=new ArrayList<MOB>(R.numInhabitants());
		MOB M=null;
		for(int r=0;r<R.numInhabitants();r++)
		{
			M=R.fetchInhabitant(r);
			if((M!=mob)&&(M!=null))
			{
				if(M.isMonster())
					mobs.add(M);
				else
				if(!M.isMonster())
					players.add(M);
			}
		}
		if(players.size()==1)
			return players.get(0);
		if(players.size()>1)
			return null;
		if(mobs.size()==1)
			return mobs.get(0);
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg==lastMsg)
			return super.okMessage(myHost, msg);
		lastMsg=msg;
		if(affected instanceof MOB)
		{
			if((msg.source()==affected)
			&&(msg.sourceMessage()!=null)
			&&((msg.tool()==null)||(msg.tool().ID().equals("Common")))
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
			&&(mood != null)
			&&(msg.source().phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
			{
				String str=CMStrings.getSayFromMessage(msg.othersMessage());
				if(str==null)
					str=CMStrings.getSayFromMessage(msg.targetMessage());
				if(str!=null)
				{
					final MOB M=target(msg.source(),msg.target());
					if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
					{
						if(msg.sourceMinor()>= CMMsg.TYP_CHANNEL)
						{
							final int channelNum = msg.sourceMinor()-CMMsg.TYP_CHANNEL;
							final CMChannel C=CMLib.channels().getChannel(channelNum);
							if((C!=null) && (C.flags().contains(ChannelFlag.NOMOOD)))
								return super.okMessage(myHost, msg);
						}
						final String[] tags={"<S-NAME>","You",msg.source().Name()};
						String tag=null;
						for (final String tag2 : tags)
						{
							tag=tag2;
							if((msg.othersMessage()!=null)
							&&(msg.othersMessage().indexOf(mood.adv)<0)
							&&(msg.othersMessage().indexOf(tag)<msg.othersMessage().indexOf('\'')))
								msg.setOthersMessage(CMStrings.replaceFirst(msg.othersMessage(),tag,tag+" "+mood.adv));
							if((msg.targetMessage()!=null)
							&&(msg.targetMessage().indexOf(mood.adv)<0)
							&&(msg.targetMessage().indexOf(tag)<msg.targetMessage().indexOf('\'')))
								msg.setTargetMessage(CMStrings.replaceFirst(msg.targetMessage(),tag,tag+" "+mood.adv));
							if((msg.sourceMessage()!=null)
							&&(msg.sourceMessage().indexOf(mood.adv)<0)
							&&(msg.sourceMessage().indexOf(tag)<msg.sourceMessage().indexOf('\'')))
								msg.setSourceMessage(CMStrings.replaceFirst(msg.sourceMessage(),tag,tag+" "+mood.adv));
						}
					}
					final String oldStr=str;
					switch(mood)
					{
					case FORMAL: // formal
					{
						if(str.toUpperCase().startsWith("YOU "))
							str=CMStrings.replaceFirstWord(str,"you","thou");
						str=CMStrings.replaceWord(str,"you`ll","thou willst");
						str=CMStrings.replaceWord(str,"youll","thou willst");
						str=CMStrings.replaceWord(str,"you`re","thou art");
						str=CMStrings.replaceWord(str,"youre","thou art");
						str=CMStrings.replaceWord(str,"you`d","thou wouldst");
						str=CMStrings.replaceWord(str,"youd","thou wouldst");
						str=CMStrings.replaceWord(str,"you`ve","thou hast");
						str=CMStrings.replaceWord(str,"youve","thou hast");
						str=CMStrings.replaceWord(str,"he`s","he ist");
						str=CMStrings.replaceWord(str,"hes","he ist");
						str=CMStrings.replaceWord(str,"she`s","she ist");
						str=CMStrings.replaceWord(str,"shes","she ist");
						str=CMStrings.replaceWord(str,"it`s","it ist");
						str=CMStrings.replaceWord(str,"its","it ist");
						str=CMStrings.replaceWord(str,"it`ll","it willst");
						str=CMStrings.replaceWord(str,"itll","it willst");
						str=CMStrings.replaceWord(str,"it`d","it wouldst");
						str=CMStrings.replaceWord(str,"itd","it wouldst");
						str=CMStrings.replaceWord(str,"you","thee");
						str=CMStrings.replaceWord(str,"your","thine");
						str=CMStrings.replaceWord(str,"really","indeed");
						str=CMStrings.replaceWord(str,"mine","my own");
						str=CMStrings.replaceWord(str,"my","mine");
						str=CMStrings.replaceWord(str,"I`m","we art");
						str=CMStrings.replaceWord(str,"Im","we art");
						str=CMStrings.replaceWord(str,"I`ll","we willst");
						str=CMStrings.replaceWord(str,"Ill","we willst");
						str=CMStrings.replaceWord(str,"I`d","we had");
						str=CMStrings.replaceWord(str,"Id","we had");
						str=CMStrings.replaceWord(str,"I`ve","we hast");
						str=CMStrings.replaceWord(str,"Ive","we hast");
						str=CMStrings.replaceWord(str,"i am","we art");
						str=CMStrings.replaceWord(str,"i","we");
						str=CMStrings.replaceWord(str,"hi","greetings");
						str=CMStrings.replaceWord(str,"hello","salutations");
						str=CMStrings.replaceWord(str,"no","negative");
						str=CMStrings.replaceWord(str,"hey","greetings");
						str=CMStrings.replaceWord(str,"where is","where might we find");
						str=CMStrings.replaceWord(str,"how do","how wouldst");
						str=CMStrings.replaceWord(str,"can`t","canst not");
						str=CMStrings.replaceWord(str,"cant","canst not");
						str=CMStrings.replaceWord(str,"couldn`t","couldst not");
						str=CMStrings.replaceWord(str,"couldnt","couldst not");
						str=CMStrings.replaceWord(str,"aren`t","are not");
						str=CMStrings.replaceWord(str,"arent","are not");
						str=CMStrings.replaceWord(str,"didn`t","didst not");
						str=CMStrings.replaceWord(str,"didnt","didst not");
						str=CMStrings.replaceWord(str,"doesn`t","doth not");
						str=CMStrings.replaceWord(str,"doesnt","doth not");
						str=CMStrings.replaceWord(str,"does","doth");
						str=CMStrings.replaceWord(str,"wont","willst not");
						str=CMStrings.replaceWord(str,"won`t","willst not");
						str=CMStrings.replaceWord(str,"wasnt","wast not");
						str=CMStrings.replaceWord(str,"wasn`t","wast not");
						str=CMStrings.replaceWord(str,"werent","were not");
						str=CMStrings.replaceWord(str,"weren`t","were not");
						str=CMStrings.replaceWord(str,"wouldnt","wouldst not");
						str=CMStrings.replaceWord(str,"wouldn`t","wouldst not");
						str=CMStrings.replaceWord(str,"don`t","doest not");
						str=CMStrings.replaceWord(str,"dont","doest not");
						str=CMStrings.replaceWord(str,"haven`t","hast not");
						str=CMStrings.replaceWord(str,"havent","hast not");
						str=CMStrings.replaceWord(str,"hadn`t","hath not");
						str=CMStrings.replaceWord(str,"hadnt","hath not");
						str=CMStrings.replaceWord(str,"hasn`t","hast not");
						str=CMStrings.replaceWord(str,"hasnt","hast not");
						str=CMStrings.replaceWord(str,"have","hast");
						str=CMStrings.replaceWord(str,"had","hath");
						str=CMStrings.replaceWord(str,"isn`t","is not");
						str=CMStrings.replaceWord(str,"isnt","is not");
						str=CMStrings.replaceWord(str,"mustn`t","must not");
						str=CMStrings.replaceWord(str,"mustnt","must not");
						str=CMStrings.replaceWord(str,"needn`t","need not");
						str=CMStrings.replaceWord(str,"neednt","need not");
						str=CMStrings.replaceWord(str,"shouldn`t","should not");
						str=CMStrings.replaceWord(str,"shouldnt","should not");
						str=CMStrings.replaceWord(str,"are","art");
						str=CMStrings.replaceWord(str,"would","wouldst");
						str=CMStrings.replaceWord(str,"have","hast");
						str=CMStrings.replaceWord(str,"we`ll","we willst");
						str=CMStrings.replaceWord(str,"we`re","we art");
						str=CMStrings.replaceWord(str,"we`d","we wouldst");
						str=CMStrings.replaceWord(str,"we`ve","we hast");
						str=CMStrings.replaceWord(str,"weve","we hast");
						str=CMStrings.replaceWord(str,"they`ll","they willst");
						str=CMStrings.replaceWord(str,"theyll","they willst");
						str=CMStrings.replaceWord(str,"they`re","they art");
						str=CMStrings.replaceWord(str,"theyre","they art");
						str=CMStrings.replaceWord(str,"they`d","they wouldst");
						str=CMStrings.replaceWord(str,"theyd","they wouldst");
						str=CMStrings.replaceWord(str,"they`ve","they hast");
						str=CMStrings.replaceWord(str,"theyve","they hast");
						str=CMStrings.replaceWord(str,"there`s","there ist");
						str=CMStrings.replaceWord(str,"theres","there ist");
						str=CMStrings.replaceWord(str,"there`d","there wouldst");
						str=CMStrings.replaceWord(str,"thered","there wouldst");
						str=CMStrings.replaceWord(str,"there`ll","there willst");
						str=CMStrings.replaceWord(str,"therell","there shall");
						str=CMStrings.replaceWord(str,"that`s","that ist");
						str=CMStrings.replaceWord(str,"thats","that ist");
						str=CMStrings.replaceWord(str,"that`d","that wouldst");
						str=CMStrings.replaceWord(str,"thatd","that wouldst");
						str=CMStrings.replaceWord(str,"that`ll","that willst");
						str=CMStrings.replaceWord(str,"thatll","that willst");
						str=CMStrings.replaceWord(str,"is","ist");
						str=CMStrings.replaceWord(str,"will","shall");
						str=CMStrings.replaceWord(str,"would","wouldst");
						str=CMStrings.endWithAPeriod(str);
						switch(CMLib.dice().roll(1,15,0))
						{
						case 1:
							changeAllSays(msg, "state(s)");
							break;
						case 2:
							changeAllSays(msg, "declare(s)");
							break;
						case 3:
							changeAllSays(msg, "announce(s)");
							break;
						case 4:
							changeAllSays(msg, "elucidate(s)");
							break;
						case 5:
							changeAllSays(msg, "enunciate(s)");
							break;
						case 6:
							changeAllSays(msg, "indicate(s)");
							break;
						case 7:
							changeAllSays(msg, "communicate(s)");
							break;
						case 8:
							changeAllSays(msg, "avow(s)");
							break;
						case 9:
							changeAllSays(msg, "inform(s)");
							break;
						case 10:
							changeAllSays(msg, "propound(s)");
							break;
						default:
							break;
						}
						break;
					}
					case POLITE: // polite
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("HANDSHAKE",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,8,0))
						{
						case 1:
							str = L("If you please, @x1", str);
							break;
						case 2:
							str = L("@x1 Thank you.", CMStrings.endWithAPeriod(str));
							break;
						case 3:
							str = L("@x1 If you please.", CMStrings.endWithAPeriod(str));
							break;
						case 4:
							str = L("Forgive me but, @x1", str);
							break;
						case 5:
							str = L("If I may, @x1", str);
							break;
						case 6:
							str = L("Please, @x1", str);
							break;
						case 7:
							str = L("Humbly speaking, @x1", str);
							break;
						default:
							if(msg.source().charStats().reproductiveCode()=='F')
							{
								if(M!=null)
									msg.source().doCommand(new XVector<String>("CURTSEY",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
								else
									msg.source().doCommand(new XVector<String>("CURTSEY"),MUDCmdProcessor.METAFLAG_FORCED);
							}
							else
							if(M!=null)
								msg.source().doCommand(new XVector<String>("BOW",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("BOW"),MUDCmdProcessor.METAFLAG_FORCED);
							break;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, "politely say(s)");
							break;
						case 2:
							changeAllSays(msg, "humbly say(s)");
							break;
						case 3:
							changeAllSays(msg, "meekly say(s)");
							break;
						case 4:
							changeAllSays(msg, "politely say(s)");
							break;
						default:
							break;
						}
						break;
					}
					case APATHETIC: // apathetic
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							str = L("Not like I care, but @x1", str);
							break;
						case 2:
							str = L("@x1 or whatever, I don't care.", str);
							break;
						case 3:
							str = L("@x1 or whatever.", str);
							break;
						case 4:
							str = L("I don't care, but, @x1", str);
							break;
						case 5:
							changeAllSays(msg, "apathetically say(s)");
							break;
						case 6:
							changeAllSays(msg, "shrug(s)");
							break;
						case 7:
							changeAllSays(msg, "passively say(s)");
							break;
						default:
							break;
						}
						break;
					}
					case HAPPY: // happy
					case HAPPYSILLY: // happysilly
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("SMILE",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,7,0))
						{
						case 1:
							changeAllSays(msg, "laugh(s)");
							break;
						case 2:
							changeAllSays(msg, "smile(s)");
							break;
						case 3:
							changeAllSays(msg, "beam(s)");
							break;
						case 4:
							changeAllSays(msg, "cheerfully say(s)");
							break;
						case 5:
							changeAllSays(msg, "happily say(s)");
							break;
						case 6:
							changeAllSays(msg, "playfully say(s)");
							break;
						case 7:
							changeAllSays(msg, "sweetly say(s)");
							break;
						}
						break;
					}
					case SAD: // sad
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("CRY",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							changeAllSays(msg, "sigh(s)");
							break;
						case 2:
							changeAllSays(msg, "cr(ys)");
							break;
						case 3:
							changeAllSays(msg, "sob(s)");
							break;
						case 4:
							changeAllSays(msg, "sadly say(s)");
							break;
						case 5:
							changeAllSays(msg, "moap(s)");
							break;
						case 6:
							changeAllSays(msg, "sulk(s)");
							break;
						case 7:
							changeAllSays(msg, "ache(s)");
							break;
						default:
							break;
						}
						break;
					}
					case ANGRY: // angry
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							changeAllSays(msg, "growl(s)");
							break;
						case 2:
							changeAllSays(msg, "snarl(s)");
							break;
						case 3:
							changeAllSays(msg, "rage(s)");
							break;
						case 4:
							changeAllSays(msg, "snap(s)");
							break;
						case 5:
							changeAllSays(msg, "roar(s)");
							break;
						case 6:
							changeAllSays(msg, "yell(s)");
							break;
						case 7:
							changeAllSays(msg, "angrily say(s)");
							break;
						case 8:
							if(M!=null)
								msg.source().doCommand(new XVector<String>("GRUMBLE",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("GRUMBLE"),MUDCmdProcessor.METAFLAG_FORCED);
							break;
						default:
							break;
						}
						str=str.toUpperCase();
						break;
					}
					case RUDE: // rude
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							changeAllSays(msg, "sneer(s)");
							break;
						case 2:
							changeAllSays(msg, "jeer(s)");
							break;
						case 3:
							changeAllSays(msg, "sniff(s)");
							break;
						case 4:
							changeAllSays(msg, "disdainfully say(s)");
							break;
						case 5:
							changeAllSays(msg, "insultingly say(s)");
							break;
						case 6:
							changeAllSays(msg, "scoff(s)");
							break;
						case 7:
							changeAllSays(msg, "rudely say(s)");
							break;
						case 8:
							changeAllSays(msg, "gibe(s)");
							break;
						case 9:
							changeAllSays(msg, "mockingly say(s)");
							break;
						case 10:
							changeAllSays(msg, "interrupt(s)");
							break;
						default:
							break;
						}
						break;
					}
					case MEAN: // mean
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							changeAllSays(msg, "sneer(s)");
							break;
						case 2:
							changeAllSays(msg, "jeer(s)");
							break;
						case 3:
							changeAllSays(msg, "sniff(s)");
							break;
						case 4:
							changeAllSays(msg, "disdainfully say(s)");
							break;
						case 5:
							changeAllSays(msg, "insultingly say(s)");
							break;
						case 6:
							changeAllSays(msg, "scoff(s)");
							break;
						case 7:
							changeAllSays(msg, "meanly say(s)");
							break;
						case 8:
							changeAllSays(msg, "gibe(s)");
							break;
						case 9:
							changeAllSays(msg, "mockingly say(s)");
							break;
						case 10:
							changeAllSays(msg, "tauntingly say(s)");
							break;
						default:
							break;
						}
						final int rand=CMLib.dice().roll(1,20,0);
						if(rand<5)
							str=L("Hey @x1, @x2",uglyPhrases[CMLib.dice().roll(1,uglyPhrases.length,-1)],str);
						else
						if(rand<15)
							str=L("@x1..you @x2.",CMStrings.endWithAPeriod(str),uglyPhrases[CMLib.dice().roll(1,uglyPhrases.length,-1)]);
						else
						{
							if(M!=null)
								msg.source().doCommand(new XVector<String>("WHAP",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("WHAP"),MUDCmdProcessor.METAFLAG_FORCED);
						}
						if((M!=null)
						&&(CMLib.dice().roll(1,10,0)==1)
						&&(!msg.source().isInCombat())
						&&(!M.isInCombat())
						&&(M.mayPhysicallyAttack(msg.source()))
						&&(CMath.abs(M.phyStats().level()-msg.source().phyStats().level())<10))
							M.setVictim(msg.source());
						break;
					}
					case PROUD: // proud
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("FLEX",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, "boast(s)");
							break;
						case 2:
							changeAllSays(msg, "announce(s)");
							break;
						case 3:
							changeAllSays(msg, "proudly say(s)");
							break;
						default:
							break;
						}
						break;
					}
					case GRUMPY: // grumpy
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("GRUMBLE"),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,2,0))
						{
						case 1:
							changeAllSays(msg, "mutter(s)");
							break;
						case 2:
							changeAllSays(msg, "grumble(s)");
							break;
						default:
							break;
						}
						break;
					}
					case EXCITED: // excited
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("EXCITED",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, "shout(s)");
							break;
						case 2:
							changeAllSays(msg, "blurt(s)");
							break;
						case 3:
							changeAllSays(msg, "screech(es)");
							break;
						case 4:
							changeAllSays(msg, "excitedly say(s)");
							break;
						case 5:
							if(M!=null)
								msg.source().doCommand(new XVector<String>("FIDGET",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("FIDGET"),MUDCmdProcessor.METAFLAG_FORCED);
							break;
						default:
							break;
						}
						while(str.endsWith("."))
							str=str.substring(0,str.length()-1);
						final int num=CMLib.dice().roll(1,10,3);
						for(int i=0;i<num;i++)
							str+="!";
						str=str.toUpperCase();
						break;
					}
					case SCARED: // scared
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("COWER",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,6,0))
						{
						case 1:
							changeAllSays(msg, "meekly say(s)");
							break;
						case 2:
							changeAllSays(msg, "stutter(s)");
							break;
						case 3:
							changeAllSays(msg, ", shivering, say(s)");
							break;
						case 4:
							changeAllSays(msg, "squeek(s)");
							break;
						case 5:
							changeAllSays(msg, "barely say(s)");
							break;
						case 6:
							if(M!=null)
								msg.source().doCommand(new XVector<String>("WINCE",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("WINCE"),MUDCmdProcessor.METAFLAG_FORCED);
							break;
						default:
							break;
						}
						if((str.length()>0)&&(!Character.isWhitespace(str.charAt(0)))&&(CMLib.dice().rollPercentage()>50))
							str=str.charAt(0)+"-"+str.charAt(0)+"-"+str.charAt(0)+"-"+str;
						break;
					}
					case LONELY: // lonely
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(new XVector<String>("SIGH",M.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, "sigh(s)");
							break;
						case 2:
							changeAllSays(msg, "whisper(s)");
							break;
						case 3:
							changeAllSays(msg, ", alone, say(s)");
							break;
						case 4:
							changeAllSays(msg, "mutter(s)");
							break;
						case 5:
							changeAllSays(msg, "whine(s)");
							break;
						default:
							break;
						}
						break;
					}
					case REFLECTIVE: // reflective
					{
						switch(CMLib.dice().roll(1,6,0))
						{
						case 1:
							break;
						case 2:
							changeAllSays(msg, "reflect(s)");
							break;
						case 3:
							break;
						case 4:
							changeAllSays(msg, "contemplate(s)");
							break;
						case 5:
							changeAllSays(msg, "ponder(s)");
							break;
						default:
							break;
						}
						final StringBuilder s=new StringBuilder(str);
						int state=0;
						for(int i=0;i<s.length();i++)
						{
							if(Character.isLetter(s.charAt(i)))
							{
								if(state==2)
									s.setCharAt(i, Character.toUpperCase(s.charAt(i)));
								state=1;
							}
							else
							if(Character.isWhitespace(s.charAt(i))
							&&(state==1))
							{
								state=0;
								switch(CMLib.dice().roll(1, 3, 0))
								{
								case 0:
									break;
								case 1:
									s.insert(i, "...");
									break;
								case 2:
									s.insert(i, ". ");
									state=2;
									break;
								}

							}
						}
						str=s.toString();
						break;
					}
					default:
						break;
					}
					if(!oldStr.equals(str))
					{
						msg.modify(msg.source(),
								  msg.target(),
								  msg.tool(),
								  msg.sourceCode(),
								  CMStrings.substituteSayInMessage(msg.sourceMessage(),str),
								  msg.targetCode(),
								  CMStrings.substituteSayInMessage(msg.targetMessage(),str),
								  msg.othersCode(),
								  CMStrings.substituteSayInMessage(msg.othersMessage(),str));
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((mood != null)
		&&(affected != null)
		&&(!affected.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
		{
			switch(mood)
			{
			case SILLY: // silly
			{
				if((msg.source()==affected)
				&&(msg.sourceMessage()!=null)
				&&((msg.tool()==null)||(msg.tool().ID().equals("Common")))
				&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
				   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
				   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
				&&(CMLib.dice().rollPercentage()<33))
				{
					final int sillySocialIndex=CMLib.dice().roll(1, sillySocials.length, -1);
					final String socialName = sillySocials[sillySocialIndex];
					final Social social = CMLib.socials().fetchSocial(socialName, true);
					if(social != null)
					{
						if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
						{
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=msg.source();
								final int channelCode = msg.othersMinor() - CMMsg.TYP_CHANNEL;
								@Override
								public void run()
								{
									final CMChannel chan = CMLib.channels().getChannel(channelCode);
									if(chan != null)
									{
										final String channelName = chan.name();
										mob.enqueCommand(new XVector<String>(channelName,","+socialName), 0, 0);
									}
								}
							}, 500);
						}
						else
						if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
						||(msg.sourceMinor()==CMMsg.TYP_TELL))
						{
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=msg.source();
								@Override
								public void run()
								{
									mob.enqueCommand(new XVector<String>(socialName), 0, 0);
								}
							}, 500);
						}
					}
				}
				break;
			}
			case HAPPYSILLY: // happysilly
			{
				if((msg.source()==affected)
				&&(msg.sourceMessage()!=null)
				&&((msg.tool()==null)||(msg.tool().ID().equals("Common")))
				&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
				   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
				   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
				&&(CMLib.dice().rollPercentage()<33))
				{
					final int sillySocialIndex=CMLib.dice().roll(1, happySillySocials.length, -1);
					final String socialName = happySillySocials[sillySocialIndex];
					final Social social = CMLib.socials().fetchSocial(socialName, true);
					if(social != null)
					{
						if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
						{
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=msg.source();
								final int channelCode = msg.othersMinor() - CMMsg.TYP_CHANNEL;
								@Override
								public void run()
								{
									final CMChannel chan = CMLib.channels().getChannel(channelCode);
									if(chan != null)
									{
										final String channelName = chan.name();
										mob.enqueCommand(new XVector<String>(channelName,","+socialName), 0, 0);
									}
								}
							}, 500);
						}
						else
						if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
						||(msg.sourceMinor()==CMMsg.TYP_TELL))
						{
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=msg.source();
								@Override
								public void run()
								{
									mob.enqueCommand(new XVector<String>(socialName), 0, 0);
								}
							}, 500);
						}
					}
				}
				break;
			}
			case PROUD: // proud
			{
				if((msg.sourceMinor()==CMMsg.TYP_DEATH)
				&&(msg.tool()==affected)
				&&(this.lastOne!=msg.source()))
				{
					lastOne=msg.source();
					int channelIndex=-1;
					int channelC=-1;
					final String[] CHANNELS=CMLib.channels().getChannelNames();
					for(int c=0;c<CHANNELS.length;c++)
					{
						if(CMStrings.contains(BOAST_CHANNELS,CHANNELS[c]))
						{
							channelIndex=CMLib.channels().getChannelIndex(CHANNELS[c]);
							channelC=c;
							if(channelIndex>=0)
								break;
						}
					}
					if(channelIndex>=0)
					{
						String addOn=".";
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							addOn = ", but that`s not suprising, is it?";
							break;
						case 2:
							addOn = ". I rock.";
							break;
						case 3:
							addOn = ". I am **POWERFUL**.";
							break;
						case 4:
							addOn = ". I am sooo cool.";
							break;
						case 5:
							addOn = ". You can`t touch me.";
							break;
						case 6:
							addOn = ".. never had a chance, either.";
							break;
						case 7:
							addOn = ", with my PINKEE!";
							break;
						default:
							break;
						}
						((MOB)affected).doCommand(new XVector<String>(CHANNELS[channelC],"*I* just killed "+msg.source().Name()+addOn),MUDCmdProcessor.METAFLAG_FORCED);
					}
				}
				break;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String entered=CMParms.combine(commands,0);
		final String origEntered=CMParms.combine(commands,0);
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		Ability moodA=target.fetchEffect(ID());
		boolean add=false;
		if(moodA==null)
		{
			add=true;
			moodA=(Ability)copyOf();
			moodA.setMiscText("NORMAL");
		}
		String moodCode = moodA.text();
		if(moodCode.trim().length()==0)
			moodCode="NORMAL";
		final String moodName = CMLib.english().startWithAorAn(moodCode.toLowerCase());
		if(entered.trim().length()==0)
		{
			mob.tell(L("You are currently in @x1 mood.",moodName));
			return false;
		}
		if(entered.equalsIgnoreCase("RANDOM"))
		{
			final int rand=CMLib.dice().roll(1,MoodType.values().length+3,-1);
			if(rand>=MoodType.values().length)
				entered="NORMAL";
			else
				entered=MoodType.values()[rand].name();
		}
		String choice=null;
		String mask="";
		if(entered.equalsIgnoreCase("NORMAL"))
			choice="NORMAL";
		else
		{
			for (final MoodType element : MoodType.values())
			{
				if(element.name().equalsIgnoreCase(entered))
				{
					choice=element.name();
					mask=element.statChanges;
				}
			}
		}
		if((choice==null)&&(entered.length()>0)&&(Character.isLetter(entered.charAt(0))))
		{
			if("NORMAL".startsWith(entered.toUpperCase()))
				choice="NORMAL";
			else
			{
				for (final MoodType element : MoodType.values())
				{
					if(element.name().startsWith(entered.toUpperCase()))
					{
						choice=element.name();
						mask=element.statChanges;
					}
				}
			}
		}
		if((choice==null)||(entered.equalsIgnoreCase("list")))
		{
			String choices=", NORMAL";
			for (final MoodType element : MoodType.values())
				choices+=", "+element.name();
			if(entered.equalsIgnoreCase("LIST"))
				mob.tell(L("Mood choices include: @x1",choices.substring(2)));
			else
				mob.tell(L("'@x1' is not a known mood. Choices include: @x2",entered,choices.substring(2)));
			return false;
		}
		if(moodCode.equalsIgnoreCase(choice))
		{
			if(origEntered.equalsIgnoreCase("RANDOM"))
				return false;
			mob.tell(L("You are already in @x1 mood.",CMLib.english().startWithAorAn(choice.toLowerCase())));
			return false;
		}

		if((mask.length()>0)&&(!CMLib.masking().maskCheck(mask,mob,true)))
		{
			if(origEntered.equalsIgnoreCase("RANDOM"))
				return false;
			mob.tell(L("You must meet the following criteria to be in that mood: @x1",CMLib.masking().maskDesc(mask,true)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,L("<T-NAME> appear(s) to be in @x1 mood.",CMLib.english().startWithAorAn(choice.toLowerCase())));
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					if(choice.equalsIgnoreCase("NORMAL"))
						target.delEffect(moodA);
					else
					{
						if(add)
							target.addNonUninvokableEffect(moodA);
						moodA.setMiscText(choice);
					}
					target.recoverPhyStats();
					target.location().recoverRoomStats();
				}
			}
		}
		return success;
	}
}
