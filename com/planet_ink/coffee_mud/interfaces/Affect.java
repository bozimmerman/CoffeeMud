package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

public interface Affect extends Cloneable
{
	public int targetMajor();
	public int targetMinor();
	public int targetCode();
	public String targetMessage();

	public int sourceMajor();
	public int sourceMinor();
	public int sourceCode();
	public String sourceMessage();

	public int othersMajor();
	public int othersMinor();
	public int othersCode();
	public String othersMessage();

	public Environmental target();
	public Environmental tool();
	public MOB source();
	
	public Affect copyOf();
			

	public boolean amITarget(Environmental thisOne);
	public boolean amISource(MOB thisOne);

	public boolean wasModified();
	public void tagModified(boolean newStatus);

	public void modify(MOB source,
						Environmental target,
						Environmental tool,
						int newSourceCode,
						String sourceMessage,
						int newTargetCode,
						String targetMessage,
						int newOthersCode,
						String othersMessage);

	public Vector trailerMsgs();
	public void addTrailerMsg(Affect msg);
	
	// helpful seperator masks
	public static final int MINOR_MASK=2047;
	public static final int MAJOR_MASK=4192256;
	
	// masks for all messages
	public static final int MASK_HANDS=2048;       // small hand movements
	public static final int MASK_MOVE=4096;        // large body movements (travel)
	public static final int MASK_EYES=8192;        // looking and seeing
	public static final int MASK_MOUTH=16384;      // speaking and eating
	public static final int MASK_SOUND=32768;      // general body noises 
	public static final int MASK_GENERAL=65536;    // anything!
	public static final int MASK_MAGIC=131072;     // the magic mask!
	public static final int MASK_DELICATE=262144;  // for thief skills!
	public static final int MASK_MALICIOUS=524288; // for attacking
	public static final int MASK_CHANNEL=1048576;  // for channel messages
	public static final int MASK_OPTIMIZE=2097152; // to optomize a repeated msg
	public static final int MASK_HURT=MASK_GENERAL|1024;
	
	// minor messages
	public static final int TYP_AREAAFFECT=1;
	public static final int TYP_PUSH=2;
	public static final int TYP_PULL=3;
	public static final int TYP_RECALL=4;
	public static final int TYP_OPEN=5;
	public static final int TYP_CLOSE=6;
	public static final int TYP_PUT=7;
	public static final int TYP_GET=8;
	public static final int TYP_UNLOCK=9;
	public static final int TYP_LOCK=10;
	public static final int TYP_WIELD=11;
	public static final int TYP_GIVE=12;
	public static final int TYP_BUY=13;
	public static final int TYP_SELL=14;
	public static final int TYP_DROP=15;
	public static final int TYP_WEAR=16;
	public static final int TYP_FILL=17;
	public static final int TYP_DELICATE_HANDS_ACT=18;
	public static final int TYP_VALUE=19;
	public static final int TYP_HOLD=20;
	public static final int TYP_NOISYMOVEMENT=21;
	public static final int TYP_QUIETMOVEMENT=22;
	public static final int TYP_WEAPONATTACK=23;
	public static final int TYP_EXAMINESOMETHING=24;
	public static final int TYP_READSOMETHING=25;
	public static final int TYP_NOISE=26;
	public static final int TYP_SPEAK=27;
	public static final int TYP_CAST_SPELL=28;
	public static final int TYP_LIST=29;
	public static final int TYP_EAT=30;
	public static final int TYP_ENTER=31;
	public static final int TYP_FOLLOW=32;
	public static final int TYP_LEAVE=33;
	public static final int TYP_SLEEP=34;
	public static final int TYP_SIT=35;
	public static final int TYP_STAND=36;
	public static final int TYP_FLEE=37;
	public static final int TYP_NOFOLLOW=38;
	public static final int TYP_WRITE=39;
	public static final int TYP_FIRE=40;
	public static final int TYP_COLD=41;
	public static final int TYP_WATER=42;
	public static final int TYP_GAS=43;
	public static final int TYP_MIND=44;
	public static final int TYP_GENERAL=45;
	public static final int TYP_JUSTICE=46;
	public static final int TYP_ACID=47;
	public static final int TYP_ELECTRIC=48;
	public static final int TYP_POISON=49;
	public static final int TYP_UNDEAD=50;
	public static final int TYP_MOUNT=51;
	public static final int TYP_DISMOUNT=52;
	public static final int TYP_OK_ACTION=53;
	public static final int TYP_OK_VISUAL=54;
	public static final int TYP_DRINK=55;
	public static final int TYP_HANDS=56;
	public static final int TYP_PARALYZE=57;
	public static final int TYP_WAND_USE=58;
	public static final int TYP_SERVE=59;
	public static final int TYP_REBUKE=60;
	public static final int TYP_ADVANCE=61;
	public static final int TYP_DISEASE=62;
	public static final int TYP_DEATH=63;
	public static final int TYP_DEPOSIT=64;
	public static final int TYP_WITHDRAW=65;
	public static final int TYP_EMOTE=66;
	public static final int TYP_QUIT=67;
	public static final int TYP_SHUTDOWN=68;
	public static final int TYP_VIEW=69;
	public static final int TYP_RETIRE=70;
	public static final int TYP_RETREAT=71;
	public static final int TYP_PANIC=72;
	public static final int TYP_THROW=73;
	public static final int TYP_EXTINGUISH=74;
	public static final int TYP_TELL=75;
	public static final int TYP_SITMOVE=76;
	public static final int TYP_KNOCK=77;
	public static final int TYP_PRACTICE=78;
	public static final int TYP_TEACH=79;
	public static final int TYP_REMOVE=80;
	
	public static final int TYP_CHANNEL=100; //(100-131 are channels)

	// helpful message groupings
	public static final int MSK_CAST_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC;
	public static final int MSK_CAST_MALICIOUS_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC|MASK_MALICIOUS;
	public static final int MSK_CAST_SOMANTIC=MASK_HANDS|MASK_MAGIC;
	public static final int MSK_CAST_MALICIOUS_SOMANTIC=MASK_HANDS|MASK_MAGIC|MASK_MALICIOUS;
	public static final int MSK_HAGGLE=MASK_HANDS|MASK_SOUND|MASK_MOUTH;
	public static final int MSK_CAST=MSK_CAST_VERBAL|MSK_CAST_SOMANTIC;
	public static final int MSK_CAST_MALICIOUS=MSK_CAST_MALICIOUS_VERBAL|MSK_CAST_MALICIOUS_SOMANTIC;
	public static final int MSK_MALICIOUS_MOVE=MASK_MALICIOUS|MASK_MOVE|MASK_SOUND;
	
	// all major messages
	public static final int NO_EFFECT=0;
	public static final int MSG_AREAAFFECT=MASK_GENERAL|TYP_AREAAFFECT;
	public static final int MSG_PUSH=MASK_HANDS|TYP_AREAAFFECT;
	public static final int MSG_PULL=MASK_HANDS|TYP_PULL;
	public static final int MSG_RECALL=MASK_MOUTH|MASK_SOUND|TYP_RECALL;
	public static final int MSG_OPEN=MASK_HANDS|TYP_OPEN;
	public static final int MSG_CLOSE=MASK_HANDS|TYP_CLOSE;
	public static final int MSG_PUT=MASK_HANDS|TYP_PUT;
	public static final int MSG_GET=MASK_HANDS|TYP_GET;
	public static final int MSG_UNLOCK=MASK_HANDS|TYP_UNLOCK;
	public static final int MSG_LOCK=MASK_HANDS|TYP_LOCK;
	public static final int MSG_WIELD=MASK_HANDS|TYP_WIELD;
	public static final int MSG_GIVE=MASK_HANDS|TYP_GIVE;
	public static final int MSG_BUY=MSK_HAGGLE|TYP_BUY;
	public static final int MSG_SELL=MSK_HAGGLE|TYP_SELL;
	public static final int MSG_DROP=MASK_HANDS|TYP_DROP;
	public static final int MSG_WEAR=MASK_HANDS|TYP_WEAR;
	public static final int MSG_FILL=MASK_HANDS|MASK_MOVE|MASK_SOUND|TYP_FILL;
	public static final int MSG_DELICATE_HANDS_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
	public static final int MSG_THIEF_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_JUSTICE;
	public static final int MSG_VALUE=MSK_HAGGLE|TYP_VALUE;
	public static final int MSG_HOLD=MASK_HANDS|TYP_HOLD;
	public static final int MSG_NOISYMOVEMENT=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_NOISYMOVEMENT;
	public static final int MSG_QUIETMOVEMENT=MASK_HANDS|MASK_MOVE|TYP_QUIETMOVEMENT;
	public static final int MSG_WEAPONATTACK=MASK_HANDS|MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_WEAPONATTACK;
	public static final int MSG_EXAMINESOMETHING=MASK_EYES|TYP_EXAMINESOMETHING;
	public static final int MSG_READSOMETHING=MASK_EYES|TYP_READSOMETHING;
	public static final int MSG_NOISE=MASK_SOUND|TYP_NOISE;
	public static final int MSG_SPEAK=MASK_SOUND|MASK_MOUTH|TYP_SPEAK;
	public static final int MSG_CAST_VERBAL_SPELL=MSK_CAST_VERBAL|TYP_CAST_SPELL;
	public static final int MSG_LIST=MASK_SOUND|MASK_MOUTH|TYP_LIST;
	public static final int MSG_EAT=MASK_HANDS|MASK_MOUTH|TYP_EAT;
	public static final int MSG_ENTER=MASK_MOVE|MASK_SOUND|TYP_ENTER;
	public static final int MSG_CAST_ATTACK_VERBAL_SPELL=MSK_CAST_MALICIOUS_VERBAL|TYP_CAST_SPELL;
	public static final int MSG_LEAVE=MASK_MOVE|MASK_SOUND|TYP_LEAVE;
	public static final int MSG_SLEEP=MASK_MOVE|TYP_SLEEP;
	public static final int MSG_SIT=MASK_MOVE|TYP_SIT;
	public static final int MSG_STAND=MASK_MOVE|TYP_STAND;
	public static final int MSG_FLEE=MASK_MOVE|MASK_SOUND|TYP_FLEE;
	public static final int MSG_CAST_SOMANTIC_SPELL=MSK_CAST_SOMANTIC|TYP_CAST_SPELL;
	public static final int MSG_CAST_ATTACK_SOMANTIC_SPELL=MSK_CAST_MALICIOUS_SOMANTIC|TYP_CAST_SPELL;
	public static final int MSG_CAST=MSK_CAST|TYP_CAST_SPELL;
	public static final int MSG_CAST_MALICIOUS=MSK_CAST_MALICIOUS|TYP_CAST_SPELL;
	public static final int MSG_OK_ACTION=MASK_SOUND|MASK_GENERAL|TYP_OK_ACTION;
	public static final int MSG_OK_VISUAL=MASK_GENERAL|TYP_OK_VISUAL;
	public static final int MSG_DRINK=MASK_HANDS|MASK_MOUTH|TYP_DRINK;
	public static final int MSG_HANDS=MASK_HANDS|TYP_HANDS;
	public static final int MSG_EMOTE=MASK_SOUND|MASK_HANDS|TYP_EMOTE;
	public static final int MSG_FOLLOW=MASK_GENERAL|TYP_FOLLOW;
	public static final int MSG_NOFOLLOW=MASK_GENERAL|TYP_NOFOLLOW;
	public static final int MSG_WRITE=MASK_HANDS|TYP_WRITE;
	public static final int MSG_MOUNT=MASK_MOVE|MASK_SOUND|TYP_MOUNT;
	public static final int MSG_DISMOUNT=MASK_MOVE|MASK_SOUND|TYP_DISMOUNT;
	public static final int MSG_SERVE=MASK_MOUTH|MASK_SOUND|TYP_SERVE;
	public static final int MSG_REBUKE=MASK_MOUTH|MASK_SOUND|TYP_REBUKE;
	public static final int MSG_ADVANCE=MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_ADVANCE;
	public static final int MSG_DEATH=MASK_SOUND|MASK_GENERAL|TYP_DEATH;
	public static final int MSG_WITHDRAW=MASK_HANDS|TYP_WITHDRAW;
	public static final int MSG_DEPOSIT=MASK_HANDS|TYP_DEPOSIT;
	public static final int MSG_QUIT=MASK_GENERAL|TYP_QUIT;
	public static final int MSG_SHUTDOWN=MASK_GENERAL|TYP_SHUTDOWN;
	public static final int MSG_VIEW=MASK_SOUND|MASK_MOUTH|TYP_VIEW;
	public static final int MSG_RETIRE=MASK_GENERAL|TYP_RETIRE;
	public static final int MSG_RETREAT=MASK_MOVE|MASK_SOUND|TYP_RETREAT;
	public static final int MSG_PANIC=MASK_MOVE|MASK_SOUND|TYP_PANIC;
	public static final int MSG_THROW=MASK_HANDS|MASK_SOUND|TYP_THROW;
	public static final int MSG_EXTINGUISH=MASK_HANDS|TYP_EXTINGUISH;
	public static final int MSG_TELL=MASK_GENERAL|TYP_TELL;
	public static final int MSG_SITMOVE=MASK_MOVE|TYP_SITMOVE;
	public static final int MSG_KNOCK=MASK_HANDS|MASK_SOUND|TYP_KNOCK;
	public static final int MSG_PRACTICE=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_PRACTICE;
	public static final int MSG_TEACH=MASK_HANDS|MASK_SOUND|MASK_MOUTH|MASK_MOVE|TYP_TEACH;
	public static final int MSG_REMOVE=MASK_HANDS|TYP_REMOVE;
}

