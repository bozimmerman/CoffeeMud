package com.planet_ink.grinder;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

public class TheGrinder extends Thread
{
	// The following function is a placeholder for control initialization.
	// You should call this function from a constructor or initialization function.
	public void vcInit() {
		//{{INIT_CONTROLS
		//}}
	}
	
    private static Vector roomTypes=null;
    private static Vector mobTypes=null;
    private static Vector itemTypes=null;
    private static Vector classList=null;
    private static Vector areas=null;
    private static Vector exitTypes=null;
    private static Vector behaviorTypes=null;
    private static Vector abilityTypes=null;
    private static Vector fillers=new Vector();
    
    private static String areaName="";
    private static Vector areaRooms=null;
    
    public static boolean loggedIn=false;
    
    private static StringBuffer expect(String outPut, String expectedEnd)
    {
        StringBuffer p=GrinderConnection.sendAndExpectResponse(outPut);
        
        if(p==null) return null;
        StringBuffer p2=expectPrompt(p.toString());
        if(p2==null) return null;
        p=p2;
        if(p.toString().indexOf(expectedEnd)>=0)
            return p;
        return null;
    }
    
    public static String getAreaName()
    {
        return areaName;
    }
    
    public static synchronized StringBuffer safelyExpect(String roomFirst, String input, String outPut)
    {
        if((roomFirst!=null)&&(roomFirst.length()>0))
        {
            StringBuffer ok=expect("GOTO "+roomFirst,"Done.");
            if(ok==null)
                return null;
        }
        return expect(input,outPut);
    }

    private static StringBuffer expectPrompt(String addThis)
    {
        return expectPrompt(addThis,10);
    }
    private static StringBuffer expectPrompt(String addThis, int tries)
    {
        StringBuffer p=new StringBuffer(addThis);
        while((--tries)>=0)
        {
            StringBuffer p2=GrinderConnection.readIn(0,true);
            if(p2==null) 
                return null;
            p.append(p2);
            int x=-1;
            if(p.toString().endsWith(">"))
                x=p.length()-1;
            else
            {
                x=p.toString().lastIndexOf(">\n");
                if(x<0)
                    x=p.toString().lastIndexOf(">\r");
            }
            if(x>=0)
            {
                if(x==0)
                    return p;
                else
                if(x<20)
                {
                    if(Character.isDigit(p.charAt(x-1)))
                        return p;
                }
                else
                {
                    int y=p.toString().lastIndexOf("<",x);
                    if(y<0) return p;
                    String sub=p.substring(y,x);
                    if(sub.indexOf("hp")>=0)
                        return p;
                }
            }
        }
        return null;
    }
    
    public static boolean login(String name, String password)
    {
        try{Thread.sleep(5000);}catch(Exception e){}
        loggedIn=false;
        StringBuffer p=GrinderConnection.sendAndExpectResponse(name);
        if(p==null) return false;
        if(p.toString().indexOf("word:")<0)
            return false;
            
        p=GrinderConnection.sendAndExpectResponse(password);
        String add="";
        if(p!=null) add=p.toString();
        if((p=expectPrompt(add,50))==null)
            return false;
        loggedIn=true;
        return true;
    }
    
    public static Vector parseListFrom(String thisType)
    {
        Vector V=new Vector();
        StringBuffer rawBlock=expect("INFOXML <LIST>"+thisType.trim().toUpperCase()+"</LIST>","</LIST>");
        if(rawBlock==null)
            return V;
        String block=XMLManager.returnXMLValue(rawBlock.toString(), "LIST");
        if(block.length()==0)
            return V;
        int x=block.indexOf(";");
        while(x>=0)
        {
            String str=block.substring(0,x);
            if(str.length()>0)
                V.addElement(str.trim());
            block=block.substring(x+1);
            x=block.indexOf(";");
        }
        return V;
    }
    
    public static Vector getAreasList()
    {
        if(areas!=null)
            return areas;
        areas=parseListFrom("AREA");
        return areas;
    }
    
    public static Vector getExitTypes()
    {
        if(exitTypes!=null)
            return exitTypes;
        exitTypes=parseListFrom("EXIT");
        return exitTypes;
    }
    
    public static void setNewAreaName(String newName)
    {
        if((newName==null)||((newName!=null)&&(newName.length()==0)))
        {
            areaName="";
            areaRooms=null;
        }
        else
        {
            areaName=newName;
            areaRooms=null;
        }
    }
    
    public static boolean inAnArea()
    {
        if(!pickedAnArea())
            return false;
        if(areaRooms==null)
            return false;
        if(MapGrinder.getHashRooms()==null)
            return false;
        return true;
    }
    
    public static boolean pickedAnArea()
    {
        if(areaName==null)
            return false;
        if(areaName.length()==0)
            return false;
        return true;
    }
    
    public static void addFiller(Object me)
    {
        String myClass="";
        String myType="ITEM";
        if(me instanceof MapGrinder.Item)
            myClass=((MapGrinder.Item)me).classID;
        if(me instanceof MapGrinder.MOB)
        {
            myClass=((MapGrinder.MOB)me).classID;
            myType="MOB";
        }
        if(myClass.length()==0) return;
        for(int i=0;i<fillers.size();i++)
        {
            Object phil=fillers.elementAt(i);
            if((me instanceof MapGrinder.Item)&&(phil instanceof MapGrinder.Item))
                if(((MapGrinder.Item)phil).classID.equalsIgnoreCase(myClass))
                    return;
            if((me instanceof MapGrinder.MOB)&&(phil instanceof MapGrinder.MOB))
                if(((MapGrinder.MOB)phil).classID.equalsIgnoreCase(myClass))
                    return;
        }
        fillers.addElement(me);
    }
    
    public static boolean findMe(Object me, String myClass)
    {
        for(int i=0;i<fillers.size();i++)
        {
            Object phil=fillers.elementAt(i);
            if((me instanceof MapGrinder.Item)&&(phil instanceof MapGrinder.Item))
                if(((MapGrinder.Item)phil).classID.equalsIgnoreCase(myClass))
                {
                    ((MapGrinder.Item)phil).copyInto((MapGrinder.Item)me);
                    return true;
                }
            if((me instanceof MapGrinder.MOB)&&(phil instanceof MapGrinder.MOB))
                if(((MapGrinder.MOB)phil).classID.equalsIgnoreCase(myClass))
                {
                    ((MapGrinder.MOB)phil).copyInto((MapGrinder.MOB)me);
                    return true;
                }
        }
        return false;
    }
    
    public static void reFillMe(Object me, String myClass, boolean doLevelTrick)
    {
        String fullClass=myClass;
        if((doLevelTrick)&&(me instanceof MapGrinder.MOB))
            fullClass+=" "+((MapGrinder.MOB)me).level;
                
        StringBuffer buf=TheGrinder.safelyExpect(null,"INFOXML <ID>"+fullClass+"</ID>","</OBJECT>");
        String myBuf=buf.toString();
        String myBlock=XMLManager.returnXMLBlock(buf.toString(),"OBJECT");
        while(myBlock.length()>10)
        {
            String type=XMLManager.returnXMLValue(myBlock,"OBJECTTYPE");
            String text=XMLManager.returnXMLValue(myBlock,"OBJECTTEXT");
            String level=XMLManager.returnXMLValue(myBlock,"OBJECTLEVEL");
            String ability=XMLManager.returnXMLValue(myBlock,"OBJECTABILITY");
            String rejuv=XMLManager.returnXMLValue(myBlock,"OBJECTREJUV");
            String uses=XMLManager.returnXMLValue(myBlock,"OBJECTUSES");
            String armor=XMLManager.returnXMLValue(myBlock,"OBJECTARMOR");
            String attack=XMLManager.returnXMLValue(myBlock,"OBJECTATTACK");
            String damage=XMLManager.returnXMLValue(myBlock,"OBJECTDAMAGE");
            String money=XMLManager.returnXMLValue(myBlock,"OBJECTMONEY");
            if(type.equalsIgnoreCase("ITEM"))
            {
                MapGrinder.Item item=new MapGrinder.Item();
                item.classID=myClass;
                item.setMiscText(text);
                item.level=GenGrinder.s_int(level);
                item.ability=GenGrinder.s_int(ability);
                item.rejuv=GenGrinder.s_int(rejuv);
                item.usesRemaining=GenGrinder.s_int(uses);
                if(!doLevelTrick)
                    addFiller(item);
            }
            else
            if(type.equalsIgnoreCase("MOB"))
            {
                MapGrinder.MOB mob=new MapGrinder.MOB();
                mob.classID=myClass;
                mob.setMiscText(text);
                mob.level=GenGrinder.s_int(level);
                mob.ability=GenGrinder.s_int(ability);
                mob.rejuv=GenGrinder.s_int(rejuv);
                if((myClass.startsWith("Gen")&&(myClass.length()>4)&&(Character.isUpperCase(myClass.charAt(3)))))
                {
                    MapGrinder.GenGen g=new MapGrinder.GenGen();
                    GenGrinder.setPropertiesStr(mob,g,mob.miscText());
                    g.armor=GenGrinder.s_int(armor);
                    g.attack=GenGrinder.s_int(attack);
                    g.damage=GenGrinder.s_int(damage);
                    g.money=GenGrinder.s_int(money);
                    mob.setMiscText(GenGrinder.getPropertiesStr(mob,g));
                }
                if(!doLevelTrick)
                    addFiller(mob);
                else
                if(me instanceof MapGrinder.MOB)
                    mob.copyInto((MapGrinder.MOB)me);
            }
            buf=new StringBuffer(buf.toString().substring(buf.toString().indexOf(myBlock)+myBlock.length()));
            myBlock=XMLManager.returnXMLBlock(buf.toString(),"OBJECT");
        }
    }
    
    public static void fillMe(Object me)
    {
        String myClass="";
        String myType="ITEM";
        if(me instanceof MapGrinder.Item)
            myClass=((MapGrinder.Item)me).classID;
        if(me instanceof MapGrinder.MOB)
        {
            myClass=((MapGrinder.MOB)me).classID;
            myType="MOB";
        }
        
        if(findMe(me,myClass)) return;
        reFillMe(me,myClass,false);
        findMe(me,myClass);
    }
    
    public static Vector getRoomIDs()
    {
        if(areaRooms!=null)
            return areaRooms;
        areaRooms=TheGrinder.parseListFrom(areaName);
        return areaRooms;
    }
    
    public static Vector getRoomTypes()
    {
        if(roomTypes!=null)
            return roomTypes;
        roomTypes=TheGrinder.parseListFrom("LOCALE");
        return roomTypes;
    }
    
    public static Vector getItemTypes()
    {
        if(itemTypes!=null)
            return itemTypes;
        itemTypes=TheGrinder.parseListFrom("ITEM");
        return itemTypes;
    }
    
    public static Vector getAbilityTypes()
    {
        if(abilityTypes!=null)
            return abilityTypes;
        abilityTypes=TheGrinder.parseListFrom("ABILITY");
        return abilityTypes;
    }
    
    public static Vector getBehaviorTypes()
    {
        if(behaviorTypes!=null)
            return behaviorTypes;
        behaviorTypes=TheGrinder.parseListFrom("BEHAVIOR");
        return behaviorTypes;
    }
    
    public static Vector getMOBTypes()
    {
        if(mobTypes!=null)
            return mobTypes;
        mobTypes=TheGrinder.parseListFrom("MOB");
        return mobTypes;
    }
    
    public static Vector getClassList()
    {
        if(classList!=null)
            return classList;
        classList=TheGrinder.parseListFrom("CLASS");
        return classList;
    }
    
	//{{DECLARE_CONTROLS
	//}}
}