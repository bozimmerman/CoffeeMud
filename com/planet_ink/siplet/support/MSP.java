package com.planet_ink.siplet.support;
import java.applet.*;
import java.net.*;
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
public class MSP
{

    public MSP(){super();}
    
    private static Hashtable cache=new Hashtable();
    
    private String defMusicPath=null;
    private String defSoundPath=null;
    private String defPath=null;
    private MSPplayer musicClip = null;
    private MSPplayer soundClip = null;
    
    public String trimQuotes(String s)
    {
        s=s.trim();
        if(s.startsWith("\""))
        {
            s=s.substring(1);
            if(s.endsWith("\""))
                s=s.substring(0,s.length()-1);
        }
        return s.trim();
    }
    
    public int process(StringBuffer buf, int i, Applet applet)
    {
        int oldI=i;
        if(i+12>=buf.length()) return -2;
        String tag=buf.substring(i+2,i+7).toUpperCase();
        if((!tag.equals("SOUND"))&&(!tag.equals("MUSIC")))
            return -2;
        if(buf.charAt(i+7)!='(') return -2;
        i+=7;
        Vector parts=new Vector();
        StringBuffer part=new StringBuffer("");
        boolean done=false;
        while(((++i)<buf.length())&&(!done))
        {
            switch(buf.charAt(i))
            {
            case ')':
                if(part.length()>0)
                    parts.addElement(part.toString());
                done=true;
                break;
            case ' ':
                if(part.length()>0)
                    parts.addElement(part.toString());
                part.setLength(0);
                break;
            case '\n':
                if(part.length()>0)
                    parts.addElement(part.toString());
                done=true;
            default:
                part.append(buf.charAt(i));
                break;
            }
        }
        if(!done) return oldI;
        buf.delete(oldI,i+1);
        if(parts.size()==0) return -1;
        MSPplayer currentClip=tag.equals("MUSIC")?musicClip:soundClip;
        if((currentClip!=null)&&(!currentClip.playing))
        {
            if(tag.equals("MUSIC")) 
                musicClip=null;
            else
            if(tag.equals("SOUND")) 
                soundClip=null;
            currentClip=null;
        }
        if(((String)parts.firstElement()).equalsIgnoreCase("off"))
        {
            if(tag.equals("MUSIC")&&(musicClip!=null))
                musicClip.stopPlaying();
            if(tag.equals("SOUND")&&(soundClip!=null))
                soundClip.stopPlaying();
            for(int v=1;v<parts.size();v++)
            {
                String s=((String)parts.elementAt(v)).toUpperCase();
                if(s.startsWith("V="))
                {
                    s=trimQuotes(s.substring(2));
                    if(!s.endsWith("/")) s=s+"/";
                    if(tag.equals("MUSIC"))
                        defMusicPath=s;
                    else
                    if(tag.equals("SOUND"))
                        defSoundPath=s;
                    if(defPath==null)
                        defPath=s;
                    break;
                }
            }
            return -1;
        }
        MSPplayer newOne=new MSPplayer(applet);
        newOne.key=(String)parts.firstElement();
        newOne.url=(tag.equals("MUSIC")?defMusicPath:defSoundPath);
        if(newOne.url==null) newOne.url=defPath;
        String defaultUrl=newOne.url;
        for(int v=1;v<parts.size();v++)
        {
            String s=(String)parts.elementAt(v);
            if((s.startsWith("V="))||(s.startsWith("v=")))
                newOne.volume=Util.s_int(trimQuotes(s.substring(2)).trim());
            if((s.startsWith("L="))||(s.startsWith("l=")))
                newOne.repeats=Util.s_int(trimQuotes(s.substring(2)).trim());
            if((s.startsWith("P="))||(s.startsWith("p=")))
                newOne.priority=Util.s_int(trimQuotes(s.substring(2)).trim());
            if((s.startsWith("C="))||(s.startsWith("c=")))
                newOne.continueValue=Util.s_int(trimQuotes(s.substring(2)).trim());
            if((s.startsWith("U="))||(s.startsWith("u=")))
                newOne.url=trimQuotes(s.substring(2)).trim();
        }
        if((newOne.url!=null)&&(!newOne.url.trim().endsWith("/")))
            newOne.url=newOne.url.trim()+"/";
        if((currentClip!=null)&&(currentClip.priority>=newOne.priority))
            return -1;
        if((currentClip!=null)&&(currentClip.key.equals(newOne.key)))
        {
            currentClip.repeats=newOne.repeats;
            if(currentClip.continueValue==0)
                currentClip.iterations=0;
            return -1;
        }
        if(currentClip!=null)
        {
            currentClip.stopPlaying();
            if(tag.equals("MUSIC")) 
                musicClip=null;
            else
            if(tag.equals("SOUND")) 
                soundClip=null;
            currentClip=null;
        }
        currentClip=(MSPplayer)cache.get(newOne.key.toUpperCase());
        if(currentClip==null)
        {
            currentClip=newOne;
            cache.put(newOne.key,currentClip);
        }
        else
        {
            if(newOne.volume!=100) currentClip.volume=newOne.volume;
            if(newOne.repeats!=1) currentClip.repeats=newOne.repeats;
            if(newOne.priority!=50) currentClip.priority=newOne.priority;
            if(newOne.continueValue!=1) currentClip.continueValue=newOne.continueValue;
            if(newOne.url!=defaultUrl) currentClip.url=newOne.url;
        }
        currentClip.startPlaying();
        if(tag.equals("MUSIC")) 
            musicClip=currentClip;
        else
        if(tag.equals("SOUND")) 
            soundClip=currentClip;
        return -1;
    }

}
