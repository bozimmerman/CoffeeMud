package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenItem;
import java.util.*;

public class GenWand extends GenItem implements Wand
{
	private String secretWord=StdWand.words[Dice.roll(1,StdWand.words.length,0)-1];
	public GenWand()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a wand";
		displayText="a simple wand is here.";
		description="A wand made out of wood.";
		secretIdentity=null;
		this.setUsesRemaining(50);
		baseGoldValue=20000;
		baseEnvStats().setLevel(12);
		setReadable(false);
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
			id="'A wand of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message,
						   Wand me)
	{
		new StdWand().waveIfAble(mob,afftarget,message,me);
	}

	public String magicWord(){return secretWord;}

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		
		switch(affect.targetMinor())
		{
		case Affect.TYP_CAST_SPELL:
			if((affect.amITarget(this))
			   &&(Util.bset(affect.targetCode(),Affect.ACT_GENERAL))
			   &&(affect.sourceCode()==Affect.NO_EFFECT)
			   &&(affect.othersCode()==Affect.NO_EFFECT))
					waveIfAble(mob,affect.tool(),affect.targetMessage(),this);
			break;
		case Affect.TYP_SPEAK:
			if(affect.sourceMinor()==Affect.TYP_SPEAK)
				affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.target(),affect.NO_EFFECT,null,Affect.ACT_GENERAL|Affect.TYP_CAST_SPELL,affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(affect);
	}
}
