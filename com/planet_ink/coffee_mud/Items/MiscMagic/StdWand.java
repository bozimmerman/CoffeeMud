package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdWand extends StdItem implements Wand
{
	public String ID(){	return "StdWand";}
	public static final String[] words={"ZAP","ZAP","ZAP","ZOT","ZIT","ZEK","ZOM","ZUP","ZET","ZYT","ZVP","ZOP"};
	protected String secretWord=words[Dice.roll(1,words.length,0)-1];

	public StdWand()
	{
		super();

		setName("a crooked stick");
		baseEnvStats.setWeight(1);
		setDisplayText("a small crooked stick is here.");
		setDescription("Looks like an broken piece of a tree.");
		secretIdentity="";
		baseGoldValue=200;
		material=EnvResource.RESOURCE_OAK;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public int maxUses(){return Integer.MAX_VALUE;}
	public void setMaxUses(int newMaxUses){}
	
	public static boolean useTheWand(Ability A, MOB mob)
	{
		int manaRequired=5;
		int q=CMAble.qualifyingLevel(mob,A);
		if(q>0)
		{
			if(q<CMAble.qualifyingClassLevel(mob,A))
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
			manaRequired=25;
		if(manaRequired>mob.curState().getMana())
		{
			mob.tell("You don't have enough mana.");
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}
	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		else
			return super.value();
	}
	public static String getWandWord(String from)
	{
		int hash=from.hashCode();
		if(hash<0) hash=hash*-1;
		return words[hash%words.length];
	}

	public void setSpell(Ability theSpell)
	{
		miscText="";
		if(theSpell!=null)
			miscText=theSpell.ID();
		secretWord=StdWand.getWandWord(miscText);
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord=StdWand.getWandWord(newText);
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(text());
	}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
			id="'A wand of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message)
	{
		if((mob.isMine(this))
		   &&(!this.amWearingAt(Item.INVENTORY)))
		{
			Environmental target=null;
			if((mob.location()!=null))
				target=afftarget;
			int x=message.toUpperCase().indexOf(this.magicWord().toUpperCase());
			if(x>=0)
			{
				message=message.substring(x+this.magicWord().length());
				int y=message.indexOf("'");
				if(y>=0) message=message.substring(0,y);
				message=message.trim();
				Ability wandUse=mob.fetchAbility("Skill_WandUse");
				if((wandUse==null)||(!wandUse.profficiencyCheck(null,0,false)))
					mob.tell(this.name()+" glows faintly for a moment, then fades.");
				else
				{
					Ability A=this.getSpell();
					if(A==null)
						mob.tell("Something seems wrong with "+this.name()+".");
					else
					if(this.usesRemaining()<=0)
						mob.tell(this.name()+" seems spent.");
					else
					{
						A=(Ability)A.newInstance();
						if(useTheWand(A,mob))
						{
							Vector V=new Vector();
							if(target!=null)
								V.addElement(target.name());
							V.addElement(message);
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,this.name()+" glows brightly.");
							this.setUsesRemaining(this.usesRemaining()-1);
							A.invoke(mob, V, target, true,envStats().level());
							wandUse.helpProfficiency(mob);
							return;
						}
					}
				}
			}
		}
	}

	public static void waveIfAble(MOB mob,
								  Environmental afftarget,
								  String message,
								  Wand me)
	{
		if((mob.isMine(me))
		   &&(!me.amWearingAt(Item.INVENTORY)))
		{
			Environmental target=null;
			if((mob.location()!=null))
				target=afftarget;
			int x=message.toUpperCase().indexOf(me.magicWord().toUpperCase());
			if(x>=0)
			{
				message=message.substring(x+me.magicWord().length());
				int y=message.indexOf("'");
				if(y>=0) message=message.substring(0,y);
				message=message.trim();
				Ability wandUse=mob.fetchAbility("Skill_WandUse");
				if((wandUse==null)||(!wandUse.profficiencyCheck(null,0,false)))
					mob.tell(me.name()+" glows faintly for a moment, then fades.");
				else
				{
					Ability A=me.getSpell();
					if(A==null)
						mob.tell("Something seems wrong with "+me.name()+".");
					else
					if(me.usesRemaining()<=0)
						mob.tell(me.name()+" seems spent.");
					else
					{
						A=(Ability)A.newInstance();
						if(useTheWand(A,mob))
						{
							Vector V=new Vector();
							if(target!=null)
								V.addElement(target.name());
							V.addElement(message);
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,me.name()+" glows brightly.");
							me.setUsesRemaining(me.usesRemaining()-1);
							A.invoke(mob, V, target, true, me.envStats().level());
							wandUse.helpProfficiency(mob);
							return;
						}
					}
				}
			}
		}
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this))
				waveIfAble(mob,msg.tool(),msg.targetMessage());
			break;
		case CMMsg.TYP_SPEAK:
			if(msg.sourceMinor()==CMMsg.TYP_SPEAK)
				msg.addTrailerMsg(new FullMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_GENERAL|CMMsg.TYP_WAND_USE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	public String magicWord(){return secretWord;}


	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+baseEnvStats().ability();
		case 2: return ""+baseEnvStats().level();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: baseEnvStats().setLevel(Util.s_int(val)); break;
		case 2: baseEnvStats().setAbility(Util.s_int(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdWand)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
