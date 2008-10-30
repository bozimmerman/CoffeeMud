package com.planet_ink.coffee_mud.Behaviors;
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

@SuppressWarnings("unchecked")
public class ScriptableEverymob extends StdBehavior
{
    public String ID(){return "ScriptableEverymob";}
    protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

    private void giveUpTheScript(Area metroA, MOB M)
    {
        if((M==null)
        ||(!M.isMonster())
        ||(M.getStartRoom()==null)
        ||(metroA==null)
        ||(!metroA.inMyMetroArea(M.getStartRoom().getArea()))
        ||(M.fetchBehavior("Scriptable")!=null))
            return;
        Scriptable S=new Scriptable();
        S.setParms(getParms());
        S.setSavable(false);
        M.addBehavior(S);
        S.setSavable(false);
    }
    
    private Area determineArea(Environmental forMe)
    {
        if(forMe instanceof Room)
            return ((Room)forMe).getArea();
        else
        if(forMe instanceof Area)
            return (Area)forMe;
        return null;
    }
    
    private Enumeration determineRooms(Environmental forMe)
    {
        if(forMe instanceof Room)
            return CMParms.makeVector(forMe).elements();
        else
        if(forMe instanceof Area)
            return ((Area)forMe).getMetroMap();
        return null;
    }
    
    private void giveEveryoneTheScript(Environmental forMe)
    {
        Enumeration rooms=determineRooms(forMe);
        Area A=determineArea(forMe);
        if((A!=null)&&(rooms!=null))
        {
            Room R=null;
            for(;rooms.hasMoreElements();)
            {
                R=(Room)rooms.nextElement();
                for(int m=0;m<R.numInhabitants();m++)
                    giveUpTheScript(A,R.fetchInhabitant(m));
            }
        }
    }
    
    public void startBehavior(Environmental forMe)
    {
        giveEveryoneTheScript(forMe);
    }
    
    public void executeMsg(Environmental host, CMMsg msg)
    {
        if((msg.target() instanceof Room)
        &&(msg.targetMinor()==CMMsg.TYP_LOOK))
            giveUpTheScript(determineArea(host),msg.source());
        super.executeMsg(host,msg);
    }
}
