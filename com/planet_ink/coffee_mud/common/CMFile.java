package com.planet_ink.coffee_mud.common;

import java.io.*;
import java.util.*;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

public class CMFile
{
    public static final int VFS_MASK_TEXT=0;
    public static final int VFS_MASK_BINARY=1;
    public static final int VFS_MASK_READONLY=2;
    public static final int VFS_MASK_HIDDEN=4;
    public static final int VFS_MASK_DIRECTORY=8;
    public static final int VFS_MASK_LOCALFILE=16;
    public static final int VFS_MASK_WRITABLE=32;
    public static final int VFS_MASK_NOTINVFS=64;
    public static final int VFS_MASK_NONEXISTANT=128;
    public static final int VFS_MASK_NOTLOCAL=265;
    
    public static final int VFS_INFO_FILENAME=0;
    public static final int VFS_INFO_BITS=1;
    public static final int VFS_INFO_DATE=2;
    public static final int VFS_INFO_WHOM=3;
    public static final int VFS_INFO_DATA=4;

    public static final char pathSeparator=File.separatorChar;
    private static Vector vfs=null;
    
    
    private int vfsBits=0;
    private String localPath=null;
    private String path=null;
    private String name=null;
    private String author=null;
    private MOB accessor=null;
    private boolean isAccessible=false;
    private long modifiedDateTime=System.currentTimeMillis();
    private File localFile=null;
    
    public CMFile(String filename, MOB user, boolean logErrors)
    {
        accessor=user;
        String savedFilename=filename;
        localFile=null;
        isAccessible=false;
        boolean demandLocal=filename.trim().startsWith("||");
        boolean demandVFS=filename.trim().startsWith("::");
        if(accessor!=null) author=accessor.Name();
        filename=vfsifyFilename(filename);
        name=filename;
        int x=filename.lastIndexOf('/');
        if(x>=0)
        {
            path=filename.substring(0,x);
            name=filename.substring(x+1);
        }
        localPath=path.replace('/',File.separatorChar);

        // fill in all we can
        vfsBits=0;
        Vector info=getVFSInfo(filename);
        localFile=new File(path.replace('/',pathSeparator)+pathSeparator+name);
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
            if(localFile.canWrite()) vfsBits=vfsBits|CMFile.VFS_MASK_WRITABLE;
        }
        
        boolean canAccessVFS=doesFilenameExistInVFS(filename)
                           &&((accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),filename,true)));
        boolean canAccessLocal=localFile.exists()
                            &&((accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),filename,false)));
        if(!canAccessVFS) vfsBits=vfsBits|CMFile.VFS_MASK_NOTINVFS;
        if(!canAccessLocal) vfsBits=vfsBits|CMFile.VFS_MASK_NOTLOCAL;
        
        if(doesExistAsPathInVFS(filename)||localFile.isDirectory())
            vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
        
        // does it exist anywhere?
        if((!canAccessVFS)&&(!canAccessLocal))
        {
            if(logErrors) Log.errOut("CMFile",((accessor!=null)?accessor.Name()+" ":"")+"Unable to access file: '"+savedFilename+"'");
            vfsBits=vfsBits|CMFile.VFS_MASK_NONEXISTANT|CMFile.VFS_MASK_WRITABLE;
            return;
        }
        // I DEMAND VFS -- may I?
        if((demandVFS)&&(!canAccessVFS))
        {
            if(logErrors) Log.errOut("CMFile",((accessor!=null)?accessor.Name()+" ":"")+"Unable to access file: '"+savedFilename+"'");
            vfsBits=vfsBits|CMFile.VFS_MASK_NONEXISTANT|CMFile.VFS_MASK_WRITABLE;
            return;
        }
        // I DEMAND LOCAL -- may I?
        if((demandLocal)&&(!canAccessLocal))
        {
            if(logErrors) Log.errOut("CMFile",((accessor!=null)?accessor.Name()+" ":"")+"Unable to access file: '"+savedFilename+"'");
            vfsBits=vfsBits|CMFile.VFS_MASK_NONEXISTANT|CMFile.VFS_MASK_WRITABLE|CMFile.VFS_MASK_LOCALFILE;
            return;
        }
        
        if((!demandLocal)&&(canAccessVFS))
        {
            isAccessible=true;
            if(Util.bset(vfsBits,CMFile.VFS_MASK_NONEXISTANT))
                vfsBits=vfsBits-CMFile.VFS_MASK_NONEXISTANT;
            return;
        }
        
        if((!demandVFS)&&(canAccessLocal))
        {
            vfsBits=vfsBits|CMFile.VFS_MASK_LOCALFILE;
            isAccessible=true;
            if(!localFile.exists())  vfsBits=vfsBits|CMFile.VFS_MASK_NONEXISTANT;
        }
    }
    public boolean canRead(){return exists();}
    public boolean canWrite(){return exists()&&Util.bset(vfsBits,CMFile.VFS_MASK_WRITABLE);}
    public boolean isDirectory(){return exists()&&Util.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY);}
    public boolean exists(){return isAccessible&&(!Util.bset(vfsBits,CMFile.VFS_MASK_NONEXISTANT));}
    public boolean isFile(){return exists()&&(!Util.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY));}
    public long lastModified(){return modifiedDateTime;}
    public String author(){return ((author!=null))?author:"unknown";}
    public boolean isLocalFile(){return Util.bset(vfsBits,CMFile.VFS_MASK_LOCALFILE);}
    public boolean isVFSFile(){return (!Util.bset(vfsBits,CMFile.VFS_MASK_LOCALFILE));}
    public boolean canVFSEquiv(){return (!Util.bset(vfsBits,CMFile.VFS_MASK_NOTINVFS));}
    public boolean canLocalEquiv(){return (!Util.bset(vfsBits,CMFile.VFS_MASK_NOTLOCAL));}
    public String getName(){return name;}
    public String getLocalStyleAbsolutePath(){return pathSeparator+localPath+pathSeparator+name;}
    public String getLocalStyleCanonicalPath(){return localPath+pathSeparator+name;}
    public boolean deleteLocal()
    {
        if(!exists()) return false;
        if((canLocalEquiv())&&(localFile!=null))
            return localFile.delete();
        return false;
    }
    public boolean deleteVFS()
    {
        if(!exists()) return false;
        if(canVFSEquiv())
        {
            Vector info=getVFSInfo(path+'/'+name);
            if((info==null)||(info.size()<1)) return false;
            CMClass.DBEngine().DBDeleteVFSFile((String)info.elementAt(CMFile.VFS_INFO_FILENAME));
            getVFSDirectory().remove(info);
        }
        return false;
    }
    
    public boolean delete()
    {
        if(!exists()) return false;
        if(isVFSFile()) return deleteVFS();
        if(isLocalFile()) return deleteLocal();
        return false;
    }
    
    public StringBuffer text()
    {
        StringBuffer buf=new StringBuffer("");
        if((!isVFSFile())&&(!isLocalFile()))
            return buf;
        if(isVFSFile())
        {
            Vector info=getVFSInfo(path+'/'+name);
            if(info!=null)
            {
                int bits=((Integer)info.elementAt(CMFile.VFS_INFO_BITS)).intValue();
                if(Util.bset(bits,CMFile.VFS_MASK_BINARY))
                    return buf;
                Object data=getVFSData(path+'/'+name);
                if(data==null) return buf;
                if(data instanceof String)
                    return new StringBuffer((String)data);
                if(data instanceof StringBuffer)
                    return (StringBuffer)data;
                if(data instanceof byte[])
                    return new StringBuffer(new String((byte[])data));
            }
            return buf;
        }
        try
        {
            FileReader F=new FileReader(localPath+pathSeparator+name);
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
            Log.errOut("CMFile",e.getMessage());
            return buf;
        }
        return buf;
    }
    
    public StringBuffer textUnformatted()
    {
        StringBuffer buf=new StringBuffer("");
        if((!isVFSFile())&&(!isLocalFile()))
            return buf;
        if(isVFSFile())
        {
            Vector info=getVFSInfo(path+'/'+name);
            if(info!=null)
            {
                int bits=((Integer)info.elementAt(CMFile.VFS_INFO_BITS)).intValue();
                if(Util.bset(bits,CMFile.VFS_MASK_BINARY))
                    return buf;
                Object data=getVFSData(path+'/'+name);
                if(data==null) return buf;
                if(data instanceof String)
                    return new StringBuffer((String)data);
                if(data instanceof StringBuffer)
                    return (StringBuffer)data;
                if(data instanceof byte[])
                    return new StringBuffer(new String((byte[])data));
            }
            return buf;
        }
        try
        {
            FileReader F=new FileReader(localPath+pathSeparator+name);
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
            Log.errOut("CMFile",e.getMessage());
        }
        return buf;
    }
    
    public byte[] raw()
    {
        byte[] buf=new byte[0];
        if((!isVFSFile())&&(!isLocalFile()))
            return buf;
        if(isVFSFile())
        {
            Vector info=getVFSInfo(path+'/'+name);
            if(info!=null)
            {
                Object data=getVFSData(path+'/'+name);
                if(data==null) return buf;
                if(data instanceof byte[])
                    return (byte[])data;
                if(data instanceof String)
                    return ((String)data).getBytes();
                if(data instanceof StringBuffer)
                    return ((StringBuffer)data).toString().getBytes();
            }
            return buf;
        }
        try
        {
            DataInputStream fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(localPath+pathSeparator+name) ) );
            buf = new byte [ fileIn.available() ];
            fileIn.readFully(buf);
            fileIn.close();
            fileIn.close();
        }
        catch(Exception e)
        {
            Log.errOut("CMFile",e.getMessage());
        }
        return buf;
    }

    // 0 = error, abort!
    // 1 = save as vfs
    // -1 = save as local
    private int determineSavabilityAndFixBits(Object data)
    {
        int order=Util.bset(vfsBits,CMFile.VFS_MASK_LOCALFILE)?-1:1;
            
        if(order==-1)
        {
            if((accessor==null)
            ||(CMSecurity.canAccessFile(accessor,accessor.location(),localPath+pathSeparator+name,false)))
            {
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_NONEXISTANT);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_NOTLOCAL);
                vfsBits=vfsBits|CMFile.VFS_MASK_WRITABLE;
                vfsBits=vfsBits|CMFile.VFS_MASK_LOCALFILE;
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_DIRECTORY);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_BINARY);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_TEXT);
                if(data instanceof Vector)
                    vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
                else
                if(data instanceof byte[])
                    vfsBits=vfsBits|CMFile.VFS_MASK_BINARY;
                else
                if((data instanceof String)
                ||(data instanceof StringBuffer))
                    vfsBits=vfsBits|CMFile.VFS_MASK_TEXT;
                return order;
            }
            return 0;
        }
        else
        if(order==1)
        {
            if((accessor==null)
            ||(CMSecurity.canAccessFile(accessor,accessor.location(),path+'/'+name,true)))
            {
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_NONEXISTANT);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_NOTINVFS);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_LOCALFILE);
                vfsBits=vfsBits|CMFile.VFS_MASK_WRITABLE;
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_DIRECTORY);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_BINARY);
                vfsBits=Util.unsetb(vfsBits,CMFile.VFS_MASK_TEXT);
                if(data instanceof Vector)
                    vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
                else
                if(data instanceof byte[])
                    vfsBits=vfsBits|CMFile.VFS_MASK_BINARY;
                else
                if((data instanceof String)
                ||(data instanceof StringBuffer))
                    vfsBits=vfsBits|CMFile.VFS_MASK_TEXT;
                return order;
            }
            return 0;
        }
        return 0;
    }
    
    public boolean saveRaw(Object data)
    {
        if(data==null)
        {
            Log.errOut("Resources","Unable to save file '"+path+"/"+name+"': No Data.");
            return false;
        }
        int order=determineSavabilityAndFixBits(data);
        if(order==0)
        {
            Log.errOut("Resources","Access error saving file '"+path+"/"+name+"'.");
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
            O=(byte[])data;
        else
        if(data!=null)
            O=new StringBuffer(data.toString());
        if(order==1)
        {
            String filename=path+'/'+name;
            Vector info=getVFSInfo(filename);
            if(info!=null)
            {
                filename=(String)info.firstElement();
                if(vfs!=null) vfs.removeElement(info);
                CMClass.DBEngine().DBDeleteVFSFile(filename);
            }
            if(vfsBits<0) vfsBits=0;
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(author());
            vfs.addElement(info);
            CMClass.DBEngine().DBCreateVFSFile(filename,vfsBits,author(),O);
            return true;
        }
        try
        {
            File F=new File(localPath+pathSeparator+name);
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
            return true;
        }
        catch(IOException e)
        {
            Log.errOut("Resources","Error Saving "+localPath+pathSeparator+name+": "+e.getMessage());
        }
        return false;
    }
    
    public boolean saveText(Object data)
    {
        if(data==null)
        {
            Log.errOut("Resources","Unable to save file '"+path+"/"+name+"': No Data.");
            return false;
        }
        int order=determineSavabilityAndFixBits(data);
        if(order==0)
        {
            Log.errOut("Resources","Access error saving file '"+path+"/"+name+"'.");
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
        if(order==1)
        {
            String filename=path+'/'+name;
            Vector info=getVFSInfo(filename);
            if(info!=null)
            {
                filename=(String)info.firstElement();
                if(vfs!=null) vfs.removeElement(info);
                CMClass.DBEngine().DBDeleteVFSFile(filename);
            }
            if(vfsBits<0) vfsBits=0;
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(author());
            vfs.addElement(info);
            CMClass.DBEngine().DBCreateVFSFile(filename,vfsBits,author(),O);
            return true;
        }
        try
        {
            File F=new File(localPath+pathSeparator+name);
            FileWriter FW=new FileWriter(F);
            FW.write(saveBufNormalize(O).toString());
            FW.close();
            return true;
        }
        catch(IOException e)
        {
            Log.errOut("Resources","Error Saving "+localPath+pathSeparator+name+": "+e.getMessage());
        }
        return false;
    }

    public boolean mkdir()
    {
        if(exists())
        {
            Log.errOut("Resources","File exists '"+path+"/"+name+"'.");
            return false;
        }
        int order=determineSavabilityAndFixBits(new Vector());
        if(order==0)
        {
            Log.errOut("Resources","Access error making directory '"+path+"/"+name+"'.");
            return false;
        }
        if(order==1)
        {
            String filename=path+'/'+name;
            Vector info=getVFSInfo(filename);
            filename=filename+"/";
            if(info==null) info=getVFSInfo(filename);
            if(info!=null)
            {
                Log.errOut("Resources","File exists '"+path+"/"+name+"'.");
                return false;
            }
            if(vfsBits<0) vfsBits=0;
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(author());
            vfs.addElement(info);
            CMClass.DBEngine().DBCreateVFSFile(filename,vfsBits,author(),new StringBuffer(""));
            return true;
        }
        File F=new File(localPath+pathSeparator+name);
        if(F.exists())
        {
            Log.errOut("Resources","File exists '"+path+"/"+name+"'.");
            return false;
        }
        return F.mkdir();
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
        filename=vfsifyFilename(filename);
        Vector file=null;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
                return CMClass.DBEngine().DBReadVFSFile((String)file.firstElement());
        }
        return null;
    }
    
    private static boolean doesFilenameExistInVFS(String filename)
    {
        Vector file=null;
        Vector vfs=getVFSDirectory();
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
                return true;
        }
        return false;
    }
    
    private static boolean doesExistAsPathInVFS(String filename)
    {
        Vector file=null;
        Vector vfs=getVFSDirectory();
        filename=vfsifyFilename(filename);
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).toUpperCase().startsWith(filename+'/'))
                return true;
        }
        return false;
    }
    
    public CMFile[] listFiles()
    {
        if(!isDirectory()) return new CMFile[0];
        Vector dir=new Vector();
        Vector fcheck=new Vector();
        Vector info=null;
        String thisDir=path+'/'+name;
        Vector vfs=getVFSDirectory();
        for(int v=0;v<vfs.size();v++)
        {
            info=(Vector)vfs.elementAt(v);
            String totalVFSString=(String)info.firstElement();
            if(totalVFSString.toUpperCase().startsWith(thisDir.toUpperCase()+"/")) // SOMETHING in this vfs dir
            {
                String entryName=totalVFSString.substring((thisDir+"/").length());
                int x=entryName.indexOf("/");
                if(x>0) // this entry is a dir in our dir! 
                {
                    entryName=entryName.substring(0,x);
                    String thisPath=(thisDir.length()>0)?thisDir+"/"+entryName:entryName;
                    if((!fcheck.contains(entryName.toUpperCase()))
                    &&((accessor==null)||(CMSecurity.canTraverseDir(accessor,accessor.location(),thisPath))))
                    {
                        fcheck.addElement(entryName.toUpperCase());
                        CMFile CF=new CMFile(thisPath,accessor,false);
                        CF.isAccessible=true;
                        CF.vfsBits=Util.unsetb(CF.vfsBits,CMFile.VFS_MASK_NONEXISTANT);
                        dir.addElement(CF);
                    }
                }
                else
                {
                    String thisPath=(thisDir.length()>0)?thisDir+"/"+entryName:entryName;
                    if((!fcheck.contains(entryName.toUpperCase()))
                    &&((accessor==null)||CMSecurity.canAccessFile(accessor,accessor.location(),thisPath,true)))
                    {
                        fcheck.addElement(entryName.toUpperCase());
                        dir.addElement(new CMFile(thisPath,accessor,false));
                    }
                }
            }
        }
        
        File F=null;
        if(thisDir.length()==0) F=new File(".");
        else F=new File(thisDir.replace('/',CMFile.pathSeparator));
        if(F.isDirectory())
        {
            String[] list=F.list();
            File F2=null;
            for(int l=0;l<list.length;l++)
            {
                String thisFile=(thisDir.length()>0)?thisDir+"/"+list[l]:list[l];
                String thisName=list[l];
                F2=new File(thisFile.replace('/',CMFile.pathSeparator));
                if(F2.isDirectory())
                {
                    if((!fcheck.contains(thisName.toUpperCase()))
                    &&((accessor==null)||(CMSecurity.canTraverseDir(accessor,accessor.location(),thisFile))))
                    {
                        CMFile CF=new CMFile(path,accessor,false);
                        CF.isAccessible=true;
                        CF.vfsBits=Util.unsetb(CF.vfsBits,CMFile.VFS_MASK_NONEXISTANT);
                        dir.addElement(CF);
                    }
                }
                else
                if((!fcheck.contains(thisName.toUpperCase()))
                &&((accessor==null)||CMSecurity.canAccessFile(accessor,accessor.location(),thisFile,false)))
                {
                    fcheck.addElement(thisName.toUpperCase());
                    dir.addElement(new CMFile(thisFile,accessor,false));
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
            vfs=CMClass.DBEngine().DBReadVFSDirectory();
        return vfs;
    }
    
    public static String vfsifyFilename(String filename)
    {
        filename=filename.trim();
        if(filename.startsWith("::"))
            filename=filename.substring(2);
        if(filename.startsWith("||"))
            filename=filename.substring(2);
        while(filename.startsWith("/")) filename=filename.substring(1);
        while(filename.startsWith("\\")) filename=filename.substring(1);
        while(filename.endsWith("/"))
            filename=filename.substring(0,filename.length()-1);
        while(filename.endsWith("\\"))
            filename=filename.substring(0,filename.length()-1);
        return filename.replace(File.separatorChar,'/');
    }
    
    private static StringBuffer saveBufNormalize(StringBuffer myRsc)
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
    
    
}
