package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;


public class ManualArchon extends StdItem implements MiscMagic
{
	public ManualArchon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an ornately decorated book";
		baseEnvStats.setWeight(1);
		displayText="an ornately decorated book has definitely been left behind by someone.";
		description="A book covered with mystical symbols, inside and out.";
		secretIdentity="The Manual of the Archons.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=50000;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new ManualArchon();
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_READSOMETHING:
				if(mob.isMine(this))
				{
					mob.tell("The manual glows softly, enveloping you in its magical energy.");
					Session session=mob.session();
					CharClass newClass=(CharClass)CMClass.getCharClass("Archon");
					if((session!=null)&&(newClass!=null))
					{
						mob.setSession(null);

						mob.baseCharStats().setStat(CharStats.STRENGTH,25);
						mob.baseCharStats().setStat(CharStats.WISDOM,25);
						mob.baseCharStats().setStat(CharStats.INTELLIGENCE,25);
						mob.baseCharStats().setStat(CharStats.CONSTITUTION,25);
						mob.baseCharStats().setStat(CharStats.DEXTERITY,25);
						mob.baseCharStats().setStat(CharStats.CHARISMA,25);
						mob.recoverCharStats();
						for(int i=mob.envStats().level();i<=30;i++)
						{
							mob.charStats().getMyClass().gainExperience(mob,null,mob.getExpNeededLevel()+1);
						}

						mob.baseCharStats().setMyClass(newClass);
						mob.recoverCharStats();
						mob.recoverEnvStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
						mob.charStats().getMyClass().logon(mob);
						mob.setSession(session);
					}
				}
				mob.tell("The book vanishes out of your hands.");
				this.destroyThis();
				affect.source().location().recoverRoomStats();
				return;
			default:
				break;
			}
		}
		super.affect(affect);
	}

}
