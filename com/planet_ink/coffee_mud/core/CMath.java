package com.planet_ink.coffee_mud.core;
import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class CMath
{
    private CMath(){super();}
    private static CMath inst=new CMath();
    public static CMath instance(){return inst;}
    
    
    public static long absDiff(long x, long y)
    {
        long d=x-y;
        if(d<0) return d*-1;
        return d;
    }
    
    public static boolean isNumber(String s)
    {
        if(s==null) return false;
        s=s.trim();
        if(s.length()==0) return false;
        if((s.length()>1)&&(s.startsWith("-")))
            s=s.substring(1);
        for(int i=0;i<s.length();i++)
            if("0123456789.,".indexOf(s.charAt(i))<0)
                return false;
        return true;
    }
    
    public static double div(double a, double b)
    {
        return a/b;
    }
    public static double div(double a, int b)
    {
        return a/new Integer(b).doubleValue();
    }
    public static double div(int a, double b)
    {
        return new Integer(a).doubleValue()/b;
    }
    public static double div(double a, long b)
    {
        return a/new Long(b).doubleValue();
    }
    public static double div(long a, double b)
    {
        return new Long(a).doubleValue()/b;
    }
    
    public static double mul(double a, double b)
    {
        return a*b;
    }
    public static double mul(double a, int b)
    {
        return a*new Integer(b).doubleValue();
    }
    public static double mul(int a, double b)
    {
        return new Integer(a).doubleValue()*b;
    }
    public static double mul(double a, long b)
    {
        return a*new Long(b).doubleValue();
    }
    public static double mul(long a, double b)
    {
        return new Long(a).doubleValue()*b;
    }
    public static long mul(long a, long b)
    {
        return a*b;
    }
    public static int mul(int a, int b)
    {
        return a*b;
    }
    public static double div(long a, long b)
    {
        return new Long(a).doubleValue()/new Long(b).doubleValue();
    }
    public static double div(int a, int b)
    {
        return new Integer(a).doubleValue()/new Integer(b).doubleValue();
    }
    public static int pow(int x, int y)
    {
        return (int)Math.round(Math.pow(new Integer(x).doubleValue(),new Integer(y).doubleValue()));
    }
    public static int squared(int x)
    {
        return (int)Math.round(Math.pow(new Integer(x).doubleValue(),new Integer(x).doubleValue()));
    }
    public static boolean bset(int num, int bitmask)
    {
        return ((num&bitmask)==bitmask);
    }
    public static boolean bset(long num, long bitmask)
    {
        return ((num&bitmask)==bitmask);
    }
    public static boolean bset(long num, int bitmask)
    {
        return ((num&bitmask)==bitmask);
    }
    public static int setb(int num, int bitmask)
    {
        return num|bitmask;
    }
    public static boolean banyset(int num, int bitmask)
    {
        return ((num&bitmask)>0);
    }
    public static boolean banyset(long num, long bitmask)
    {
        return ((num&bitmask)>0);
    }
    public static boolean banyset(long num, int bitmask)
    {
        return ((num&bitmask)>0);
    }
    public static long setb(long num, int bitmask)
    {
        return num|bitmask;
    }
    public static long setb(long num, long bitmask)
    {
        return num|bitmask;
    }
    public static int unsetb(int num, int bitmask)
    {
        if(bset(num,bitmask))
            num-=bitmask;
        return num;
    }
    public static long unsetb(long num, long bitmask)
    {
        if(bset(num,bitmask))
            num-=bitmask;
        return num;
    }
    public static long unsetb(long num, int bitmask)
    {
        if(bset(num,bitmask))
            num-=bitmask;
        return num;
    }
    public static boolean isSet(int number, int bitnumber)
    {
        if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
            return true;
        return false;
    }
    public static boolean isSet(long number, int bitnumber)
    {
        if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
            return true;
        return false;
    }
    

    /** Convert an integer to its Roman Numeral equivalent
     * 
     * <br><br><b>Usage:</b> Return=MiscFunc.convertToRoman(Number)+".";
     * @param i Integer to convert
     * 
     * @return String Converted integer
     */
    public static String convertToRoman(int i)
    {
        String Roman="";
        String Hundreds[]={"C","CC","CCC","CD","D","DC","DCC","DCCC","CM","P"};
        String Tens[]={"X","XX","XXX","XL","L","LX","LXX","LXXX","XC","C"};
        String Ones[]={"I","II","III","IV","V","VI","VII","VIII","IX","X"};
        if(i>1000)
        {
            Roman="Y";
            i=i%1000;
        }
        if(i>=100)
        {
            int x=i%100;
            int y=Math.round((i-x)/100);
            if(y>0)
                Roman+=Hundreds[y-1];
            i=x;
        }
        if(i>=10)
        {
            int x=i%10;
            int y=Math.round((i-x)/10);
            if(y>0)
                Roman+=Tens[y-1];
        }
        i=i%10;
        if(i>0)
            Roman+=Ones[i-1];
        return Roman;
    }
    
    /**
     * Returns the long value of a string without crashing
     * 
     * <br><br><b>Usage:</b> lSize = WebIQBase.s_long(WebIQBase.getRes(AttStatsRes,"BlobSize"));
     * @param LONG String to convert
     * @return long Long value of the string
     */
    public static long s_long(String LONG)
    {
        long slong=0;
        try{ slong=Long.parseLong(LONG); }
        catch(Exception e){ return 0;}
        return slong;
    }
    
    /**
     * Returns the floating point value of a string without crashing
     * 
     * <br><br><b>Usage:</b> lSize = WebIQBase.s_float(WebIQBase.getRes(AttStatsRes,"BlobSize"));
     * @param FLOAT String to convert
     * @return Float value of the string
     */
    public static float s_float(String FLOAT)
    {
        float sfloat=(float)0.0;
        try{ sfloat=Float.parseFloat(FLOAT); }
        catch(Exception e){ return 0;}
        return sfloat;
    }
    
    /**
     * Returns the double value of a string without crashing
     * 
     * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
     * @param DOUBLE String to convert
     * @return double Double value of the string
     */
    public static double s_double(String DOUBLE)
    {
        double sdouble=0;
        try{ sdouble=Double.parseDouble(DOUBLE); }
        catch(Exception e){ return 0;}
        return sdouble;
    }
    
    
    public static int abs(int val)
    {
        if(val>=0) return val;
        return val*-1;
    }
    
    public static long abs(long val)
    {
        if(val>=0) return val;
        return val*-1;
    }
    
    /**
     * Returns the boolean value of a string without crashing
     * 
     * <br><br><b>Usage:</b> int num=s_bool(CMD.substring(14));
     * @param BOOL Boolean value of string
     * @return int Boolean value of the string
     */
    public static boolean s_bool(String BOOL)
    {
        return Boolean.valueOf(BOOL).booleanValue(); 
    }
    /**
     * Returns the integer value of a string without crashing
     * 
     * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
     * @param INT Integer value of string
     * @return int Integer value of the string
     */
    public static int s_int(String INT)
    {
        int sint=0;
        try{ sint=Integer.parseInt(INT); }
        catch(Exception e){ return 0;}
        return sint;
    }
    
    public static boolean isLong(String INT){return isInteger(INT);}
    public static boolean isInteger(String INT)
    {
        if(INT.length()==0) return false;
        if(INT.startsWith("-")&&(INT.length()>1))
            INT=INT.substring(1);
        for(int i=0;i<INT.length();i++)
            if(!Character.isDigit(INT.charAt(i)))
                return false;
        return true;
    }
    
    public static boolean isFloat(String DBL){return isDouble(DBL);}
    public static boolean isDouble(String DBL)
    {
        if(DBL.length()==0) return false;
        if(DBL.startsWith("-")&&(DBL.length()>1))
            DBL=DBL.substring(1);
        boolean alreadyDot=false;
        for(int i=0;i<DBL.length();i++)
            if(!Character.isDigit(DBL.charAt(i)))
            {
                if(DBL.charAt(i)=='.')
                {
                    if(alreadyDot)
                        return false;
                    alreadyDot=true;
                }
                else
                    return false;
            }
        return alreadyDot;
    }
    
    public long round(double d){return Math.round(d);}
    public long round(float d){return Math.round(d);}
    public double abs(double d){return Math.abs(d);}
    public float abs(float d){return Math.abs(d);}
    public double random(){return Math.random();}
    public double floor(double d){return Math.floor(d);}
    public float floor(float d){return (float)Math.floor(d);}
    public double ceiling(double d){return Math.ceil(d);}
    public float ceiling(float d){return (float)Math.ceil(d);}
    public double sqrt(double d){return Math.sqrt(d);}
    public float sqrt(float d){return (float)Math.sqrt(d);}
}
