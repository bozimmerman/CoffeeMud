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
		setReadable(false);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GenWand();
	}
	public boolean isGeneric(){return true;}

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

	public void affect(Environmental myHost, Affect affect)
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
				affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.target(),affect.NO_EFFECT,null,Affect.MASK_GENERAL|Affect.TYP_WAND_USE,affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(myHost,affect);
	}
	// stats handled by genitem, spells by readabletext
}
