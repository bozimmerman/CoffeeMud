package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;


public class ManualClasses extends StdItem implements MiscMagic,ArchonOnly
{
	public String ID(){	return "ManualClasses";}
	public ManualClasses()
	{
		super();

		setName("a book");
		baseEnvStats.setWeight(1);
		setDisplayText("an roughly treated book sits here.");
		setDescription("An roughly treated book filled with mystical symbols.");
		secretIdentity="The Manual of Classes.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=5000;
		material=EnvResource.RESOURCE_PAPER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new ManualClasses();
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
					if(mob.fetchEffect("Spell_ReadMagic")!=null)
					{
						if(this.usesRemaining()<=0)
							mob.tell("The markings have been read off the parchment, and are no longer discernable.");
						else
						{
							this.setUsesRemaining(this.usesRemaining()-1);
							mob.tell("The manual glows softly, enveloping you in its wisdom.");
							CharClass lastC=null;
							CharClass thisC=null;
							for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
							{
								CharClass C=(CharClass)c.nextElement();
								if(thisC==null) thisC=C;
								if((lastC!=null)&&(thisC==mob.charStats().getCurrentClass()))
								{
									thisC=C;
									break;
								}
								lastC=C;
							}
							if((thisC!=null)&&(!(thisC.ID().equals("Archon"))))
							{
								mob.charStats().setCurrentClass(thisC);
								mob.tell("You are now a "+thisC.name()+".");
								mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,mob.name()+" undergoes a traumatic change.");
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
		super.executeMsg(myHost,msg);
	}

}
