package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenItem;
import java.util.*;

public class GenScroll extends GenItem implements Scroll
{
	public String ID(){	return "GenScroll";}
	protected boolean readableScroll=false;
	protected Vector theSpells=new Vector();

	public GenScroll()
	{
		super();

		setName("a scroll");
		baseEnvStats.setWeight(1);
		setDisplayText("a scroll is rolled up here.");
		setDescription("A rolled up parchment marked with mystical symbols.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_PAPER;
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
		return StdScroll.makeSecretIdentity("scroll",super.secretIdentity()," Charges: "+usesRemaining(),getSpells());
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READSOMETHING:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
					readIfAble(mob,this,msg.targetMessage());
				else
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
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
	// stats handled by genitem, spells by readabletext
}
