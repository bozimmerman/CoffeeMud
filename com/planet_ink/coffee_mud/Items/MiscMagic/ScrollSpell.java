package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;


public class ScrollSpell extends Scroll implements MiscMagic
{
	protected Vector theSpells=new Vector();
										  
	public ScrollSpell()
	{
		super();
		this.setUsesRemaining(2);
		baseGoldValue=200;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new ScrollSpell();
	}
	
	public void setMiscText(String newText)
	{
		miscText=newText;
		String names=this.miscText;
		
		baseGoldValue=200;
		theSpells=new Vector();
		int del=names.indexOf(";");
		while(del>0)
		{
			String thisOne=names.substring(0,del);
			Ability A=(Ability)MUD.getAbility(thisOne);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				baseGoldValue+=(100*A.envStats().level());
				theSpells.addElement(A);
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		Ability A=(Ability)MUD.getAbility(names);
		if(A!=null)
		{
			A=(Ability)A.copyOf();
			baseGoldValue+=(100*A.envStats().level());
			theSpells.addElement(A);
		}
		recoverEnvStats();
	}
	
	
	public int numSpells()
	{
		return theSpells.size();
	}
	
	
	public Vector getSpells()
	{
		return theSpells;
	}
	
	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.VISUAL_READ:
				if(mob.isMine(this))
				{
					boolean readingMagic=(mob.fetchAffect(new Spell_ReadMagic().ID())!=null);
					if(readingMagic)
					{
						mob.tell(name()+" glows softly.");
						readable=true;
					}
					if(readable)
					{
						if(this.usesRemaining()<=0)
							mob.tell("The markings have been read off the parchment, and are no longer discernable.");
						else
						{
							Vector Spells=getSpells();
							if(Spells.size()==0)
								mob.tell("The scroll appears to contain no discernable information.");
							else
							{
								Ability thisOne=null;
								if(affect.targetMessage().length()>0)
									for(int a=0;a<theSpells.size();a++)
									{
										Ability A=(Ability)theSpells.elementAt(a);
										if(Util.containsString(A.name().toUpperCase(),affect.targetMessage().toUpperCase()))
										{
											thisOne=A;
											break;
										}
									}
								
								if((thisOne!=null)&&(useTheScroll(thisOne,mob)))
								{
									thisOne=(Ability)thisOne.copyOf();
									thisOne.setProfficiency(100);
									thisOne.invoke(mob,new Vector());
									this.setUsesRemaining(this.usesRemaining()-1);
								}
								else
								if(affect.targetMessage().length()>0)
									mob.tell("That is not written on the scroll.");
								else
								if(!mob.isMonster())
								{
									StringBuffer theNews=new StringBuffer("The scroll contains the following spells:\n\r");
									for(int u=0;u<theSpells.size();u++)
									{
										Ability A=(Ability)theSpells.elementAt(u);
										theNews.append("Level "+Util.padRight(""+A.envStats().level(),2)+": "+A.name()+"\n\r");
									}
									mob.tell(theNews.toString());
								}
							}
						}
					}
					else
						mob.tell("The markings look magical, and are unknown to you.");
				}
				return;
			default:
				break;
			}
		}
		super.affect(affect);
	}
	
	public void setReadableText(String text)
	{ setMiscText(text); }
}
