package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;

public class StdWand extends StdItem implements Wand
{
	public String ID(){	return "StdWand";}
	public static final String[] words={"ZAP","ZAP","ZAP","ZOT","ZIT","ZEK","ZOM","ZUP","ZET","ZYT","ZVP","ZOP"};
	protected String secretWord=words[Dice.roll(1,words.length,0)-1];

	public StdWand()
	{
		super();

		name="a crooked stick";
		baseEnvStats.setWeight(1);
		displayText="a small crooked stick is here.";
		description="Looks like an broken piece of a tree.";
		secretIdentity="";
		baseGoldValue=200;
		material=EnvResource.RESOURCE_OAK;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public boolean useTheWand(Ability A, MOB mob)
	{
		int manaRequired=5;
		int q=A.qualifyingLevel(mob);
		if(q>0)
		{
			if(q<mob.envStats().level())
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
		{
			manaRequired=50;
		}
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
				if((wandUse==null)||(!wandUse.profficiencyCheck(0,false)))
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
						if(me.useTheWand(A,mob))
						{
							Vector V=new Vector();
							if(target!=null)
								V.addElement(target.name());
							V.addElement(message);
							mob.location().show(mob,null,Affect.MSG_OK_VISUAL,me.name()+" glows brightly.");
							me.setUsesRemaining(me.usesRemaining()-1);
							A.invoke(mob, V, target, true);
							wandUse.helpProfficiency(mob);
							return;
						}
					}
				}
			}
		}
	}

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		
		switch(affect.targetMinor())
		{
		case Affect.TYP_WAND_USE:
			if(affect.amITarget(this))
				waveIfAble(mob,affect.tool(),affect.targetMessage(),this);
			break;
		case Affect.TYP_SPEAK:
			if(affect.sourceMinor()==Affect.TYP_SPEAK)
				affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.target(),affect.NO_EFFECT,null,Affect.ACT_GENERAL|Affect.TYP_WAND_USE,affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(affect);
	}

	public String magicWord(){return secretWord;}

	public Environmental newInstance()
	{
		return new StdWand();
	}
}
