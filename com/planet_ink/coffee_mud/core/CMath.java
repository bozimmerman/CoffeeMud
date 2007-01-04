package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.io.*;

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
    private static final String[] ROMAN_HUNDREDS={"C","CC","CCC","CD","D","DC","DCC","DCCC","CM","P"};
    private static final String[] ROMAN_TENS={"X","XX","XXX","XL","L","LX","LXX","LXXX","XC","C"};
    private static final String[] ROMAN_ONES={"I","II","III","IV","V","VI","VII","VIII","IX","X"};
    private static final String   ROMAN_ALL="CDMPXLIV";
    
    
    /** Convert an integer to its Roman Numeral equivalent
     * 
     * Usage: Return=MiscFunc.convertToRoman(Number)+".";
     * @param i Integer to convert
     * 
     * @return String Converted integer
     */
    public static String convertToRoman(int i)
    {
        StringBuffer roman=new StringBuffer("");
        if(i>1000)
        {
            roman.append("Y");
            i=i%1000;
        }
        if(i>=100)
        {
            int x=i%100;
            int y=Math.round((i-x)/100);
            if(y>0)
                roman.append(ROMAN_HUNDREDS[y-1]);
            i=x;
        }
        if(i>=10)
        {
            int x=i%10;
            int y=Math.round((i-x)/10);
            if(y>0)
                roman.append(ROMAN_TENS[y-1]);
        }
        i=i%10;
        if(i>0)
            roman.append(ROMAN_ONES[i-1]);
        return roman.toString();
    }

    /** Convert an integer to its Roman Numeral equivalent
     * 
     * Usage: Return=MiscFunc.convertToRoman(Number)+".";
     * @param i Integer to convert
     * 
     * @return String Converted integer
     */
    public static int convertFromRoman(String s)
    {
        int x=0;
        while(s.startsWith("Y"))
            x+=1000;
        for(int i=ROMAN_HUNDREDS.length-1;i>=0;i--)
            if(s.startsWith(ROMAN_HUNDREDS[i]))
            {
                x+=(100*(i+1));
                break;
            }
        for(int i=ROMAN_TENS.length-1;i>=0;i--)
            if(s.startsWith(ROMAN_TENS[i]))
            {
                x+=(10*(i+1));
                break;
            }
        for(int i=ROMAN_ONES.length-1;i>=0;i--)
            if(s.startsWith(ROMAN_ONES[i]))
            {
                x+=i+1;
                break;
            }
        return x;
    }

    public static String numAppendage(int num)
	{
        if((num<11)||(num>13))
        {
            String strn=""+num;
            switch(strn.charAt(strn.length()-1))
            {
            case '1': return "st";
            case '2': return "nd";
            case '3': return "rd";
            }
        }
        return "th";
	}

    public static boolean isRomanDigit(char c){ return ROMAN_ALL.indexOf(c)>=0;}
    
    public static boolean isRomanNumeral(String s)
    {
        if(s==null) return false; 
        s=s.toUpperCase().trim();
        if(s.length()==0) return false; 
        for(int c=0;c<s.length();c++)
            if(!isRomanDigit(s.charAt(c)))
                return false;
        return true;
    }
    
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
    public static long pow(long x, long y)
    {
        return Math.round(Math.pow(new Long(x).doubleValue(),new Long(y).doubleValue()));
    }
    public static int squared(int x)
    {
        return (int)Math.round(Math.pow(new Integer(x).doubleValue(),new Integer(x).doubleValue()));
    }
    public static boolean bset(short num, short bitmask)
    {
        return ((num&bitmask)==bitmask);
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
    	int mask=(int)pow(2,bitnumber);
    	return ((number&mask)==mask);
    }
    public static boolean isPct(String s)
    {
        if(s==null) return false;
        s=s.trim();
        if(!s.endsWith("%")) return false;
        return CMath.isNumber(s.substring(0,s.length()-1));
    }
    public static double s_pct(String s)
    {
    	if(s==null) return 0.0;
    	if(s.trim().endsWith("%")) s=s.trim().substring(0,s.length()-1).trim();
    	return div(s_double(s),100.0);
    }
    
    public static boolean isSet(long number, int bitnumber)
    {
        if((number&(pow(2,bitnumber)))==(pow(2,bitnumber)))
            return true;
        return false;
    }

    public static boolean isMathExpression(String st){
    	if((st==null)||(st.length()==0)) return false;
    	try{ parseMathExpression(st);}catch(Exception e){ return false;}
    	return true;
    }
    public static double s_parseMathExpression(String st){ try{ return parseMathExpression(st);}catch(Exception e){ return 0.0;}}
    public static long s_parseLongExpression(String st){ try{ return parseLongExpression(st);}catch(Exception e){ return 0;}}
    public static int s_parseIntExpression(String st){ try{ return parseIntExpression(st);}catch(Exception e){ return 0;}}
    
    private static double parseMathExpression(StreamTokenizer st)
    	throws ArithmeticException
    {
		double finalValue=0;
		try{
			int c=st.nextToken();
			char lastOperation='+';
			while(c!=StreamTokenizer.TT_EOF)
			{
				double curValue=0.0;
				if(c==StreamTokenizer.TT_NUMBER)
					curValue=st.nval;
				else
				if(c=='(')
					curValue=parseMathExpression(st);
				else
				if(c==')')
					return finalValue;
				else
				if("+-*\\?".indexOf((char)c)>=0)
				{
					lastOperation=(char)c;
					c=st.nextToken();
					continue;
				}
				else
					throw new ArithmeticException("'"+c+"' is an illegal expression.");
				switch(lastOperation)
				{
				case '+': finalValue+=curValue; break;
				case '-': finalValue-=curValue; break;
				case '*': finalValue*=curValue; break;
				case '\\': finalValue/=curValue; break;
				case '?': finalValue=((curValue-finalValue)*Math.random())+finalValue;
				}
				c=st.nextToken();
			}
		}
		catch(IOException e){}
		return finalValue;
    }
    
    public static long parseLongExpression(String formula)
    {return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes())))));}
    
    public static int parseIntExpression(String formula) throws ArithmeticException
    {return (int)Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes())))));}
    
    public static double parseMathExpression(String formula) throws ArithmeticException
    {return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))));}
    
    
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
     * Returns whether the given string is a boolean value
     * 
     * <br><br><b>Usage:</b> if(isBool(CMD.substring(14)));
     * @param BOOL Boolean value of string
     * @return whether it is a boolean
     */
    public static boolean isBool(String BOOL)
    {
        return BOOL.equalsIgnoreCase("true")||BOOL.equalsIgnoreCase("false");
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
    /**
     * Returns the short value of a string without crashing
     * 
     * <br><br><b>Usage:</b> int num=s_short(CMD.substring(14));
     * @param SHORT Short value of string
     * @return short Short value of the string
     */
    public static short s_short(String SHORT)
    {
    	short sint=0;
        try{ sint=Short.parseShort(SHORT); }
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
