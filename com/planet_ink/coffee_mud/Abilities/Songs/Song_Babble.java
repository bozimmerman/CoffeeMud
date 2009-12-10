package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Song_Babble extends Song
{
	public String ID() { return "Song_Babble"; }
	public String name(){ return "Babble";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
    protected final static String consonants="bcdfghjklmnpqrstvwxz";
    protected final static String vowels="aeiouy";
	protected boolean skipStandardSongInvoke(){return true;}
    protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}
	protected boolean maliciousButNotAggressiveFlag(){return true;}

	protected int numChars(String words)
	{
		int num=0;
		for(int i=0;i<words.length();i++)
		{
			if(Character.isLetter(words.charAt(i)))
				num++;
		}
		return num;
	}

	protected char fixCase(char like,char make)
	{
		if(Character.isUpperCase(like))
			return Character.toUpperCase(make);
		return Character.toLowerCase(make);
	}
	protected String messChars(String words, int numToMess)
	{
		numToMess=numToMess/2;
		if(numToMess==0) return words;
		StringBuffer w=new StringBuffer(words);
		while(numToMess>0)
		{
			int x=CMLib.dice().roll(1,words.length(),-1);
			char c=words.charAt(x);
			if(Character.isLetter(c))
			{
				if(vowels.indexOf(c)>=0)
					w.setCharAt(x,fixCase(c,vowels.charAt(CMLib.dice().roll(1,vowels.length(),-1))));
				else
					w.setCharAt(x,fixCase(c,consonants.charAt(CMLib.dice().roll(1,consonants.length(),-1))));
				numToMess--;
			}
		}
		return w.toString();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(!msg.amISource(invoker))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
		{
			String str=msg.othersMessage();
			if(str==null) str=msg.targetMessage();
			if(str!=null) str=CMStrings.getSayFromMessage(str);
			if(str!=null)
			{
				String smsg=CMStrings.getSayFromMessage(msg.sourceMessage());
				int numToMess=numChars(str);
				if(numToMess>0)
					smsg=messChars(smsg,numChars(str));
				msg.modify(msg.source(),
							  msg.target(),
							  null,
							  msg.sourceCode(),
                              CMStrings.substituteSayInMessage(msg.sourceMessage(),smsg),
							  msg.targetCode(),
                              CMStrings.substituteSayInMessage(msg.targetMessage(),smsg),
							  msg.othersCode(),
                              CMStrings.substituteSayInMessage(msg.othersMessage(),smsg));
				helpProficiency((MOB)affected);
			}
		}
		return super.okMessage(myHost,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat()&&(mob.isMonster()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		unsing(mob,mob,true);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"The "+songOf()+" begins to play!":"<S-NAME> begin(s) to sing the "+songOf()+".";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="<S-NAME> start(s) the "+songOf()+" over again.";

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					Song newOne=(Song)this.copyOf();
					HashSet h=sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null) continue;
	
					for(Iterator f=h.iterator();f.hasNext();)
					{
						MOB follower=(MOB)f.next();
	
						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
						if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
	
						if((CMLib.flags().canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							CMMsg msg3=msg2;
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								follower.location().send(follower,msg2);
								if(msg2.value()<=0)
								{
									follower.location().send(follower,msg3);
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
									{
										if(follower!=mob)
											follower.addEffect((Ability)newOne.copyOf());
										else
											follower.addEffect(newOne);
									}
								}
							}
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
