package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Babble extends Song
{
	public String ID() { return "Song_Babble"; }
	public String name(){ return "Babble";}
	public int quality(){ return MALICIOUS;}
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";
	public Environmental newInstance(){	return new Song_Babble();}
	protected boolean skipStandardSongInvoke(){return true;}

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
		else
			return Character.toLowerCase(make);
	}
	protected String messChars(String words, int numToMess)
	{
		numToMess=numToMess/2;
		if(numToMess==0) return words;
		StringBuffer w=new StringBuffer(words);
		while(numToMess>0)
		{
			int x=Dice.roll(1,words.length(),-1);
			char c=words.charAt(x);
			if(Character.isLetter(c))
			{
				if(vowels.indexOf(c)>=0)
					w.setCharAt(x,fixCase(c,vowels.charAt(Dice.roll(1,vowels.length(),-1))));
				else
					w.setCharAt(x,fixCase(c,consonants.charAt(Dice.roll(1,consonants.length(),-1))));
				numToMess--;
			}
		}
		return w.toString();
	}

	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end).trim();
		return null;
	}
	protected String subStitute(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
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
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
		{
			String str=msg.othersMessage();
			if(str==null) str=msg.targetMessage();
			if(str!=null) str=getMsgFromAffect(str);
			if(str!=null)
			{
				String smsg=getMsgFromAffect(msg.sourceMessage());
				int numToMess=numChars(str);
				if(numToMess>0)
					smsg=messChars(smsg,numChars(str));
				msg.modify(msg.source(),
							  msg.target(),
							  null,
							  msg.sourceCode(),
							  subStitute(msg.sourceMessage(),smsg),
							  msg.targetCode(),
							  subStitute(msg.targetMessage(),smsg),
							  msg.othersCode(),
							  subStitute(msg.othersMessage(),smsg));
				helpProfficiency((MOB)affected);
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(!Sense.canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);
		unsing(mob,mob,null);
		if(success)
		{
			String str=auto?"The "+songOf()+" begins to play!":"<S-NAME> begin(s) to sing the "+songOf()+".";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="<S-NAME> start(s) the "+songOf()+" over again.";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				Hashtable h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mob.location().okMessage(mob,msg2))&&(mob.location().okMessage(mob,msg3)))
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
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}