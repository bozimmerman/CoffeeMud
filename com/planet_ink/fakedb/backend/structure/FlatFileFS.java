package com.planet_ink.fakedb.backend.structure;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
/*
Copyright 2025-2025 Bo Zimmerman

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
public class FlatFileFS implements Closeable
{
	final File		file;
	int				filenameSize		= 256;
	int				filenameMetaSize	= 256;
	final int		version				= 1;
	int				headerSize			= 256;
	final int		intSize				= 12;
	final String	intPad				= makePad(intSize);
	final int		longSize			= 20;
	final String	longPad				= makePad(longSize);
	String			blockPad			= "";
	int				linkSize			= longSize;
	String			linkPad				= "";
	long			firstDirBlock		= 0;
	long			rootDirEntry		= 0;
	long			firstFreeDataBlock	= 0;
	int				dataSize			= 4096 - linkSize;
	int				blockSize			= linkSize + dataSize;

	final RandomAccessFile rfile;

	int firstDirLinkOffset;
	int firstFreeLinkOffset;
	int rootEntryOffset;

	int entriesPerBlock;
	long metaOffsetLeft;
	long metaOffsetRight;
	long metaOffsetFileLink;
	long metaOffsetFileSize;

	public FlatFileFS(final String path) throws IOException
	{
		file = new File(path);
		rfile = new RandomAccessFile(file,"rws");
		blockPad = makePad(blockSize);
		if(rfile.length()==0)
		{
			final String introHeader = "PIFFFS\n"+version+"\n"+dataSize+"\n"+linkSize
					+"\n"+filenameSize+"\n"+filenameMetaSize+"\n"+headerSize+"\n"+blockPad;
			rfile.write(Arrays.copyOf(introHeader.getBytes(StandardCharsets.UTF_8), headerSize));
			linkPad = makePad(linkSize);
			final String introLinks = paddedLink(firstDirBlock)+paddedLink(firstFreeDataBlock)+paddedLink(rootDirEntry)+blockPad;
			rfile.write(Arrays.copyOf(introLinks.getBytes(StandardCharsets.UTF_8), blockSize-headerSize));
		}
		rfile.seek(0);
		final String check = readLine();
		if((check==null)||(!check.equals("PIFFFS")))
			throw new IOException("Invalid FlatFileFS File.");
		try
		{
			final int versionCheck = Integer.valueOf(readLine()).intValue();
			if((versionCheck > version) || (versionCheck <= 0))
				throw new IOException("Invalid FlatFileFS File Version.");
			dataSize = Integer.valueOf(readLine()).intValue();
			linkSize = Integer.valueOf(readLine()).intValue();
			blockSize = dataSize + linkSize;
			filenameSize = Integer.valueOf(readLine()).intValue();
			filenameMetaSize = Integer.valueOf(readLine()).intValue();
			headerSize = Integer.valueOf(readLine()).intValue();
			firstDirLinkOffset = headerSize;
			firstFreeLinkOffset = headerSize + linkSize;
			rootEntryOffset = headerSize + linkSize * 2;
			linkPad = makePad(linkSize);
			rfile.seek(firstDirLinkOffset);
			firstDirBlock = Long.valueOf(readLine()).longValue();
			rfile.seek(firstFreeLinkOffset);
			firstFreeDataBlock = Long.valueOf(readLine()).longValue();
			rfile.seek(rootEntryOffset);
			rootDirEntry = Long.valueOf(readLine()).longValue();
			entriesPerBlock = dataSize / (filenameSize + filenameMetaSize);
			metaOffsetLeft = filenameSize;
			metaOffsetRight = filenameSize + longSize;
			metaOffsetFileLink = filenameSize + longSize * 2;
			metaOffsetFileSize = filenameSize + longSize * 3;
		}
		catch(final NumberFormatException n)
		{
			throw new IOException("Invalid FlatFileFS File.");
		}
	}

	private String readLine() throws IOException
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int b;
		while(((b=rfile.read())!='\n')&&(b != -1))
			bout.write(b);
		if(bout.size()==0)
			return "";
		return new String(bout.toByteArray(),StandardCharsets.UTF_8);
	}

	private String makePad(final int size)
	{
		final StringBuilder str = new StringBuilder("");
		for(int i=0;i<size;i++)
			str.append(" ");
		return str.toString();
	}

	private String paddedInt(final int x)
	{
		final String sx = Integer.toString(x);
		return sx+"\n"+intPad.substring(0,intSize-sx.length()-1);
	}

	private String paddedLong(final long x)
	{
		final String sx = Long.toString(x);
		return sx+"\n"+longPad.substring(0,longSize-sx.length()-1);
	}

	private String paddedLink(final long x)
	{
		final String sx = Long.toString(x);
		return sx+"\n"+linkPad.substring(0,linkSize-sx.length()-1);
	}

	private long getEntryPos(final long entryId) throws IOException
	{
		if(entryId <= 0)
			throw new IOException("Invalid entryId: " + entryId);
		final long block = entryId / entriesPerBlock;
		final int index = (int)(entryId % entriesPerBlock);
		return block * blockSize + linkSize + index * (filenameSize + filenameMetaSize);
	}

	private synchronized long findNodeEntry(final String searchName) throws IOException
	{
		final String lowerSearchName = searchName.toLowerCase();
		long currentEntry = rootDirEntry;
		while (currentEntry > 0)
		{
			final String nodeName = getNodeName(currentEntry).toLowerCase();
			final int cmp = lowerSearchName.compareTo(nodeName);
			if (cmp == 0)
				return currentEntry;
			else if (cmp < 0)
				currentEntry = getLeft(currentEntry);
			else
				currentEntry = getRight(currentEntry);
		}
		return 0;
	}

	private String getNodeName(final long entryId) throws IOException
	{
		rfile.seek(getEntryPos(entryId));
		return readLine().trim();
	}

	public synchronized List<Map.Entry<String, Integer>> listAllFiles() throws IOException
	{
		final List<Map.Entry<String, Integer>> files = new ArrayList<>();
		traverse(rootDirEntry, files);
		return files;
	}

	private void traverse(final long entryId, final List<Map.Entry<String, Integer>> files) throws IOException
	{
		if (entryId == 0)
			return;
		traverse(getLeft(entryId), files);
		final String name = getNodeName(entryId);
		final int size = getFileSize(entryId);
		files.add(new AbstractMap.SimpleEntry<>(name, Integer.valueOf(size)));
		traverse(getRight(entryId), files);
	}

	private long getLeft(final long entryId) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetLeft);
		return Long.valueOf(readLine()).longValue();
	}

	private long getRight(final long entryId) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetRight);
		return Long.valueOf(readLine()).longValue();
	}

	private long getFileLink(final long entryId) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetFileLink);
		return Long.valueOf(readLine()).longValue();
	}

	private int getFileSize(final long entryId) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetFileSize);
		return Integer.valueOf(readLine()).intValue();
	}

	private void setLeft(final long entryId, final long value) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetLeft);
		rfile.write(paddedLong(value).getBytes());
	}

	private void setRight(final long entryId, final long value) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetRight);
		rfile.write(paddedLong(value).getBytes());
	}

	private void setNodeName(final long entryId, final String name) throws IOException
	{
		final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		if(nameBytes.length > 255)
			throw new IOException("Filename too long");
		rfile.seek(getEntryPos(entryId));
		rfile.write(nameBytes);
		rfile.write('\n');
		rfile.write(makePad(filenameSize - nameBytes.length - 1).getBytes(StandardCharsets.UTF_8));
	}

	private void setFileLink(final long entryId, final long value) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetFileLink);
		rfile.write(paddedLong(value).getBytes());
	}

	private void setFileSize(final long entryId, final int value) throws IOException
	{
		rfile.seek(getEntryPos(entryId) + metaOffsetFileSize);
		rfile.write(paddedInt(value).getBytes());
	}

	private synchronized void deleteDirEntry(final long entryId) throws IOException
	{
		if(entryId > 0)
		{
			final long entryPos = getEntryPos(entryId);
			rfile.seek(entryPos);
			rfile.write('\n');
			rfile.write(makePad(filenameSize - 1).getBytes(StandardCharsets.UTF_8));
			rfile.seek(entryPos + metaOffsetLeft);
			rfile.write(paddedLong(0).getBytes());
			rfile.seek(entryPos + metaOffsetRight);
			rfile.write(paddedLong(0).getBytes());
			rfile.seek(entryPos + metaOffsetFileLink);
			rfile.write(paddedLong(0).getBytes());
			rfile.seek(entryPos + metaOffsetFileSize);
			rfile.write(paddedInt(0).getBytes());
		}
	}

	private synchronized void freeFileDataBlocks(final long link) throws IOException
	{
		long nextLink = link;
		while(nextLink > 0)
		{
			final long position = blockSize * nextLink;
			rfile.seek(position);
			final long nextNextLink = Long.valueOf(readLine()).longValue();
			rfile.seek(position);
			rfile.write(paddedLong(firstFreeDataBlock).getBytes());
			firstFreeDataBlock = nextLink;
			nextLink = nextNextLink;
		}
		rfile.seek(firstFreeLinkOffset);
		rfile.write(paddedLong(firstFreeDataBlock).getBytes());
	}

	public synchronized boolean deleteFile(final String fileName) throws IOException
	{
		if(fileName == null)
			return false;
		final String lowerName = fileName.toLowerCase();
		long parentEntry = 0;
		long currentEntry = rootDirEntry;
		boolean isLeft = false;
		while (currentEntry > 0)
		{
			final String currentName = getNodeName(currentEntry).toLowerCase();
			final int cmp = lowerName.compareTo(currentName);
			if (cmp == 0)
				break;
			parentEntry = currentEntry;
			if (cmp < 0)
			{
				currentEntry = getLeft(currentEntry);
				isLeft = true;
			}
			else
			{
				currentEntry = getRight(currentEntry);
				isLeft = false;
			}
		}
		if (currentEntry == 0)
			return false;
		final long fileLink = getFileLink(currentEntry);
		if(fileLink > 0)
			freeFileDataBlocks(fileLink);
		final long left = getLeft(currentEntry);
		final long right = getRight(currentEntry);
		if (left == 0 && right == 0)
		{
			if (parentEntry == 0)
			{
				rootDirEntry = 0;
				rfile.seek(rootEntryOffset);
				rfile.write(paddedLong(0).getBytes());
			}
			else if (isLeft)
				setLeft(parentEntry, 0);
			else
				setRight(parentEntry, 0);
			deleteDirEntry(currentEntry);
		}
		else if (left == 0)
		{
			if (parentEntry == 0)
			{
				rootDirEntry = right;
				rfile.seek(rootEntryOffset);
				rfile.write(paddedLong(right).getBytes());
			}
			else if (isLeft)
				setLeft(parentEntry, right);
			else
				setRight(parentEntry, right);
			deleteDirEntry(currentEntry);
		}
		else if (right == 0)
		{
			if (parentEntry == 0)
			{
				rootDirEntry = left;
				rfile.seek(rootEntryOffset);
				rfile.write(paddedLong(left).getBytes());
			}
			else if (isLeft)
				setLeft(parentEntry, left);
			else
				setRight(parentEntry, left);
			deleteDirEntry(currentEntry);
		}
		else
		{
			long successorEntry = right;
			long succParentEntry = currentEntry;
			while (getLeft(successorEntry) > 0)
			{
				succParentEntry = successorEntry;
				successorEntry = getLeft(successorEntry);
			}
			final String succName = getNodeName(successorEntry);
			final long succLink = getFileLink(successorEntry);
			final int succSize = getFileSize(successorEntry);
			setNodeName(currentEntry, succName);
			setFileLink(currentEntry, succLink);
			setFileSize(currentEntry, succSize);
			setLeft(succParentEntry, getRight(successorEntry));
			deleteDirEntry(successorEntry);
		}
		return true;
	}

	public synchronized String readFile(final String fileName) throws IOException
	{
		if(fileName == null)
			return null;
		final long entryId = findNodeEntry(fileName.toLowerCase());
		if(entryId == 0)
			return null;
		final long link = getFileLink(entryId);
		final int size = getFileSize(entryId);
		if(link == 0 || size == 0)
			return "";
		long nextLink = link;
		long bytesRemain = size;
		final byte[] buf = newBuf(dataSize);
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while((nextLink > 0)&&(bytesRemain>0))
		{
			long position = blockSize * nextLink;
			rfile.seek(position);
			final long nextNextLink = Long.valueOf(readLine()).longValue();
			position += linkSize;
			rfile.seek(position);
			final int bytesToRead = (int)Math.min(bytesRemain, dataSize);
			final int bytesRead = rfile.read(buf, 0, bytesToRead);
			if(bytesRead > 0)
			{
				bout.write(buf,0,bytesRead);
				bytesRemain -= bytesRead;
			}
			nextLink = nextNextLink;
		}
		return new String(bout.toByteArray(),StandardCharsets.UTF_8);
	}

	private byte[] newBuf(final int size)
	{
		final byte[] buf = new byte[size];
		Arrays.fill(buf, (byte)32);
		return buf;
	}

	private synchronized long nextFreeDirectoryEntryId(final long nextActualFreeBlock) throws IOException
	{
		long nextLink = firstDirBlock;
		try
		{
			while(nextLink > 0)
			{
				long position = blockSize * nextLink;
				rfile.seek(position);
				final long nextNextLink = Long.valueOf(readLine()).longValue();
				position += linkSize;
				rfile.seek(position);
				final long fileEntrySize = (filenameSize+filenameMetaSize);
				final long lastPossibleEntryOffset = dataSize -fileEntrySize;
				for(int i=0;i<=lastPossibleEntryOffset;i+=fileEntrySize)
				{
					final long entryPosition = position + i;
					rfile.seek(entryPosition);
					if(rfile.read() == '\n')
					{
						final int index = i / (int)fileEntrySize;
						return nextLink * entriesPerBlock + index;
					}
				}
				nextLink = nextNextLink;
			}
		}
		catch(final NumberFormatException n)
		{
			throw new IOException("Error FlatFileFS Directory, Link#"+nextLink+".");
		}
		// this inserts a new directory block at the head of the chain, not the end.
		final long newBlockNumber = nextActualFreeBlock;
		rfile.seek(newBlockNumber * blockSize);
		rfile.write(paddedLong(firstDirBlock).getBytes()); // add an empty next link to first dir block
		final byte[] restOfDirBlock = newBuf(blockSize - linkSize);
		rfile.write(restOfDirBlock); // fill out the rest of the block so file size remains perfect
		final long entryStart = (newBlockNumber * blockSize) + linkSize;
		final long entrySize = filenameSize + filenameMetaSize;
		final int numEntries = entriesPerBlock;
		for (int e = 0; e < numEntries; e++)
		{
			final long namePos = entryStart + (e * entrySize);
			rfile.seek(namePos);
			rfile.write('\n');
			rfile.write(makePad(filenameSize - 1).getBytes(StandardCharsets.UTF_8));
			final long metaPos = namePos + filenameSize;
			rfile.seek(metaPos);
			rfile.write(paddedLong(0).getBytes()); // left
			rfile.seek(metaPos + longSize);
			rfile.write(paddedLong(0).getBytes()); // right
			rfile.seek(metaPos + longSize * 2);
			rfile.write(paddedLong(0).getBytes()); // fileLink
			rfile.seek(metaPos + longSize * 3);
			rfile.write(paddedInt(0).getBytes()); // fileSize
		}
		rfile.seek(firstDirLinkOffset);
		rfile.write(paddedLong(newBlockNumber).getBytes()); // rewrite first dir link
		firstDirBlock = newBlockNumber;
		return (firstDirBlock * entriesPerBlock) + 0; // return first entry id
	}

	public boolean fileExists(final String fileName)
	{
		try
		{
			return findNodeEntry(fileName.toLowerCase()) > 0;
		}
		catch(final IOException e)
		{
			return false;
		}
	}

	public synchronized boolean writeFile(final String fileName, final String data) throws IOException
	{
		long entryId = findNodeEntry(fileName.toLowerCase());
		if(entryId > 0)
			freeFileDataBlocks(getFileLink(entryId));
		final byte[] finalDataBytes = data.getBytes(StandardCharsets.UTF_8);
		final List<Long> allocated = new ArrayList<Long>();
		long bytesLeftToAllocate = finalDataBytes.length;
		if((rfile.length() % blockSize) != 0)
			throw new IOException("Error FlatFileFS Wrong Length "+rfile.length()+"/"+(rfile.length() % blockSize)+".");
		long nextBlockAtTheEnd =  rfile.length() / blockSize;
		while(bytesLeftToAllocate > 0)
		{
			long dataBlock = firstFreeDataBlock;
			if(dataBlock == 0)
			{
				dataBlock = nextBlockAtTheEnd;
				nextBlockAtTheEnd++; // gets written later.
			}
			else
			{
				rfile.seek(dataBlock * blockSize);
				firstFreeDataBlock = Long.valueOf(readLine()).longValue();
			}
			allocated.add(Long.valueOf(dataBlock));
			bytesLeftToAllocate -= dataSize;
		}
		long firstFileBlock = 0;
		if(allocated.size()>0)
			firstFileBlock = allocated.get(0).longValue();
		if(entryId == 0)
		{
			entryId = this.nextFreeDirectoryEntryId(nextBlockAtTheEnd);
			setNodeName(entryId, fileName);
			setLeft(entryId, 0);
			setRight(entryId, 0);
			setFileLink(entryId, firstFileBlock);
			setFileSize(entryId, finalDataBytes.length);
			if(rootDirEntry == 0)
			{
				rootDirEntry = entryId;
				rfile.seek(rootEntryOffset);
				rfile.write(paddedLong(rootDirEntry).getBytes());
			}
			else
			{
				final String lowerName = fileName.toLowerCase();
				long parentEntry = 0;
				long currentEntry = rootDirEntry;
				int cmp = 0;
				while (currentEntry > 0)
				{
					cmp = lowerName.compareTo(getNodeName(currentEntry).toLowerCase());
					if (cmp == 0)
						break;
					parentEntry = currentEntry;
					if (cmp < 0)
						currentEntry = getLeft(currentEntry);
					else
						currentEntry = getRight(currentEntry);
				}
				if (cmp < 0)
					setLeft(parentEntry, entryId);
				else
					setRight(parentEntry, entryId);
			}
			if(this.firstDirBlock == nextBlockAtTheEnd)
				nextBlockAtTheEnd++;
		}
		else
		{
			setFileLink(entryId, firstFileBlock);
			setFileSize(entryId, finalDataBytes.length);
		}
		// now write the data
		int nextBytesPosition = 0;
		for(int b=0;b<allocated.size();b++)
		{
			final long block = allocated.get(b).longValue();
			long nextBlock = 0;
			if(b<allocated.size()-1)
				nextBlock = allocated.get(b+1).longValue();
			rfile.seek(block * blockSize);
			rfile.write(paddedLong(nextBlock).getBytes());
			if((finalDataBytes.length - nextBytesPosition) >= dataSize)
				rfile.write(finalDataBytes,nextBytesPosition,dataSize);
			else
			{
				rfile.write(finalDataBytes,nextBytesPosition,(finalDataBytes.length - nextBytesPosition));
				final byte[] remaining = newBuf(blockSize - linkSize - (finalDataBytes.length - nextBytesPosition));
				rfile.write(remaining);
				break;
			}
			nextBytesPosition += dataSize;
		}
		rfile.seek(firstFreeLinkOffset);
		rfile.write(paddedLong(firstFreeDataBlock).getBytes());
		return true;
	}

	@Override
	public void close() throws IOException
	{
		if(rfile != null)
			rfile.close();
	}

	public static void main(final String[] args)
	{
		final String testPath = "c:\\tmp\\test_flatfs.db"; // Use a fixed path for simplicity; delete at end
		final File testFile = new File(testPath);
		if (testFile.exists()) {
			testFile.delete(); // Start fresh
		}

		try {
			System.out.println("Starting FlatFileFS functional tests...");

			// Test 1: Create new FS and check initialization
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				assertTrue(fs.rfile.length() > 0, "FS file should be initialized with header and initial block");
				System.out.println("Test 1: FS Creation - PASSED");
			}

			// Test 2: Reopen FS and write empty file
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final boolean wrote = fs.writeFile("empty.txt", "");
				assertTrue(wrote, "Should write empty file");
				assertTrue(fs.fileExists("empty.txt"), "Empty file should exist");
				final String read = fs.readFile("empty.txt");
				assertEquals("", read, "Read empty file should return empty string");
				System.out.println("Test 2: Empty File Write/Read/Exists - PASSED");
			}

			// Test 3: Write small file (< dataSize)
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final String smallData = "Hello, FlatFileFS!";
				fs.writeFile("small.txt", smallData);
				assertTrue(fs.fileExists("small.txt"), "Small file should exist");
				final String readSmall = fs.readFile("small.txt");
				assertEquals(smallData, readSmall, "Read small file mismatch");
				System.out.println("Test 3: Small File Write/Read - PASSED");
			}

			// Test 4: Write large file (> dataSize, spans multiple blocks)
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final int dataSize = fs.dataSize; // Assuming accessible or known ~4096 - linkSize
				final StringBuilder largeData = new StringBuilder();
				for (int i = 0; i < dataSize * 3 + 100; i++)
					largeData.append('A');
				fs.writeFile("large.txt", largeData.toString());
				assertTrue(fs.fileExists("large.txt"), "Large file should exist");
				final String readLarge = fs.readFile("large.txt");
				assertEquals(largeData.toString(), readLarge, "Read large file mismatch");
				System.out.println("Test 4: Large File Write/Read (Multi-Block) - PASSED");
			}

			// Test 5: Overwrite existing file
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final long beforeLength = fs.rfile.length();
				fs.writeFile("small.txt", "Overwritten content");
				final String readOver = fs.readFile("small.txt");
				assertEquals("Overwritten content", readOver, "Overwrite mismatch");
				// Check if old blocks were freed (file length shouldn't grow much)
				assertTrue(fs.rfile.length() <= beforeLength + fs.blockSize, "Overwrite should reuse blocks");
				System.out.println("Test 5: Overwrite Existing File - PASSED");
			}

			// Test 6: Delete file and check non-existence
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				fs.deleteFile("empty.txt");
				assertFalse(fs.fileExists("empty.txt"), "Deleted file should not exist");
				assertNull(fs.readFile("empty.txt"), "Read deleted file should return null");
				fs.deleteFile("nonexistent.txt"); // Should not throw, return false
				System.out.println("Test 6: Delete File/Non-Existent - PASSED");
			}

			// Test 7: Block reuse after deletion
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final long initialLength = fs.rfile.length();
				// Write multiple files to allocate blocks
				fs.writeFile("file1.txt", new String(new char[fs.dataSize * 2]).replace('\0', 'B'));
				fs.writeFile("file2.txt", new String(new char[fs.dataSize * 2]).replace('\0', 'C'));
				final long afterWrite = fs.rfile.length();
				assertTrue(afterWrite > initialLength, "File grew after writes");

				// Delete them
				fs.deleteFile("file1.txt");
				fs.deleteFile("file2.txt");

				// Write new files of similar size
				fs.writeFile("newfile1.txt", new String(new char[fs.dataSize * 2]).replace('\0', 'D'));
				fs.writeFile("newfile2.txt", new String(new char[fs.dataSize * 2]).replace('\0', 'E'));

				final long afterReuse = fs.rfile.length();
				assertTrue(afterReuse <= afterWrite + fs.blockSize, "Blocks should be reused, minimal growth");
				assertEquals(new String(new char[fs.dataSize * 2]).replace('\0', 'D'), fs.readFile("newfile1.txt"), "New file read mismatch");
				fs.deleteFile("newfile1.txt");
				fs.deleteFile("newfile2.txt");
				System.out.println("Test 7: Block Reuse After Deletion - PASSED");
			}

			// Test 8: Multiple files and directory persistence
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				fs.writeFile("fileA.txt", "A");
				fs.writeFile("fileB.txt", "B");
				fs.writeFile("fileC.txt", "C");
				System.out.println("Test 8a: Multiple Files Write - PASSED");
			}
			// Reopen to check persistence
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				assertTrue(fs.fileExists("fileA.txt"), "Persisted file A");
				assertEquals("B", fs.readFile("fileB.txt"), "Persisted read B");
				System.out.println("Test 8b: Persistence After Reopen - PASSED");
			}

			// Test 9: Long filename (should fail)
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final String longName = new String(new char[300]).replace('\0', 'X') + ".txt";
				try
				{
					fs.writeFile(longName, "data");
					assertFalse(true, "Should throw on long filename");
				}
				catch (final IOException e)
				{
					assertTrue(e.getMessage().contains("too long"), "Expected too long exception");
				}
				System.out.println("Test 9: Long Filename Error - PASSED");
			}

			// Test 10: Directory block extension (many files to force new dir block)
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				final long initialLength = fs.rfile.length();
				final int entriesPerDirBlock = fs.entriesPerBlock;
				for (int i = 0; i < entriesPerDirBlock * 2 + 5; i++)
				{ // Force at least 2 dir blocks
					fs.writeFile("many" + i + ".txt", "data" + i);
				}
				assertTrue(fs.rfile.length() > initialLength + fs.blockSize, "File grew for new dir block");
				for (int i = 0; i < entriesPerDirBlock * 2 + 5; i++)
				{
					assertEquals("data" + i, fs.readFile("many" + i + ".txt"), "Many file " + i + " mismatch");
				}
				System.out.println("Test 10: Directory Block Extension - PASSED");
			}

			// Test 11: Case sensitivity in filenames
			try (FlatFileFS fs = new FlatFileFS(testPath)) {
				fs.writeFile("caseTest.txt", "Original");
				assertTrue(fs.fileExists("caseTest.txt"), "Original case file should exist");
				assertTrue(fs.fileExists("CASETesT.TXT"), "Should exist case-insensitively");
				System.out.println("Test 11: Case Sensitivity - PASSED");
			}

			// Test 12: Filename with special characters
			try (FlatFileFS fs = new FlatFileFS(testPath)) {
				final String specialName = "file@with#special$chars!.txt";
				final String data = "Special content";
				fs.writeFile(specialName, data);
				assertTrue(fs.fileExists(specialName), "Special char file should exist");
				final String read = fs.readFile(specialName);
				assertEquals(data, read, "Read special char file mismatch");
				fs.deleteFile(specialName);
				assertFalse(fs.fileExists(specialName), "Special char file should be deleted");
				System.out.println("Test 12: Special Characters in Filename - PASSED");
			}

			// Test 13: Maximum filename length
			try (FlatFileFS fs = new FlatFileFS(testPath)) {
				final String maxName = new String(new char[255]).replace('\0', 'X'); // 255 bytes
				fs.writeFile(maxName, "Max length data");
				assertTrue(fs.fileExists(maxName), "Max length file should exist");
				final String read = fs.readFile(maxName);
				assertEquals("Max length data", read, "Read max length file mismatch");

				final String tooLongName = new String(new char[256]).replace('\0', 'X'); // 256 bytes
				try {
					fs.writeFile(tooLongName, "Too long");
					assertFalse(true, "Should throw on too-long filename");
				} catch (final IOException e) {
					assertTrue(e.getMessage().contains("too long"), "Expected too long exception");
				}
				System.out.println("Test 13: Maximum Filename Length - PASSED");
			}

			// Test 14: File size exactly dataSize (and multiple)
			try (FlatFileFS fs = new FlatFileFS(testPath)) {
				final int dataSize = fs.dataSize;
				final String exactData = new String(new char[dataSize]).replace('\0', 'E');
				fs.writeFile("exact_block.txt", exactData);
				assertTrue(fs.fileExists("exact_block.txt"), "Exact block file should exist");
				final String readExact = fs.readFile("exact_block.txt");
				assertEquals(exactData, readExact, "Read exact block mismatch");

				final String doubleData = new String(new char[dataSize * 2]).replace('\0', 'F');
				fs.writeFile("double_block.txt", doubleData);
				final String readDouble = fs.readFile("double_block.txt");
				assertEquals(doubleData, readDouble, "Read double block mismatch");
				System.out.println("Test 14: Exact DataSize Boundaries - PASSED");
			}

			// Test 15: Reuse directory entry after delete
			try (FlatFileFS fs = new FlatFileFS(testPath)) {
				fs.writeFile("to_delete.txt", "Temp");
				final long initialLength = fs.rfile.length();
				fs.deleteFile("to_delete.txt");
				fs.writeFile("reused.txt", "Reused content");
				assertTrue(fs.fileExists("reused.txt"), "Reused entry file should exist");
				final String read = fs.readFile("reused.txt");
				assertEquals("Reused content", read, "Read reused entry mismatch");
				assertEquals(Long.valueOf(initialLength), Long.valueOf(fs.rfile.length()), "File length should not grow (dir entry reused)");
				System.out.println("Test 15: Directory Entry Reuse - PASSED");
			}

			// Test 16: Multiple overwrites with varying sizes
			try (FlatFileFS fs = new FlatFileFS(testPath)) {
				final long initialLength = fs.rfile.length();
				fs.writeFile("vary.txt", new String(new char[fs.dataSize * 2]).replace('\0', 'G')); // Large
				fs.writeFile("vary.txt", "Small"); // Overwrite smaller
				assertEquals("Small", fs.readFile("vary.txt"), "Small overwrite mismatch");
				fs.writeFile("vary.txt", new String(new char[fs.dataSize * 3]).replace('\0', 'H')); // Overwrite larger
				assertEquals(new String(new char[fs.dataSize * 3]).replace('\0', 'H'), fs.readFile("vary.txt"), "Large overwrite mismatch");
				assertTrue(fs.rfile.length() <= initialLength + fs.blockSize * 3, "File growth should be minimal after overwrites");
				System.out.println("Test 16: Varying Size Overwrites - PASSED");
			}

			// Test 17: Persistence after close and modifications
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				fs.writeFile("persist.txt", "Before close");
			}
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				assertEquals("Before close", fs.readFile("persist.txt"), "Persist before close mismatch");
				fs.writeFile("persist.txt", "After reopen");
			}
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				assertEquals("After reopen", fs.readFile("persist.txt"), "Persist after reopen mismatch");
				System.out.println("Test 17: Persistence After Close/Reopen - PASSED");
			}


			// Cleanup: Delete all files and check empty
			try (FlatFileFS fs = new FlatFileFS(testPath))
			{
				//for(final Map.Entry<String,Integer> e : fs.listAllFiles())
				//	System.out.println(e.getKey() +": "+e.getValue());
				for(final Map.Entry<String,Integer> e : fs.listAllFiles())
					fs.deleteFile(e.getKey());
				System.out.println("Test 18:  All files deleted - PASSED");
			}

			// Test 19: Corrupted file handling
			try {
				// Manually corrupt by truncating
				final RandomAccessFile corrupt = new RandomAccessFile(testPath, "rw");
				corrupt.setLength(15);
				corrupt.close();
				try (FlatFileFS fs = new FlatFileFS(testPath))
				{
					assertFalse(true, "Should throw on corrupted file");
				}
				catch (final IOException e)
				{
					assertTrue(e.getMessage().contains("Invalid FlatFileFS File."), "Expected corruption exception");
				}
				// Restore by recreating (or skip if you have a backup mechanism)
				if(new File(testPath).exists())
					new File(testPath).delete();
				System.out.println("Test 19: Corrupted File Handling - PASSED");
			}
			catch (final IOException e)
			{
				throw new RuntimeException("Test 19 setup failed", e);
			}

			System.out.println("All tests PASSED!");
		}
		catch (final Exception e)
		{
			System.err.println("Test FAILED: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if (testFile.exists())
				testFile.delete();
		}
	}

	// Helper assertion methods
	private static void assertEquals(final Object expected, final Object actual, final String message) {
		if (!Objects.equals(expected, actual)) {
			throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
		}
	}

	private static void assertTrue(final boolean condition, final String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	private static void assertFalse(final boolean condition, final String message)
	{
		if (condition)
			throw new AssertionError(message);
	}

	private static void assertNull(final Object obj, final String message)
	{
		if (obj != null)
			throw new AssertionError(message + " - Expected null, Actual: " + obj);
	}
}