package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenItem;
import java.util.*;

public class GenWand extends GenItem implements Wand
{
	public String ID(){	return "GenWand";}
	private String secretWord=StdWand.words[Dice.roll(1,StdWand.words.length,0)-1];
	
	public GenWand()
	{
		super();

		setName("a wand");
		setDisplayText("a simple wand is here.");
		setDescription("A wand made out of wood.");
		secretIdentity=null;
		setUsesRemaining(0);
		baseGoldValue=20000;
		baseEnvStats().setLevel(12);
		Sense.setReadable(this,false);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}


	public boolean isGeneric(){return true;}

	protected int maxUses=Integer.MAX_VALUE;
	public int maxUses(){return maxUses;}
	public void setMaxUses(int newMaxUses){maxUses=newMaxUses;}
	
	public boolean useTheWand(Ability A, MOB mob)
	{
		return new StdWand().useTheWand(A,mob);
	}
	public void setSpell(Ability theSpell)
	{
		readableText="";
		if(theSpell!=null)
			readableText=theSpell.ID();
		secretWord=StdWand.getWandWord(readableText);
	}

	public void setReadableText(String text)
	{
		super.setReadableText(text);
		secretWord=StdWand.getWandWord(readableText);
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(readableText());
	}
	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		else
			return super.value();
	}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
		{
			id="'A wand of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
			return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
		}
		return id;
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message,
						   Wand me)
	{
		new StdWand().waveIfAble(mob,afftarget,message,me);
	}

	public String magicWord(){return secretWord;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this))
				waveIfAble(mob,msg.tool(),msg.targetMessage(),this);
			break;
		case CMMsg.TYP_SPEAK:
			if(msg.sourceMinor()==CMMsg.TYP_SPEAK)
				msg.addTrailerMsg(new FullMsg(msg.source(),this,msg.target(),msg.NO_EFFECT,null,CMMsg.MASK_GENERAL|CMMsg.TYP_WAND_USE,msg.targetMessage(),msg.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}
	// stats handled by genitem, spells by readabletext
}
