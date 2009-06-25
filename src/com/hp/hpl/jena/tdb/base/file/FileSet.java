/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file ;

import java.io.*;
import java.nio.channels.FileChannel;

import atlas.lib.FileOps;
import atlas.lib.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.sys.Names;

/** Naming, access and metadata management to a collection of related files
 *  (same directory, same basename within directory, various extensions).
 */
public class FileSet extends MetaBase
{
    // Cope with "in-memory" fileset (location == null)
    
    private static Logger log = LoggerFactory.getLogger(FileSet.class) ;
    
    private Location location ;
    private String basename ;

    /** FileSet for "in-memory" */
    public static FileSet mem()
    {
        FileSet fs = new FileSet() ;
        fs.location = Location.mem() ;
        fs.basename = "mem" ;
        fs.initMetaFile("mem", Names.memName) ;
        return fs ;
    }

    private FileSet() {}        // Uninitialized.

    
    /** Create a FileSet given Location (directory) and name within the directory */  
    public FileSet(String directory, String basename)
    {
        initFileSet(new Location(directory), basename) ;
    }
    
    /** Create a FileSet given Location (directory) and name within the directory */  
    public FileSet(String filename)
    {
        Tuple<String> t = FileOps.splitDirFile(filename) ;
        String dir = t.get(0) ;
        String fn = t.get(1) ;
        if ( dir == null )
            dir = "." ;
        initFileSet(new Location(dir), fn) ;
    }
    
    /** Create a FileSet given Location (directory) and name within the directory */  
    public FileSet(Location directory, String basename)
    {
        initFileSet(directory, basename) ;
    }
    
    private void initFileSet(Location directory, String basename)
    {
        this.location = directory ;
        this.basename = basename ;
        String metaFileName = basename+"."+Names.extMeta ;
        super.initMetaFile(this.basename, metaFileName) ;
    }
    
    
    public Location getLocation()   { return location ; }
    public String getBasename()     { return basename ; }
    
    public RandomAccessFile openReadOnly(String ext)
    {
        return open(ext, "r") ;
    }
    
    public RandomAccessFile open(String ext)
    {
        return open(ext, "rw") ;
    }
        
    public boolean exists(String ext)
    {
        String fn = filename(ext) ;
        File f = new File(fn) ;
        if ( f.isDirectory() )
            log.warn("File clashes with a directory") ;
        return f.exists() && f.isFile() ;
    }
    
    @Override
    public String toString()
    {
        return "FileSet:"+filename(null) ;
    }
    
    public boolean isMem()
    {
        return location.isMem() ;
    }
    
 
    public String filename(String ext)
    {
        return location.getPath(basename, ext) ;
    }
    
    public RandomAccessFile open(String ext, String mode)
    {
        // "rwd" - Syncs only the file contents
        // "rws" - Syncs the file contents and metadata
        // "rw" -
        try {
            RandomAccessFile out = new RandomAccessFile(filename(ext), mode) ;
            return out ;
        } catch (IOException ex) { throw new FileException("Failed to open file", ex) ; } 
    }
    
    public FileChannel openChannel(String ext)
    {
        RandomAccessFile out = open(ext, "rw") ;
        return out.getChannel() ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */