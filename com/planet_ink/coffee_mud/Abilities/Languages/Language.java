package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Language extends StdAbility
{
	public String ID() { return "Language"; }
	public String name(){ return "Languages";}
	private static final String[] triggerStrings = {"SPEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_LANGTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_LANGPRACCOST);}
	public int classificationCode(){return Ability.LANGUAGE;}

	private static Hashtable emptyHash=new Hashtable();
	private static Vector emptyVector=new Vector();
	protected boolean spoken=false;
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";
	public boolean beingSpoken(){return spoken;}
	public void setBeingSpoken(boolean beingSpoken){spoken=beingSpoken;}
	public Hashtable translationHash(){ return emptyHash; }
	public Vector translationVector(){ return emptyVector; }
	public String displayText()
	{
		if(beingSpoken()) return "(Speaking "+name()+")";
		return "";
	}

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
		if(translationHash().containsKey(word.toUpperCase()))
			return fixCase(word,(String)translationHash().get(word.toUpperCase()));
		MOB M=CMMap.getPlayer(word);
		if(M!=null) return word;
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
			return msg.substring(start+1,end);
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((beingSpoken())
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
		{
			String str=getMsgFromAffect(msg.othersMessage());
			if(str==null) str=getMsgFromAffect(msg.targetMessage());
			if(str!=null)
			{
				String smsg=getMsgFromAffect(msg.sourceMessage());
				int numToMess=(int)Math.round(Util.mul(numChars(str),Util.div(100-profficiency(),100)));
				if(numToMess>0)
					smsg=messChars(smsg,numToMess);
				StringBuffer newStr=new StringBuffer("");
				int start=0;
				int end=0;
				int state=-1;
				while(start<=str.length())
				{
					char c='\0';
					if(end>=str.length())
						c=' ';
					else
						c=str.charAt(end);
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
						{ newStr.append(str.substring(start,end+1)); end++; start=end; state=1; }
						else
						{ newStr.append(translate(str.substring(start,end))+c); end++; start=end; state=-1; }
						break;
					case 1:
						if(Character.isLetterOrDigit(c))
						{ newStr.append(c); end++; start=end;}
						else
						{ newStr.append(c); end++; start=end; state=-1; }
						break;
					}
				}
				str=newStr.toString();
				msg.modify(msg.source(),
							  msg.target(),
							  this,
							  msg.sourceCode(),
							  subStitute(msg.sourceMessage(),smsg),
							  msg.targetCode(),
							  subStitute(msg.targetMessage(),str),
							  msg.othersCode(),
							  subStitute(msg.othersMessage(),str));
				helpProfficiency((MOB)affected);
			}
		}
		return super.okMessage(myHost,msg);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			for(int a=0;a<mob.numAllEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&(A instanceof Language))
				{
					if(mob.isMonster())
						A.setProfficiency(100);
					if(A.ID().equals(ID()))
						((Language)A).setBeingSpoken(true);
					else
						((Language)A).setBeingSpoken(false);
				}
			}
			isAnAutoEffect=false;
			mob.tell("You are now speaking "+name()+".");
		}
		else
			setBeingSpoken(true);
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Language)
		&&(msg.tool().ID().equals(ID())))
		{
			String str=this.getMsgFromAffect(msg.sourceMessage());
			if(str!=null)
			{
				int numToMess=(int)Math.round(Util.mul(numChars(str),Util.div(100-profficiency(),100)));
				if(numToMess>0)
					str=messChars(str,numToMess);
				if(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
					msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),this.subStitute(msg.othersMessage(),str)+" (translated from "+ID()+")"));
				else
				if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
					msg.addTrailerMsg(new FullMsg(msg.source(),(MOB)affected,null,CMMsg.NO_EFFECT,msg.targetCode(),CMMsg.NO_EFFECT,this.subStitute(msg.targetMessage(),str)+" (translated from "+ID()+")"));
				else
				if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
				{
					String otherMes=msg.othersMessage();
					if(msg.target()!=null)
						otherMes=CoffeeFilter.fullOutFilter(((MOB)affected).session(),(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
					msg.addTrailerMsg(new FullMsg(msg.source(),(MOB)affected,null,CMMsg.NO_EFFECT,msg.othersCode(),CMMsg.NO_EFFECT,this.subStitute(otherMes,str)+" (translated from "+ID()+")"));
				}
			}
		}
	}

}
