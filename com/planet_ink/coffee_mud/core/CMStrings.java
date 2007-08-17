package com.planet_ink.coffee_mud.core;
import java.util.*;

/* 
   Copyright 2000-2007 Bo Zimmerman

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
        if((str==null)||(str.length()==0)) return str;
        for(int x=str.length()-1;x>=0;x--)
            if("`~!@#$%^&*()_-+=[]{}\\|;:'\",<.>/?".indexOf(str.charAt(x))>=0)
                str=str.substring(0,x)+str.substring(x+1);
        return str;
    }
    public static String endWithAPeriod(String str)
    {
        if((str==null)||(str.length()==0)) return str;
        int x=str.length()-1;
        while((x>=0)
        &&((Character.isWhitespace(str.charAt(x)))
            ||((x>0)&&((str.charAt(x)!='^')&&(str.charAt(x-1)=='^')&&((--x)>=0))))) 
                x--;
        if(x<0) return str;
        if((str.charAt(x)=='.')||(str.charAt(x)=='!')||(str.charAt(x)=='?')) 
        	return str.trim()+" ";
        return str.substring(0,x+1)+". "+str.substring(x+1).trim();
    }
    
    public static String bytesToStr(byte[] b){ if(b==null) return ""; try{ return new String(b,"iso-8859-1");}catch(Exception e){return new String(b);}}
    public static byte[] strToBytes(String str){ try{ return str.getBytes("iso-8859-1");}catch(Exception e){return str.getBytes();}}
    
    public static String startWithAorAn(String str)
    {
        if((str==null)||(str.length()==0)) return str;
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
    
    
    public static String insertAdjective(String str, String adjective)
    {
        if(str.length()==0) 
            return str;
        str=str.trim();
        if((str.toUpperCase().startsWith("A "))
        ||(str.toUpperCase().startsWith("AN ")))
        {
            if("aeiouAEIOU".indexOf(adjective.charAt(0))>=0) 
                return "an "+adjective+" "+str;
            return "a "+adjective+" "+str;
        }
        if((!str.toUpperCase().startsWith("THE "))
        &&(!str.toUpperCase().startsWith("SOME ")))
        {
            if("aeiouAEIOU".indexOf(adjective.charAt(0))>=0) 
                return "an "+adjective+" "+str;
            return "a "+adjective+" "+str;
        }
        int x=str.indexOf(' ');
        return str.substring(0,x)+" "+adjective+" "+str.substring(x+1);
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
    
    public static String replaceWord(String str, String thisStr, String withThisStr)
    {
        if((str==null)
        ||(thisStr==null)
        ||(withThisStr==null)
        ||(str.length()==0)
        ||(thisStr.length()==0))
            return str;
        withThisStr=withThisStr.toUpperCase();
        for(int i=str.length()-1;i>=0;i--)
        {
            if((str.charAt(i)==thisStr.charAt(0))
            &&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
                if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
                &&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
                {
                	String oldWord=str.substring(i,i+thisStr.length());
                	if(oldWord.toUpperCase().equals(oldWord)) 
                        str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
                	else
                	if(oldWord.toLowerCase().equals(oldWord))
                        str=str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
                	else
                	if((oldWord.length()>0)&&(Character.isUpperCase(oldWord.charAt(0)))) 
                        str=str.substring(0,i)+withThisStr.charAt(0)+withThisStr.substring(1).toLowerCase()+str.substring(i+thisStr.length());
                	else
                        str=str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
                }
        }
        return str;
    }
    
    public static String replaceFirstWord(String str, String thisStr, String withThisStr)
    {
        if((str==null)
        ||(thisStr==null)
        ||(withThisStr==null)
        ||(str.length()==0)
        ||(thisStr.length()==0))
            return str;
        withThisStr=withThisStr.toUpperCase();
        for(int i=str.length()-1;i>=0;i--)
        {
            if((str.charAt(i)==thisStr.charAt(0))
            &&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
                if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
                &&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
                {
                	String oldWord=str.substring(i,i+thisStr.length());
                	if(oldWord.toUpperCase().equals(oldWord)) 
                        return str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
                	else
                	if(oldWord.toLowerCase().equals(oldWord))
                		return str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
                	else
                	if((oldWord.length()>0)&&(Character.isUpperCase(oldWord.charAt(0)))) 
                		return str.substring(0,i)+withThisStr.charAt(0)+withThisStr.substring(1).toLowerCase()+str.substring(i+thisStr.length());
                	else
                		return str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
                }
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
    	if((name==null)||(name.length()==0)) return "";
        return (Character.toUpperCase(name.charAt(0))+name.substring(1).toLowerCase()).trim();
    }
    public static String capitalizeFirstLetter(String name)
    {
    	if((name==null)||(name.length()==0)) return "";
        return (Character.toUpperCase(name.charAt(0))+name.substring(1)).trim();
    }
    
    public static String lastWordIn(String thisStr)
    {
        int x=thisStr.lastIndexOf(' ');
        if(x>=0)
            return thisStr.substring(x+1);
        return thisStr;
    }
    
    public static String getSayFromMessage(String msg)
    {
        if(msg==null) return null;
        int start=msg.indexOf("'");
        int end=msg.lastIndexOf("'");
        if((start>0)&&(end>start))
            return msg.substring(start+1,end);
        return null;
    }
    public static String substituteSayInMessage(String affmsg, String msg)
    {
        if(affmsg==null) return null;
        int start=affmsg.indexOf("'");
        int end=affmsg.lastIndexOf("'");
        if((start>0)&&(end>start))
            return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
        return affmsg;
    }

    public static boolean containsIgnoreCase(String[] strs, String str)
    {
    	if((str==null)||(strs==null)) return false;
    	for(int s=0;s<strs.length;s++)
    		if(strs[s].equalsIgnoreCase(str))
    			return true;
    	return false;
    }
    
    public static boolean compareStringArrays(String[] A1, String[] A2)
    {
        if(((A1==null)||(A1.length==0))
        &&((A2==null)||(A2.length==0)))
            return true;
        if((A1==null)||(A2==null)) return false;
        if(A1.length!=A2.length) return false;
        for(int i=0;i<A1.length;i++)
        {
            boolean found=false;
            for(int i2=0;i2<A2.length;i2++)
                if(A1[i].equalsIgnoreCase(A2[i]))
                { found=true; break;}
            if(!found) return false;
        }
        return true;
    }
    
    public static boolean contains(String[] strs, String str)
    {
    	if((str==null)||(strs==null)) return false;
    	for(int s=0;s<strs.length;s++)
    		if(strs[s].equals(str))
    			return true;
    	return false;
    }
    
    public static String removeColors(String s)
    {
        if(s==null) return "";
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
        if(thisStr==null) return 0;
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
    
    public static Hashtable makeNumericHash(Object[] obj)
    {
    	Hashtable H=new Hashtable();
    	for(int i=0;i<obj.length;i++)
    		H.put(obj[i],new Integer(i));
    	return H;
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
