package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Language extends StdAbility
{
	protected boolean spoken=false;
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";

	public Language()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Languages";
		displayText="";
		miscText="";
		triggerStrings.addElement("SPEAK");
		canBeUninvoked=false;
		isAutoinvoked=true;
	}

	public int classificationCode()
	{
		return Ability.LANGUAGE;
	}

	public Environmental newInstance()
	{
		return new Language();
	}

	public boolean beingSpoken(){return spoken;}
	public void setBeingSpoken(boolean beingSpoken){spoken=beingSpoken;}
	public Hashtable translationHash(){ return new Hashtable(); }
	public Vector translationVector(){ return new Vector(); }
	
	
	protected String fixCase(String like,String make)
	{
		StringBuffer s=new StringBuffer(make);
		char lastLike=' ';
		for(int x=0;x<make.length();x++)
		{
			if(x<like.length()) lastLike=like.charAt(x);
			s.setCharAt(x,fixCase(lastLike,make.charAt(x)));
		}
		return s.toString();
	}
	protected char fixCase(char like,char make)
	{
		if(Character.isUpperCase(like))
			return Character.toUpperCase(make);
		else
			return Character.toLowerCase(make);
	}
	protected String translate(String word) 
	{
		if(translationHash().contains(word.toUpperCase()))
			return fixCase(word,(String)translationHash().get(word.toUpperCase()));
		if(translationVector().size()>0)
		{
			String[] choices=null;
			try{ choices=(String[])translationVector().elementAt(word.length()-1);}catch(Exception e){}
			if(choices==null) choices=(String[])translationVector().lastElement();
			return choices[Dice.roll(1,choices.length,-1)];
		}
		return word; 
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
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}
	
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
	
	protected String messChars(String words, int numToMess)
	{
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
	
	public boolean okAffect(Affect affect)
	{
		if((beingSpoken())
		&&(affected instanceof MOB)
		&&(affect.amISource((MOB)affected))
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
				int numToMess=(int)Math.round(Util.mul(numChars(smsg),100-profficiency()));
				if(numToMess>0)
					smsg=messChars(smsg,numToMess);
				StringBuffer newStr=new StringBuffer("");
				while(msg.length()>0)
				{
					int x=msg.indexOf(" ");
					if(x<0)
					{
						newStr.append(msg);
						break;
					}
					String word=msg.substring(0,x);
					boolean nonChar=false;
					for(int i=0;i<word.length();i++)
						if((word.charAt(i)!=' ')&&(!Character.isLetter(word.charAt(i))))
						{
							nonChar=true;
							break;
						}
					if(nonChar)
						newStr.append(word+" ");
					else
						newStr.append(translate(word)+" ");
					msg=msg.substring(x+1);
				}
				affect.modify(affect.source(),
							  affect.target(),
							  this,
							  affect.sourceCode(),
							  smsg,
							  affect.targetCode(),
							  subStitute(affect.targetMessage(),newStr.toString()),
							  affect.othersCode(),
							  subStitute(affect.othersMessage(),newStr.toString()));
				helpProfficiency((MOB)affected);
			}
		}
		return super.okAffect(affect);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Ability A=mob.fetchAffect(ID());
		if(A instanceof Language)
		{
			if(A==this)
				((Language)A).setBeingSpoken(true);
			else
				((Language)A).setBeingSpoken(false);
		}
		isAnAutoEffect=false;
		mob.tell("You are now speaking "+name()+".");
		return true;
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected instanceof MOB)
		&&(!affect.amISource((MOB)affected))
		&&((affect.sourceMinor()==Affect.TYP_SPEAK)
		   ||(Util.bset(affect.sourceCode(),Affect.MASK_CHANNEL)))
		&&(affect.tool() !=null)
		&&(affect.sourceMessage()!=null)
		&&(affect.tool() instanceof Language)
		&&(affect.tool().ID().equals(ID())))
		{
			String msg=this.getMsgFromAffect(affect.sourceMessage());
			if(msg!=null)
			if(affect.amITarget(null)&&(affect.targetMessage()!=null))
				affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affected,null,Affect.NO_EFFECT,affect.targetCode(),Affect.NO_EFFECT,this.subStitute(affect.targetMessage(),msg)));
			else
			if(!affect.amITarget(null)&&(affect.othersMessage()!=null))
				affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affected,null,Affect.NO_EFFECT,affect.othersCode(),Affect.NO_EFFECT,this.subStitute(affect.othersMessage(),msg)));
		}
	}
	
}