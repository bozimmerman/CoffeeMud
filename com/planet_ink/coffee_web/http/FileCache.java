package com.planet_ink.coffee_web.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.FileCacheManager;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_web.util.CWConfig;

/*
   Copyright 2012-2018 Bo Zimmerman

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
 * Everything below here is the singleton that maintains the cache proper
 * Classes that do similar things to this one (such as the servlets and sessions manager)
 * are singleton instances, but in this case, the file manager should serve the entire
 * vm, and not even possibly be duplicated across multiple web servers
 * 
 * BTW -- a HashMap is used for the cache object as a reminder that every operation on
 * the cache is synchronized against it anyway, so having a synchronized container like
 * hashtable would be even more overhead.
 */

public class FileCache implements FileCacheManager
{
	private final AtomicLong 				  totalBytes= new AtomicLong(0);	// maintained tally of the size of the file cache
	private final Map<String, FileCacheEntry> cache	 	= new HashMap<String, FileCacheEntry>(); // the file cache itself -- hashmap means unsynched!
	private final long						  cacheMaxBytes; // maximum number of bytes the cache will hold
	private final long						  cacheMaxFileBytes; // maximum size of file in bytes the cache will hold
	private final long						  cacheExpireMs; // expiration time of an entry, in ellapsed ms
	private final boolean 					  cacheActive; // whether the cache is active at all, or whether this is just a file reader
	private final long						  compressionMaxFileBytes; // maximum size of file in bytes that will be compressed
	private final Logger					  logger;
	private final FileManager				  fileManager;


	/**
	 * Create a new file cache
	 * @param config
	 */
	public FileCache(CWConfig config, FileManager fileManager)
	{
		this.cacheMaxBytes=config.getFileCacheMaxBytes();
		this.cacheExpireMs=config.getFileCacheExpireMs();
		this.cacheMaxFileBytes=config.getFileCacheMaxFileBytes();
		this.compressionMaxFileBytes=config.getFileCompMaxFileBytes();
		this.cacheActive=(cacheMaxBytes > 0) || (compressionMaxFileBytes>0);
		this.logger=config.getLogger();
		this.fileManager=fileManager;
	}
	
	/**
	 * Empty the cache and make it start over
	 */
	public void clear()
	{
		synchronized(cache)
		{
			cache.clear();
		}
	}

	/**
	 * Internal method to check and see if a read/write eTag container actually matches the
	 * given file cache entry.  If it does match, a 304 exception is thrown up the chain.
	 * Otherwise, the r/w container is updated with the cache eTag value
	 * @param entry the cache entry that represents the file we want
	 * @param eTag a r/w single dimension string array for the eTag value
	 * @throws HTTPException 
	 */
	private void checkAndSetETag(final FileCacheEntry entry, final String[] eTag) throws HTTPException
	{
		if((eTag!=null) && (eTag.length>0))
		{
			if((entry.eTag.equals(eTag[0])))
				throw HTTPException.standardException(HTTPStatus.S304_NOT_MODIFIED);
			eTag[0]=entry.eTag;
		}
	}

	/**
	 * Since we are trying to maintain a limit on the amount of file data stored, 
	 * adding an entry to the cache can be tricky.  First we check to see if
	 * adding this entry would put us over the limit.  If so, we go through and
	 * time out some old entries to make room.
	 * This function should only be called when you are certain that an entry
	 * needs to be added to the cache.  After processing it into the cache,
	 * this function may end up throwing a 304 not modified if the eTag
	 * matches the data anyway.
	 * @param fileName   the name of the file being added
	 * @param entry      the new fancy cache entry for this file
	 * @param eTag       the r/w single dimension eTag holder
	 * @throws HTTPException
	 */
	private void possibleAddEntryToCache(final String fileName, final FileCacheEntry entry, final String[] eTag) throws HTTPException
	{
		synchronized(cache)
		{
			if(entry.buf[CompressionType.NONE.ordinal()] == null)
			{
				cache.put(fileName, entry);
				return;
			}
			final long targetBytes = cacheMaxBytes-entry.bufsSize.get();
			if(totalBytes.get() > targetBytes)
			{
				final long currentTime=System.currentTimeMillis();
				for(final Iterator<String> k=cache.keySet().iterator(); k.hasNext();)
				{
					final String key=k.next();
					final FileCacheEntry e=cache.get(key);
					if(currentTime >= entry.expires.getTime())
					{
						k.remove();
						cache.remove(key);
						if(e.bufsSize.get() > 0)
						{
							if(totalBytes.addAndGet(-e.bufsSize.get()) <= targetBytes)
								break;
						}
					}
				}
			}
			if(totalBytes.get() <= targetBytes)
			{
				totalBytes.addAndGet(entry.bufsSize.get());
				cache.put(fileName, entry);
				checkAndSetETag(entry,eTag); // DO THIS AFTER CACHING!!!
			}
		}
	}
	
	/**
	 * Internal method for retreiving a cache entry, and potentially throwing a 304 not modified,
	 * or even a 404 not found if appropriate
	 * @param fileName   the name of the file to look for
	 * @param eTag       a read/write eTag container that is matched for 304 errors, and filled otherwise
	 * @return the HTTPFileCache entry if one is found, or null if a new one needs generating
	 * @throws HTTPException
	 */
	private FileCacheEntry getFileData(final String fileName, final String[] eTag)  throws HTTPException
	{
		final FileCacheEntry entry = cache.get(fileName);
		if(entry != null)
		{
			if(System.currentTimeMillis() < entry.expires.getTime())
			{
				if(entry.notFound)
					throw HTTPException.standardException(HTTPStatus.S404_NOT_FOUND);
				checkAndSetETag(entry,eTag);
				return entry;
			}
			synchronized(cache)
			{
				if(entry.bufsSize.get() > 0)
					totalBytes.addAndGet(-entry.bufsSize.get());
				cache.remove(fileName);
			}
		}
		return null;
	}
	
	/**
	 * The publically accessible method for either compressing file data, or
	 * potentially from the cache. 
	 * @param pageFile the file that is being compressed, for reference
	 * @param type the type of compression to look for
	 * @param uncompressedData the data before compression
	 * @return the byte[] buffer of the file, compressed
	 * @throws HTTPException either 304 or 404
	 */
	@Override
	public DataBuffers compressFileData(final File pageFile, final CompressionType type, final DataBuffers uncompressedData) throws HTTPException
	{
		if(uncompressedData == null)
			return null;
		if((!uncompressedData.hasNext())
		||((this.compressionMaxFileBytes!=0)&&(this.compressionMaxFileBytes<uncompressedData.getLength())))
			return uncompressedData;
		final String fileName;
		if(pageFile != null)
		{
			fileName = pageFile.getAbsolutePath();
			final FileCacheEntry entry = getFileData(fileName, null);
			if((entry != null) && (entry.buf[type.ordinal()]!=null))
			{
				uncompressedData.close(); // we aren't going to need this.
				return new CWDataBuffers(entry.buf[type.ordinal()],entry.modified, true);
			}
		}
		else
			fileName=Integer.toString(uncompressedData.hashCode());
		final boolean cacheActive=(cacheMaxBytes > 0);
		synchronized(fileName.intern())
		{
			final FileCacheEntry entry = getFileData(fileName, null);
			if((entry != null) && (entry.buf[type.ordinal()]!=null))
			{
				uncompressedData.close(); // we aren't going to need this.
				return new CWDataBuffers(entry.buf[type.ordinal()],entry.modified, true);
			}
			DeflaterOutputStream compressor=null;
			final ByteArrayOutputStream bufStream=new ByteArrayOutputStream();
			try
			{
				switch(type)
				{
				case GZIP:
					compressor=new GZIPOutputStream(bufStream);
					break;
				case DEFLATE:
					compressor=new DeflaterOutputStream(bufStream);
					break;
				default: return null;
				}
				while(uncompressedData.hasNext())
				{
					final ByteBuffer buf=uncompressedData.next();
					compressor.write(buf.array(),buf.position(),buf.remaining());
					buf.position(buf.limit());
				}
				compressor.flush();
				compressor.finish();
			}
			catch(final IOException ioe)
			{
				uncompressedData.close();
				logger.throwing("", "", ioe);
				return null;
			}
			finally
			{
				if(compressor!=null)
					try
					{ compressor.close();} catch(final Exception e){}
			}
			final byte[] compressedBytes = bufStream.toByteArray();
			if((cacheActive) && (entry != null) && (entry.buf[type.ordinal()] == null))
			{
				entry.buf[type.ordinal()] = compressedBytes;
				totalBytes.addAndGet(entry.bufsSize.addAndGet(compressedBytes.length));
			}
			final long lastModified=uncompressedData.getLastModified().getTime();
			uncompressedData.close();
			return new CWDataBuffers(compressedBytes, lastModified, true);
		}
	}
	
	
	/**
	 * The publically accessible method for getting data from a file (or
	 * potentially from the cache.  You can also pass in a one dimensional
	 * eTay holder.  If the holder contains a valid tag that matches the
	 * file requested, a 304 not modified exception is thrown.  Otherwise,
	 * the holder is populated with the valid eTag when the byte[] buffer
	 * is returned.
	 * @param pageFile the local file to fetch
	 * @param eTag the r/w eTag holder, a one dimensional string array
	 * @return the byte[] buffer of the file to send to the client
	 * @throws HTTPException either 304 or 404
	 */
	@Override
	public DataBuffers getFileData(final File pageFile, final String[] eTag) throws HTTPException
	{
		final String fileName = pageFile.getAbsolutePath();
		FileCacheEntry entry = getFileData(fileName, eTag);
		if(entry != null)
			return new CWDataBuffers(entry.buf[CompressionType.NONE.ordinal()], entry.modified, true);
		
		synchronized(fileName.intern())
		{
			entry = getFileData(fileName, eTag);
			if(entry != null)
				return new CWDataBuffers(entry.buf[CompressionType.NONE.ordinal()], entry.modified, true);
			if(!fileManager.allowedToReadData(pageFile))
			{
				if(cacheActive)
				{
					synchronized(cache) 
					{
					cache.put(fileName, new FileCacheEntry(null,0));
					}
				}
				throw HTTPException.standardException(HTTPStatus.S404_NOT_FOUND);
			}
			final boolean cacheActiveThisFile=cacheActive;
			try
			{
				if((cacheMaxFileBytes==0)||(pageFile.length()<=cacheMaxFileBytes)||(!fileManager.supportsRandomAccess(pageFile)))
				{
					final byte[] fileBuf = fileManager.readFile(pageFile);
					if(cacheActiveThisFile)
					{
						entry = new FileCacheEntry(fileBuf, pageFile.lastModified());
						possibleAddEntryToCache(fileName, entry, eTag);
					}
					return new CWDataBuffers(fileBuf, pageFile.lastModified(), true);
				}
				else
				{
					return new CWDataBuffers(fileManager.getRandomAccessFile(pageFile), pageFile.lastModified(), true);
				}
			}
			catch(final FileNotFoundException e)
			{
				logger.throwing("", "", e);
				if(cacheActiveThisFile)
				{
					synchronized(cache) 
					{
					cache.put(fileName, new FileCacheEntry(null,0));
					}
				}
				// not quite sure how we could get here.
				throw HTTPException.standardException(HTTPStatus.S404_NOT_FOUND);
			}
			catch (final IOException e)
			{
				logger.throwing("", "", e);
				if(cacheActiveThisFile)
				{
					synchronized(cache) 
					{
					cache.put(fileName, new FileCacheEntry(null,0));
					}
				}
				throw HTTPException.standardException(HTTPStatus.S404_NOT_FOUND);
			}
		}
	}
	
	private class FileCacheEntry
	{
		private final Date		 expires;		// when this cache entry expires (to allow us to edit our htdocs)
		private final byte[][]   buf;			// the data contents of this file cache entry
		private final boolean 	 notFound;		// true if the file was not found (buffer will be NULL)
		private final String  	 eTag;			// the string eTag associated with this file -- typically a hash of the data
		private final AtomicLong bufsSize;		// the total size of all stored buffers.
		private final long		 modified;		// when this cache entry was last modified
		
		/**
		 * Construct a cache entry with the given buffer
		 * @param buffer
		 * @param lastModified
		 */
		public FileCacheEntry(byte[] buffer, long lastModified)
		{
			if(buffer == null)
			{
				this.notFound=true;
				this.bufsSize = new AtomicLong(0); 
			}
			else
			{
				this.notFound=false;
				this.bufsSize = new AtomicLong(buffer.length); 
			}
			this.buf=new byte[CompressionType.values().length][];
			this.buf[CompressionType.NONE.ordinal()]=buffer;
			this.eTag=""+Arrays.hashCode(buffer);
			this.expires=new Date(System.currentTimeMillis() + cacheExpireMs);
			this.modified=lastModified;
		}
	}
}
