package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Compare extends StdCommand
{
	public Compare(){}

	private String[] access={"COMPARE","COMP"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Compare what to what?");
			return false;
		}
		commands.removeElementAt(0);
		Item compareThis=mob.fetchInventory((String)commands.elementAt(0));
		if((compareThis==null)||((compareThis!=null)&&(!Sense.canBeSeenBy(compareThis,mob))))
		{
			mob.tell("You don't have a "+((String)commands.elementAt(0))+".");
			return false;
		}
		long compareThisCode = compareThis.rawProperLocationBitmap();
		if(Util.bset(compareThisCode,Item.HELD)
		&&(!Util.bset(compareThisCode,Item.WIELD))
		&&(compareThisCode!=Item.HELD))
			compareThisCode=Util.unsetb(compareThisCode,Item.HELD);
		Item toThis=null;
		if(commands.size()==1)
		{
			Item possible=null;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=(Item)mob.fetchInventory(i);
				if((I!=null)
				&&(I!=compareThis)
				&&(I.rawLogicalAnd()==compareThis.rawLogicalAnd()))
				{
					long compareThatCode = I.rawProperLocationBitmap();
					if(Util.bset(compareThatCode,Item.HELD)
					&&(!Util.bset(compareThatCode,Item.WIELD))
					&&(compareThatCode!=Item.HELD))
						compareThatCode=Util.unsetb(compareThatCode,Item.HELD);
					if(compareThisCode==compareThatCode)
					{
						if(!I.amWearingAt(Item.INVENTORY))
						{ toThis=I; break;}
						if(possible==null) possible=I;
					}
				}
			}
			if(toThis==null) toThis=possible;
			if((toThis==null)||((toThis!=null)&&(!Sense.canBeSeenBy(toThis,mob))))
			{
				mob.tell("Compare a "+compareThis.name()+" to what?");
				return false;
			}
		}
		else
			toThis=mob.fetchInventory(Util.combine(commands,1));
		if((toThis==null)||((toThis!=null)&&(!Sense.canBeSeenBy(toThis,mob))))
		{
			mob.tell("You don't have a "+((String)commands.elementAt(1))+".");
			return false;
		}

		if((compareThis instanceof Weapon)&&(toThis instanceof Weapon))
		{
			int cDmg=compareThis.baseEnvStats().damage();
			int tDmg=toThis.baseEnvStats().damage();
			cDmg+=(int)Math.round(Util.div(compareThis.baseEnvStats().attackAdjustment()-toThis.baseEnvStats().attackAdjustment(),100.0)*cDmg);

			if(cDmg==tDmg)
				mob.tell(compareThis.name()+" and "+toThis.name()+" look about the same.");
			else
			if(cDmg>tDmg)
				mob.tell(compareThis.name()+" looks better than "+toThis.name()+".");
			else
				mob.tell(compareThis.name()+" looks worse than "+toThis.name()+".");
		}
		else
		if((compareThis instanceof Armor)&&(toThis instanceof Armor))
		{
			if(!compareThis.compareProperLocations(toThis))
			{
				mob.tell(compareThis.name()+" is not worn the same way as "+toThis.name()+", and can't be compared to it.");
				return false;
			}
			if(compareThis.baseEnvStats().armor()==toThis.baseEnvStats().armor())
				mob.tell(compareThis.name()+" and "+toThis.name()+" look about the same.");
			else
			if(compareThis.baseEnvStats().armor()>toThis.baseEnvStats().armor())
				mob.tell(compareThis.name()+" look better than "+toThis.name()+".");
			else
				mob.tell(compareThis.name()+" look worse than "+toThis.name()+".");

		}
		else
			mob.tell("You can't compare "+compareThis.name()+" and "+toThis.name()+".");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
