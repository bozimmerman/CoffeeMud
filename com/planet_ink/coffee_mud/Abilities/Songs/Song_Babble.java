package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Babble extends Song
{
	public String ID() { return "Song_Babble"; }
	public String name(){ return "Babble";}
	public String displayText(){ return "(Song of Babble)";}
	public int quality(){ return MALICIOUS;}
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";
	public Environmental newInstance(){	return new Song_Babble();}
	protected boolean skipStandardSongInvoke(){return true;}
	protected boolean mindAttack(){return true;}
	
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
	
	public boolean okAffect(Affect affect)
	{
		if((affected instanceof MOB)
		&&(affect.amISource((MOB)affected))
		&&(!affect.amISource(invoker))
		&&(affect.sourceMessage()!=null)
		&&(affect.tool()==null)
		&&((affect.sourceMinor()==Affect.TYP_SPEAK)
		   ||(Util.bset(affect.sourceCode(),Affect.MASK_CHANNEL))))
		{
			String msg=affect.othersMessage();
			if(msg==null) msg=affect.targetMessage();
			if(msg!=null) msg=getMsgFromAffect(msg);
			if(msg!=null)
			{
				String smsg=getMsgFromAffect(affect.sourceMessage());
				int numToMess=numChars(msg);
				if(numToMess>0)
					smsg=messChars(smsg,numChars(msg));
				affect.modify(affect.source(),
							  affect.target(),
							  null,
							  affect.sourceCode(),
							  subStitute(affect.sourceMessage(),smsg),
							  affect.targetCode(),
							  subStitute(affect.targetMessage(),smsg),
							  affect.othersCode(),
							  subStitute(affect.othersMessage(),smsg));
				helpProfficiency((MOB)affected);
			}
		}
		return super.okAffect(affect);
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
		unsing(mob);
		if(success)
		{
			String str=auto?"The song of "+name()+" begins to play!":"<S-NAME> begin(s) to sing the Song of "+name()+".";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="<S-NAME> start(s) the Song of "+name()+" over again.";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				Hashtable h=ExternalPlay.properTargets(this,mob,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					int affectType=Affect.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|Affect.ACT_GENERAL;
					
					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mob.location().okAffect(msg2))&&(mob.location().okAffect(msg3)))
						{
							follower.location().send(follower,msg2);
							if(!msg2.wasModified())
							{
								follower.location().send(follower,msg3);
								if((!msg3.wasModified())&&(follower.fetchAffect(newOne.ID())==null))
								{
									if(follower!=mob)
										follower.addAffect((Ability)newOne.copyOf());
									else
										follower.addAffect(newOne);
								}
							}
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}