package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/*
   Copyright 2000-2007 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdAuctioneer extends StdShopKeeper implements Auctioneer
{
    public String ID(){return "StdPostman";}

    public StdAuctioneer()
    {
        super();
        Username="an auctioneer";
        setDescription("He talks faster than you!");
        setDisplayText("The local auctioneer is here calling prices.");
        CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);
        setMoney(0);
        whatISell=ShopKeeper.DEAL_POSTMAN;
        baseEnvStats.setWeight(150);
        setWimpHitPoint(0);

        baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
        baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

        baseEnvStats().setArmor(0);

        baseState.setHitPoints(1000);

        recoverMaxState();
        resetToMaxState();
        recoverEnvStats();
        recoverCharStats();
    }


    public String auctionHouse(){return text();}
    public void setAuctionHouse(String named){setMiscText(named);}
    
    protected double liveListingPrice=-1.0;
    public double liveListingPrice(){return liveListingPrice;}
    public void setLiveListingPrice(double d){liveListingPrice=d;}

    protected double timedListingPrice=-1.0;
    public double timedListingPrice(){return timedListingPrice;}
    public void setTimedListingPrice(double d){timedListingPrice=d;}

    protected double timedListingPct=-1.0;
    public double timedListingPct(){return timedListingPct;}
    public void setTimedListingPct(double d){timedListingPct=d;}

    protected double liveFinalCutPct=-1.0;
    public double liveFinalCutPct(){return liveFinalCutPct;}
    public void setLiveFinalCutPct(double d){liveFinalCutPct=d;}

    protected double timedFinalCutPct=-1.0;
    public double timedFinalCutPct(){return timedFinalCutPct;}
    public void setTimedFinalCutPct(double d){timedFinalCutPct=d;}

    protected int maxTimedAuctionDays=-1;
    public int maxTimedAuctionDays(){return maxTimedAuctionDays;}
    public void setMaxTimedAuctionDays(int d){maxTimedAuctionDays=d;}

    protected int minTimedAuctionDays=-1;
    public int minTimedAuctionDays(){return minTimedAuctionDays;}
    public void setMinTimedAuctionDays(int d){minTimedAuctionDays=d;}

    public void destroy()
    {
        super.destroy();
        CMLib.map().delAuctionHouse(this);
    }
    public void bringToLife(Room newLocation, boolean resetStats)
    {
        super.bringToLife(newLocation,resetStats);
        CMLib.map().addAuctionHouse(this);
    }

    public int whatIsSold(){return whatISell;}
    public void setWhatIsSold(int newSellCode){
        whatISell=ShopKeeper.DEAL_AUCTIONEER;
    }

    public String postalChain(){return text();}
    public void setPostalChain(String name){setMiscText(name);}
    public String postalBranch(){return CMLib.map().getExtendedRoomID(getStartRoom());}


    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) return true;

        return true;
    }

    public void autoGive(MOB src, MOB tgt, Item I)
    {
        CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
        location().send(this,msg2);
        msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
        location().send(this,msg2);
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_SELL:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return;
            case CMMsg.TYP_BUY:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return;
            case CMMsg.TYP_VALUE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
	            {
	                CMLib.commands().postSay(this,mob,"That's for the people to decide.",true,false);
	            }
                return;
            case CMMsg.TYP_VIEW:
                super.executeMsg(myHost,msg);
                return;
            case CMMsg.TYP_LIST:
            {
                super.executeMsg(myHost,msg);
    			if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
    			{
    			}
                return;
            }
            default:
                break;
            }
        }
        super.executeMsg(myHost,msg);
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
        &&(msg.target()==location())
        &&(CMLib.flags().isInTheGame(this,true)))
        	return false;
        else
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_SELL:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
	                return false;
                }
                return false;
            case CMMsg.TYP_BUY:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return false;
            case CMMsg.TYP_VALUE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
	            {
	                CMLib.commands().postSay(this,mob,"That's for the people to decide.",true,false);
	                return false;
	            }
                return false;
            case CMMsg.TYP_VIEW:
                super.executeMsg(myHost,msg);
                break;
            default:
                break;
            }
        }
        return super.okMessage(myHost,msg);
    }
}