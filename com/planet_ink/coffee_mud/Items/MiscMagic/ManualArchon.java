package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;


public class ManualArchon extends StdItem implements MiscMagic,ArchonOnly
{
	public String ID(){	return "ManualArchon";}
	public ManualArchon()
	{
		super();

		setName("an ornately decorated book");
		baseEnvStats.setWeight(1);
		setDisplayText("an ornately decorated book has definitely been left behind by someone.");
		setDescription("A book covered with mystical symbols, inside and out.");
		secretIdentity="The Manual of the Archons.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=50000;
		material=EnvResource.RESOURCE_PAPER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new ManualArchon();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READSOMETHING:
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
						mob.baseCharStats().setCurrentClass(newClass);
						mob.recoverCharStats();
						while(mob.baseEnvStats().level()<=100)
						{
							if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
								mob.charStats().getCurrentClass().level(mob);
							else
								MUDFight.postExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
						}
						mob.recoverCharStats();
						mob.recoverEnvStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
						mob.charStats().getCurrentClass().startCharacter(mob,true,false);
						mob.charStats().getCurrentClass().outfit(mob);
						mob.setSession(session);
						CMClass.DBEngine().DBUpdateMOB(mob);
					}
				}
				mob.tell("The book vanishes out of your hands.");
				destroy();
				msg.source().location().recoverRoomStats();
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

}
