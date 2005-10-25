package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
        ||(!metroA.inMetroArea(M.getStartRoom().getArea()))
        ||(M.fetchBehavior("Scriptable")!=null))
            return;
        Scriptable S=new Scriptable();
        S.setParms(getParms());
        S.setBorrowed(true);
        M.addBehavior(S);
        S.setBorrowed(true);
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
            return Util.makeVector(forMe).elements();
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
