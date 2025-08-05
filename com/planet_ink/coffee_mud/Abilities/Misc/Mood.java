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
   Copyright 2006-2025 Bo Zimmerman

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
		FORMAL("+ADJCHA 17",CMLib.lang().L("^Bformal"),CMLib.lang().L("formally")),
		POLITE("+ADJCHA 13",CMLib.lang().L("^Bpolite"),CMLib.lang().L("politely")),
		HAPPY("",CMLib.lang().L("^Yhappy"),CMLib.lang().L("happily")),
		HAPPYSILLY("",CMLib.lang().L("^Yhappy"),CMLib.lang().L("happily")),
		SAD("",CMLib.lang().L("^Csad"),CMLib.lang().L("sadly")),
		ANGRY("",CMLib.lang().L("^rangry"),CMLib.lang().L("angrily")),
		RUDE("",CMLib.lang().L("^grude"),CMLib.lang().L("rudely")),
		MEAN("",CMLib.lang().L("^rmean"),CMLib.lang().L("meanly")),
		PROUD("",CMLib.lang().L("^bproud"),CMLib.lang().L("proudly")),
		GRUMPY("",CMLib.lang().L("^Ggrumpy"),CMLib.lang().L("grumpily")),
		EXCITED("",CMLib.lang().L("^Wexcited"),CMLib.lang().L("excitedly")),
		SCARED("",CMLib.lang().L("^yscared"),CMLib.lang().L("scaredly")),
		LONELY("",CMLib.lang().L("^Clonely"),CMLib.lang().L("lonely")),
		REFLECTIVE("",CMLib.lang().L("^Creflective"),CMLib.lang().L("reflectively")),
		SILLY("",CMLib.lang().L("^psilly"),CMLib.lang().L("sillily")),
		APATHETIC("",CMLib.lang().L("^kapathetic"), CMLib.lang().L("apathetically")),
		PASSIVE("",CMLib.lang().L("^Wpassive"), CMLib.lang().L("passively")),
		HORNY("",CMLib.lang().L("^Rhorny"), CMLib.lang().L("hornily")),
		QUEER("",CMLib.lang().L("^Rqueer"), CMLib.lang().L("queerly")),
		LUSTY("",CMLib.lang().L("^Rlusty"), CMLib.lang().L("lustily")),
		SUBMISSIVE("",CMLib.lang().L("^Wsubmissive"), CMLib.lang().L("submissively")),
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
		CMLib.lang().L("orc-brain"),
		CMLib.lang().L("jerk"),
		CMLib.lang().L("dork"),
		CMLib.lang().L("dim-wit"),
		CMLib.lang().L("excremental waste"),
		CMLib.lang().L("squeegy"),
		CMLib.lang().L("ding-dong"),
		CMLib.lang().L("female-dog"),
		CMLib.lang().L("smelly dork"),
		CMLib.lang().L("geek"),
		CMLib.lang().L("illegitimate offspring"),
		CMLib.lang().L("gluteus maximus cavity"),
		CMLib.lang().L("uncle copulator"),
		CMLib.lang().L("ugly yokle"),
		CMLib.lang().L("brainless goop"),
		CMLib.lang().L("stupid noodle"),
		CMLib.lang().L("stupid ugly-bottom"),
		CMLib.lang().L("pig-dog"),
		CMLib.lang().L("son of a silly person"),
		CMLib.lang().L("silly K...kanigget"),
		CMLib.lang().L("empty-headed animal"),
		CMLib.lang().L("food trough wiper"),
		CMLib.lang().L("perfidious mousedropping hoarder"),
		CMLib.lang().L("son of a window-dresser"),
		CMLib.lang().L("brightly-colored, mealy-templed, cranberry-smeller"),
		CMLib.lang().L("electric donkey-bottom biter"),
		CMLib.lang().L("bed-wetting type"),
		CMLib.lang().L("tiny-brained wiper of other people`s bottoms")
	};

	public static String[] DEFAULT_SILLY_SOCIALS = null;
	public  String[] sillySocials() {
		if(DEFAULT_SILLY_SOCIALS == null)
		{
			DEFAULT_SILLY_SOCIALS = I(new String[] {
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
			});
		}
		return DEFAULT_SILLY_SOCIALS;
	}

	protected boolean isLustyMatch(final MOB mob, final MoodType moodType, final MOB M)
	{
		final char g = mob.baseCharStats().reproductiveCode();
		return
		 ((M!=null)
		&&(M!=mob)
		&&((moodType==MoodType.HORNY)
			||((moodType==MoodType.LUSTY)
				&&(M.baseCharStats().reproductiveCode()!=g)
				&&(M.charStats().reproductiveCode()!=('N'))
				&&(g!=('N')))
			||((moodType==MoodType.QUEER)&&(M.baseCharStats().reproductiveCode()==g))))
			;
	}

	public static String[] DEFAULT_HAPPY_SILLY_SOCIALS = null;
	public  String[] happySillySocials() {
		if(DEFAULT_HAPPY_SILLY_SOCIALS == null)
		{
			DEFAULT_HAPPY_SILLY_SOCIALS = I(new String[] {
				"AGREE","ANGELIC","APPLAUD","BEAM","BEARHUG","BEER","BKISS","BOW","CHEER",
				"CHUCKLE","CLAP","COZY","CURTSEY","DROOL","EMBRACE","EYEBROW",
				"FLIRT","FLUTTER","GIGGLE","GREET","GROUPHUG","HIGHFIVE","JIG",
				"LAUGH","PAT","PECK","PET","ROFL","ROFLMAO","SERENADE","SHAKE","SLOBBER",
				"SMILE","SMILE","SMILE","SMILE","SQUEEL","SSMILE","SSMILE","THANK","THANK",
				"THANK","TICKLE"
			});
		}
		return DEFAULT_HAPPY_SILLY_SOCIALS;
	}

	public static String[] DEFAULT_HORNY_SOCIALS = null;
	public  String[] hornySocials() {
		if(DEFAULT_HORNY_SOCIALS == null)
			DEFAULT_HORNY_SOCIALS = I(new String[] {
				"BITE", "BKISS", "BLUSH", "BSHAKE", "CARESS", "COZY", "COMB", "EYE", "FLIRT",
				"FOOTSIE", "FONDLE", "FROLICK", "FRENCH", "GROPE", "LAPDANCE", "MISCHIEVOUS",
				"MOAN", "LUST", "NIBBLE", "OGLE", "PURSE", "PURR", "SMILE", "STRUT", "STRADDLE",
				"SWOON"
			});
		return DEFAULT_HORNY_SOCIALS;
	}

	public static String[] DEFAULT_PASSIVE_SOCIALS = null;
	public  String[] passiveSocials() {
		if(DEFAULT_PASSIVE_SOCIALS == null)
			DEFAULT_PASSIVE_SOCIALS = I(new String[] {"STARE","SULK","CONTEMPLATE","BLINK"});
		return DEFAULT_PASSIVE_SOCIALS;
	}

	public static String[][] formalReplacements = new String[][]
	{
		{ CMLib.lang().L("you"),CMLib.lang().L("thou") },
		{ CMLib.lang().L("you`ll"),CMLib.lang().L("thou willst") },
		{ CMLib.lang().L("youll"),CMLib.lang().L("thou willst") },
		{ CMLib.lang().L("you`re"),CMLib.lang().L("thou art") },
		{ CMLib.lang().L("youre"),CMLib.lang().L("thou art") },
		{ CMLib.lang().L("you`d"),CMLib.lang().L("thou wouldst") },
		{ CMLib.lang().L("youd"),CMLib.lang().L("thou wouldst") },
		{ CMLib.lang().L("you`ve"),CMLib.lang().L("thou hast") },
		{ CMLib.lang().L("youve"),CMLib.lang().L("thou hast") },
		{ CMLib.lang().L("he`s"),CMLib.lang().L("he ist") },
		{ CMLib.lang().L("hes"),CMLib.lang().L("he ist") },
		{ CMLib.lang().L("she`s"),CMLib.lang().L("she ist") },
		{ CMLib.lang().L("shes"),CMLib.lang().L("she ist") },
		{ CMLib.lang().L("it`s"),CMLib.lang().L("it ist") },
		{ CMLib.lang().L("its"),CMLib.lang().L("it ist") },
		{ CMLib.lang().L("it`ll"),CMLib.lang().L("it willst") },
		{ CMLib.lang().L("itll"),CMLib.lang().L("it willst") },
		{ CMLib.lang().L("it`d"),CMLib.lang().L("it wouldst") },
		{ CMLib.lang().L("itd"),CMLib.lang().L("it wouldst") },
		{ CMLib.lang().L("you"),CMLib.lang().L("thee") },
		{ CMLib.lang().L("your"),CMLib.lang().L("thine") },
		{ CMLib.lang().L("really"),CMLib.lang().L("indeed") },
		{ CMLib.lang().L("mine"),CMLib.lang().L("my own") },
		{ CMLib.lang().L("my"),CMLib.lang().L("mine") },
		{ CMLib.lang().L("I`m"),CMLib.lang().L("we art") },
		{ CMLib.lang().L("Im"),CMLib.lang().L("we art") },
		{ CMLib.lang().L("I`ll"),CMLib.lang().L("we willst") },
		{ CMLib.lang().L("Ill"),CMLib.lang().L("we willst") },
		{ CMLib.lang().L("I`d"),CMLib.lang().L("we had") },
		{ CMLib.lang().L("Id"),CMLib.lang().L("we had") },
		{ CMLib.lang().L("I`ve"),CMLib.lang().L("we hast") },
		{ CMLib.lang().L("Ive"),CMLib.lang().L("we hast") },
		{ CMLib.lang().L("i am"),CMLib.lang().L("we art") },
		{ CMLib.lang().L("i"),CMLib.lang().L("we") },
		{ CMLib.lang().L("hi"),CMLib.lang().L("greetings") },
		{ CMLib.lang().L("hello"),CMLib.lang().L("salutations") },
		{ CMLib.lang().L("no"),CMLib.lang().L("negative") },
		{ CMLib.lang().L("hey"),CMLib.lang().L("greetings") },
		{ CMLib.lang().L("where is"),CMLib.lang().L("where might we find") },
		{ CMLib.lang().L("how do"),CMLib.lang().L("how wouldst") },
		{ CMLib.lang().L("can`t"),CMLib.lang().L("canst not") },
		{ CMLib.lang().L("cant"),CMLib.lang().L("canst not") },
		{ CMLib.lang().L("couldn`t"),CMLib.lang().L("couldst not") },
		{ CMLib.lang().L("couldnt"),CMLib.lang().L("couldst not") },
		{ CMLib.lang().L("aren`t"),CMLib.lang().L("are not") },
		{ CMLib.lang().L("arent"),CMLib.lang().L("are not") },
		{ CMLib.lang().L("didn`t"),CMLib.lang().L("didst not") },
		{ CMLib.lang().L("didnt"),CMLib.lang().L("didst not") },
		{ CMLib.lang().L("doesn`t"),CMLib.lang().L("doth not") },
		{ CMLib.lang().L("doesnt"),CMLib.lang().L("doth not") },
		{ CMLib.lang().L("does"),CMLib.lang().L("doth") },
		{ CMLib.lang().L("wont"),CMLib.lang().L("willst not") },
		{ CMLib.lang().L("won`t"),CMLib.lang().L("willst not") },
		{ CMLib.lang().L("wasnt"),CMLib.lang().L("wast not") },
		{ CMLib.lang().L("wasn`t"),CMLib.lang().L("wast not") },
		{ CMLib.lang().L("werent"),CMLib.lang().L("were not") },
		{ CMLib.lang().L("weren`t"),CMLib.lang().L("were not") },
		{ CMLib.lang().L("wouldnt"),CMLib.lang().L("wouldst not") },
		{ CMLib.lang().L("wouldn`t"),CMLib.lang().L("wouldst not") },
		{ CMLib.lang().L("don`t"),CMLib.lang().L("doest not") },
		{ CMLib.lang().L("dont"),CMLib.lang().L("doest not") },
		{ CMLib.lang().L("haven`t"),CMLib.lang().L("hast not") },
		{ CMLib.lang().L("havent"),CMLib.lang().L("hast not") },
		{ CMLib.lang().L("hadn`t"),CMLib.lang().L("hath not") },
		{ CMLib.lang().L("hadnt"),CMLib.lang().L("hath not") },
		{ CMLib.lang().L("hasn`t"),CMLib.lang().L("hast not") },
		{ CMLib.lang().L("hasnt"),CMLib.lang().L("hast not") },
		{ CMLib.lang().L("have"),CMLib.lang().L("hast") },
		{ CMLib.lang().L("had"),CMLib.lang().L("hath") },
		{ CMLib.lang().L("isn`t"),CMLib.lang().L("is not") },
		{ CMLib.lang().L("isnt"),CMLib.lang().L("is not") },
		{ CMLib.lang().L("mustn`t"),CMLib.lang().L("must not") },
		{ CMLib.lang().L("mustnt"),CMLib.lang().L("must not") },
		{ CMLib.lang().L("needn`t"),CMLib.lang().L("need not") },
		{ CMLib.lang().L("neednt"),CMLib.lang().L("need not") },
		{ CMLib.lang().L("shouldn`t"),CMLib.lang().L("should not") },
		{ CMLib.lang().L("shouldnt"),CMLib.lang().L("should not") },
		{ CMLib.lang().L("are"),CMLib.lang().L("art") },
		{ CMLib.lang().L("would"),CMLib.lang().L("wouldst") },
		{ CMLib.lang().L("have"),CMLib.lang().L("hast") },
		{ CMLib.lang().L("we`ll"),CMLib.lang().L("we willst") },
		{ CMLib.lang().L("we`re"),CMLib.lang().L("we art") },
		{ CMLib.lang().L("we`d"),CMLib.lang().L("we wouldst") },
		{ CMLib.lang().L("we`ve"),CMLib.lang().L("we hast") },
		{ CMLib.lang().L("weve"),CMLib.lang().L("we hast") },
		{ CMLib.lang().L("they`ll"),CMLib.lang().L("they willst") },
		{ CMLib.lang().L("theyll"),CMLib.lang().L("they willst") },
		{ CMLib.lang().L("they`re"),CMLib.lang().L("they art") },
		{ CMLib.lang().L("theyre"),CMLib.lang().L("they art") },
		{ CMLib.lang().L("they`d"),CMLib.lang().L("they wouldst") },
		{ CMLib.lang().L("theyd"),CMLib.lang().L("they wouldst") },
		{ CMLib.lang().L("they`ve"),CMLib.lang().L("they hast") },
		{ CMLib.lang().L("theyve"),CMLib.lang().L("they hast") },
		{ CMLib.lang().L("there`s"),CMLib.lang().L("there ist") },
		{ CMLib.lang().L("theres"),CMLib.lang().L("there ist") },
		{ CMLib.lang().L("there`d"),CMLib.lang().L("there wouldst") },
		{ CMLib.lang().L("thered"),CMLib.lang().L("there wouldst") },
		{ CMLib.lang().L("there`ll"),CMLib.lang().L("there willst") },
		{ CMLib.lang().L("therell"),CMLib.lang().L("there shall") },
		{ CMLib.lang().L("that`s"),CMLib.lang().L("that ist") },
		{ CMLib.lang().L("thats"),CMLib.lang().L("that ist") },
		{ CMLib.lang().L("that`d"),CMLib.lang().L("that wouldst") },
		{ CMLib.lang().L("thatd"),CMLib.lang().L("that wouldst") },
		{ CMLib.lang().L("that`ll"),CMLib.lang().L("that willst") },
		{ CMLib.lang().L("thatll"),CMLib.lang().L("that willst") },
		{ CMLib.lang().L("is"),CMLib.lang().L("ist") },
		{ CMLib.lang().L("will"),CMLib.lang().L("shall") },
		{ CMLib.lang().L("would"),CMLib.lang().L("wouldst") }
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
			case PASSIVE:
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
						final int sillySocialIndex=CMLib.dice().roll(1, passiveSocials().length, -1);
						final String socialName = passiveSocials()[sillySocialIndex];
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
			case HORNY:
			case LUSTY:
			case QUEER:
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
						final int sillySocialIndex=CMLib.dice().roll(1, passiveSocials().length, -1);
						final String socialName = passiveSocials()[sillySocialIndex];
						final Social social = CMLib.socials().fetchSocial(socialName, true);
						if(social != null)
						{
							counter=CMLib.dice().roll(1, 22, 5);
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								final MOB mob=(MOB)affected;
								final Room R = mob.location();
								final MoodType mymood = mood;
								@Override
								public void run()
								{
									if((R!=null)&&(mob.location()==R)&&(CMLib.flags().isInTheGame(mob, true)))
									{
										MOB targetM = null;
										//final char og =
										for(int m=0;m<R.numInhabitants();m++)
										{
											final MOB M = R.fetchInhabitant(m);
											if(isLustyMatch(mob,mymood,M))
											{
												targetM = M;
												break;
											}
										}
										if(targetM != null)
											mob.enqueCommand(new XVector<String>(socialName,R.getContextName(targetM)), 0, 0);
										else
											counter=5;
									}
								}
							}, 500);
						}
					}
				}
				break;
			}
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
						final int sillySocialIndex=CMLib.dice().roll(1, sillySocials().length, -1);
						final String socialName = sillySocials()[sillySocialIndex];
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

	private final static String DEFAULT_SAYS = CMLib.lang().L("say(s)");
	private final static String DEFAULT_YELL = CMLib.lang().L("YELL(S)");

	private String changeSay(final String sayWord, final String msg, final String to)
	{
		if(msg==null)
			return null;
		final int x=msg.indexOf('\'');
		if(x<0)
			return msg;
		final int y=msg.indexOf(sayWord);
		if((y>=0)&&(y<x))
			return msg.substring(0,y)+to+msg.substring(y+6);
		return msg;
	}

	private void changeAllSays(final CMMsg msg, final String to)
	{
		msg.setSourceMessage(changeSay(DEFAULT_SAYS,msg.sourceMessage(),to));
		msg.setTargetMessage(changeSay(DEFAULT_SAYS,msg.targetMessage(),to));
		msg.setOthersMessage(changeSay(DEFAULT_SAYS,msg.othersMessage(),to));
	}

	private void changeAllSays(final String sayWord, final CMMsg msg, final String to)
	{
		msg.setSourceMessage(changeSay(sayWord,msg.sourceMessage(),to));
		msg.setTargetMessage(changeSay(sayWord,msg.targetMessage(),to));
		msg.setOthersMessage(changeSay(sayWord,msg.othersMessage(),to));
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
			&&(!msg.source().phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
			{
				String str=CMStrings.getSayFromMessage(msg.othersMessage());
				if(str==null)
					str=CMStrings.getSayFromMessage(msg.targetMessage());
				if(str!=null)
				{
					final MOB targetM=target(msg.source(),msg.target());
					if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
					{
						if(msg.sourceMinor()>= CMMsg.TYP_CHANNEL)
						{
							final int channelNum = msg.sourceMinor()-CMMsg.TYP_CHANNEL;
							final CMChannel C=CMLib.channels().getChannel(channelNum);
							if((C!=null) && (C.flags().contains(ChannelFlag.NOMOOD)))
								return super.okMessage(myHost, msg);
						}
						if(mood.adv.length()>0)
						{
							final String[] tags={"<S-NAME>",L("You"),msg.source().Name()};
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
					}
					final String oldStr=str;
					switch(mood)
					{
					case FORMAL: // formal
					{
						if(str.toUpperCase().startsWith(formalReplacements[0][0].toUpperCase()+" "))
							str=CMStrings.replaceFirstWord(str,formalReplacements[0][0],formalReplacements[0][1]);
						for(final String[] form : formalReplacements)
							str=CMStrings.replaceWord(str,form[0],form[1]);
						str=CMStrings.endWithAPeriod(str, '.');
						switch(CMLib.dice().roll(1,15,0))
						{
						case 1:
							changeAllSays(msg, L("state(s)"));
							break;
						case 2:
							changeAllSays(msg, L("declare(s)"));
							break;
						case 3:
							changeAllSays(msg, L("announce(s)"));
							break;
						case 4:
							changeAllSays(msg, L("elucidate(s)"));
							break;
						case 5:
							changeAllSays(msg, L("enunciate(s)"));
							break;
						case 6:
							changeAllSays(msg, L("indicate(s)"));
							break;
						case 7:
							changeAllSays(msg, L("communicate(s)"));
							break;
						case 8:
							changeAllSays(msg, L("avow(s)"));
							break;
						case 9:
							changeAllSays(msg, L("inform(s)"));
							break;
						case 10:
							changeAllSays(msg, ("propound(s)"));
							break;
						default:
							break;
						}
						break;
					}
					case PASSIVE:
					{
						if(msg.sourceMessage() != null)
							msg.setSourceMessage(msg.sourceMessage().toLowerCase());
						if(msg.targetMessage() != null)
							msg.setTargetMessage(msg.targetMessage().toLowerCase());
						if(msg.othersMessage() != null)
							msg.setOthersMessage(msg.othersMessage().toLowerCase());
						changeAllSays(DEFAULT_SAYS,msg, ("whisper(s)"));
						changeAllSays(DEFAULT_YELL.toLowerCase(),msg, ("say(s)"));
						break;
					}
					case HORNY:
					case LUSTY:
					case QUEER:
					{
						if(isLustyMatch(msg.source(),mood,targetM))
						switch(CMLib.dice().roll(1, 13, -1))
						{
						case 0:
							str = L("Darling, @x1", str);
							break;
						case 1:
							str = L("Honey, @x1", str);
							break;
						case 2:
							str = L("Sugar, @x1", str);
							break;
						case 3:
							str = L("Sweetness, @x1", str);
							break;
						case 4:
							str = L("Luscious, @x1", str);
							break;
						case 5:
							str = L("@x1 Darling.", CMStrings.endWithAPeriod(str, ','));
							break;
						case 6:
							str = L("@x1Honey.", CMStrings.endWithAPeriod(str, ','));
							break;
						case 7:
							str = L("@x1Sugar.", CMStrings.endWithAPeriod(str, ','));
							break;
						case 8:
							str = L("@x1Sweetness.", CMStrings.endWithAPeriod(str, ','));
							break;
						case 9:
							str = L("@x1Luscious.", CMStrings.endWithAPeriod(str, ','));
							break;
						default:
							break;
						}
						break;
					}
					case POLITE: // polite
					{
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("HANDSHAKE",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,8,0))
						{
						case 1:
							str = L("If you please, @x1", str);
							break;
						case 2:
							str = L("@x1Thank you.", CMStrings.endWithAPeriod(str, '.'));
							break;
						case 3:
							str = L("@x1If you please.", CMStrings.endWithAPeriod(str, '.'));
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
								if(targetM!=null)
									msg.source().doCommand(new XVector<String>("CURTSEY",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
								else
									msg.source().doCommand(new XVector<String>("CURTSEY"),MUDCmdProcessor.METAFLAG_FORCED);
							}
							else
							if(targetM!=null)
								msg.source().doCommand(new XVector<String>("BOW",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("BOW"),MUDCmdProcessor.METAFLAG_FORCED);
							break;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, L("politely say(s)"));
							break;
						case 2:
							changeAllSays(msg, L("humbly say(s)"));
							break;
						case 3:
							changeAllSays(msg, L("meekly say(s)"));
							break;
						case 4:
							changeAllSays(msg, L("politely say(s)"));
							break;
						default:
							break;
						}
						break;
					}
					case SUBMISSIVE:
					{
						switch(CMLib.dice().roll(1,11,0))
						{
						case 1:
							str = L("If it pleases you, @x1", str);
							break;
						case 2:
							str = L("Please, @x1", str);
							break;
						case 3:
							str = L("If you'll permit, @x1", str);
							break;
						case 4:
							str = L("Your call but, @x1", str);
							break;
						case 5:
							str = L("If you'd like, @x1", str);
							break;
						case 6:
							str = L("With your permission, @x1", str);
							break;
						case 7:
							str = L("If it`s OK with you, @x1", str);
							break;
						case 8:
							str = L("@x1or whatever you want.", CMStrings.endWithAPeriod(str,','));
							break;
						case 9:
							if(targetM != null)
								msg.source().doCommand(new XVector<String>("KNEEL",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							break;
						case 10:
							if(targetM != null)
								msg.source().doCommand(new XVector<String>("GROVEL",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
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
							changeAllSays(msg, L("apathetically say(s)"));
							break;
						case 6:
							changeAllSays(msg, L("shrug(s)"));
							break;
						case 7:
							changeAllSays(msg, L("passively say(s)"));
							break;
						default:
							break;
						}
						break;
					}
					case HAPPY: // happy
					case HAPPYSILLY: // happysilly
					{
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("SMILE",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,7,0))
						{
						case 1:
							changeAllSays(msg, L("laugh(s)"));
							break;
						case 2:
							changeAllSays(msg, L("smile(s)"));
							break;
						case 3:
							changeAllSays(msg, L("beam(s)"));
							break;
						case 4:
							changeAllSays(msg, L("cheerfully say(s)"));
							break;
						case 5:
							changeAllSays(msg, L("happily say(s)"));
							break;
						case 6:
							changeAllSays(msg, L("playfully say(s)"));
							break;
						case 7:
							changeAllSays(msg, L("sweetly say(s)"));
							break;
						}
						break;
					}
					case SAD: // sad
					{
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("CRY",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1:
							changeAllSays(msg, L("sigh(s)"));
							break;
						case 2:
							changeAllSays(msg, L("cr(ys)"));
							break;
						case 3:
							changeAllSays(msg, L("sob(s)"));
							break;
						case 4:
							changeAllSays(msg, L("sadly say(s)"));
							break;
						case 5:
							changeAllSays(msg, L("moap(s)"));
							break;
						case 6:
							changeAllSays(msg, L("sulk(s)"));
							break;
						case 7:
							changeAllSays(msg, L("ache(s)"));
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
							changeAllSays(msg, L("growl(s)"));
							break;
						case 2:
							changeAllSays(msg, L("snarl(s)"));
							break;
						case 3:
							changeAllSays(msg, L("rage(s)"));
							break;
						case 4:
							changeAllSays(msg, L("snap(s)"));
							break;
						case 5:
							changeAllSays(msg, L("roar(s)"));
							break;
						case 6:
							changeAllSays(msg, L("yell(s)"));
							break;
						case 7:
							changeAllSays(msg, L("angrily say(s)"));
							break;
						case 8:
							if(targetM!=null)
								msg.source().doCommand(new XVector<String>("GRUMBLE",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
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
							changeAllSays(msg, L("sneer(s)"));
							break;
						case 2:
							changeAllSays(msg, L("jeer(s)"));
							break;
						case 3:
							changeAllSays(msg, L("sniff(s)"));
							break;
						case 4:
							changeAllSays(msg, L("disdainfully say(s)"));
							break;
						case 5:
							changeAllSays(msg, L("insultingly say(s)"));
							break;
						case 6:
							changeAllSays(msg, L("scoff(s)"));
							break;
						case 7:
							changeAllSays(msg, L("rudely say(s)"));
							break;
						case 8:
							changeAllSays(msg, L("gibe(s)"));
							break;
						case 9:
							changeAllSays(msg, L("mockingly say(s)"));
							break;
						case 10:
							changeAllSays(msg, L("interrupt(s)"));
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
							changeAllSays(msg, L("sneer(s)"));
							break;
						case 2:
							changeAllSays(msg, L("jeer(s)"));
							break;
						case 3:
							changeAllSays(msg, L("sniff(s)"));
							break;
						case 4:
							changeAllSays(msg, L("disdainfully say(s)"));
							break;
						case 5:
							changeAllSays(msg, L("insultingly say(s)"));
							break;
						case 6:
							changeAllSays(msg, L("scoff(s)"));
							break;
						case 7:
							changeAllSays(msg, L("meanly say(s)"));
							break;
						case 8:
							changeAllSays(msg, L("gibe(s)"));
							break;
						case 9:
							changeAllSays(msg, L("mockingly say(s)"));
							break;
						case 10:
							changeAllSays(msg, L("tauntingly say(s)"));
							break;
						default:
							break;
						}
						final int rand=CMLib.dice().roll(1,20,0);
						if(rand<5)
							str=L("Hey @x1, @x2",uglyPhrases[CMLib.dice().roll(1,uglyPhrases.length,-1)],str);
						else
						if(rand<15)
							str=L("@x1..you @x2.",CMStrings.endWithAPeriod(str, '.'),uglyPhrases[CMLib.dice().roll(1,uglyPhrases.length,-1)]);
						else
						{
							if(targetM!=null)
								msg.source().doCommand(new XVector<String>("WHAP",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								msg.source().doCommand(new XVector<String>("WHAP"),MUDCmdProcessor.METAFLAG_FORCED);
						}
						if((targetM!=null)
						&&(CMLib.dice().roll(1,10,0)==1)
						&&(!msg.source().isInCombat())
						&&(!targetM.isInCombat())
						&&(targetM.mayPhysicallyAttack(msg.source()))
						&&(CMath.abs(targetM.phyStats().level()-msg.source().phyStats().level())<10))
							targetM.setVictim(msg.source());
						break;
					}
					case PROUD: // proud
					{
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("FLEX",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, L("boast(s)"));
							break;
						case 2:
							changeAllSays(msg, L("announce(s)"));
							break;
						case 3:
							changeAllSays(msg, L("proudly say(s)"));
							break;
						default:
							break;
						}
						break;
					}
					case GRUMPY: // grumpy
					{
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("GRUMBLE"),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,2,0))
						{
						case 1:
							changeAllSays(msg, L("mutter(s)"));
							break;
						case 2:
							changeAllSays(msg, L("grumble(s)"));
							break;
						default:
							break;
						}
						break;
					}
					case EXCITED: // excited
					{
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("EXCITED",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, L("shout(s)"));
							break;
						case 2:
							changeAllSays(msg, L("blurt(s)"));
							break;
						case 3:
							changeAllSays(msg, L("screech(es)"));
							break;
						case 4:
							changeAllSays(msg, L("excitedly say(s)"));
							break;
						case 5:
							if(targetM!=null)
								msg.source().doCommand(new XVector<String>("FIDGET",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
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
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("COWER",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,6,0))
						{
						case 1:
							changeAllSays(msg, L("meekly say(s)"));
							break;
						case 2:
							changeAllSays(msg, L("stutter(s)"));
							break;
						case 3:
							changeAllSays(msg, L(", shivering, say(s)"));
							break;
						case 4:
							changeAllSays(msg, L("squeek(s)"));
							break;
						case 5:
							changeAllSays(msg, L("barely say(s)"));
							break;
						case 6:
							if(targetM!=null)
								msg.source().doCommand(new XVector<String>("WINCE",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
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
						if((targetM!=null)
						&&(lastOne!=targetM))
						{
							msg.source().doCommand(new XVector<String>("SIGH",targetM.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							lastOne=targetM;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1:
							changeAllSays(msg, L("sigh(s)"));
							break;
						case 2:
							changeAllSays(msg, L("whisper(s)"));
							break;
						case 3:
							changeAllSays(msg, L(", alone, say(s)"));
							break;
						case 4:
							changeAllSays(msg, L("mutter(s)"));
							break;
						case 5:
							changeAllSays(msg, L("whine(s)"));
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
							changeAllSays(msg, L("reflect(s)"));
							break;
						case 3:
							break;
						case 4:
							changeAllSays(msg, L("contemplate(s)"));
							break;
						case 5:
							changeAllSays(msg, L("ponder(s)"));
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
					final int sillySocialIndex=CMLib.dice().roll(1, sillySocials().length, -1);
					final String socialName = sillySocials()[sillySocialIndex];
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
					final int sillySocialIndex=CMLib.dice().roll(1, happySillySocials().length, -1);
					final String socialName = happySillySocials()[sillySocialIndex];
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
							addOn = L(", but that`s not suprising, is it?");
							break;
						case 2:
							addOn = L(". I rock.");
							break;
						case 3:
							addOn = L(". I am **POWERFUL**.");
							break;
						case 4:
							addOn = L(". I am sooo cool.");
							break;
						case 5:
							addOn = L(". You can`t touch me.");
							break;
						case 6:
							addOn = L(".. never had a chance, either.");
							break;
						case 7:
							addOn = L(", with my PINKEE!");
							break;
						default:
							break;
						}
						((MOB)affected).doCommand(new XVector<String>(CHANNELS[channelC],L("*I* just killed @x1",msg.source().Name())+addOn),MUDCmdProcessor.METAFLAG_FORCED);
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
