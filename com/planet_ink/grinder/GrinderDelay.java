package com.planet_ink.grinder;

import java.util.*;

public class GrinderDelay extends Thread
{
    private MUDGrinder myParent;
    private int taskID=0;
    
    public GrinderDelay(MUDGrinder parent)
    {
        super();
        myParent=parent;
    }
    
    public void setTaskID(int newTaskID)
    {
        taskID=newTaskID;
    }
    
    public void run()
    {
        try
        {
            this.sleep(500);
        }
        catch(Exception e)
        {
            
        }
        switch(taskID)
        {
            case 1:
            	Vector V=MapGrinder.getMap(myParent);
            	break;
            default:
                break;

        }
    }
    
}