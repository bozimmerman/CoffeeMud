package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenItem;
import java.util.*;

public class GenScroll extends GenItem implements Scroll
{
	protected boolean readableScroll=false;
	protected Vector theSpells=new Vector();

	public GenScroll()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a scroll";
		baseEnvStats.setWeight(1);
		displayText="a scroll is rolled up here.";
		description="A rolled up parchment marked with mystical symbols.";
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=Item.PAPER;
	}

	public Environmental newInstance()
	{
		return new GenScroll();
	}
	public boolean isGeneric(){return true;}

	public int numSpells()
	{
		return theSpells.size();
	}

	public Vector getSpells()
	{
		return theSpells;
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
		return StdScroll.makeSecretIdentity("scroll",super.secretIdentity(),getSpells())+" Charges: "+usesRemaining();
	}

	public String getScrollText()
	{
		return readableText;
	}

	public boolean useTheScroll(Ability A, MOB mob)
	{
		return new StdScroll().useTheScroll(A,mob);
	}

	public void readIfAble(MOB mob, Scroll me, String spellName)
	{
		new StdScroll().readIfAble(mob,me,spellName);
	}

	public void parseSpells(Scroll me, String names)
	{
		new StdScroll().parseSpells(me,names);
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_READSOMETHING:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
					readIfAble(mob,this,affect.targetMessage());
				else
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
				return;
			default:
				break;
			}
		}
		super.affect(affect);
	}
	public void setReadableText(String newText)
	{
		readableText=newText;
		parseSpells(this,newText);
	}
	public void setScrollText(String text)
	{ this.setReadableText(text); }
	public void setSpellList(Vector newOne){theSpells=newOne;}
	public boolean isReadableScroll(){return readableScroll;}
	public void setReadableScroll(boolean isTrue){readableScroll=isTrue;}
}
