package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

// submitted by jeremy vyska
// full and functional help sample:
// min=100 max=200 chance=100;damage=77 Title="A Horribly Blackened Spot" Description="This place was once just like any part of the road.  Now it is nothing more than a sooty pit." NOSTOP eqchance=17

public class FieryRoom extends ActiveTicker 
{
    public String ID(){return "FieryRoom";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS;}
    private String newDisplay="";
    private String newDesc="";
    private int directDamage=10;
    private int eqChance=0;
    private boolean noStop=false;
    private boolean noNpc=false;
    private boolean notStart=false;

    public FieryRoom() 
	{
        minTicks=100;maxTicks=200;chance=100;
        tickReset();
    }

    public void setParms(String newParms)
    {
        super.setParms(newParms);
        newDisplay=Util.getParmStr(newParms,"Title","A Charred Ruin");
        newDesc=Util.getParmStr(newParms,"Description","Whatever was once here is now nothing more than ash.");
        directDamage=Util.getParmInt(newParms,"damage",10);
        if(newParms.toUpperCase().indexOf("NOSTOP")>0) noStop = true;
        if(newParms.toUpperCase().indexOf("NONPC")>0) noNpc = true;
        if(newParms.toUpperCase().indexOf("NOTSTART")>0) notStart = true;
        eqChance=Util.getParmInt(newParms,"eqchance",0);
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        super.tick(ticking, tickID);
        // on every tick, we may do damage OR eq handling.
        Room room=(Room)ticking;
/*                room.showHappens(Affect.MSG_OK_ACTION,"FieryRoom Report: \n\r"
                         +"The room will eventually be: \n\r"
                         +newDisplay+"\n\r"
                         +newDesc+"\n\r"
                         +"-----------------\n\r"
                         +"Damage per tick will be:   "+directDamage+"\n\r"
                         +"This room will "+(noStop?"NOT ":"")+"stop burning.\n\r"
                         +"There is a "+eqChance+"% of equipment loss.\n\r"
                         );*/
        if((directDamage>0)||(eqChance>0)) 
		{
            // for each inhab, do directDamage to them.
            for (int i = 0; i < room.numInhabitants(); i++) 
			{
                MOB inhab = room.fetchInhabitant(i);
                if(inhab.isMonster()) 
				{
                    boolean reallyAffect=true;
                    if(notStart&&(inhab.getStartRoom()!=null)&&
                       inhab.getStartRoom().roomID().equalsIgnoreCase(room.roomID())) 
					{
                        reallyAffect=false;
                    }
                    if(noNpc) 
					{
                        reallyAffect=false;
                        HashSet group=inhab.getGroupMembers(new HashSet());
						for(Iterator e=group.iterator();e.hasNext();)
                        {
                            MOB follower=(MOB)e.next();
                            if(!(follower.isMonster())) 
							{
                                reallyAffect=true;
                                break;
                            }
                        }
                    }
                    if (reallyAffect) 
					{
                        dealDamage(inhab);
                        if(Dice.rollPercentage()>eqChance)
                            eqRoast(inhab);
                    }
                }
                else
                {
                    if(!CMSecurity.isAllowed(inhab,room,"CMDROOMS"))
					{
                        dealDamage(inhab);
                        if(Dice.rollPercentage()>eqChance)
                            eqRoast(inhab);
                    }
                }
            }
        }
        if (canAct(ticking, tickID)) 
		{
            if (ticking instanceof Room) 
			{
                // The tick happened.  This means the room gets set
                // to the torched text and the behavior goes away.
                room.setDisplayText(newDisplay);
                room.setDescription(newDesc);
                room.delBehavior(this);
            }
        }
        return true;
    }

    private void dealDamage(MOB mob) 
	{
        MUDFight.postDamage(mob, mob, null, directDamage, CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE, Weapon.TYPE_BURNING,
                                "The fire here <DAMAGE> <T-NAME>!");
    }

    private void eqRoast(MOB mob) 
	{
        Item target=getSomething(mob);
        if(target!=null) 
		{
            switch(target.material()&EnvResource.MATERIAL_MASK)
            {
                case EnvResource.MATERIAL_GLASS:
                case EnvResource.MATERIAL_METAL:
                case EnvResource.MATERIAL_MITHRIL:
                case EnvResource.MATERIAL_PLASTIC:
                case EnvResource.MATERIAL_PRECIOUS:
                case EnvResource.MATERIAL_ROCK:
                case EnvResource.MATERIAL_UNKNOWN:
                {
                    // all these we'll make get hot and be dropped.
                    int damage=Dice.roll(1,6,1);
                    MUDFight.postDamage(mob,mob,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,target.name()+" <DAMAGE> <T-NAME>!");
                    if(Dice.rollPercentage()<mob.charStats().getStat(CharStats.STRENGTH))
                    {
                        CommonMsgs.drop(mob,target,false,false);
                    }
                    break;
                }
                default:
                {
                    Ability burn=CMClass.getAbility("Burning");
                    if(burn!=null) 
					{
                        mob.location().showHappens(CMMsg.MSG_OK_ACTION,target.Name()+" begins to burn!");
                        target.addEffect(burn);
                        target.recoverEnvStats();
                    }
                }
            }
        }
    }

    private static Item getSomething(MOB mob)
    {
        Vector good = new Vector();
        Vector great = new Vector();
        Item target = null;
        for (int i = 0; i < mob.inventorySize(); i++) 
		{
            Item I = mob.fetchInventory(i);
            if (I.amWearingAt(Item.INVENTORY))
                good.addElement(I);
            else
                great.addElement(I);
        }
        if (great.size() > 0)
            target = (Item) great.elementAt(Dice.roll(1, great.size(), -1));
        else
        if (good.size() > 0)
            target = (Item) good.elementAt(Dice.roll(1, good.size(), -1));
        return target;
    }
}
