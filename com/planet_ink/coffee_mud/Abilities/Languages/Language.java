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
		trainsRequired=0;
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
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
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
				int start=0;
				int end=0;
				int state=-1;
				while(start<=msg.length())
				{
					char c='\0';
					if(end>=msg.length())
						c=' ';
					else
						c=msg.charAt(end);
					switch(state)
					{
					case -1:
						if(Character.isLetter(c))
						{ state=0; end++;}
						else
						{ newStr.append(c); end++;start=end;}
						break;
					case 0:
						if(Character.isLetter(c))
						{ end++;}
						else
						if(Character.isDigit(c))
						{ newStr.append(msg.substring(start,end+1)); end++; start=end; state=1; }
						else
						{ newStr.append(translate(msg.substring(start,end))+c); end++; start=end; state=-1; }
						break;
					case 1:
						if(Character.isLetterOrDigit(c))
						{ newStr.append(c); end++; start=end;}
						else
						{ newStr.append(c); end++; start=end; state=-1; }
						break;
					}
				}
				msg=newStr.toString().trim();
				affect.modify(affect.source(),
							  affect.target(),
							  this,
							  affect.sourceCode(),
							  subStitute(affect.sourceMessage(),smsg),
							  affect.targetCode(),
							  subStitute(affect.targetMessage(),msg),
							  affect.othersCode(),
							  subStitute(affect.othersMessage(),msg));
				helpProfficiency((MOB)affected);
			}
		}
		return super.okAffect(affect);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Language))
			{
				if(A.ID().equals(ID()))
					((Language)A).setBeingSpoken(true);
				else
					((Language)A).setBeingSpoken(false);
			}
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
			{
				int numToMess=(int)Math.round(Util.mul(numChars(msg),100-profficiency()));
				if(numToMess>0)
					msg=messChars(msg,numToMess);
				if(affect.amITarget(null)&&(affect.targetMessage()!=null))
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affected,null,Affect.NO_EFFECT,affect.targetCode(),Affect.NO_EFFECT,this.subStitute(affect.targetMessage(),msg)+"(translated from "+ID()+")"));
				else
				if(!affect.amITarget(null)&&(affect.othersMessage()!=null))
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affected,null,Affect.NO_EFFECT,affect.othersCode(),Affect.NO_EFFECT,this.subStitute(affect.othersMessage(),msg)+"(translated from "+ID()+")"));
			}
		}
	}
	
}