package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Mood extends StdAbility
{
	public String ID() { return "Mood"; }
	public String name(){ return "Mood";}
	public String displayText(){ return (moodCode<=0)?"":"(In "+CMLib.english().startWithAorAn(MOODS[moodCode][0].toLowerCase())+" mood)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public int classificationCode(){return Ability.ACODE_PROPERTY;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int moodCode=-1;
	protected Object lastOne=null;
	public static final String[] BOAST_CHANNELS={"BOAST","GRATZ","ANNOUNCE","GOSSIP","OOC","CHAT"};
	public static final String[][] MOODS={
		/*0 */{"FORMAL","+ADJCHA 17","^Bformal","formally"},
		/*1 */{"POLITE","+ADJCHA 13","^Bpolite","politely"},
		/*2 */{"HAPPY","","^Yhappy","happily"},
		/*3 */{"SAD","","^Csad","sadly"},
		/*4 */{"ANGRY","","^rangry","angrily"},
		/*5 */{"RUDE","","^grude","rudely"},
		/*6 */{"MEAN","","^rmean","meanly"},
		/*7 */{"PROUD","","^bproud","proudly"},
		/*8 */{"GRUMPY","","^Ggrumpy","grumpily"},
		/*9 */{"EXCITED","","^Wexcited","excitedly"},
		/*10*/{"SCARED","","^yscared","scaredly"},
		/*11*/{"LONELY","","^Clonely","lonely"},
	};

	public final static String[] uglyPhrases={
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
	"gluteous maximus cavity",
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

	public void setMiscText(String newText)
	{
	    // this checks the input, and allows us to get mood
	    // lists without having the code in front of us.
	    if(newText.length()>0)
	    {
	        moodCode=-1;
	        if(CMath.isInteger(newText))
	        {
	            int x=CMath.s_int(newText);
	            if((x>=0)&&(x<MOODS.length))
	            {
                    moodCode=x;
	                newText=MOODS[x][0];
	            }
	        }
	        else
    	    for(int i=0;i<MOODS.length;i++)
    	    	if(MOODS[i][0].equalsIgnoreCase(newText))
    	    		moodCode=i;
	        if(moodCode<0)
	            newText="";
	    }
        super.setMiscText(newText);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
	    if(!super.tick(ticking,tickID))
	        return false;
	    switch(moodCode)
	    {

	    }
	    return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats stats)
	{
		super.affectEnvStats(affected,stats);
		if(moodCode>=0) stats.addAmbiance(MOODS[moodCode][2].toLowerCase());
	}

	private String changeSay(String msg, String to)
	{
		if(msg==null) return null;
		int x=msg.indexOf("'");
		if(x<0) return msg;
		int y=msg.indexOf("say(s)");
		if((y>=0)&&(y<x))
			return msg.substring(0,y)+to+msg.substring(y+6);
		return msg;
	}
	private void changeAllSays(CMMsg msg, String to)
	{
		msg.setSourceMessage(changeSay(msg.sourceMessage(),to));
		msg.setTargetMessage(changeSay(msg.targetMessage(),to));
		msg.setOthersMessage(changeSay(msg.othersMessage(),to));
	}

    public MOB target(MOB mob, Environmental target)
    {
        if(target instanceof MOB) return (MOB)target;
        if(mob==null) return null;
        Room R=mob.location();
        if(R==null) return null;
        if(R.numInhabitants()==1) return null;
        if(R.numInhabitants()==2)
        for(int r=0;r<R.numInhabitants();r++)
            if(R.fetchInhabitant(r)!=mob)
                return R.fetchInhabitant(r);
        if((lastOne instanceof MOB)&&(R.isInhabitant((MOB)lastOne)))
            return (MOB)lastOne;
        Vector players=new Vector();
        Vector mobs=new Vector();
        MOB M=null;
        for(int r=0;r<R.numInhabitants();r++)
        {
            M=R.fetchInhabitant(r);
            if((M!=mob)&&(M!=null))
            {
                if(M.isMonster())
                    mobs.addElement(M);
                else
                if(!M.isMonster())
                    players.addElement(M);
            }
        }
        if(players.size()==1) return (MOB)players.firstElement();
        if(players.size()>1) return null;
        if(mobs.size()==1) return (MOB)mobs.firstElement();
        return null;
    }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.source()==affected)
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
            &&(moodCode>=0))
			{
                String str=CMStrings.getSayFromMessage(msg.othersMessage());
				if(str==null) str=CMStrings.getSayFromMessage(msg.targetMessage());
				if(str!=null)
				{
                    MOB M=target(msg.source(),msg.target());
					if(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
					{
						final String[] tags={"<S-NAME>","You"};
						String tag=null;
						for(int i=0;i<tags.length;i++)
						{
							tag=tags[i];
							if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf(MOODS[moodCode][3])<0))
								msg.setOthersMessage(CMStrings.replaceFirst(msg.othersMessage(),tag,tag+" "+MOODS[moodCode][3]));
							if((msg.targetMessage()!=null)&&(msg.targetMessage().indexOf(MOODS[moodCode][3])<0))
								msg.setTargetMessage(CMStrings.replaceFirst(msg.targetMessage(),tag,tag+" "+MOODS[moodCode][3]));
							if((msg.sourceMessage()!=null)&&(msg.sourceMessage().indexOf(MOODS[moodCode][3])<0))
								msg.setSourceMessage(CMStrings.replaceFirst(msg.sourceMessage(),tag,tag+" "+MOODS[moodCode][3]));
						}
					}
                    String oldStr=str;
					switch(moodCode)
					{
					case 0: // formal
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
						case 1: changeAllSays(msg,"state(s)"); break;
						case 2: changeAllSays(msg,"declare(s)"); break;
						case 3: changeAllSays(msg,"announces(s)"); break;
						case 4: changeAllSays(msg,"elucidate(s)"); break;
						case 5: changeAllSays(msg,"enunciate(s)"); break;
						case 6: changeAllSays(msg,"indicate(s)"); break;
						case 7: changeAllSays(msg,"communicate(s)"); break;
						case 8: changeAllSays(msg,"avow(s)"); break;
						case 9: changeAllSays(msg,"inform(s)"); break;
						case 10: changeAllSays(msg,"propound(s)"); break;
						default:
							break;
						}
						break;
					}
					case 1: // polite
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("HANDSHAKE",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1: str="If you please, "+str; break;
						case 2: str=CMStrings.endWithAPeriod(str)+" Thank you."; break;
						case 3: str=CMStrings.endWithAPeriod(str)+" If you please."; break;
                        case 4: str="Forgive me but, "+str; break;
                        case 5: str="If I may, "+str; break;
                        case 6: str="Please, "+str; break;
                        case 7: str="Humbly speaking, "+str; break;
						default:
							if(msg.source().charStats().getStat(CharStats.STAT_GENDER)=='F')
							{
								if(M!=null)
									msg.source().doCommand(CMParms.makeVector("CURTSEY",M.Name()),Command.METAFLAG_FORCED);
								else
									msg.source().doCommand(CMParms.makeVector("CURTSEY"),Command.METAFLAG_FORCED);
							}
							else
							if(M!=null)
								msg.source().doCommand(CMParms.makeVector("BOW",M.Name()),Command.METAFLAG_FORCED);
							else
								msg.source().doCommand(CMParms.makeVector("BOW"),Command.METAFLAG_FORCED);
							break;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1: changeAllSays(msg,"politely say(s)"); break;
						case 2: changeAllSays(msg,"humbly say(s)"); break;
						case 3: changeAllSays(msg,"meekly say(s)"); break;
                        case 4: changeAllSays(msg,"politely say(s)"); break;
						default:
							break;
						}
						break;
					}
					case 2: // happy
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("SMILE",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,7,0))
						{
						case 1: changeAllSays(msg,"laugh(s)"); break;
						case 2: changeAllSays(msg,"smile(s)"); break;
						case 3: changeAllSays(msg,"beam(s)"); break;
						case 4: changeAllSays(msg,"cheerfully say(s)"); break;
						case 5: changeAllSays(msg,"happily say(s)"); break;
						case 6: changeAllSays(msg,"playfully say(s)"); break;
						case 7: changeAllSays(msg,"sweetly say(s)"); break;
						}
						break;
					}
					case 3: // sad
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("CRY",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1: changeAllSays(msg,"sigh(s)"); break;
						case 2: changeAllSays(msg,"cr(ys)"); break;
						case 3: changeAllSays(msg,"sob(s)"); break;
						case 4: changeAllSays(msg,"sadly say(s)"); break;
						case 5: changeAllSays(msg,"moap(s)"); break;
						case 6: changeAllSays(msg,"sulk(s)"); break;
						case 7: changeAllSays(msg,"ache(s)"); break;
						default:
							break;
						}
						break;
					}
					case 4: // angry
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1: changeAllSays(msg,"growl(s)"); break;
						case 2: changeAllSays(msg,"snarl(s)"); break;
						case 3: changeAllSays(msg,"rage(s)"); break;
						case 4: changeAllSays(msg,"snap(s)"); break;
						case 5: changeAllSays(msg,"roar(s)"); break;
						case 6: changeAllSays(msg,"yell(s)"); break;
						case 7: changeAllSays(msg,"angrily say(s)"); break;
						case 8:
                            if(M!=null)
								msg.source().doCommand(CMParms.makeVector("GRUMBLE",M.Name()),Command.METAFLAG_FORCED);
							else
								msg.source().doCommand(CMParms.makeVector("GRUMBLE"),Command.METAFLAG_FORCED);
							break;
						default:
							break;
						}
						str=str.toUpperCase();
						break;
					}
					case 5: // rude
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1: changeAllSays(msg,"sneer(s)"); break;
						case 2: changeAllSays(msg,"jeer(s)"); break;
						case 3: changeAllSays(msg,"sniff(s)"); break;
						case 4: changeAllSays(msg,"disdainfully say(s)"); break;
						case 5: changeAllSays(msg,"insultingly say(s)"); break;
						case 6: changeAllSays(msg,"scoff(s)"); break;
						case 7: changeAllSays(msg,"rudely say(s)"); break;
						case 8: changeAllSays(msg,"gibe(s)"); break;
						case 9: changeAllSays(msg,"mockingly say(s)"); break;
						case 10: changeAllSays(msg,"interrupt(s)"); break;
						default:
							break;
						}
						break;
					}
					case 6: // mean
					{
						switch(CMLib.dice().roll(1,10,0))
						{
						case 1: changeAllSays(msg,"sneer(s)"); break;
						case 2: changeAllSays(msg,"jeer(s)"); break;
						case 3: changeAllSays(msg,"sniff(s)"); break;
						case 4: changeAllSays(msg,"disdainfully say(s)"); break;
						case 5: changeAllSays(msg,"insultingly say(s)"); break;
						case 6: changeAllSays(msg,"scoff(s)"); break;
						case 7: changeAllSays(msg,"meanly say(s)"); break;
						case 8: changeAllSays(msg,"gibe(s)"); break;
						case 9: changeAllSays(msg,"mockingly say(s)"); break;
						case 10: changeAllSays(msg,"tauntingly say(s)"); break;
						default:
							break;
						}
						int rand=CMLib.dice().roll(1,20,0);
						if(rand<5)
							str="Hey "+uglyPhrases[CMLib.dice().roll(1,uglyPhrases.length,-1)]+", "+str;
						else
						if(rand<15)
							str=CMStrings.endWithAPeriod(str)+"..you "+uglyPhrases[CMLib.dice().roll(1,uglyPhrases.length,-1)]+".";
						else
						{
                            if(M!=null)
								msg.source().doCommand(CMParms.makeVector("WHAP",M.Name()),Command.METAFLAG_FORCED);
							else
								msg.source().doCommand(CMParms.makeVector("WHAP"),Command.METAFLAG_FORCED);
						}
						if((M!=null)
						&&(CMLib.dice().roll(1,10,0)==1)
						&&(!msg.source().isInCombat())
						&&(!((MOB)M).isInCombat())
						&&(((MOB)M).mayPhysicallyAttack(msg.source())))
							((MOB)M).setVictim(msg.source());
						break;
					}
					case 7: // proud
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("FLEX",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1: changeAllSays(msg,"boast(s)"); break;
						case 2: changeAllSays(msg,"announce(s)"); break;
						case 3: changeAllSays(msg,"proudly say(s)"); break;
						default:
							break;
						}
						break;
					}
					case 8: // grumpy
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("GRUMBLE"),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,2,0))
						{
						case 1: changeAllSays(msg,"mutter(s)"); break;
						case 2: changeAllSays(msg,"grumble(s)"); break;
						default:
							break;
						}
						break;
					}
					case 9: // excited
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("EXCITED",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1: changeAllSays(msg,"shout(s)"); break;
						case 2: changeAllSays(msg,"blurt(s)"); break;
						case 3: changeAllSays(msg,"screech(es)"); break;
						case 4: changeAllSays(msg,"excitedly say(s)"); break;
						case 5:
							if(M!=null)
								msg.source().doCommand(CMParms.makeVector("FIDGET",M.Name()),Command.METAFLAG_FORCED);
							else
								msg.source().doCommand(CMParms.makeVector("FIDGET"),Command.METAFLAG_FORCED);
							break;
						default:
							break;
						}
						while(str.endsWith(".")) str=str.substring(0,str.length()-1);
						int num=CMLib.dice().roll(1,10,3);
						for(int i=0;i<num;i++)
							str+="!";
						str=str.toUpperCase();
						break;
					}
					case 10: // scared
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("COWER",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,6,0))
						{
						case 1: changeAllSays(msg,"meekly say(s)"); break;
						case 2: changeAllSays(msg,"stutter(s)"); break;
						case 3: changeAllSays(msg,", shivering, say(s)"); break;
						case 4: changeAllSays(msg,"squeek(s)"); break;
						case 5: changeAllSays(msg,"barely say(s)"); break;
						case 6:
							if(M!=null)
								msg.source().doCommand(CMParms.makeVector("WINCE",M.Name()),Command.METAFLAG_FORCED);
							else
								msg.source().doCommand(CMParms.makeVector("WINCE"),Command.METAFLAG_FORCED);
							break;
						default:
							break;
						}
						break;
					}
					case 11: // lonely
					{
						if((M!=null)
						&&(lastOne!=M))
						{
							msg.source().doCommand(CMParms.makeVector("SIGH",M.Name()),Command.METAFLAG_FORCED);
							lastOne=M;
						}
						switch(CMLib.dice().roll(1,5,0))
						{
						case 1: changeAllSays(msg,"sigh(s)"); break;
						case 2: changeAllSays(msg,"whisper(s)"); break;
						case 3: changeAllSays(msg,", alone, say(s)"); break;
						case 4: changeAllSays(msg,"mutter(s)"); break;
						case 5: changeAllSays(msg,"whine(s)"); break;
						default:
							break;
						}
						break;
					}
					default:
						break;
					}
					if(!oldStr.equals(str))
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
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		switch(moodCode)
		{
		case 7: // proud
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(msg.tool()==affected)
			&&(msg.source()!=null)
			&&(this.lastOne!=msg.source()))
			{
				lastOne=msg.source();
				int channelIndex=-1;
				int channelC=-1;
				String[] CHANNELS=CMLib.channels().getChannelNames();
				for(int c=0;c<CHANNELS.length;c++)
					if(CMStrings.contains(BOAST_CHANNELS,CHANNELS[c]))
					{
						channelIndex=CMLib.channels().getChannelIndex(CHANNELS[c]);
						channelC=c;
						if(channelIndex>=0) break;
					}
				if(channelIndex>=0)
				{
					String addOn=".";
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1: addOn=", but that`s not suprising, is it?"; break;
					case 2: addOn=". I rock."; break;
					case 3: addOn=". I am **POWERFUL**."; break;
					case 4: addOn=". I am sooo cool."; break;
					case 5: addOn=". You can`t touch me."; break;
					case 6: addOn=".. never had a chance, either."; break;
					case 7: addOn=", with my PINKEE!"; break;
					default:
						break;
					}
					((MOB)affected).doCommand(CMParms.makeVector(CHANNELS[channelC],"*I* just killed "+msg.source().Name()+addOn),Command.METAFLAG_FORCED);
				}
			}
			break;
		}
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String entered=CMParms.combine(commands,0);
        String origEntered=CMParms.combine(commands,0);
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		Ability MOOD=target.fetchEffect(ID());
		boolean add=false;
		if(MOOD==null)
		{
			add=true;
			MOOD=(Ability)copyOf();
			MOOD.setMiscText("NORMAL");
		}
		String moodCode = MOOD.text();
		if(moodCode.trim().length()==0) moodCode="NORMAL";
		String moodName = CMLib.english().startWithAorAn(moodCode.toLowerCase());
		if(entered.trim().length()==0)
		{
			mob.tell("You are currently in "+moodName+" mood.");
			return false;
		}
        if(entered.equalsIgnoreCase("RANDOM"))
        {
            int rand=CMLib.dice().roll(1,MOODS.length+3,-1);
            if(rand>=MOODS.length)
                entered="NORMAL";
            else
                entered=MOODS[rand][0];
        }
		String choice=null;
		String mask="";
		if(entered.equalsIgnoreCase("NORMAL"))
			choice="NORMAL";
		else
		for(int i=0;i<MOODS.length;i++)
			if(MOODS[i][0].equalsIgnoreCase(entered))
			{
				choice=MOODS[i][0];
				mask=MOODS[i][1];
			}
		if((choice==null)&&(entered.length()>0)&&(Character.isLetter(entered.charAt(0))))
		{
			if("NORMAL".startsWith(entered.toUpperCase()))
				choice="NORMAL";
			else
			for(int i=0;i<MOODS.length;i++)
			if(MOODS[i][0].startsWith(entered.toUpperCase()))
			{
				choice=MOODS[i][0];
				mask=MOODS[i][1];
			}
		}
		if((choice==null)||(entered.equalsIgnoreCase("list")))
		{
			String choices=", NORMAL";
			for(int i=0;i<MOODS.length;i++)
				choices+=", "+MOODS[i][0];
            if(entered.equalsIgnoreCase("LIST"))
                mob.tell("Mood choices include: "+choices.substring(2));
            else
    			mob.tell("'"+entered+"' is not a known mood. Choices include: "+choices.substring(2));
			return false;
		}
        if(moodCode.equalsIgnoreCase(choice))
        {
            if(origEntered.equalsIgnoreCase("RANDOM"))
                return false;
            mob.tell("You are already in "+CMLib.english().startWithAorAn(choice.toLowerCase())+" mood.");
            return false;
        }

		if((mask.length()>0)&&(!CMLib.masking().maskCheck(mask,mob,true)))
		{
            if(origEntered.equalsIgnoreCase("RANDOM"))
                return false;
			mob.tell("You must meet the following criteria to be in that mood: "+CMLib.masking().maskDesc(mask,true));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"<T-NAME> appear(s) to be in "+CMLib.english().startWithAorAn(choice.toLowerCase())+" mood.");
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
				    target.location().send(target,msg);
					if(choice.equalsIgnoreCase("NORMAL"))
						target.delEffect(MOOD);
					else
					{
						if(add) target.addNonUninvokableEffect(MOOD);
					    MOOD.setMiscText(choice);
					}
                    target.recoverEnvStats();
                    target.location().recoverRoomStats();
				}
			}
		}
        return success;
	}
}
