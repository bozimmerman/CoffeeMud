package com.planet_ink.coffee_mud.core;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class CMStrings
{
    private CMStrings(){super();}
    private static CMStrings inst=new CMStrings();
    public static CMStrings instance(){return inst;}
    
    public final static String SPACES="                                                                     ";
    public static String repeat(String str1, int times)
    {
        if(times<=0) return "";
        StringBuffer str=new StringBuffer("");
        for(int i=0;i<times;i++)
            str.append(str1);
        return str.toString();
    }
    
    public static String stripPunctuation(String str)
    {
        if(str.length()==0) return str;
        for(int x=str.length()-1;x>=0;x--)
            if("`~!@#$%^&*()_-+=[]{}\\|;:'\",<.>/?".indexOf(str.charAt(x))>=0)
                str=str.substring(0,x)+str.substring(x+1);
        return str;
    }
    public static String endWithAPeriod(String str)
    {
        if(str.length()==0) return str;
        int x=str.length()-1;
        while((x>=0)
        &&((Character.isWhitespace(str.charAt(x)))
            ||((x>0)&&((str.charAt(x)!='^')&&(str.charAt(x-1)=='^')&&((--x)>=0))))) 
                x--;
        if(x<0) return str;
        if(str.charAt(x)=='.') return str.trim()+" ";
        return str.substring(0,x+1)+". "+str.substring(x+1).trim();
    }
    
    public static String startWithAorAn(String str)
    {
        if(str.length()==0) 
            return str;
        if((!str.toUpperCase().startsWith("A "))
        &&(!str.toUpperCase().startsWith("AN "))
        &&(!str.toUpperCase().startsWith("THE "))
        &&(!str.toUpperCase().startsWith("SOME ")))
        {
            if("aeiouAEIOU".indexOf(str.charAt(0))>=0) 
                return "an "+str;
            return "a "+str;
        }
        return str;
    }
    
    
    public static boolean isVowel(char c)
    { return (("aeiou").indexOf(Character.toLowerCase(c))>=0);}
    
    public static String replaceAll(String str, String thisStr, String withThisStr)
    {
        if((str==null)
        ||(thisStr==null)
        ||(withThisStr==null)
        ||(str.length()==0)
        ||(thisStr.length()==0))
            return str;
        for(int i=str.length()-1;i>=0;i--)
        {
            if(str.charAt(i)==thisStr.charAt(0))
                if(str.substring(i).startsWith(thisStr))
                    str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
        }
        return str;
    }
    public static String replaceFirst(String str, String thisStr, String withThisStr)
    {
        if((str==null)
        ||(thisStr==null)
        ||(withThisStr==null)
        ||(str.length()==0)
        ||(thisStr.length()==0))
            return str;
        for(int i=str.length()-1;i>=0;i--)
        {
            if(str.charAt(i)==thisStr.charAt(0))
                if(str.substring(i).startsWith(thisStr))
                {
                    str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
                    return str;
                }
        }
        return str;
    }
    
    public static String capitalizeAndLower(String name)
    {
        return (Character.toUpperCase(name.charAt(0))+name.substring(1).toLowerCase()).trim();
    }
    public static String capitalizeFirstLetter(String name)
    {
        return (Character.toUpperCase(name.charAt(0))+name.substring(1)).trim();
    }
    
    public static String lastWordIn(String thisStr)
    {
        int x=thisStr.lastIndexOf(' ');
        if(x>=0)
            return thisStr.substring(x+1);
        return thisStr;
    }
    
    public static String removeColors(String s)
    {
        StringBuffer str=new StringBuffer(s);
        int colorStart=-1;
        for(int i=0;i<str.length();i++)
        {
            switch(str.charAt(i))
            {
            case 'm':
                if(colorStart>=0)
                {
                    str.delete(colorStart,i+1);
                    colorStart=-1;
                }
                break;
            case (char)27: colorStart=i; break;
            case '^':
                if((i+1)<str.length())
                {
                    int tagStart=i;
                    char c=str.charAt(i+1);
                    if((c=='<')||(c=='&'))
                    {
                        i+=2;
                        while(i<(str.length()-1))
                        {
                            if(((c=='<')&&((str.charAt(i)!='^')||(str.charAt(i+1)!='>')))
                            ||((c=='&')&&(str.charAt(i)!=';')))
                            {
                                i++;
                                if(i>=(str.length()-1))
                                {
                                    i=tagStart;
                                    str.delete(i,i+2); 
                                    i--;
                                    break;
                                }
                            }
                            else
                            {
                                if(c=='<')
                                    str.delete(tagStart,i+2);
                                else
                                    str.delete(tagStart,i+1);
                                i=tagStart-1;
                                break;
                            }
                        }
                    }
                    else
                    {
                        str.delete(i,i+2); 
                        i--;
                    }
                }
                else
                {
                    str.delete(i,i+2); 
                    i--;
                }
                break;
            }
        }
        return str.toString();
    }
    
    public static int lengthMinusColors(String thisStr)
    {
        int size=0;
        for(int i=0;i<thisStr.length();i++)
        {
            if(thisStr.charAt(i)=='^')
            {
                i++;
                if((i+1)<thisStr.length())
                {
                    int tagStart=i;
                    char c=thisStr.charAt(i);
                    if((c=='<')||(c=='&'))
                    while(i<(thisStr.length()-1))
                    {
                        if(((c=='<')&&((thisStr.charAt(i)!='^')||(thisStr.charAt(i+1)!='>')))
                        ||((c=='&')&&(thisStr.charAt(i)!=';')))
                        {
                            i++;
                            if(i>=(thisStr.length()-1))
                            {
                                i=tagStart+1;
                                break;
                            }
                        }
                        else
                        {
                            i++;
                            break;
                        }
                    }
                }
            }
            else
                size++;
        }
        return size;
    }
    
    public static String padCenter(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr).substring(0,thisMuch);
        int size=(thisMuch-lenMinusColors)/2;
        int rest=thisMuch-lenMinusColors-size;
        if(rest<0) rest=0;
        return SPACES.substring(0,size)+thisStr+SPACES.substring(0,rest);
    }
    public static String padLeft(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr).substring(0,thisMuch);
        return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
    }
    public static String padLeft(String thisStr, String colorPrefix, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return colorPrefix+removeColors(thisStr).substring(0,thisMuch);
        return SPACES.substring(0,thisMuch-lenMinusColors)+colorPrefix+thisStr;
    }
    public static String padRight(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr).substring(0,thisMuch);
        return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
    }
    public static String limit(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr).substring(0,thisMuch);
        return thisStr;
    }
    public static String padRight(String thisStr, String colorSuffix, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr).substring(0,thisMuch)+colorSuffix;
        return thisStr+colorSuffix+SPACES.substring(0,thisMuch-lenMinusColors);
    }
    public static String padRightPreserve(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr);
        return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
    }
    public static String centerPreserve(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr);
        int left=(thisMuch-lenMinusColors)/2;
        int right=((left+left+lenMinusColors)<thisMuch)?left+1:left;
        return SPACES.substring(0,left)+thisStr+SPACES.substring(0,right);
    }
    public static String padLeftPreserve(String thisStr, int thisMuch)
    {
        int lenMinusColors=lengthMinusColors(thisStr);
        if(lenMinusColors>thisMuch)
            return removeColors(thisStr);
        return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
    }
    
    public static String sameCase(String str, char c)
    {
        if(Character.isUpperCase(c))
            return str.toUpperCase();
        return str.toLowerCase();
    }
    
}
