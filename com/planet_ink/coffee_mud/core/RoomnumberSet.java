package com.planet_ink.coffee_mud.utils;
import java.util.*;

public class RoomnumberSet implements Cloneable
{
    protected DVector root=new DVector(2);
    
    public Object clone()
    {
        RoomnumberSet R=new RoomnumberSet();
        R.root=new DVector(2);
        for(int r=0;r<root.size();r++)
            R.root.addElement(root.elementAt(r,1),((IntegerGrouper)root.elementAt(r,2)).clone());
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
                    return ((IntegerGrouper)root.elementAt(mid,2)).roomCount();
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
                    return ((IntegerGrouper)root.elementAt(mid,2)).contains(roomNum);
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
                str.append("<NUMS>"+((IntegerGrouper)root.elementAt(i,2)).text()+"</NUMS>");
            str.append("</AREA>");
        }
        return str.toString()+"</AREAS>";
    }
    public void parseXML(String xml)
    {
        Vector V=XMLManager.parseAllXML(xml);
        if((V==null)||(V.size()==0)) return;
        Vector xV=XMLManager.getRealContentsFromPieces(V,"AREAS");
        root.clear();
        String ID=null;
        String NUMS=null;
        if((xV!=null)&&(xV.size()>0))
            for(int x=0;x<xV.size();x++)
            {
                XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)xV.elementAt(x);
                if((ablk.tag.equalsIgnoreCase("AREA"))&&(ablk.contents!=null))
                {
                    ID=XMLManager.getValFromPieces(ablk.contents,"ID");
                    NUMS=XMLManager.getValFromPieces(ablk.contents,"NUMS");
                    if((NUMS!=null)&&(NUMS.length()>0))
                        root.addElement(ID,new IntegerGrouper().parseText(NUMS));
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
                ((IntegerGrouper)root.elementAt(mid,2)).add(roomNum);
        }
        else
        {
            if(mid<0)
                root.addElement(areaName,new IntegerGrouper().add(roomNum));
            else
            {
                for(comp=lastStart;comp<=lastEnd;comp++)
                    if(areaName.compareTo((String)root.elementAt(comp,1))<0)
                    {
                        root.insertElementAt(comp,areaName,new IntegerGrouper().add(roomNum));
                        return;
                    }
                root.addElement(areaName,new IntegerGrouper().add(roomNum));
            }
        }
    }
    
    public static class IntegerGrouper implements Cloneable
    {
        protected static final int NEXT_FLAG=(Integer.MAX_VALUE/2)+1;
        protected static final int NEXT_BITS=NEXT_FLAG-1;
        protected static final long NEXT_FLAGL=(Long.MAX_VALUE/2)+1;
        protected static final long NEXT_BITSL=NEXT_FLAGL-1;
        protected int[] xs=new int[0];
        protected long[] ys=new long[0];
        
        public IntegerGrouper()
        {
            super();
        }
        
        public Object clone()
        {
            IntegerGrouper R=new IntegerGrouper();
            R.xs=new int[xs.length];
            for(int i=0;i<xs.length;i++)
                R.xs[i]=xs[i];
            R.ys=new long[ys.length];
            for(int i=0;i<ys.length;i++)
                R.ys[i]=ys[i];
            return R;
        }
        
        
        public String text()
        {
            return "{"+Util.toStringList(xs)+"},{"+Util.toStringList(ys)+"}";
        }
        
        public IntegerGrouper parseText(String txt)
        {
            xs=new int[0];
            ys=new long[0];
            txt=txt.trim();
            if(txt.length()==0) return null;
            if((!txt.startsWith("{"))&&(!txt.endsWith("}"))) 
                return null;
            int x=txt.indexOf("},{");
            if(x<0) return null;
            String Xstr=txt.substring(1,x);
            String Ystr=txt.substring(x+3,txt.length()-1);
            Vector XV=Util.parseCommas(Xstr,true);
            Vector YV=Util.parseCommas(Ystr,true);
            xs=new int[XV.size()];
            for(int v=0;v<XV.size();v++)
                xs[v]=Util.s_int((String)XV.elementAt(v));
            ys=new long[YV.size()];
            for(int v=0;v<YV.size();v++)
                ys[v]=Util.s_long((String)YV.elementAt(v));
            return this;
        }
        
        public boolean contains(long x)
        {
            if(x<0) return true;
            if(x<=Integer.MAX_VALUE)
            {
                for(int i=0;i<xs.length;i++)
                    if((xs[i]&NEXT_FLAG)==NEXT_FLAG)
                    {
                        if((x>=(xs[i]&NEXT_BITS))&&(x<=xs[i+1]))
                            return true;
                        if(x<=xs[i+1])
                            return false;
                        i++;
                    }
                    else
                    if(x==xs[i])
                        return true;
                    else
                    if(x<xs[i])
                        return false;
            }
            else
            {
                for(int i=0;i<ys.length;i++)
                    if((ys[i]&NEXT_FLAGL)==NEXT_FLAGL)
                    {
                        if((x>=(ys[i]&NEXT_BITSL))&&(x<=ys[i+1]))
                            return true;
                        if(x<=ys[i+1])
                            return false;
                        i++;
                    }
                    else
                    if(x==ys[i])
                        return true;
                    else
                    if(x<ys[i])
                        return false;
            }
            return false;
        }
        
        public int roomCount()
        {
            int count=0;
            for(int i=0;i<xs.length;i++)
                if(((xs[i]&NEXT_FLAG)==NEXT_FLAG)
                &&(i<(xs.length-1)))
                    count=count+1+(xs[i+1]-(xs[i]&NEXT_BITS));
                else
                    count++;
            for(int i=0;i<ys.length;i++)
                if(((ys[i]&NEXT_FLAGL)==NEXT_FLAGL)
                &&(i<(ys.length-1)))
                    count=count+1+(int)(ys[i+1]-(ys[i]&NEXT_BITSL));
                else
                    count++;
            return count;
        }
        
        private void growarrayx(int here)
        {
            int[] newis=new int[xs.length+1];
            for(int i=0;i<here;i++)
                newis[i]=xs[i];
            for(int i=here;i<xs.length;i++)
                newis[i+1]=xs[i];
            xs=newis;
        }
       
        private void growarrayy(int here)
        {
            long[] newis=new long[ys.length+1];
            for(int i=0;i<here;i++)
                newis[i]=ys[i];
            for(int i=here;i<ys.length;i++)
                newis[i+1]=ys[i];
            ys=newis;
        }
        
        private void shrinkarrayx(int here)
        {
            int[] newis=new int[xs.length-1];
            for(int i=0;i<here;i++)
                newis[i]=xs[i];
            for(int i=here;i<xs.length;i++)
                newis[i-1]=xs[i];
            xs=newis;
        }
        
        private void shrinkarrayy(int here)
        {
            long[] newis=new long[ys.length-1];
            for(int i=0;i<here;i++)
                newis[i]=ys[i];
            for(int i=here;i<ys.length;i++)
                newis[i-1]=ys[i];
            ys=newis;
        }
        
        private void consolodatex()
        {
            for(int i=0;i<xs.length-1;i++)
                if(((xs[i]&NEXT_FLAG)==0)
                &&((xs[i]&NEXT_BITS)==((xs[i+1]&NEXT_BITS)+1)))
                {
                    if((xs[i+1]&NEXT_FLAG)==NEXT_FLAG)
                    {
                        if((i>0)&&((xs[i-1]&NEXT_FLAG)==NEXT_FLAG))
                        {
                            shrinkarrayx(i+1);
                            shrinkarrayx(i);
                            return;
                        }
                        shrinkarrayx(i);
                        xs[i]=((xs[i]&NEXT_BITS)-1)|NEXT_FLAG;
                        return;
                    }
                    if((i>0)&&((xs[i-1]&NEXT_FLAG)==NEXT_FLAG))
                    {
                        shrinkarrayx(i+1);
                        xs[i]++;
                        return;
                    }
                    xs[i]=xs[i]|NEXT_FLAG;
                    return;
                }
        }
        
        private void consolodatey()
        {
            for(int i=0;i<ys.length-1;i++)
                if(((ys[i]&NEXT_FLAGL)==0)
                &&((ys[i]&NEXT_BITSL)==((ys[i+1]&NEXT_BITSL)+1)))
                {
                    if((ys[i+1]&NEXT_FLAGL)==NEXT_FLAGL)
                    {
                        if((i>0)&&((ys[i-1]&NEXT_FLAGL)==NEXT_FLAGL))
                        {
                            shrinkarrayy(i+1);
                            shrinkarrayy(i);
                            return;
                        }
                        shrinkarrayy(i);
                        ys[i]=((ys[i]&NEXT_BITSL)-1)|NEXT_FLAGL;
                        return;
                    }
                    if((i>0)&&((ys[i-1]&NEXT_FLAG)==NEXT_FLAG))
                    {
                        shrinkarrayy(i+1);
                        ys[i]++;
                        return;
                    }
                    ys[i]=ys[i]|NEXT_FLAGL;
                    return;
                }
        }
        
        public synchronized IntegerGrouper add(long x)
        {
            if(x<0) return null;
            if(x<NEXT_FLAG)
                addx((int)x);
            else
                addy(x);
            return this;
        }
        
        private void addy(long x)
        {
            for(int i=0;i<ys.length;i++)
                if((ys[i]&NEXT_FLAGL)==NEXT_FLAGL)
                {
                    if((x>=(ys[i]&NEXT_BITSL))&&(x<=ys[i+1]))
                        return;
                    if(x==((ys[i]&NEXT_BITSL)-1))
                    {
                        ys[i]=x|NEXT_FLAGL;
                        consolodatey();
                        return;
                    }
                    if(x==(ys[i+1]+1))
                    {
                        ys[i+1]=x;
                        consolodatey();
                        return;
                    }
                    if(x<(ys[i]&NEXT_BITSL))
                    {
                        growarrayy(i);
                        ys[i]=x;
                        consolodatey();
                        return;
                    }
                    i++;
                }
                else
                if(x==ys[i])
                    return;
                else
                if(x==ys[i]-1)
                {
                    growarrayy(i);
                    ys[i]=x|NEXT_FLAGL;
                    consolodatey();
                    return;
                }
                else
                if(x==ys[i]+1)
                {
                    growarrayy(i+1);
                    ys[i]=ys[i]|NEXT_FLAGL;
                    ys[i+1]=x;
                    consolodatey();
                    return;
                }
                else
                if(x<ys[i])
                {
                    growarrayy(i);
                    ys[i]=x;
                    consolodatey();
                    return;
                }
            growarrayy(ys.length);
            ys[ys.length-1]=x;
            consolodatey();
            return;
        }
        
        private void addx(int x)
        {
            for(int i=0;i<xs.length;i++)
                if((xs[i]&NEXT_FLAG)==NEXT_FLAG)
                {
                    if((x>=(xs[i]&NEXT_BITS))&&(x<=xs[i+1]))
                        return;
                    if(x==((xs[i]&NEXT_BITS)-1))
                    {
                        xs[i]=x|NEXT_FLAG;
                        consolodatex();
                        return;
                    }
                    if(x==(xs[i+1]+1))
                    {
                        xs[i+1]=x;
                        consolodatex();
                        return;
                    }
                    if(x<(xs[i]&NEXT_BITS))
                    {
                        growarrayx(i);
                        xs[i]=x;
                        consolodatex();
                        return;
                    }
                    i++;
                }
                else
                if(x==xs[i])
                    return;
                else
                if(x==xs[i]-1)
                {
                    growarrayx(i);
                    xs[i]=x|NEXT_FLAG;
                    consolodatex();
                    return;
                }
                else
                if(x==xs[i]+1)
                {
                    growarrayx(i+1);
                    xs[i]=xs[i]|NEXT_FLAG;
                    xs[i+1]=x;
                    consolodatex();
                    return;
                }
                else
                if(x<xs[i])
                {
                    growarrayx(i);
                    xs[i]=x;
                    consolodatex();
                    return;
                }
            growarrayx(xs.length);
            xs[xs.length-1]=x;
            consolodatex();
            return;
        }
    }
}
