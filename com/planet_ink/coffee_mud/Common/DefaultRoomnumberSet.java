package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

public class DefaultRoomnumberSet implements RoomnumberSet
{
    public DVector root=new DVector(2);
    public String ID(){return "DefaultRoomnumberSet";}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultRoomnumberSet();}}
    public CMObject copyOf()
    {
        DefaultRoomnumberSet R=new DefaultRoomnumberSet();
        R.root=new DVector(2);
        for(int r=0;r<root.size();r++)
            R.root.addElement(root.elementAt(r,1),((CMIntegerGrouper)root.elementAt(r,2)).copyOf());
        return R;
    }
    
    public int roomCount(String areaName)
    {
        areaName=areaName.toUpperCase();
        int x=areaName.indexOf("#");
        if(x>0)
            areaName=areaName.substring(0,x);
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0)
            {
                if(root.elementAt(mid,2)!=null)
                    return ((CMIntegerGrouper)root.elementAt(mid,2)).roomCount();
                return 0;
            }
            else
            if(comp<0)
                end=mid-1;
            else
                start=mid+1;
        }
        return 0;
    }
    
    public boolean contains(String str)
    {
        String areaName=str.toUpperCase();
        String theRest=null;
        long roomNum=-1;
        int x=areaName.indexOf("#");
        if(x>0)
        {
            theRest=areaName.substring(x+1).trim();
            areaName=areaName.substring(0,x);
            x=theRest.indexOf("#(");
            if((x>=0)&&(theRest.endsWith(")"))&&(Util.isInteger(theRest.substring(0,x))))
            {
                int comma=theRest.indexOf(",",x);
                if(comma>0)
                {
                    roomNum=Long.parseLong(theRest.substring(0,x))<<30;
                    roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
                    roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
                }
            }
            else
            if(Util.isInteger(theRest))
                roomNum=Integer.parseInt(theRest.substring(x+1).trim());
        }
        
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0)
            {
                if(root.elementAt(mid,2)!=null)
                    return ((CMIntegerGrouper)root.elementAt(mid,2)).contains(roomNum);
                return true;
            }
            else
            if(comp<0)
                end=mid-1;
            else
                start=mid+1;
        }
        return false;
    }
    
    public String xml()
    {
        StringBuffer str=new StringBuffer("<AREAS>");
        for(int i=0;i<root.size();i++)
        {
            str.append("<AREA><ID>"+(String)root.elementAt(i,1)+"</ID>");
            if(root.elementAt(i,2)!=null)
                str.append("<NUMS>"+((CMIntegerGrouper)root.elementAt(i,2)).text()+"</NUMS>");
            str.append("</AREA>");
        }
        return str.toString()+"</AREAS>";
    }
    public void parseXML(String xml)
    {
        Vector V=CMLib.xml().parseAllXML(xml);
        if((V==null)||(V.size()==0)) return;
        Vector xV=CMLib.xml().getRealContentsFromPieces(V,"AREAS");
        root.clear();
        String ID=null;
        String NUMS=null;
        if((xV!=null)&&(xV.size()>0))
            for(int x=0;x<xV.size();x++)
            {
                XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                if((ablk.tag.equalsIgnoreCase("AREA"))&&(ablk.contents!=null))
                {
                    ID=CMLib.xml().getValFromPieces(ablk.contents,"ID");
                    NUMS=CMLib.xml().getValFromPieces(ablk.contents,"NUMS");
                    if((NUMS!=null)&&(NUMS.length()>0))
                        root.addElement(ID,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).parseText(NUMS));
                    else
                        root.addElement(ID,null);
                }
            }
    }
    
    
    public void add(String str)
    {
        String areaName=str.toUpperCase().trim();
        if(areaName.length()==0) return;
        
        String theRest=null;
        long roomNum=-1;
        int x=areaName.indexOf("#");
        if(x>0)
        {
            theRest=areaName.substring(x+1).trim();
            areaName=areaName.substring(0,x);
            x=theRest.indexOf("#(");
            if((x>=0)&&(theRest.endsWith(")"))&&(Util.isInteger(theRest.substring(0,x))))
            {
                int comma=theRest.indexOf(",",x);
                if(comma>0)
                {
                    roomNum=(Long.parseLong(theRest.substring(0,x))<<30);
                    roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
                    roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
                }
            }
            else
            if(Util.isInteger(theRest))
                roomNum=Integer.parseInt(theRest.substring(x+1).trim());
        }
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        int lastStart=0;
        int lastEnd=root.size()-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0)
                break;
            else
            if(comp<0)
            {
                lastEnd=end;
                end=mid-1;
            }
            else
            {
                lastStart=start;
                start=mid+1;
            }
        }
        if(comp==0)
        {
            if(root.elementAt(mid,2)!=null)
                ((CMIntegerGrouper)root.elementAt(mid,2)).add(roomNum);
        }
        else
        {
            if(mid<0)
                root.addElement(areaName,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).add(roomNum));
            else
            {
                for(comp=lastStart;comp<=lastEnd;comp++)
                    if(areaName.compareTo((String)root.elementAt(comp,1))<0)
                    {
                        root.insertElementAt(comp,areaName,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).add(roomNum));
                        return;
                    }
                root.addElement(areaName,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).add(roomNum));
            }
        }
    }
}
