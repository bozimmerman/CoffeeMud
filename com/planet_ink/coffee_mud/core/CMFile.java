package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.Areas.interfaces.Area;
import com.planet_ink.coffee_mud.Common.interfaces.Clan;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.WorldMap;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.database.DBConnections;
import com.planet_ink.coffee_mud.core.interfaces.MudHost;
import com.planet_ink.coffee_web.interfaces.FileManager;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
/**
 * An object to abstractly access the CoffeeMud File System (CMFS), which
 * is a layer of database-based files (VFS) on top of the normal systems
 * file system.  It uses the unix path system.
 *
 * @author Bo Zimmerman
 */
public class CMFile extends File
{
	private static volatile AtomicLong OpenLocalFiles = new AtomicLong(0);
	
	private static final long serialVersionUID = -3965083655590304708L;

	/** Flag for a CMVFSDir object, allowing you to open it */
	public static final int  VFS_MASK_MASKSAVABLE=1+2+4;

	/** Flag for opening a CMFile to suppress error logging */
	public static final int FLAG_LOGERRORS 	= 1;
	/** Flag for opening a CMFile to force allowing a given user to open despite security */
	public static final int FLAG_FORCEALLOW = 2;

	//private static final int VFS_MASK_BINARY=1;
	private static final int VFS_MASK_DIRECTORY=2;
	private static final int VFS_MASK_HIDDEN=4;
	private static final int VFS_MASK_ISLOCAL=8;
	private static final int VFS_MASK_NOWRITEVFS=16;
	private static final int VFS_MASK_NOWRITELOCAL=32;
	private static final int VFS_MASK_NOREADVFS=64;
	private static final int VFS_MASK_NOREADLOCAL=128;
	private static final int VFS_MASK_NODELETEANY=256;

	private static final char		pathSeparator		= File.separatorChar;

	private static final String		inCharSet			= Charset.defaultCharset().name();
	private static final String		outCharSet			= Charset.defaultCharset().name();

	private static CMVFSDir[]		vfs					= new CMVFSDir[256];
	
	private static CatalogLibrary	catalogPluginAdded	= null;
	private static WorldMap			mapPluginAdded		= null;

	private boolean	logErrors		= false;
	private int		vfsBits			= 0;
	private String	localPath		= null;
	private String	path			= null;
	private String	name			= null;
	private String	author			= null;
	private MOB		accessor		= null;
	private long	modifiedDateTime= System.currentTimeMillis();
	private File	localFile		= null;
	private boolean	demandVFS		= false;
	private boolean	demandLocal		= false;
	private String	parentDir		= null;

	/**
	 * Constructor for a CMFS file from a full path. The creating/opening
	 * user may be null to ignore user security features.  Will always
	 * suppress error logging, and if a user is given, they must have
	 * permission to open/create the file.
	 * @param absolutePath the path to the file to open/create
	 * @param user the user to check for permissions on
	 */
	public CMFile (final String absolutePath, final MOB user)
	{
		this(null, absolutePath,user,0);
	}

	/**
	 * Constructor for a CMFS file from a full path. The creating/opening
	 * user may be null to ignore user security features.  For flags, see
	 * {@link #FLAG_LOGERRORS} and {@link #FLAG_FORCEALLOW}
	 * @param absolutePath the path to the file to open/create
	 * @param user the user to check for permissions on
	 * @param flagBitmap bitmap flag to turn on error logging or force allow
	 */
	public CMFile (String absolutePath, final MOB user, final int flagBitmap)
	{
		this(null, absolutePath,user,flagBitmap);
	}

	private CMFile (CMVFSFile info, final String absolutePath, final MOB user)
	{
		this(info, absolutePath,user,0);
	}

	private CMFile (CMVFSFile info, String absolutePath, final MOB user, final int flagBitmap)
	{
		super(parsePathParts(absolutePath)[2]);
		final boolean pleaseLogErrors=(flagBitmap&FLAG_LOGERRORS)>0;
		final boolean forceAllow=(flagBitmap&FLAG_FORCEALLOW)>0;
		accessor=user;
		localFile=null;
		logErrors=pleaseLogErrors;
		demandLocal=absolutePath.trim().startsWith("//");
		demandVFS=absolutePath.trim().startsWith("::");
		if(accessor!=null)
			author=accessor.Name();
		final String[] pathParts=parsePathParts(absolutePath);
		absolutePath=pathParts[0];
		path=pathParts[1];
		name=pathParts[2];
		parentDir=path;
		localPath=path.replace('/',pathSeparator);
		// fill in all we can
		vfsBits=0;
		if(info==null)
			info=getVFSInfo(absolutePath);
		String ioPath=getIOReadableLocalPathAndName();
		localFile=new File(ioPath);
		if(!localFile.exists())
		{
			File localDir=new File(".");
			int endZ=-1;
			boolean found=true;
			if((localDir.exists())&&(localDir.isDirectory()))
				parentDir="//"+localPath;
			while((!localFile.exists())&&(endZ<ioPath.length())&&(localDir.exists())&&(localDir.isDirectory())&&(found))
			{
				final int startZ=endZ+1;
				endZ=ioPath.indexOf(pathSeparator,startZ);
				if(endZ<0)
					endZ=ioPath.length();
				final String[] files=localDir.list();
				found=false;
				for(int f=0;f<files.length;f++)
				{
					if(files[f].equalsIgnoreCase(ioPath.substring(startZ,endZ)))
					{
						if(!files[f].equals(ioPath.substring(startZ,endZ)))
							ioPath=ioPath.substring(0,startZ)+files[f]+((endZ<ioPath.length())?ioPath.substring(endZ):"");
						found=true;
						break;
					}
				}
				if(found)
				{
					if(endZ==ioPath.length())
					{
						final int lastSep=ioPath.lastIndexOf(pathSeparator);
						if(lastSep>=0)
						{
							localPath=ioPath.substring(0,lastSep);
							name=ioPath.substring(lastSep+1);
						}
						else
							name=ioPath;
						localFile=new File(getIOReadableLocalPathAndName());
					}
					else
						localDir=new File(localDir.getAbsolutePath()+pathSeparator+ioPath.substring(startZ,endZ));
				}
			}
		}
		else
			parentDir="::"+parentDir;

		if((info!=null)&&((!demandLocal)||(!localFile.exists())))
		{
			vfsBits=vfsBits|info.getMaskBits(accessor);
			author=info.author;
			modifiedDateTime=info.modifiedDateTime;
		}
		else
		{
			modifiedDateTime=localFile.lastModified();
			if(localFile.isHidden())
				vfsBits=vfsBits|CMFile.VFS_MASK_HIDDEN;
		}

		final boolean doesFilenameExistInVFS=(info!=null) && (info.path.equalsIgnoreCase(absolutePath) || info.path.equalsIgnoreCase(absolutePath+"/"));
		final boolean doesExistAsPathInVFS= (info!=null) && doesFilenameExistInVFS 
										&& ((info instanceof CMVFSDir)||(CMath.bset(info.mask, CMFile.VFS_MASK_DIRECTORY)));
		if((info!=null)&&(!doesFilenameExistInVFS))
			Log.debugOut(new Exception("CMFile sent: '"+absolutePath+"' != vfs found:'"+info.path+"' fetch/create error."));

		final boolean isADirectory=((localFile!=null)&&(localFile.exists())&&(localFile.isDirectory()))
						   ||doesExistAsPathInVFS;
		final boolean allowedToTraverseAsDirectory=isADirectory
										   &&((accessor==null)||CMSecurity.canTraverseDir(accessor,accessor.location(),absolutePath));
		final boolean allowedToWriteVFS=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,true)));
		final boolean allowedToReadVFS=(doesFilenameExistInVFS&&allowedToWriteVFS)||allowedToTraverseAsDirectory;
		final boolean allowedToWriteLocal=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,false)));
		final boolean allowedToReadLocal=(localFile!=null)
									&&(localFile.exists())
									&&(allowedToWriteLocal||allowedToTraverseAsDirectory);

		if(!allowedToReadVFS)
			vfsBits=vfsBits|CMFile.VFS_MASK_NOREADVFS;

		if(!allowedToReadLocal)
			vfsBits=vfsBits|CMFile.VFS_MASK_NOREADLOCAL;

		if(!allowedToWriteVFS)
			vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITEVFS;

		if(!allowedToWriteLocal)
			vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITELOCAL;

		if(allowedToTraverseAsDirectory)
			vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;

		if((demandVFS)&&(!allowedToReadVFS))
			vfsBits=vfsBits|CMFile.VFS_MASK_NOREADVFS;
		if((demandVFS)&&(!allowedToWriteVFS))
			vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITEVFS;

		if((demandLocal)&&(!allowedToReadLocal))
			vfsBits=vfsBits|CMFile.VFS_MASK_NOREADLOCAL;
		if((demandLocal)&&(!allowedToWriteLocal))
			vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITELOCAL;

		if((!demandVFS)
		&&(demandLocal||((!allowedToReadVFS) && (allowedToWriteLocal))))
			vfsBits=vfsBits|CMFile.VFS_MASK_ISLOCAL;
	}

	/**
	 *
	 * @author Bo Zimmerman
	 * Class to hold an internal database (VFS) file, or directory
	 */
	public static class CMVFSFile
	{
		private String		fName;
		private String		uName;
		protected String	path;
		protected int		mask;
		private long		modifiedDateTime;
		private String		author;
		private Object		data	= null;

		/**
		 * Creates an internal VFS file based on given variables.  Mask bitmap is determined by
		 * {@link CMFile#VFS_MASK_DIRECTORY}, {@link CMFile#VFS_MASK_HIDDEN} etc.
		 * @param path full path and filename
		 * @param mask bitmap of file bits
		 * @param modifiedDateTime creation/modified time for the vfs file
		 * @param author the author/owner of the file
		 */
		public CMVFSFile(final String path, final int mask, final long modifiedDateTime, final String author)
		{
			this.path=path;
			this.fName=path;
			int x=path.lastIndexOf('/');
			if(x==path.length()-1)
				x=path.lastIndexOf('/',path.length()-2);
			if(x>=0)
				this.fName=path.substring(x+1);
			while(this.fName.startsWith("/"))
				this.fName=this.fName.substring(1);
			while(this.fName.endsWith("/"))
				this.fName=this.fName.substring(0,this.fName.length()-1);
			this.uName=this.fName.toUpperCase();
			this.mask=mask;
			this.modifiedDateTime=modifiedDateTime;
			this.author=author;
		}

		/**
		 * Makes the given VFS file identical to this one
		 * @param f2 the VFS file object to alter
		 */
		public void copyInto(final CMVFSFile f2)
		{
			f2.author=author;
			f2.data=data;
			f2.fName=fName;
			f2.mask=mask;
			f2.modifiedDateTime=modifiedDateTime;
			f2.path=path;
			f2.uName=uName;
			if((this instanceof CMVFSDir)&&(f2 instanceof CMVFSDir))
			{
				((CMVFSDir)f2).parent=((CMVFSDir)this).parent;
				if((((CMVFSDir)this).files!=null)
				&&(((CMVFSDir)this).files.length>0)
				&&(((CMVFSDir)f2).files==null))
					((CMVFSDir)f2).files=((CMVFSDir)this).files;
			}
		}

		/**
		 * Get the full path of this VFS file and name
		 * @return the full path and name of this vfs file
		 */
		public String getPath()
		{
			return path;
		}

		/**
		 * Return bits associated with this file.
		 * {@link CMFile#VFS_MASK_DIRECTORY}, {@link CMFile#VFS_MASK_HIDDEN} etc.
		 * @param accessor the mob who wants to access this file.
		 * @return bits associated with this file.
		 */
		public int getMaskBits(MOB accessor)
		{
			return mask;
		}

		/**
		 * Returns the object associated with the data of this file.
		 * Can be a String, StringBuffer, byte[] array, or null.
		 * @return the object associated with the data of this file.
		 */
		public Object readData()
		{
			final CMVFSFile f=CMLib.database().DBReadVFSFile(path);
			if(f!=null)
				return f.data;
			return null;
		}

		/**
		 * Creates, Adds, and saves a VFS file with the given stats.  There is no known
		 * connection to the wrapping CMVFSFile.
		 * {@link CMFile#VFS_MASK_DIRECTORY}, {@link CMFile#VFS_MASK_HIDDEN} etc.
		 * @param filename the full path and name of the file
		 * @param vfsBits masking bits
		 * @param author the author/owner of the file
		 * @param O the string, stringbuffer, byte[], or null
		 */
		public void saveData(String filename, int vfsBits, String author, Object O)
		{
			final CMVFSFile info = new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author);
			getVFSDirectory().add(info);
			CMLib.database().DBUpSertVFSFile(filename,vfsBits,author,System.currentTimeMillis(),O);
		}

		/**
		 * Changes the internal data object of this file,
		 * typically during read.
		 * @param o the string, stringbuffer, byte[] or null object
		 */
		public void setData(Object o)
		{
			this.data=o;
		}
	}

	/**
	 * A class to represent a VFS/database directory.  An extension of CMVFSFile
	 * @author Bo Zimmerman
	 *
	 */
	public static class CMVFSDir extends CMVFSFile
	{
		protected CMVFSFile[] files=null;
		protected CMVFSDir parent=null;

		public static Comparator<CMVFSFile> fcomparator=new Comparator<CMVFSFile>()
		{
			@Override
			public int compare(final CMVFSFile arg0, final CMVFSFile arg1)
			{
				return arg0.uName.compareTo(arg1.uName);
			}
		};

		protected CMVFSFile[] getFiles()
		{
			return files;
		}

		/**
		 * Creates a new directory
		 *
		 * @param parent the directory containing this one
		 * @param path the full path and name of this vfs file
		 */
		public CMVFSDir(final CMVFSDir parent, final String path)
		{
			super(path,VFS_MASK_DIRECTORY,System.currentTimeMillis(),"SYS");
			this.parent=parent;
		}

		/**
		 * Creates a new directory
		 * {@link CMFile#VFS_MASK_DIRECTORY}, {@link CMFile#VFS_MASK_HIDDEN} etc.
		 *
		 * @param parent the directory containing this one
		 * @param mask bitmap of info about this directory
		 * @param path the full path and name of this vfs file
		 */
		public CMVFSDir(final CMVFSDir parent, final int mask, final String path)
		{
			super(path,mask|VFS_MASK_DIRECTORY,System.currentTimeMillis(),"SYS");
			this.parent=parent;
		}

		private CMVFSDir(final CMVFSDir parent, final String path, final int mask, final long modifiedDateTime, final String author)
		{
			super(path,mask,modifiedDateTime,author);
			this.parent=parent;
		}

		/**
		 * Returns a subdirectory of this directory at the given path, and
		 * possibly creates it if not found.
		 * @param path the path to look down
		 * @param create true to create if not found, false just to search
		 * @return the found subdir, or null
		 */
		public synchronized CMVFSDir fetchSubDir(final String path, final boolean create)
		{
			final String[] ppath=path.split("/");
			CMVFSDir currDir=this;
			for(final String p : ppath)
			{
				if(p.length()>0)
				{
					synchronized(currDir)
					{
						final CMVFSDir key = new CMVFSDir(currDir,currDir.getPath()+p+"/");
						int dex = -1;
						final CMVFSFile[] files=currDir.getFiles();
						if(files!=null)
							dex=Arrays.binarySearch(files, key, fcomparator);
						if((files!=null)&&(dex>=0))
						{
							if(files[dex] instanceof CMVFSDir)
							{
								currDir=(CMVFSDir)files[dex];
								continue;
							}
							return null; // found a step, but its a file, not a subdir
						}
						else
						if(!create)
							return null;
						if(files==null)
							currDir.files=new CMVFSFile[0];
						currDir.files=Arrays.copyOf(currDir.files, currDir.files.length+1);
						currDir.files[currDir.files.length-1]=key;
						Arrays.sort(currDir.files,fcomparator);
						currDir=key;
					}
				}
			}
			return currDir;
		}

		/**
		 * Adds the given vfs file to this directory
		 * @param f the file to add
		 * @return true if successful, false otherwise
		 */
		public final boolean add(CMVFSFile f)
		{
			int x=f.path.lastIndexOf('/');
			if(x==f.path.length()-1)
				x=f.path.lastIndexOf('/',x-1);
			CMVFSDir subDir=this;
			if((x>0)&&(!f.path.substring(0,x+1).equals(path)))
				subDir=fetchSubDir(f.path.substring(0,x),true);
			if(subDir!=null)
			synchronized(subDir)
			{
				if(subDir.files==null)
					subDir.files=new CMVFSFile[0];
				final CMVFSFile old = subDir.get(f);
				if(old!=null)
					f.copyInto(old);
				else
				{
					subDir.files=Arrays.copyOf(subDir.files, subDir.files.length+1);
					if(CMath.bset(f.mask, CMFile.VFS_MASK_DIRECTORY))
					{
						if(!(f instanceof CMVFSDir))
						{
							final CMVFSDir d = new CMVFSDir(subDir,f.path,f.mask,f.modifiedDateTime,f.author);
							f.copyInto(d);
							f=d;
						}
					}
					subDir.files[subDir.files.length-1]=f;
				}
				Arrays.sort(subDir.files,fcomparator);
			}
			else
				return false;
			return true;
		}

		/**
		 * Deletes the given VFS file from this directory.
		 * @param file the file to delete
		 * @return true if deleted, false otherwise
		 */
		public final boolean delete(final CMVFSFile file)
		{
			final CMVFSDir dir = vfsV();
			if(dir==null)
				return false;
			final int x=file.path.lastIndexOf('/');
			CMVFSDir subDir=dir;
			if(x>0)
				subDir=dir.fetchSubDir(file.path.substring(0,x),true);
			synchronized(subDir)
			{
				if((x==file.path.length()-1)&&(subDir.parent!=null))
				{
					if(subDir.parent.files!=null)
					{
						final List<CMVFSFile> list = new Vector<CMVFSFile>();
						list.addAll(Arrays.asList(subDir.parent.files));
						if(!list.remove(subDir))
							return false;
						subDir.parent.files = list.toArray(new CMVFSFile[0]);
						return true;
					}
					else
						return false;
				}
				else
				{
					final List<CMVFSFile> list = new Vector<CMVFSFile>();
					if(subDir.files==null)
						return false;
					list.addAll(Arrays.asList(subDir.files));
					if(!list.remove(file))
						return false;
					subDir.files = list.toArray(new CMVFSFile[0]);
					return true;
				}
			}
		}

		private final synchronized CMVFSFile get(final String fileName)
		{
			final CMVFSFile[] files=getFiles();
			if(files==null)
				return null;
			final CMVFSFile key = new CMVFSFile(fileName, 0, 0, "");
			final int dex = Arrays.binarySearch(files, key, fcomparator);
			if(dex>=0)
				return files[dex];
			return null;
		}

		private final synchronized CMVFSFile get(final CMVFSFile file)
		{
			final CMVFSFile[] files=getFiles();
			if(files==null)
				return null;
			final int dex = Arrays.binarySearch(files, file, fcomparator);
			if(dex>=0)
				return file;
			return null;
		}

		/**
		 * Returns the vfs file at the given path starting here.
		 * @param filePath the path to get the file from
		 * @return the file at that path, or null if nonexistant.
		 */
		public final synchronized CMVFSFile fetch(final String filePath)
		{
			final int x=filePath.lastIndexOf('/');
			CMVFSDir dir = this;
			if(x>=0)
				dir = this.fetchSubDir(filePath.substring(0,x), false);
			if(dir==null)
				return null;
			if(x==filePath.length()-1)
				return dir;
			final String fileName=(x<0)?filePath:filePath.substring(x+1).trim();
			if(fileName.length()==0)
				return dir;
			return dir.get(fileName);
		}
	}

	private static final CMVFSDir vfsV()
	{
		return vfs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}

	@Override
	public final CMFile getParentFile()
	{
		return new CMFile(path, accessor);
	}

	/**
	 * Weird function.  Returns true if the file is a readable non-directory,
	 * or an existing specified VFS directory.
	 * @return true under weird conditions
	 */
	public final boolean mustOverwrite()
	{
		if(!isDirectory())
			return canRead();
		if(isLocalFile())
			return ((localFile!=null)&&(localFile.isDirectory()));
		String filename=getVFSPathAndName();
		CMVFSFile info=getVFSInfo(filename);
		if(!filename.endsWith("/"))
			filename=filename+"/";
		if(info==null)
			info=getVFSInfo(filename);
		return (info!=null);
	}

	@Override
	public final boolean canExecute()
	{
		return false;
	}

	@Override
	public final boolean canRead()
	{
		if(!exists())
			return false;
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

	@Override
	public final boolean canWrite()
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

	/**
	 * Returns true if this file was opened only as a vfs file,
	 * and not as a local file.  This is a request status.
	 * @return true if this file was opened only as a vfs file
	 */
	public final boolean demandedVFS()
	{
		return demandVFS;
	}

	/**
	 * Returns true if this file was opened only as a local file,
	 * and not as a VFS file.  This is a request status.
	 * @return true if this file was opened only as a local file
	 */
	public final boolean demandedLocal()
	{
		return demandLocal;
	}

	@Override 
	public final boolean isDirectory() 
	{ 
		return exists() && CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY); 
	}

	@Override 
	public final boolean exists() 
	{ 
		return !((demandLocal||CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS))
					&& (demandVFS || CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL))); 
	}

	@Override 
	public final boolean isFile() 
	{ 
		return canRead()&&(!CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY)); 
	}

	@Override
	public final long lastModified()
	{
		return modifiedDateTime;
	}
	
	/**
	 * Returns the author of this file, if available.
	 * @return the author of this file
	 */
	public final String author()
	{
		return ((author != null)) ? author : "SYS_UNK";
	}

	/**
	 * Returns true if writing to this will write to a local file.
	 * Basically if VFS is not demanded and either Local FS is demanded, or
	 * the opener can't write to VFS but can write to a local file (weird security)
	 * @return true if this cmfile is a local file for sure
	 */
	public final boolean isLocalFile()
	{
		return CMath.bset(vfsBits, CMFile.VFS_MASK_ISLOCAL);
	}

	/**
	 * Returns true if writing to this will write to a VFS file.
	 * @return true if writing to this will write to a VFS file.
	 */
	public final boolean isVFSFile()
	{
		return (!CMath.bset(vfsBits, CMFile.VFS_MASK_ISLOCAL));
	}

	/**
	 * Returns true if reading this file as VFS is possible, or this directory is permitted.
	 * @return true if reading this file as VFS is possible, or this directory is permitted.
	 */
	public final boolean canVFSEquiv()
	{
		return (!CMath.bset(vfsBits, CMFile.VFS_MASK_NOREADVFS));
	}

	/**
	 * Returns true if reading this file as local file is possible, or this directory is permitted.
	 * @return true if reading this file as local file is possible, or this directory is permitted.
	 */
	public final boolean canLocalEquiv()
	{
		return (!CMath.bset(vfsBits, CMFile.VFS_MASK_NOREADLOCAL));
	}

	@Override 
	public final String getName() 
	{ 
		return name; 
	}

	@Override 
	public final String getAbsolutePath() 
	{ 
		return "/"+getVFSPathAndName(); 
	}

	@Override 
	public final String getCanonicalPath() 
	{ 
		return getVFSPathAndName(); 
	}

	/**
	 * Returns local file path and name in simple form.
	 * @return local file path and name
	 */
	public final String getLocalPathAndName()
	{
		if(path.length()==0)
			return name;
		return localPath+pathSeparator+name;
	}

	/**
	 * Returns the local path and name that can be used
	 * for a local file.  If empty, returns "."
	 * @return the local path and name
	 */
	public final String getIOReadableLocalPathAndName()
	{
		final String s=getLocalPathAndName();
		if(s.trim().length()==0)
			return ".";
		return s;
	}

	/**
	 * Returns the path and name of this file.
	 * @return the path and name of this file
	 */
	public final String getVFSPathAndName()
	{
		if(path.length()==0)
			return name;
		return path+'/'+name;
	}

	/**
	 * If this file is a directory and the directory
	 * is empty of other stuff.
	 * @return true if the file is a deletable director
	 */
	public final boolean mayDeleteIfDirectory()
	{
		if(!isDirectory())
			return true;
		if(localFile!=null)
		{
			if(localFile.isDirectory() && localFile.exists())
			{
				if(localFile.list().length>0)
					return false;
			}
			else
			if((!localFile.isDirectory())&&(localFile.isFile()))
				return false;
		}
		CMVFSDir vfsDir=getVFSDirectory().fetchSubDir(getVFSPathAndName().toUpperCase(), false);
		if(vfsDir!=null)
		{
			if((vfsDir.getFiles()!=null)&&(vfsDir.getFiles().length>0))
				return false;
		}
		return true;
	}

	/**
	 * If permitted, deletes this file from local
	 * filesystem
	 * @return true if deleted, false otherwise
	 */
	public final boolean deleteLocal()
	{
		if(!exists())
			return false;
		if(!canWrite())
			return false;
		if(CMath.bset(vfsBits, CMFile.VFS_MASK_NODELETEANY))
			return false;
		if(!mayDeleteIfDirectory())
			return false;
		if((canLocalEquiv())&&(localFile!=null))
			return localFile.delete();
		return false;
	}

	/**
	 * If permitted, deletes this file from VFS
	 * (database) filesystem
	 * @return true if deleted, false otherwise
	 */
	public final boolean deleteVFS()
	{
		if(!exists())
			return false;
		if(!canWrite())
			return false;
		if(CMath.bset(vfsBits, CMFile.VFS_MASK_NODELETEANY))
			return false;
		if(!mayDeleteIfDirectory())
			return false;
		if(canVFSEquiv())
		{
			String name=getVFSPathAndName();
			if(isDirectory())
				name=name+"/";
			final CMVFSFile info=getVFSInfo(name);
			if(info==null)
				return false;
			CMLib.database().DBDeleteVFSFile(info.path);
			getVFSDirectory().delete(info);
			return true;
		}
		return false;
	}

	/**
	 * Same as delete, though it deletes both vfs and local
	 * versions of a file unless the file is specified as
	 * vfs or local.
	 * @return true if 2 files deleted, false otherwise
	 */
	public final boolean deleteAll()
	{
		if(!exists())
			return false;
		if(!canWrite())
			return false;
		if(CMath.bset(vfsBits, CMFile.VFS_MASK_NODELETEANY))
			return false;
		if(!mayDeleteIfDirectory())
			return false;
		if(demandVFS)
			return deleteVFS();
		if(demandLocal)
			return deleteLocal();
		final boolean delVfs=deleteVFS();
		final boolean delLoc=deleteLocal();
		return delVfs || delLoc;
	}

	@Override
	public final boolean delete()
	{
		if(!exists())
			return false;
		if(!canWrite())
			return false;
		if(CMath.bset(vfsBits, CMFile.VFS_MASK_NODELETEANY))
			return false;
		if(!mayDeleteIfDirectory())
			return false;
		if((isVFSDirectory())&&(!demandLocal))
			return deleteVFS();
		if((isLocalDirectory())&&(!demandVFS))
			return deleteLocal();
		if(isVFSFile())
			return deleteVFS();
		if(isLocalFile())
			return deleteLocal();
		return false;
	}

	/**
	 * Reads and returns all of the data in this file as an
	 * converted text stream.  This means the line-ends
	 * are converted to mud-format.
	 * @return all of the data in this file as a stream
	 */
	public final InputStream getTextStream()
	{
		return new ByteArrayInputStream(text().toString().getBytes());
	}

	/**
	 * Reads and returns all of the data in this file as an
	 * converted text stringbuffer.  This means the line-ends
	 * are converted to mud-format.
	 * @return all of the data in this file as a stringbuffer
	 */
	public final StringBuffer text()
	{
		final StringBuffer buf=new StringBuffer("");
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
			return buf;
		}
		if(isVFSFile())
		{
			final CMVFSFile info=getVFSInfo(getVFSPathAndName());
			if(info!=null)
			{
				final Object data=getVFSData(getVFSPathAndName());
				if(data==null)
					return buf;
				if(data instanceof String)
					return new StringBuffer((String)data);
				if(data instanceof StringBuffer)
					return (StringBuffer)data;
				if(data instanceof StringBuilder)
					return new StringBuffer((StringBuilder)data);
				if(data instanceof byte[])
					return new StringBuffer(CMStrings.bytesToStr((byte[])data));
			}
			else
			if(logErrors)
				Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
			return buf;
		}

		if(CMFile.OpenLocalFiles.get() >= 256)
		{
			Log.errOut("CMFile","Local File '"+getVFSPathAndName()+"' not be opened.");
			Log.errOut("CMFile",new Exception());
			return buf;
		}
		
		BufferedReader reader = null;
		try
		{
			CMFile.OpenLocalFiles.addAndGet(1);
			String charSet=CMProps.getVar(CMProps.Str.CHARSETINPUT);
			if((charSet==null)||(charSet.length()==0))
				charSet=inCharSet;
			reader=new BufferedReader(
				   new InputStreamReader(
				   new FileInputStream(
					   getIOReadableLocalPathAndName()
			),charSet));
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
		}
		catch(final Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
			return buf;
		}
		finally
		{
			CMFile.OpenLocalFiles.addAndGet(-1);
			try
			{
				if ( reader != null )
				{
					reader.close();
					reader = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	/**
	 * Reads and returns all of the data in this file as an
	 * unconverted text stream.  This means the line-ends
	 * aren't converted to mud-format.
	 * @return all of the data in this file as a stream
	 */
	public final InputStream getUnformattedTextStream()
	{
		return new ByteArrayInputStream(text().toString().getBytes());
	}

	/**
	 * Reads and returns all of the data in this file as an
	 * unconverted text stringbuffer.  This means the line-ends
	 * aren't converted to mud-format.
	 * @return all of the data in this file as a stringbuffer
	 */
	public final StringBuffer textUnformatted()
	{
		final StringBuffer buf=new StringBuffer("");
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
			return buf;
		}
		if(isVFSFile())
		{
			final CMVFSFile info=getVFSInfo(getVFSPathAndName());
			if(info!=null)
			{
				final Object data=getVFSData(getVFSPathAndName());
				if(data==null)
					return buf;
				if(data instanceof String)
					return new StringBuffer((String)data);
				if(data instanceof StringBuffer)
					return (StringBuffer)data;
				if(data instanceof StringBuilder)
					return new StringBuffer((StringBuilder)data);
				if(data instanceof byte[])
					return new StringBuffer(CMStrings.bytesToStr((byte[])data));
			}
			else
			if(logErrors)
				Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
			return buf;
		}

		if(CMFile.OpenLocalFiles.get() >= 256)
		{
			Log.errOut("CMFile","Local File '"+getVFSPathAndName()+"' not be opened.");
			Log.errOut("CMFile",new Exception());
			return buf;
		}
		
		Reader F = null;
		try
		{
			CMFile.OpenLocalFiles.addAndGet(1);
			String charSet=CMProps.getVar(CMProps.Str.CHARSETINPUT);
			if((charSet==null)||(charSet.length()==0))
				charSet=inCharSet;
			F=new InputStreamReader(
			   new FileInputStream(
				   getIOReadableLocalPathAndName()
			 ),charSet);
			char c=' ';
			while(F.ready())
			{
				c=(char)F.read();
				if(c<0)
					break;
				buf.append(c);
			}
		}
		catch(final Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
		}
		finally
		{
			CMFile.OpenLocalFiles.addAndGet(-1);
			try
			{
				if ( F != null )
				{
					F.close();
					F = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	/**
	 * Reads and returns all of the data in this file as a byte
	 * input stream.
	 * @return all of the data in this file as a stream
	 */
	public final InputStream getRawStream()
	{
		return new ByteArrayInputStream(raw());
	}

	/**
	 * Reads and returns all of the data in this file as a byte array.
	 * @return all of the data in this file as a byte array
	 */
	public final byte[] raw()
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
			final CMVFSFile info=getVFSInfo(getVFSPathAndName());
			if(info!=null)
			{
				final Object data=getVFSData(getVFSPathAndName());
				if(data==null)
					return buf;
				if(data instanceof byte[])
					return (byte[])data;
				if(data instanceof String)
					return CMStrings.strToBytes((String)data);
				if(data instanceof StringBuffer)
					return CMStrings.strToBytes(((StringBuffer)data).toString());
				if(data instanceof StringBuilder)
					return CMStrings.strToBytes(((StringBuilder)data).toString());
			}
			else
			if(logErrors)
				Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
			return buf;
		}

		if(CMFile.OpenLocalFiles.get() >= 256)
		{
			Log.errOut("CMFile","Local File '"+getVFSPathAndName()+"' not be opened.");
			Log.errOut("CMFile",new Exception());
			return buf;
		}
		
		DataInputStream fileIn = null;
		try
		{
			CMFile.OpenLocalFiles.addAndGet(1);
			fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(getIOReadableLocalPathAndName()) ) );
			buf = new byte [ fileIn.available() ];
			fileIn.readFully(buf);
			fileIn.close();
		}
		catch(final Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
		}
		finally
		{
			CMFile.OpenLocalFiles.addAndGet(-1);
			try
			{
				if ( fileIn != null )
				{
					fileIn.close();
					fileIn = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	/**
	 * Converts the given bytes to a stringbuffer, if it is one,
	 * or returns null otherwise.
	 * @param bytes the bytes to convert
	 * @return stringbuffer if its text, or null otherwise
	 */
	public final StringBuffer textVersion(byte[] bytes)
	{
		final StringBuffer text=new StringBuffer(CMStrings.bytesToStr(bytes));
		for(int i=0;i<text.length();i++)
		{
			if((text.charAt(i)<0)||(text.charAt(i)>127))
				return null;
		}
		return text;
	}

	/**
	 * Saves the given data to local file if demanded, or vfs
	 * file if not.
	 * @param data string, stringbuffer, byte[], or string convertable
	 * @return true if happened without errors, false otherwise
	 */
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
			O=data;
		else
		if(data instanceof StringBuilder)
			O=new StringBuffer((StringBuilder)data);
		else
		if(data instanceof byte[])
		{
			final StringBuffer test=textVersion((byte[])data);
			if(test!=null)
				O=test;
			else
				O=data;
		}
		else
			O=new StringBuffer(data.toString());
		if((!isLocalFile())||(isVFSOnlyPathFile()))
		{
			String filename=getVFSPathAndName();
			CMVFSFile info=getVFSInfo(filename);
			if(info!=null)
			{
				filename=info.path;
				getVFSDirectory().delete(info);
			}
			if(vfsBits<0)
				vfsBits=0;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
			if(info!=null)
				info.saveData(filename,vfsBits,author(),O);
			else
			{
				info = new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author);
				getVFSDirectory().add(info);
				CMLib.database().DBUpSertVFSFile(filename,vfsBits,author,System.currentTimeMillis(),O);
			}
			return true;
		}

		FileOutputStream FW = null;
		try
		{
			final File F=new File(getIOReadableLocalPathAndName());
			if(O instanceof StringBuffer)
				O=CMStrings.strToBytes(((StringBuffer)O).toString());
			if(O instanceof StringBuilder)
				O=CMStrings.strToBytes(((StringBuilder)data).toString());
			if(O instanceof String)
				O=CMStrings.strToBytes(((String)O));
			if(O instanceof byte[])
			{
				FW=new FileOutputStream(F,false);
				FW.write((byte[])O);
				FW.flush();
				FW.close();
			}
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
			return true;
		}
		catch(final IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			try
			{
				if ( FW != null )
				{
					FW.close();
					FW = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return false;
	}

	/**
	 * Saves the given text data to local file if demanded, or vfs
	 * file if not.
	 * @param data string, stringbuffer, byte[], or string convertable
	 * @return true if happened without errors, false otherwise
	 */
	public boolean saveText(Object data)
	{ 
		return saveText(data,false); 
	}

	/**
	 * Saves the given text data to local file if demanded, or vfs
	 * file if not.
	 * @param data string, stringbuffer, byte[], or string convertable
	 * @param append true to append, false to overwrite
	 * @return true if happened without errors, false otherwise
	 */
	public boolean saveText(Object data, boolean append)
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
		if(data instanceof StringBuilder)
			O=new StringBuffer((StringBuilder)data);
		else
		if(data instanceof byte[])
			O=new StringBuffer(CMStrings.bytesToStr((byte[])data));
		else
			O=new StringBuffer(data.toString());
		if((!isLocalFile())||(isVFSOnlyPathFile()))
		{
			if(append)
				O=new StringBuffer(text().append(O).toString());
			String filename=getVFSPathAndName();
			CMVFSFile info=getVFSInfo(filename);
			if(info!=null)
			{
				filename=info.path;
				if(vfsV()!=null)
					vfsV().delete(info);
			}
			if(vfsBits<0)
				vfsBits=0;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
			// this weirdness is because of CMCatalog
			if(info!=null)
				info.saveData(filename,vfsBits,author(),O);
			else
			{
				info = new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author);
				getVFSDirectory().add(info);
				CMLib.database().DBUpSertVFSFile(filename,vfsBits,author,System.currentTimeMillis(),O);
			}
			return true;
		}

		Writer FW = null;
		try
		{
			String charSet=CMProps.getVar(CMProps.Str.CHARSETOUTPUT);
			if((charSet==null)||(charSet.length()==0))
				charSet=outCharSet;
			final File F=new File(getIOReadableLocalPathAndName());
			FW=new OutputStreamWriter(new FileOutputStream(F,append),charSet);
			FW.write(saveBufNormalize(O).toString());
			FW.close();
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
			return true;
		}
		catch(final IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			try
			{
				if ( FW != null )
				{
					FW.close();
					FW = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return false;
	}

	@Override
	public final boolean mkdir()
	{
		if(mustOverwrite())
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
			CMVFSFile info=getVFSInfo(filename);
			if(!filename.endsWith("/"))
				filename=filename+"/";
			if(info==null)
				info=getVFSInfo(filename);
			if(info!=null)
			{
				Log.errOut("CMFile","File exists '"+getVFSPathAndName()+"'.");
				return false;
			}
			if(vfsBits<0)
				vfsBits=0;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOWRITEVFS);
			vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
			info=new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author());
			vfsV().add(info);
			CMLib.database().DBUpSertVFSFile(filename,vfsBits,author(),System.currentTimeMillis(),new StringBuffer(""));
			return true;
		}
		final String fullPath=getIOReadableLocalPathAndName();
		final File F=new File(fullPath);
		File PF=F.getParentFile();
		final Vector<File> parents=new Vector<File>();
		while(PF!=null)
		{
			parents.addElement(PF);
			PF=PF.getParentFile();
		}
		for(int p=parents.size()-1;p>=0;p--)
		{
			PF=parents.elementAt(p);
			if((PF.exists())&&(PF.isDirectory()))
				continue;
			if((PF.exists()&&(!PF.isDirectory()))||(!PF.mkdir()))
			{
				Log.errOut("CMFile","Unable to mkdir '"+PF.getAbsolutePath()+"'.");
				return false;
			}
		}
		if(F.exists())
		{
			Log.errOut("CMFile","File exists '"+fullPath+"'.");
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

	@Override
	public final String[] list()
	{
		if(!isDirectory())
			return new String[0];
		final CMFile[] CF=listFiles();
		final String[] list=new String[CF.length];
		for(int f=0;f<CF.length;f++)
			list[f]=CF[f].getName();
		return list;
	}

	private static final CMVFSFile getVFSInfo(final String filename)
	{
		final CMVFSDir vfs=getVFSDirectory();
		if(vfs==null)
			return null;
		final String vfsFilename=vfsifyFilename(filename);
		return vfs.fetch(vfsFilename);
	}

	private static final Object getVFSData(final String filename)
	{
		final CMVFSFile file=getVFSInfo(filename);
		if(file!=null)
			return file.readData();
		return null;
	}

	/**
	 * If this file represents (or could represent) a VFS (database) dir,
	 * returns true.
	 * @return true if this file represents (or could represent) a VFS (database) dir
	 */
	public final boolean isVFSDirectory()
	{
		String dir=getVFSPathAndName().toLowerCase();
		if(!dir.endsWith("/"))
			dir+="/";
		final CMVFSFile file=getVFSInfo(dir);
		return (file instanceof CMVFSDir);
	}

	/**
	 * If this file represents (or could represent) a VFS (database) file,
	 * because the directory path is vfs only, this returns true.
	 * @return true if this file represents (or could represent) a VFS (database) file
	 */
	public final boolean isVFSOnlyPathFile()
	{
		if(demandLocal)
			return false;
		if(isDirectory())
			return false;
		String dir=getVFSPathAndName().toLowerCase();
		if(dir.endsWith("/"))
			return false;
		if(demandVFS)
			return true;
		int x=dir.lastIndexOf('/');
		if(x<0)
			return false;
		final CMVFSFile file=getVFSInfo(dir.substring(0,x+1));
		if(file instanceof CMVFSDir)
		{
			final CMFile localFile = new CMFile(dir.substring(0,x+1),this.accessor,this.vfsBits);
			if(localFile.isLocalDirectory())
				return false;
			return true;
		}
		return false;
	}

	/**
	 * If this file represents (or could represent) a local dir, true
	 * @return if this file represents (or could represent) a local dir
	 */
	public final boolean isLocalDirectory()
	{
		return (localFile!=null)&&(localFile.isDirectory());
	}

	@Override
	public final CMFile[] listFiles()
	{
		if((!isDirectory())||(!canRead()))
			return new CMFile[0];
		final String prefix=demandLocal?"//":(demandVFS?"::":"");
		final Vector<CMFile> dir=new Vector<CMFile>();
		final Vector<String> fcheck=new Vector<String>();
		String thisDir=getVFSPathAndName();
		CMVFSDir vfs=getVFSDirectory();
		String vfsSrchDir=thisDir+"/";
		if(thisDir.length()==0)
			vfsSrchDir="";
		if(!demandLocal)
		{
			if(vfsSrchDir.length()>0)
				vfs=vfs.fetchSubDir(vfsSrchDir, false);
			final CMVFSFile[] vfsFiles=(vfs!=null)?vfs.getFiles():null;
			if((vfsFiles!=null)&&(vfsFiles.length>0))
			{
				for(final CMVFSFile file : vfsFiles)
				{
					final CMFile CF=new CMFile(file,prefix+file.path,accessor);
					if((CF.canRead())
					&&(!fcheck.contains(file.uName)))
					{
						fcheck.addElement(file.uName);
						dir.addElement(CF);
					}
				}
			}
		}

		if(!demandVFS)
		{
			thisDir=getIOReadableLocalPathAndName();
			final File F=new File(thisDir);
			if(F.isDirectory())
			{
				final File[] list=F.listFiles();
				File F2=null;
				for (final File element : list)
				{
					F2=element;
					final String thisPath=vfsifyFilename(thisDir)+"/"+F2.getName();
					final String thisName=F2.getName();
					final CMFile CF=new CMFile(prefix+thisPath,accessor);
					if((CF.canRead())
					&&(!fcheck.contains(thisName.toUpperCase())))
						dir.addElement(CF);
				}
			}
		}
		final CMFile[] finalDir=new CMFile[dir.size()];
		for(int f=0;f<dir.size();f++)
			finalDir[f]=dir.elementAt(f);
		return finalDir;
	}

	/**
	 * Returns the entire VFS (database file) tree.
	 * @return the entire VFS tree.
	 */
	public static final CMVFSDir getVFSDirectory()
	{
		CMVFSDir vvfs=vfsV();
		if(vvfs==null)
		{
			if(CMLib.database()==null)
				return new CMVFSDir(null,"",CMFile.VFS_MASK_DIRECTORY,System.currentTimeMillis(),"SYS");
			final char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
			if(threadCode==MudHost.MAIN_HOST)
				vvfs=vfs[threadCode]=CMLib.database().DBReadVFSDirectory();
			else
			{
				if(CMProps.isPrivateToMe("DBVFS"))
					vvfs=vfs[threadCode]=CMLib.database().DBReadVFSDirectory();
				else
				if(vfs[MudHost.MAIN_HOST]!=null)
				{
					vfs[threadCode]=vfs[MudHost.MAIN_HOST];
					return vfs[threadCode];
				}
				else
					vvfs=vfs[threadCode]=vfs[MudHost.MAIN_HOST]=CMLib.database().DBReadVFSDirectory();
			}
		}
		if((catalogPluginAdded!=CMLib.catalog())&&(CMLib.catalog()!=null))
		{
			catalogPluginAdded=CMLib.catalog();
			CMVFSDir dir=vvfs.fetchSubDir("/resources/catalog", false);
			if(dir!=null)
				vvfs.delete(dir);
			dir=vvfs.fetchSubDir("/resources", true);
			vvfs.add(CMLib.catalog().getCatalogRoot(dir));
		}
		if((mapPluginAdded!=CMLib.map())&&(CMLib.map()!=null))
		{
			mapPluginAdded=CMLib.map();
			CMVFSDir dir=vvfs.fetchSubDir("/resources/map", false);
			if(dir!=null)
				vvfs.delete(dir);
			dir=vvfs.fetchSubDir("/resources", true);
			vvfs.add(CMLib.map().getMapRoot(dir));
		}
		return vvfs;
	}

	private static final String[] parsePathParts(String absolutePath)
	{
		absolutePath=vfsifyFilename(absolutePath);
		String name=absolutePath;
		final int x=absolutePath.lastIndexOf('/');
		String path="";
		if(x>=0)
		{
			path=absolutePath.substring(0,x);
			name=absolutePath.substring(x+1);
			if(name.equalsIgnoreCase("."))
				name="";
		}
		return new String[]{absolutePath,path,name};
	}

	/**
	 * Converts DOS style path/names to VFS type names.
	 * Removes any VFS prefixes like :: or //
	 * Removes any prefix path separator
	 * Changes separators to /
	 * @param filename the filename to convert
	 * @return the converted and cleaned filename
	 */
	public static final String vfsifyFilename(String filename)
	{
		if(filename == null)
			return "";
		filename=filename.trim();
		if(filename.startsWith("::"))
			filename=filename.substring(2);
		if(filename.startsWith("//"))
			filename=filename.substring(2);
		if((filename.length()>3)
		&&(Character.isLetter(filename.charAt(0))
		&&(filename.charAt(1)==':')))
			filename=filename.substring(2);
		while(filename.startsWith("/"))
			filename=filename.substring(1);
		while(filename.startsWith("\\"))
			filename=filename.substring(1);
		while(filename.endsWith("/"))
			filename=filename.substring(0,filename.length()-1);
		while(filename.endsWith("\\"))
			filename=filename.substring(0,filename.length()-1);
		return filename.replace(pathSeparator,'/');
	}

	private final StringBuffer saveBufNormalize(final StringBuffer myRsc)
	{
		for(int i=0;i<myRsc.length();i++)
		{
			if(myRsc.charAt(i)=='\n')
			{
				for(i=myRsc.length()-1;i>=0;i--)
				{
					if(myRsc.charAt(i)=='\r')
						myRsc.deleteCharAt(i);
				}
				return myRsc;
			}
		}
		for(int i=0;i<myRsc.length();i++)
		{
			if(myRsc.charAt(i)=='\r')
				myRsc.setCharAt(i,'\n');
		}
		return myRsc;
	}

	/**
	 * Returns CMFiles list for a directory at a given path
	 * @param path the full path of the directory to get list from
	 * @param user user, for security checks
	 * @param recurse true to recurse deep dirs, false otherwise
	 * @param expandDirs if path is a dir, return contents, otherwise self
	 * @param skipDirs if recursing dirs, this will skip listed paths
	 * @return list for a directory at a given path
	 */
	public static final CMFile[] getFileList(final String path, final MOB user, final boolean recurse, final boolean expandDirs, Set<String> skipDirs)
	{
		final boolean demandLocal=path.trim().startsWith("//");
		final boolean demandVFS=path.trim().startsWith("::");
		CMFile dirTest=new CMFile(path,user);
		if((dirTest.exists())
		&&(dirTest.isDirectory())
		&&(dirTest.canRead())
		&&(!recurse))
			return expandDirs ? dirTest.listFiles() : new CMFile[] { dirTest };
		final String vsPath=vfsifyFilename(path);
		String fixedName=vsPath;
		final int x=vsPath.lastIndexOf('/');
		String fixedPath="";
		if(x>=0)
		{
			fixedPath=vsPath.substring(0,x);
			fixedName=vsPath.substring(x+1);
		}
		final CMFile dir=new CMFile((demandLocal?"//":demandVFS?"::":"")+fixedPath,user);
		if((!dir.exists())||(!dir.isDirectory())||(!dir.canRead()))
			return null;
		final List<CMFile> set=new ArrayList<CMFile>();
		CMFile[] cset=dir.listFiles();
		fixedName=fixedName.toUpperCase();
		final boolean skipDirsSet = (skipDirs != null) && (skipDirs.size()>0);
		for (final CMFile element : cset)
		{
			if((recurse)
			&&(element.isDirectory())
			&&(element.canRead()))
			{
				if((skipDirsSet)
				&&(skipDirs != null)
				&&(skipDirs.contains(element.getVFSPathAndName())))
					continue;
				final CMFile[] CF2=getFileList(element.getVFSPathAndName()+"/"+fixedName,user,true,expandDirs,skipDirs);
				for (final CMFile element2 : CF2)
					set.add(element2);
			}
			final String name=element.getName().toUpperCase();
			if(CMStrings.filenameMatcher(name,fixedName))
				set.add(element);
		}
		if(set.size()==1)
		{
			dirTest=set.get(0);
			if((dirTest.exists())
			&&(dirTest.isDirectory())
			&&(dirTest.canRead())
			&&(!recurse))
				return expandDirs ? dirTest.listFiles() : new CMFile[] { dirTest };
		}
		cset=new CMFile[set.size()];
		for(int s=0;s<set.size();s++)
			cset[s]=set.get(s);
		return cset;
	}

	@Override
	public int	compareTo(File pathname)
	{
		if(pathname instanceof CMFile)
			return ((CMFile)pathname).getAbsolutePath().compareTo(getAbsolutePath());
		else
		{
			if(this.localFile!=null)
				return this.localFile.compareTo(pathname);
		}
		return pathname.compareTo(this);
	}

	@Override
	public boolean	createNewFile() throws IOException
	{
		if(isVFSFile())
		{
			if(!exists())
				return this.saveRaw(new byte[0]);
			else
				return false;
		}
		else
		if(this.localFile!=null)
			return this.localFile.createNewFile();
		return false;
	}

	@Override
	public void	deleteOnExit()
	{
		if((this.localFile!=null)&&(!this.isVFSFile()))
			this.localFile.deleteOnExit();
	}

	@Override
	public boolean	equals(Object obj)
	{
		if(obj instanceof CMFile)
			return ((CMFile)obj).getAbsolutePath().equalsIgnoreCase(getAbsolutePath());
		else
		if(obj instanceof File)
		{
			if(this.localFile!=null)
				return this.localFile.equals(obj);
		}
		return obj==this;
	}

	@Override 
	public File	getAbsoluteFile() 
	{ 
		return this; 
	}

	@Override public File	getCanonicalFile() 
	{ 
		return this; 
	}

	@Override
	public long	getFreeSpace()
	{
		if(this.localFile!=null)
			return this.localFile.getFreeSpace();
		return 65536;
	}

	@Override
	public String getParent()
	{
		return this.getParentFile().getAbsolutePath();
	}

	@Override 
	public String getPath() 
	{
		return this.getAbsolutePath(); 
	}

	@Override
	public long	getTotalSpace()
	{
		if(this.localFile!=null)
			return this.localFile.getTotalSpace();
		return 65536;
	}

	@Override
	public long	getUsableSpace()
	{
		if(this.localFile!=null)
			return this.localFile.getUsableSpace();
		return 65536;
	}

	@Override 
	public int hashCode() 
	{ 
		return this.getAbsolutePath().hashCode(); 
	}

	@Override 
	public boolean isAbsolute() 
	{ 
		return true; 
	}

	@Override 
	public boolean isHidden() 
	{ 
		return false; 
	}

	@Override
	public long	length()
	{
		if(!this.exists())
			return 0;
		if(this.isDirectory())
			return 0;
		if(this.isLocalFile()&&(this.localFile!=null))
			return this.localFile.length();
		final byte[] buf=this.raw();
		if(buf==null)
			return 0;
		return buf.length;
	}

	@Override
	public String[]	list(FilenameFilter filter)
	{
		if(filter==null)
			return this.list();
		final List<String> filteredList=new Vector<String>();
		for(final CMFile f : this.listFiles())
		{
			if(filter.accept(f.getParentFile(), f.getName()))
				filteredList.add(f.getName());
		}
		return filteredList.toArray(new String[0]);
	}

	@Override
	public File[] listFiles(FileFilter filter)
	{
		if(filter==null)
			return this.listFiles();
		final List<CMFile> filteredList=new Vector<CMFile>();
		for(final CMFile f : this.listFiles())
		{
			if(filter.accept(f))
				filteredList.add(f);
		}
		return filteredList.toArray(new CMFile[0]);
	}

	@Override
	public File[] listFiles(FilenameFilter filter)
	{
		if(filter==null)
			return this.listFiles();
		final List<CMFile> filteredList=new Vector<CMFile>();
		for(final CMFile f : this.listFiles())
		{
			if(filter.accept(f.getParentFile(),f.getName()))
				filteredList.add(f);
		}
		return filteredList.toArray(new CMFile[0]);
	}

	@Override
	public boolean mkdirs()
	{
		if((this.localFile!=null)&&(this.canLocalEquiv()))
			return this.localFile.mkdirs();
		return true;
	}

	@Override
	public boolean renameTo(File dest)
	{
		if(dest.exists())
			return false;
		if(dest instanceof CMFile)
		{
			final CMFile cmDest=(CMFile)dest;
			if((this.canVFSEquiv())&&(cmDest.canVFSEquiv()))
			{
				if(!this.isDirectory())
				{
					if(!cmDest.saveRaw(this.raw()))
						return false;
					this.deleteVFS();
				}
			}
			if(this.canLocalEquiv()&&(cmDest.canLocalEquiv()))
			{
				if((this.localFile!=null)&&(this.localFile.exists()))
					return this.localFile.renameTo(new File(cmDest.getLocalPathAndName()));
				return false;
			}
		}
		else
		if(this.canLocalEquiv())
		{
			if((this.localFile!=null)&&(this.localFile.exists()))
				return this.localFile.renameTo(dest);
			return false;
		}
		return false;
	}

	@Override 
	public boolean setExecutable(boolean executable) 
	{ 
		return false; 
	}

	@Override 
	public boolean setExecutable(boolean executable, boolean ownerOnly) 
	{ 
		return false; 
	}

	@Override 
	public boolean setLastModified(long time) 
	{ 
		return false; 
	}

	@Override 
	public boolean setReadable(boolean readable) 
	{ 
		return false; 
	}

	@Override 
	public boolean setReadable(boolean readable, boolean ownerOnly) 
	{ 
		return false; 
	}

	@Override 
	public boolean setReadOnly() 
	{ 
		return false; 
	}

	@Override 
	public boolean setWritable(boolean writable) 
	{ 
		return false; 
	}

	@Override 
	public boolean setWritable(boolean writable, boolean ownerOnly) 
	{ 
		return false; 
	}

	@Override 
	public String toString() 
	{ 
		return this.getAbsolutePath(); 
	}

	/**
	 * FileManager handler for CMFile, used by WebServer
	 * @author Bo Zimmerman
	 */
	public static class CMFileManager implements FileManager
	{
		private CMFile getFinalFile(CMFile F)
		{
			if((!F.exists())&&(F.getParent()!=null))
			{
				final ClanManager clanLib = CMLib.clans();
				if(clanLib!=null)
				{
					String templatePath = clanLib.getClanWebTemplateDir(F.getAbsolutePath());
					if(templatePath==null)
					{
						templatePath = clanLib.getClanWebTemplateDir(F.getParent());
						if(templatePath!=null)
							templatePath += "/"+F.getName();
					}
					if(templatePath!=null)
						F=new CMFile(templatePath,null);
				}
			}
			return F;
		}

		@Override
		public char getFileSeparator()
		{
			return '/';
		}

		@Override
		public File createFileFromPath(String localPath)
		{
			return new CMFile(localPath, null);
		}

		@Override
		public File createFileFromPath(File parent, String localPath)
		{
			return createFileFromPath(parent.getAbsolutePath() + '/' + localPath);
		}

		@Override 
		public byte[] readFile(File file) throws IOException, FileNotFoundException 
		{
			return getFinalFile((CMFile)file).raw();
		}

		@Override 
		public InputStream getFileStream(File file) throws IOException, FileNotFoundException 
		{
			return getFinalFile((CMFile)file).getRawStream();
		}

		@Override
		public RandomAccessFile getRandomAccessFile(File file) throws IOException, FileNotFoundException 
		{
			return new RandomAccessFile(new File(getFinalFile((CMFile)file).getLocalPathAndName()),"r");
		}

		@Override
		public boolean supportsRandomAccess(File file) 
		{
			return getFinalFile((CMFile)file).isLocalFile();
		}
	
		@Override
		public boolean allowedToReadData(File file)
		{
			final CMFile F=getFinalFile((CMFile)file);
			return F.exists() && F.canRead();
		}
	}
}
