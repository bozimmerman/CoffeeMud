package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.io.*;

/*
   Copyright 2000-2008 Bo Zimmerman

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
            int y=(i-x)/100;
            if(y>0)
                roman.append(ROMAN_HUNDREDS[y-1]);
            i=x;
        }
        if(i>=10)
        {
            int x=i%10;
            int y=(i-x)/10;
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
     * @param s String to convert
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
        return a/((double)b);
    }
    public static double div(int a, double b)
    {
        return ((double)a)/b;
    }
    public static double div(double a, long b)
    {
        return a/((double)b);
    }
    public static double div(long a, double b)
    {
        return ((double)a)/b;
    }

    public static double mul(double a, double b)
    {
        return a*b;
    }
    public static double mul(double a, int b)
    {
        return a*((double)b);
    }
    public static double mul(int a, double b)
    {
        return ((double)a)*b;
    }
    public static double mul(double a, long b)
    {
        return a*((double)b);
    }
    public static double mul(long a, double b)
    {
        return ((double)a)*b;
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
        return ((double)a)/((double)b);
    }
    public static double div(int a, int b)
    {
        return ((double)a)/((double)b);
    }
    public static long pow(long x, long y)
    {
        return Math.round(Math.pow(((double)x),((double)y)));
    }
    public static int squared(int x)
    {
        return (int)Math.round(Math.pow(((double)x),2.0));
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
    	while(s.trim().endsWith("%")) s=s.trim().substring(0,s.length()-1).trim();
    	return s_double(s)/100.0;
    }
    public static String toPct(double d)
    {
        java.text.DecimalFormat twoPlaces = new java.text.DecimalFormat("0.#####%");
        String s=twoPlaces.format(d);
        if(s.endsWith("%%")) return s.substring(0,s.length()-1);
        return s;

    }
    public static String toPct(String s) { return toPct(s_pct(s)); }

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
    public static boolean isMathExpression(String st, double[] vars){
        if((st==null)||(st.length()==0)) return false;
        try{ parseMathExpression(st,vars);}catch(Exception e){ return false;}
        return true;
    }
    public static double s_parseMathExpression(String st){ try{ return parseMathExpression(st);}catch(Exception e){ return 0.0;}}
    public static double s_parseMathExpression(String st, double[] vars){ try{ return parseMathExpression(st,vars);}catch(Exception e){ return 0.0;}}
    public static long s_parseLongExpression(String st){ try{ return parseLongExpression(st);}catch(Exception e){ return 0;}}
    public static long s_parseLongExpression(String st, double[] vars){ try{ return parseLongExpression(st,vars);}catch(Exception e){ return 0;}}
    public static int s_parseIntExpression(String st){ try{ return parseIntExpression(st);}catch(Exception e){ return 0;}}
    public static int s_parseIntExpression(String st, double[] vars){ try{ return parseIntExpression(st,vars);}catch(Exception e){ return 0;}}

    private static double parseMathExpression(StreamTokenizer st, boolean inParen, double[] vars)
        throws ArithmeticException
    {
        if(!inParen) {
            st.ordinaryChar('/');
            st.ordinaryChar('x');
            st.ordinaryChar('X');
        }
        double finalValue=0;
        try{
            int c=st.nextToken();
            char lastOperation='+';
            while(c!=StreamTokenizer.TT_EOF)
            {
                double curValue=0.0;
                switch(c)
                {
                case StreamTokenizer.TT_NUMBER:
                    curValue=st.nval;
                    break;
                case '(':
                    curValue=parseMathExpression(st,true,vars);
                    break;
                case ')':
                    if(!inParen)
                        throw new ArithmeticException("')' is an unexpected token.");
                    return finalValue;
                case '@':
                {
                    c=st.nextToken();
                    if((c!='x')&&(c!='X'))
                        throw new ArithmeticException("'"+c+"' is an unexpected token after @.");
                    c=st.nextToken();
                    if(c!=StreamTokenizer.TT_NUMBER)
                        throw new ArithmeticException("'"+c+"' is an unexpected token after @x.");
                    if(vars==null)
                        throw new ArithmeticException("vars have not been defined for @x"+st.nval);
                    if((st.nval>vars.length)||(st.nval<1.0))
                        throw new ArithmeticException("'"+st.nval+"/"+vars.length+"' is an illegal variable reference.");
                    curValue=vars[((int)st.nval)-1];
                    break;
                }
                case '+':
                case '-':
                case '*':
                case '\\':
                case '/':
                case '?':
                {
                    lastOperation=(char)c;
                    c=st.nextToken();
                    continue;
                }
                default:
                    throw new ArithmeticException("'"+c+"' is an illegal expression.");
                }
                switch(lastOperation)
                {
                case '+': finalValue+=curValue; break;
                case '-': finalValue-=curValue; break;
                case '*': finalValue*=curValue; break;
                case '/':
                case '\\': finalValue/=curValue; break;
                case '?': finalValue=((curValue-finalValue)*Math.random())+finalValue;
                }
                c=st.nextToken();
            }
        }
        catch(IOException e){}
        if(inParen)
            throw new ArithmeticException("')' was missing from this expression");
        return finalValue;
    }

    public static long parseLongExpression(String formula)
    {return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,null));}
    public static long parseLongExpression(String formula, double[] vars)
    {return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,vars));}

    public static int parseIntExpression(String formula) throws ArithmeticException
    {return (int)Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,null));}
    public static int parseIntExpression(String formula, double[] vars) throws ArithmeticException
    {return (int)Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,vars));}

    public static double parseMathExpression(String formula) throws ArithmeticException
    {return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,null);}
    public static double parseMathExpression(String formula, double[] vars) throws ArithmeticException
    {return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,vars);}


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

    /**
     * Returns whether the given string is a long value
     *
     * <br><br><b>Usage:</b> if(isLong(CMD.substring(14)));
     * @param LONG Long value of string
     * @return whether it is a long
     */
    public static boolean isLong(String LONG){return isInteger(LONG);}
    
    /**
     * Returns whether the given string is a int value
     *
     * <br><br><b>Usage:</b> if(isInteger(CMD.substring(14)));
     * @param INT Integer value of string
     * @return whether it is a int
     */
    public static boolean isInteger(String INT)
    {
        if(INT==null) return false;
        if(INT.length()==0) return false;
        int i=0;
        if(INT.charAt(0)=='-')
            if(INT.length()>1)
                i++;
            else
                return false;
        for(;i<INT.length();i++)
            if(!Character.isDigit(INT.charAt(i)))
                return false;
        return true;
    }
    
    /**
     * Returns whether the given string is a float value
     *
     * <br><br><b>Usage:</b> if(isFloat(CMD.substring(14)));
     * @param DBL float value of string
     * @return whether it is a float
     */
    public static boolean isFloat(String DBL){return isDouble(DBL);}
    
    /**
     * Returns whether the given string is a double value
     *
     * <br><br><b>Usage:</b> if(isDouble(CMD.substring(14)));
     * @param DBL double value of string
     * @return whether it is a double
     */
    public static boolean isDouble(String DBL)
    {
        if(DBL==null) return false;
        if(DBL.length()==0) return false;
        int i=0;
        if(DBL.charAt(0)=='-')
            if(DBL.length()>1)
                i++;
            else
                return false;
        boolean alreadyDot=false;
        for(;i<DBL.length();i++)
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
