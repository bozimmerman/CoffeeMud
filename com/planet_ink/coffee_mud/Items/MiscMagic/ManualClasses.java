package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;


public class ManualClasses extends StdItem implements MiscMagic
{
	public ManualClasses()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a book";
		baseEnvStats.setWeight(1);
		displayText="an roughly treated book sits here.";
		description="An roughly treated book filled with mystical symbols.";
		secretIdentity="The Manual of Classes.";
		this.setUsesRemaining(Integer.MAX_VALUE);
		baseGoldValue=5000;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new ManualClasses();
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
					if(mob.fetchAffect(new Spell_ReadMagic().ID())!=null)
					{
						if(this.usesRemaining()<=0)
							mob.tell("The markings have been read off the parchment, and are no longer discernable.");
						else
						{
							this.setUsesRemaining(this.usesRemaining()-1);
							mob.tell("The manual glows softly, enveloping you in it's wisdom.");
							CharClass lastC=null;
							CharClass thisC=null;
							for(int c=0;c<MUD.charClasses.size();c++)
							{
								CharClass C=(CharClass)MUD.charClasses.elementAt(c);
								if(thisC==null) thisC=C;
								if((lastC!=null)&&(thisC==mob.charStats().getMyClass()))
								{
									thisC=C;
									break;
								}
								lastC=C;
							}
							if((thisC!=null)&&(!(thisC instanceof Archon)))
							{
								mob.charStats().setMyClass(thisC);
								mob.tell("You are now a "+thisC.name()+".");
								mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,mob.name()+" undergoes a traumatic change.");
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

}
