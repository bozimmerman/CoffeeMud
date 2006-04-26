package com.planet_ink.coffee_mud.core;
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

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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

public class CMFile
{
    public static final int VFS_MASK_MASKSAVABLE=1+2+4;
    
    //private static final int VFS_MASK_BINARY=1;
    private static final int VFS_MASK_DIRECTORY=2;
    private static final int VFS_MASK_HIDDEN=4;
    private static final int VFS_MASK_ISLOCAL=8;
    private static final int VFS_MASK_NOWRITEVFS=16;
    private static final int VFS_MASK_NOWRITELOCAL=32;
    private static final int VFS_MASK_NOREADVFS=64;
    private static final int VFS_MASK_NOREADLOCAL=128;
    
    private static final int VFS_INFO_FILENAME=0;
    private static final int VFS_INFO_BITS=1;
    private static final int VFS_INFO_DATE=2;
    private static final int VFS_INFO_WHOM=3;
    private static final int VFS_INFO_DATA=4;

    private static final char pathSeparator=File.separatorChar;
    private static Vector vfs=null;
    private boolean logErrors=false;
    
    private int vfsBits=0;
    private String localPath=null;
    private String path=null;
    private String name=null;
    private String author=null;
    private MOB accessor=null;
    private long modifiedDateTime=System.currentTimeMillis();
    private File localFile=null;
    private boolean demandVFS=false;
    private boolean demandLocal=false;
    
    public CMFile(String filename, MOB user, boolean pleaseLogErrors)
    { super(); buildCMFile(filename,user,pleaseLogErrors,false);}
    public CMFile(String filename, MOB user, boolean pleaseLogErrors, boolean forceAllow)
    { super(); buildCMFile(filename,user,pleaseLogErrors,forceAllow);}
    public CMFile (String currentPath, String filename, MOB user, boolean pleaseLogErrors)
    { super(); buildCMFile(incorporateBaseDir(currentPath,filename),user,pleaseLogErrors,false); }
    public CMFile (String currentPath, String filename, MOB user, boolean pleaseLogErrors, boolean forceAllow)
    { super(); buildCMFile(incorporateBaseDir(currentPath,filename),user,pleaseLogErrors,forceAllow); }
    
    private void buildCMFile(String absolutePath, MOB user, boolean pleaseLogErrors, boolean forceAllow)
    {
        accessor=user;
        localFile=null;
        logErrors=pleaseLogErrors;
        demandLocal=absolutePath.trim().startsWith("//");
        demandVFS=absolutePath.trim().startsWith("::");
        if(accessor!=null) author=accessor.Name();
        absolutePath=vfsifyFilename(absolutePath);
        name=absolutePath;
        int x=absolutePath.lastIndexOf('/');
        path="";
        if(x>=0)
        {
            path=absolutePath.substring(0,x);
            name=absolutePath.substring(x+1);
        }
        localPath=path.replace('/',pathSeparator);
        // fill in all we can
        vfsBits=0;
        Vector info=getVFSInfo(absolutePath);
        localFile=new File(getIOReadableLocalPathAndName());
        if((info!=null)&&((!demandLocal)||(!localFile.exists())))
        {
            vfsBits=vfsBits|((Integer)info.elementAt(CMFile.VFS_INFO_BITS)).intValue();
            author=((String)info.elementAt(CMFile.VFS_INFO_WHOM));
            modifiedDateTime=((Long)info.elementAt(CMFile.VFS_INFO_DATE)).longValue();
        }
        else
        {
            modifiedDateTime=localFile.lastModified();
            if(localFile.isHidden()) vfsBits=vfsBits|CMFile.VFS_MASK_HIDDEN;
        }

        boolean isADirectory=((localFile!=null)&&(localFile.exists())&&(localFile.isDirectory()))
                           ||doesExistAsPathInVFS(absolutePath);
        boolean allowedToTraverseAsDirectory=isADirectory
                                           &&((accessor==null)||CMSecurity.canTraverseDir(accessor,accessor.location(),absolutePath));
        boolean allowedToWriteVFS=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,true)));
        boolean allowedToReadVFS=(doesFilenameExistInVFS(absolutePath)&&allowedToWriteVFS)||allowedToTraverseAsDirectory;
        boolean allowedToWriteLocal=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,false)));
        boolean allowedToReadLocal=(localFile!=null)
                                    &&(localFile.exists())
                                    &&(allowedToWriteLocal||allowedToTraverseAsDirectory);
        
        if(!allowedToReadVFS) vfsBits=vfsBits|CMFile.VFS_MASK_NOREADVFS;
        
        if(!allowedToReadLocal) vfsBits=vfsBits|CMFile.VFS_MASK_NOREADLOCAL;
        
        if(!allowedToWriteVFS) vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITEVFS;
        
        if(!allowedToWriteLocal) vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITELOCAL;
        
        if(allowedToTraverseAsDirectory)  vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
        
        if((demandVFS)&&(!allowedToReadVFS))  vfsBits=vfsBits|CMFile.VFS_MASK_NOREADLOCAL;
        if((demandVFS)&&(!allowedToWriteVFS))  vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITELOCAL;
        
        if((demandLocal)&&(!allowedToReadLocal))  vfsBits=vfsBits|CMFile.VFS_MASK_NOREADVFS;
        if((demandLocal)&&(!allowedToWriteLocal))  vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITEVFS;
        
        if((!demandVFS)
        &&(demandLocal||(!allowedToReadVFS)))
            vfsBits=vfsBits|CMFile.VFS_MASK_ISLOCAL;
    }
    
    public boolean canRead()
    {
        if(!exists()) return false;
        if(CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL))
        {
            if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL))
                return false;
        }
        else
        {
            if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS))
                return false;
        }
        return true;
    }
    public boolean canWrite()
    {
        if(CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL))
        {
            if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOWRITELOCAL))
                return false;
        }
        else
        {
            if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOWRITEVFS))
                return false;
        }
        return true;
    }
    
    public boolean isDirectory(){return exists()&&CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY);}
    public boolean exists(){ return !(CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS)&&CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL));}
    public boolean isFile(){return canRead()&&(!CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY));}
    public long lastModified(){return modifiedDateTime;}
    public String author(){return ((author!=null))?author:"unknown";}
    public boolean isLocalFile(){return CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL);}
    public boolean isVFSFile(){return (!CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL));}
    public boolean canVFSEquiv(){return (!CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS));}
    public boolean canLocalEquiv(){return (!CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL));}
    public String getName(){return name;}
    public String getAbsolutePath(){return "/"+getVFSPathAndName();}
    public String getCanonicalPath(){return getVFSPathAndName();}
    public String getLocalPathAndName()
    {
        if(path.length()==0)
            return name;
        return localPath+pathSeparator+name;
    }
    public String getIOReadableLocalPathAndName()
    {
        String s=getLocalPathAndName();
        if(s.trim().length()==0) return ".";
        return s;
    }
    public String getVFSPathAndName()
    {
        if(path.length()==0)
            return name;
        return path+'/'+name;
    }
    
    public boolean mayDeleteIfDirectory()
    {
        if(!isDirectory()) return true;
        if((localFile!=null)&&(localFile.isDirectory())&&(localFile.list().length>0))
            return false;
        Vector file=null;
        Vector vfs=getVFSDirectory();
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if((((String)file.firstElement()).toUpperCase().startsWith(getVFSPathAndName().toUpperCase()+'/'))
            &&(!(((String)file.firstElement()).toUpperCase().equals(getVFSPathAndName().toUpperCase()+'/'))))
                return false;
        }
        return true;
    }
    
    public boolean deleteLocal()
    {
        if(!exists()) return false;
        if(!canWrite()) return false;
        if(!mayDeleteIfDirectory()) return false;
        if((canLocalEquiv())&&(localFile!=null))
            return localFile.delete();
        return false;
    }
    public boolean deleteVFS()
    {
        if(!exists()) return false;
        if(!canWrite()) return false;
        if(!mayDeleteIfDirectory()) return false;
        if(canVFSEquiv())
        {
            Vector info=getVFSInfo(getVFSPathAndName());
            if((info==null)||(info.size()<1)) return false;
            CMLib.database().DBDeleteVFSFile((String)info.elementAt(CMFile.VFS_INFO_FILENAME));
            getVFSDirectory().remove(info);
            return true;
        }
        return false;
    }
    
    public boolean delete()
    {
        if(!exists()) return false;
        if(!canWrite()) return false;
        if(!mayDeleteIfDirectory()) return false;
        if(isVFSFile()) return deleteVFS();
        if(isLocalFile()) return deleteLocal();
        return false;
    }
    
    public StringBuffer text()
    {
        StringBuffer buf=new StringBuffer("");
        if(!canRead())
        {
            if(logErrors)
                Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
            return buf;
        }
        if(isVFSFile())
        {
            Vector info=getVFSInfo(getVFSPathAndName());
            if(info!=null)
            {
                Object data=getVFSData(getVFSPathAndName());
                if(data==null) return buf;
                if(data instanceof String)
                    return new StringBuffer((String)data);
                if(data instanceof StringBuffer)
                    return (StringBuffer)data;
                if(data instanceof byte[])
                    return new StringBuffer(new String((byte[])data));
            }
            else
            if(logErrors)
                Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
            return buf;
        }
        try
        {
            FileReader F=new FileReader(getIOReadableLocalPathAndName());
            BufferedReader reader=new BufferedReader(F);
            String line="";
            while((line!=null)&&(reader.ready()))
            {
                line=reader.readLine();
                if(line!=null)
                {
                    buf.append(line);
                    buf.append("\n\r");
                }
            }
            F.close();
        }
        catch(Exception e)
        {
            if(logErrors)
                Log.errOut("CMFile",e.getMessage());
            return buf;
        }
        return buf;
    }
    
    public StringBuffer textUnformatted()
    {
        StringBuffer buf=new StringBuffer("");
        if(!canRead())
        {
            if(logErrors)
                Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
            return buf;
        }
        if(isVFSFile())
        {
            Vector info=getVFSInfo(getVFSPathAndName());
            if(info!=null)
            {
                Object data=getVFSData(getVFSPathAndName());
                if(data==null) return buf;
                if(data instanceof String)
                    return new StringBuffer((String)data);
                if(data instanceof StringBuffer)
                    return (StringBuffer)data;
                if(data instanceof byte[])
                    return new StringBuffer(new String((byte[])data));
            }
            else
            if(logErrors)
                Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
            return buf;
        }
        try
        {
            FileReader F=new FileReader(getIOReadableLocalPathAndName());
            char c=' ';
            while(F.ready())
            {
                c=(char)F.read();
                if(c<0) break;
                buf.append(c);
            }
            F.close();
        }
        catch(Exception e)
        {
            if(logErrors)
                Log.errOut("CMFile",e.getMessage());
        }
        return buf;
    }
    
    public byte[] raw()
    {
        byte[] buf=new byte[0];
        if(!canRead())
        {
            if(logErrors)
                Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
            return buf;
        }
        if(isVFSFile())
        {
            Vector info=getVFSInfo(getVFSPathAndName());
            if(info!=null)
            {
                Object data=getVFSData(getVFSPathAndName());
                if(data==null) return buf;
                if(data instanceof byte[])
                    return (byte[])data;
                if(data instanceof String)
                    return ((String)data).getBytes();
                if(data instanceof StringBuffer)
                    return ((StringBuffer)data).toString().getBytes();
            }
            else
            if(logErrors)
                Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
            return buf;
        }
        try
        {
            DataInputStream fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(getIOReadableLocalPathAndName()) ) );
            buf = new byte [ fileIn.available() ];
            fileIn.readFully(buf);
            fileIn.close();
            fileIn.close();
        }
        catch(Exception e)
        {
            if(logErrors)
                Log.errOut("CMFile",e.getMessage());
        }
        return buf;
    }

    public StringBuffer textVersion(byte[] bytes)
    {
        StringBuffer text=new StringBuffer(new String(bytes));
        for(int i=0;i<text.length();i++)
            if((text.charAt(i)<0)||(text.charAt(i)>127))
                return null;
        return text;
    }
    
    public boolean saveRaw(Object data)
    {
        if(data==null)
        {
            Log.errOut("CMFile","Unable to save file '"+getVFSPathAndName()+"': No Data.");
            return false;
        }
        if((CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY))
        ||(!canWrite()))
        {
            Log.errOut("CMFile","Access error saving file '"+getVFSPathAndName()+"'.");
            return false;
        }
        
        Object O=null;
        if(data instanceof String) 
            O=new StringBuffer((String)data);
        else
        if(data instanceof StringBuffer) 
            O=(StringBuffer)data;
        else
        if(data instanceof byte[])
        {
            StringBuffer test=textVersion((byte[])data);
            if(test!=null)
                O=test;
            else
                O=(byte[])data;
        }
        else
        if(data!=null)
            O=new StringBuffer(data.toString());
        if(!isLocalFile())
        {
            String filename=getVFSPathAndName();
            Vector info=getVFSInfo(filename);
            if(info!=null)
            {
                filename=(String)info.firstElement();
                if(vfs!=null) vfs.removeElement(info);
                CMLib.database().DBDeleteVFSFile(filename);
            }
            if(vfsBits<0) vfsBits=0;
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits&VFS_MASK_MASKSAVABLE));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(author());
            vfs.addElement(info);
            CMLib.database().DBCreateVFSFile(filename,vfsBits,author(),O);
            return true;
        }
        try
        {
            File F=new File(getIOReadableLocalPathAndName());
            if(O instanceof StringBuffer)
                O=((StringBuffer)O).toString().getBytes();
            if(O instanceof String)
                O=((String)O).getBytes();
            if(O instanceof byte[])
            {
                FileOutputStream FW=new FileOutputStream(F,false);
                FW.write((byte[])O);
                FW.flush();
                FW.close();
            }
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
            return true;
        }
        catch(IOException e)
        {
            Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
        }
        return false;
    }
    
    public boolean saveText(Object data)
    {
        if(data==null)
        {
            Log.errOut("CMFile","Unable to save file '"+getVFSPathAndName()+"': No Data.");
            return false;
        }
        if((CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY))
        ||(!canWrite()))
        {
            Log.errOut("CMFile","Access error saving file '"+getVFSPathAndName()+"'.");
            return false;
        }
        
        StringBuffer O=null;
        if(data instanceof String) 
            O=new StringBuffer((String)data);
        else
        if(data instanceof StringBuffer) 
            O=(StringBuffer)data;
        else
        if(data instanceof byte[])
            O=new StringBuffer(new String((byte[])data));
        else
        if(data!=null)
            O=new StringBuffer(data.toString());
        if(!isLocalFile())
        {
            String filename=getVFSPathAndName();
            Vector info=getVFSInfo(filename);
            if(info!=null)
            {
                filename=(String)info.firstElement();
                if(vfs!=null) vfs.removeElement(info);
                CMLib.database().DBDeleteVFSFile(filename);
            }
            if(vfsBits<0) vfsBits=0;
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits&VFS_MASK_MASKSAVABLE));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(author());
            vfs.addElement(info);
            CMLib.database().DBCreateVFSFile(filename,vfsBits,author(),O);
            return true;
        }
        try
        {
            File F=new File(getIOReadableLocalPathAndName());
            FileWriter FW=new FileWriter(F);
            FW.write(saveBufNormalize(O).toString());
            FW.close();
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
            return true;
        }
        catch(IOException e)
        {
            Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
        }
        return false;
    }

    public boolean mkdir()
    {
        if(exists())
        {
            Log.errOut("CMFile","File exists '"+getVFSPathAndName()+"'.");
            return false;
        }
        if(!canWrite())
        {
            Log.errOut("CMFile","Access error making directory '"+getVFSPathAndName()+"'.");
            return false;
        }
        if(!isLocalFile())
        {
            String filename=getVFSPathAndName();
            Vector info=getVFSInfo(filename);
            if(!filename.endsWith("/")) filename=filename+"/";
            if(info==null) info=getVFSInfo(filename);
            if(info!=null)
            {
                Log.errOut("CMFile","File exists '"+getVFSPathAndName()+"'.");
                return false;
            }
            if(vfsBits<0) vfsBits=0;
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOWRITEVFS);
            vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits&VFS_MASK_MASKSAVABLE));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(author());
            vfs.addElement(info);
            CMLib.database().DBCreateVFSFile(filename,vfsBits,author(),new StringBuffer(""));
            return true;
        }
        File F=new File(getIOReadableLocalPathAndName());
        if(F.exists())
        {
            Log.errOut("CMFile","File exists '"+getIOReadableLocalPathAndName()+"'.");
            return false;
        }
        if(F.mkdir())
        {
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
            vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
            vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOWRITELOCAL);
            return true;
        }
        return false;
    }

    public String[] list()
    {
        if(!isDirectory()) return new String[0];
        CMFile[] CF=listFiles();
        String[] list=new String[CF.length];
        for(int f=0;f<CF.length;f++)
            list[f]=CF[f].getName();
        return list;
    }
    
    private static Vector getVFSInfo(String filename)
    {
        Vector vfs=getVFSDirectory();
        if(vfs==null) return null;
        filename=vfsifyFilename(filename);
        Vector file=null;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
                return file;
        }
        return null;
    }
    
    private static Object getVFSData(String filename)
    {
        Vector vfs=getVFSDirectory();
        if(vfs==null) return null;
        filename=vfsifyFilename(filename);
        Vector file=null;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
            {
                Vector V=CMLib.database().DBReadVFSFile((String)file.firstElement());
                if(V.size()>=CMFile.VFS_INFO_DATA)
                    return V.elementAt(CMFile.VFS_INFO_DATA);
            }
        }
        return null;
    }
    
    private static boolean doesFilenameExistInVFS(String filename)
    {
        Vector file=null;
        if(filename.length()==0) return true;
        Vector vfs=getVFSDirectory();
        if(vfs==null) return false;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if((((String)file.firstElement()).equalsIgnoreCase(filename))
            ||(((String)file.firstElement()).equalsIgnoreCase(filename+"/")))
                return true;
        }
        return false;
    }
    
    private static boolean doesExistAsPathInVFS(String filename)
    {
        Vector file=null;
        Vector vfs=getVFSDirectory();
        if(vfs==null) return false;
        filename=vfsifyFilename(filename).toUpperCase()+"/";
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).toUpperCase().startsWith(filename))
                return true;
        }
        return false;
    }
    
    public CMFile[] listFiles()
    {
        if((!isDirectory())||(!canRead())) 
            return new CMFile[0];
        String prefix=demandLocal?"//":(demandVFS?"::":"");
        Vector dir=new Vector();
        Vector fcheck=new Vector();
        Vector info=null;
        String thisDir=getVFSPathAndName();
        Vector vfs=getVFSDirectory();
        String vfsSrchDir=thisDir+"/";
        if(thisDir.length()==0) vfsSrchDir="";
        if(!demandLocal)
        for(int v=0;v<vfs.size();v++)
        {
            info=(Vector)vfs.elementAt(v);
            String totalVFSString=(String)info.firstElement();
            if((thisDir.length()==0)
            ||totalVFSString.toUpperCase().startsWith(vfsSrchDir.toUpperCase())) // SOMETHING in this vfs dir
            {
                String entryName=totalVFSString.substring(vfsSrchDir.length());
                if(entryName.length()==0) continue;
                int x=entryName.indexOf("/");
                if(x>0) // this entry is a dir in our dir! 
                {
                    entryName=entryName.substring(0,x);
                    String thisPath=(thisDir.length()>0)?thisDir+"/"+entryName:entryName;
                    CMFile CF=new CMFile(prefix+thisPath,accessor,false);
                    if((CF.canRead())
                    &&(!fcheck.contains(entryName.toUpperCase())))
                    {
                        fcheck.addElement(entryName.toUpperCase());
                        dir.addElement(CF);
                    }
                }
                else
                {
                    String thisPath=(vfsSrchDir.length()>0)?vfsSrchDir+entryName:entryName;
                    CMFile CF=new CMFile(prefix+thisPath,accessor,false);
                    if((CF.canRead())
                    &&(!fcheck.contains(entryName.toUpperCase())))
                    {
                        fcheck.addElement(entryName.toUpperCase());
                        dir.addElement(CF);
                    }
                }
            }
        }
        
        if(!demandVFS)
        {
            thisDir=getIOReadableLocalPathAndName();
            File F=new File(thisDir);
            if(F.isDirectory())
            {
                File[] list=F.listFiles();
                File F2=null;
                for(int l=0;l<list.length;l++)
                {
                    F2=list[l];
                    String thisPath=vfsifyFilename(thisDir)+"/"+F2.getName();
                    String thisName=F2.getName();
                    CMFile CF=new CMFile(prefix+thisPath,accessor,false);
                    if((CF.canRead())
                    &&(!fcheck.contains(thisName.toUpperCase())))
                        dir.addElement(CF);
                }
            }
        }
        CMFile[] finalDir=new CMFile[dir.size()];
        for(int f=0;f<dir.size();f++)
            finalDir[f]=(CMFile)dir.elementAt(f);
        return finalDir;
    }
    
    public static Vector getVFSDirectory()
    {
        if(vfs==null)
        {
        	if(CMLib.database()==null)
        	{
        		vfs=new Vector();
        		Log.errOut("CMFile","Unable to read master database directory.  Database library NOT FOUND!!");
        	}
        	else
        		vfs=CMLib.database().DBReadVFSDirectory();
        }
        return vfs;
    }
    
    public static String vfsifyFilename(String filename)
    {
        filename=filename.trim();
        if(filename.startsWith("::"))
            filename=filename.substring(2);
        if(filename.startsWith("//"))
            filename=filename.substring(2);
        if((filename.length()>3)
        &&(Character.isLetter(filename.charAt(0))
        &&(filename.charAt(1)==':')))
        	filename=filename.substring(2);
        while(filename.startsWith("/")) filename=filename.substring(1);
        while(filename.startsWith("\\")) filename=filename.substring(1);
        while(filename.endsWith("/"))
            filename=filename.substring(0,filename.length()-1);
        while(filename.endsWith("\\"))
            filename=filename.substring(0,filename.length()-1);
        return filename.replace(pathSeparator,'/');
    }
    
    private StringBuffer saveBufNormalize(StringBuffer myRsc)
    {
        for(int i=0;i<myRsc.length();i++)
            if(myRsc.charAt(i)=='\n')
            {
                for(i=myRsc.length()-1;i>=0;i--)
                    if(myRsc.charAt(i)=='\r')
                        myRsc.deleteCharAt(i);
                return myRsc;
            }
        for(int i=0;i<myRsc.length();i++)
            if(myRsc.charAt(i)=='\r')
                myRsc.setCharAt(i,'\n');
        return myRsc;
    }
    
    private static String incorporateBaseDir(String currentPath, String filename)
    {
        String starter="";
        if(filename.startsWith("::")||filename.startsWith("//"))
        {
            starter=filename.substring(0,2);
            filename=filename.substring(2);
        }
        if(!filename.startsWith("/"))
        {
            boolean didSomething=true;
            while(didSomething)
            {
                didSomething=false;
                if(filename.startsWith(".."))
                {
                    filename=filename.substring(2);
                    int x=currentPath.lastIndexOf("/");
                    if(x>=0) 
                        currentPath=currentPath.substring(0,x);
                    else
                        currentPath="";
                    didSomething=true;
                }
                if((filename.startsWith("."))&&(!(filename.startsWith("..")))) 
                {
                    filename=filename.substring(1);
                    didSomething=true;
                }
                while(filename.startsWith("/")) filename=filename.substring(1);
            }
            if((currentPath.length()>0)&&(filename.length()>0))
                filename=currentPath+"/"+filename;
            else
            if(currentPath.length()>0)
                filename=currentPath;
        }
        return starter+filename;
    }

    public static CMFile[] getFileList(String currentPath, String filename, MOB user, boolean recurse)
    { return getFileList(incorporateBaseDir(currentPath,filename),user,recurse);}
    public static CMFile[] getFileList(String parse, MOB user, boolean recurse)
    {
        boolean demandLocal=parse.trim().startsWith("//");
        boolean demandVFS=parse.trim().startsWith("::");
        CMFile dirTest=new CMFile(parse,user,false);
        if((dirTest.exists())&&(dirTest.isDirectory())&&(dirTest.canRead())&&(!recurse))
            return dirTest.listFiles();
        String vsPath=vfsifyFilename(parse);
        String fixedName=vsPath;
        int x=vsPath.lastIndexOf('/');
        String fixedPath="";
        if(x>=0)
        {
            fixedPath=vsPath.substring(0,x);
            fixedName=vsPath.substring(x+1);
        }
        CMFile dir=new CMFile((demandLocal?"//":demandVFS?"::":"")+fixedPath,user,false);
        if((!dir.exists())||(!dir.isDirectory())||(!dir.canRead())) 
            return null;
        Vector set=new Vector();
        CMFile[] cset=dir.listFiles();
        fixedName=fixedName.toUpperCase();
        for(int c=0;c<cset.length;c++)
        {
            if((recurse)&&(cset[c].isDirectory())&&(cset[c].canRead()))
            {
                CMFile[] CF2=getFileList(cset[c].getVFSPathAndName()+"/"+fixedName,user,true);
                for(int cf2=0;cf2<CF2.length;cf2++)
                    set.addElement(CF2[cf2]);
            }
            String name=cset[c].getName().toUpperCase();
            boolean ismatch=true;
            if((!name.equalsIgnoreCase(fixedName))&&(fixedName.length()>0))
            for(int f=0,n=0;f<fixedName.length();f++,n++)
                if(fixedName.charAt(f)=='?')
                {
                    if(n>=name.length()){ ismatch=false; break; }
                }
                else
                if(fixedName.charAt(f)=='*')
                {
                    if(f==fixedName.length()-1) break;
                    char mustMatchC=fixedName.charAt(f+1);
                    for(;n<name.length();n++)
                        if(name.charAt(n)==mustMatchC)
                            break;
                    if((n<name.length())&&(name.charAt(n)==mustMatchC))
                    { n--; continue;}
                    ismatch=false;
                    break;
                }
                else
                if((n>=name.length())||(fixedName.charAt(f)!=name.charAt(n)))
                { ismatch=false; break; }
            if(ismatch) set.addElement(cset[c]);
        }
        if(set.size()==1)
        {
            dirTest=(CMFile)set.firstElement();
            if((dirTest.exists())&&(dirTest.isDirectory())&&(dirTest.canRead())&&(!recurse))
                return dirTest.listFiles();
        }
        cset=new CMFile[set.size()];
        for(int s=0;s<set.size();s++)
            cset[s]=(CMFile)set.elementAt(s);
        return cset;
    }
}
