package com.planet_ink.siplet.support;
import java.io.*;
import java.util.*;

import com.planet_ink.siplet.applet.Siplet;

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
public class TelnetFilter
{
    
    protected static final char IAC_ = 255;
    protected static final char IAC_DO = 253;
    protected static final char IAC_WILL = 251;
    protected static final char IAC_WONT = 252;
    protected static final char IAC_DONT = 254;
    protected static final char IAC_MSP = 90;
    protected static final char IAC_MXP = 91;
    protected static final char TELOPT_EOR = 25;
    protected static final char TELOPT_ECHO = 1;
    protected static final char TELOPT_NAWS = 31;
    protected static final char TELOPT_TTYPE = 24;
    protected static final char TELOPT_TSPEED = 32;
    protected static final char MCCP_COMPRESS = 85;
    protected static final char MCCP_COMPRESS2 = 86;
    protected static final char TELOPT_NEWENVIRONMENT = 39;

    private static String defaultBackground="BK";
    private static String defaultForeground="WH";
    private static String[] colorCodes1={ // 30-37
            "BK","RD","GR","BR","BL","PU","CY","GY"};
    private static String[] colorCodes2={ 
            "DG","LR","LG","YL","LB","LP","LC","WH"};
    
    protected String lastBackground=null;
    protected String lastForeground=null;
    protected boolean blinkOn=false;
    protected boolean fontOn=false;
    protected boolean boldOn=false;
    protected boolean underlineOn=false;
    protected boolean italicsOn=false;
    private Siplet codeBase=null;

    protected boolean neverSupportMSP=false;
    protected boolean neverSupportMXP=true;
    protected boolean MSPsupport=false;
    protected boolean MXPsupport=false;
    
    private MSP mspPlayer=new MSP();
    
    private TelnetFilter(){};
    public TelnetFilter(Siplet codebase)
    {
        codeBase=codebase;
    }
    
    public boolean MSPsupport(){return MSPsupport;}
    public void setMSPSupport(boolean truefalse){MSPsupport=truefalse;}
    public boolean MXPsupport(){return MXPsupport;}
    public void setMXPSupport(boolean truefalse){MXPsupport=truefalse;}
    
    private String blinkOff(){ if(blinkOn){blinkOn=false; return "</BLINK>";}return ""; }
    private String underlineOff(){ if(underlineOn){underlineOn=false; return "</U>";}return ""; }
    private String fontOff()
    { 
        if(fontOn)
        {
            lastBackground=defaultBackground;
            lastForeground=defaultForeground;
            fontOn=false; 
            return "</FONT>";
        }
        return ""; 
    }
    private String italicsOff(){ if(italicsOn){italicsOn=false; return "</I>";}return ""; }
    private String allOff()
    {
        StringBuffer off=new StringBuffer("");
        off.append(blinkOff());
        off.append(underlineOff());
        off.append(fontOff());
        off.append(italicsOff());
        return off.toString();
    }
    private String escapeTranslate(String escapeString)
    {
        if(escapeString.endsWith("m"))
        {
            Vector V=Util.parseSemicolons(escapeString.substring(0,escapeString.length()-1),true);
            StringBuffer str=new StringBuffer("");
            String s=null;
            int code=0;
            String background=null;
            String foreground=null;
            for(int i=0;i<V.size();i++)
            {
                s=(String)V.elementAt(i);
                code=Util.s0_int(s);
                switch(code)
                {
                case 0: 
                        if(i==(V.size()-1))
                            str.append(allOff());
                        boldOn=false;
                        break;
                case 1: boldOn=true; break;
                case 4: 
                    {
                        if(!underlineOn)
                        {
                            underlineOn=true;
                            str.append("<U>");
                        }
                        break;
                    }
                case 5: 
                {
                    if(!blinkOn)
                    {
                        blinkOn=true;
                        str.append("<BLINK>");
                    }
                    break;
                }
                case 6: 
                {
                    if(!italicsOn)
                    {
                        italicsOn=true;
                        str.append("<I>");
                    }
                    break;
                }
                case 7: 
                {
                    // this is reverse on, and requires a wierd color reversal
                    // from whatever the previous colors were.
                    // do it later
                    break;
                }
                case 8: 
                {
                    background=defaultBackground;
                    foreground=defaultBackground;
                    break;
                }
                case 22:
                    str.append(allOff());
                    break;
                case 24:
                    str.append(underlineOff());
                    break;
                case 25:
                    str.append(blinkOff());
                    break;
                case 26:
                    str.append(italicsOff());
                    break;
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                    foreground=boldOn?colorCodes2[code-30]:colorCodes1[code-30];
                    break;
                case 39:
                    foreground=defaultForeground;
                    break;
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                    background=colorCodes1[code-40];
                    break;
                case 49:
                    background=defaultForeground;
                    break;
                }
                if((background!=null)||(foreground!=null))
                {
                    if(lastBackground==null)lastBackground=defaultBackground;
                    if(lastForeground==null)lastForeground=defaultForeground;
                    if(background==null) background=lastBackground;
                    if(foreground==null) foreground=defaultForeground;
                    if((!background.equals(lastBackground))
                    ||(!foreground.equals(lastForeground)))
                    {
                        str.append(fontOff());
                        lastBackground=background;
                        lastForeground=foreground;
                        fontOn=true;
                        str.append("<FONT CLASS="+background+foreground+">");
                    }
                }
            }
            return str.toString();
        }
        return "";
    }
    
    public int TelenetFilter(StringBuffer buf, DataOutputStream response)
    throws IOException
    {
        int i=0;
        while(i<buf.length())
        {
            switch(buf.charAt(i))
            {
            case IAC_:
                {
                    if(i>=buf.length()-3)
                        return i;
                    int oldI=i;
                    switch(buf.charAt(++i))
                    {
                    case IAC_WILL:
                        i++;
                        if(buf.charAt(i)==IAC_MSP)
                        {
                            if(neverSupportMSP)
                            {
                                if(MSPsupport())
                                {
                                    response.writeBytes(""+IAC_+IAC_DONT+IAC_MSP);
                                    setMSPSupport(false);
                                }
                            }
                            else
                            if(!MSPsupport())
                            {
                                response.writeBytes(""+IAC_+IAC_DO+IAC_MSP);
                                setMSPSupport(true);
                            }
                            response.flush();
                        }
                        else
                        if(buf.charAt(i)==IAC_MXP)
                        {
                            if(neverSupportMXP)
                            {
                                if(MXPsupport())
                                {
                                    response.writeBytes(""+IAC_+IAC_DONT+IAC_MXP);
                                    setMXPSupport(false);
                                }
                            }
                            else
                            if(!MXPsupport())
                            {
                                response.writeBytes(""+IAC_+IAC_DO+IAC_MXP);
                                response.flush();
                                setMXPSupport(true);
                            }
                        }
                        else
                        {
                            response.writeBytes(""+IAC_+IAC_DONT+buf.charAt(i));
                            response.flush();
                        }
                        break;
                    case IAC_WONT:
                        i++;
                        if(buf.charAt(i)==IAC_MSP)
                        {
                            if(MSPsupport())
                            {
                                response.writeBytes(""+IAC_+IAC_DONT+IAC_MSP);
                                setMSPSupport(false);
                            }
                        }
                        else
                        if(buf.charAt(i)==IAC_MXP)
                        {
                            if(MXPsupport())
                            {
                                response.writeBytes(""+IAC_+IAC_DONT+IAC_MXP);
                                setMXPSupport(false);
                            }
                        }
                        break;
                    }
                    buf.delete(oldI,oldI+3);
                    i=oldI-1;
                    break;
                }
            }
            i++;
        }
        return buf.length();
    }
    
    // filters out color codes -> <FONT>
    // CRS -> <BR>
    // SPACES -> &nbsp;
    // < -> &lt;
    // TELNET codes -> response outputstream
    public int HTMLFilter(StringBuffer buf)
    {
        int i=0;
        while(i<buf.length())
        {
            switch(buf.charAt(i))
            {
            case '!':
                if((i<buf.length()-3)
                &&(buf.charAt(i+1)=='!'))
                {
                    int endl=mspPlayer.process(buf,i,codeBase);
                    if(endl==-1)
                        i--;
                    else
                    if(endl>0)
                        return endl;
                }
            case ' ':
                buf.setCharAt(i,'&');
                buf.insert(i+1,"nbsp;");
                i+=5;
                break;
            case '>':
                buf.setCharAt(i,'&');
                buf.insert(i+1,"gt;");
                i+=3;
                break;
            case '<':
                buf.setCharAt(i,'&');
                buf.insert(i+1,"lt;");
                i+=3;
                break;
            case '\n':
                buf.setCharAt(i,'<');
                buf.insert(i+1,"BR>");
                i+=3;
                break;
            case '\r':
                buf.deleteCharAt(i);
                i--;
                break;
            case IAC_:
            {
                if(i>=buf.length()-3)
                    return i;
                break;
            }
            case '\033':
                {
                    int savedI=i;
                    if(i==buf.length()-1)
                        return i;
                    if(buf.charAt(++i)!='[')
                        buf.setCharAt(i,' ');
                    else
                    {
                        boolean quote=false;
                        while(((++i)<buf.length())
                        &&((quote)||(!Character.isLetter(buf.charAt(i)))))
                            if(buf.charAt(i)=='"')
                                quote=!quote;
                        if(i==buf.length())
                            return savedI;
                        String translate=escapeTranslate(buf.substring(savedI+2,i+1));
                        buf.replace(savedI,i+1,translate);
                        i=savedI+translate.length()-1;
                    }
                }
                break;
            }
            i++;
        }
        return buf.length();
    }
}
