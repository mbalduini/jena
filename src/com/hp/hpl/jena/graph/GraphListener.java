/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphListener.java,v 1.9 2003-07-11 14:32:33 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    Interface for listening to graph-level update events.
    @author Jeremy Carroll, extensions by kers
*/
public interface GraphListener 
    {
    /**
        Method called when a single triple has been added to the graph.
    */
    void notifyAdd( Triple t );
    
    /**
        Method called when an array of triples has been added to the graph.
    */
    void notifyAdd( Triple [] triples );
    
    /**
        Method called when a list [of triples] has been added to the graph.
    */
    void notifyAdd( List triples );
    
    /**
        Method called when an iterator [of triples] has been added to the graph
    */
    void notifyAdd( Iterator it );
    
    /**
        Method called when another graph <code>g</code> has been used to
        specify the triples added to our attached graph.
    	@param g the graph of triples added
     */
    void notifyAdd( Graph g );
    
    /**
        Method called when a single triple has been deleted from the graph.
    */
    void notifyDelete( Triple t );
    
    /**
        Method called when a list [of triples] has been deleted from the graph.
    */
    void notifyDelete( List L );
    
    /**
        Method called when an array of triples has been deleted from the graph.
    */
    void notifyDelete( Triple [] triples );
    
    /**
        Method called when an iterator [of triples] has been deleted from the graph.
    */
    void notifyDelete( Iterator it );
    
    /**
        Method to call when another graph has been used to specify the triples 
        deleted from our attached graph. 
    	@param g the graph of triples added
     */
    void notifyDelete( Graph g );
    }

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
