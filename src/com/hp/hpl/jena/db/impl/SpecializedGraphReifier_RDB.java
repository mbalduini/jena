/*
 *  (c) Copyright Hewlett-Packard Company 2003 
 *  All rights reserved.
 *
 */

package com.hp.hpl.jena.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.MapFiller;
import com.hp.hpl.jena.util.iterator.MapMany;

/**
 * @author hkuno
 * @version $Version$
 *
 * TripleStoreGraph is an abstract superclass for TripleStoreGraph
 * implementations.  By "triple store," we mean that the subjects, predicate
 * and object URI's are stored in a single collection (denormalized).
 *  
 */

public class SpecializedGraphReifier_RDB implements SpecializedGraphReifier {

	/**
	 * holds PSet
	 */
	public IPSet m_pset;

	/**
	 * caches a copy of LSet properties
	 */
	public DBPropLSet m_dbPropLSet;

	/**
	 * holds ID of graph in database (defaults to "0")
	 */
	public IDBID my_GID = new DBIDInt(0);

	// lset name
	private String m_lsetName;

	// lset classname
	private String m_className;

	// cache of reified statement status
	private ReifCacheMap m_reifCache;

	public PSet_ReifStore_RDB m_reif;

	// constructors

	/** 
	 * Constructor
	 * Create a new instance of a TripleStore graph.
	 */
	SpecializedGraphReifier_RDB(DBPropLSet lProp, IPSet pSet) {
		m_pset = pSet;
		m_dbPropLSet = lProp;
		m_reifCache = new ReifCacheMap(1);
		m_reif = (PSet_ReifStore_RDB) m_pset;
	}

	/** 
	 *  Constructor
	 * 
	 *  Create a new instance of a TripleStore graph, taking
	 *  DBPropLSet and a PSet as arguments
	 */
	public SpecializedGraphReifier_RDB(IPSet pSet) {
		m_pset = pSet;
		m_reifCache = new ReifCacheMap(1);
		m_reif = (PSet_ReifStore_RDB) m_pset;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#add(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(Node n, Triple t, CompletionFlag complete) throws Reifier.AlreadyReifiedException {
		ReifCache rs = m_reifCache.load((Node_URI) t.getSubject());
		if (rs != null)
			throw new Reifier.AlreadyReifiedException(n);
		m_reif.storeReifStmt(n, t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#delete(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(Node n, Triple t, CompletionFlag complete) {
		m_reifCache.flushAll();
		m_reif.deleteReifStmt( n, t, my_GID);
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#contains(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public boolean contains(Node n, Triple t, CompletionFlag complete) {
		if ( true )
			throw new RuntimeException("SpecializedGraphReifier.contains called");
		return false;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#findReifiedNodes(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public ExtendedIterator findReifiedNodes(Triple t, CompletionFlag complete) {
		complete.setDone();
		return m_reif.findReifStmtURIByTriple(t, my_GID);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraphReifier#findReifiedTriple(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public Triple findReifiedTriple(Node n, CompletionFlag complete) {
		ExtendedIterator it = m_reif.findReifStmt(n, true, my_GID);
		Triple res = null;
		if ( it.hasNext() ) {
				List l = (List) it.next();
				if ( !it.hasNext() )
					res = new Triple((Node)l.get(1), (Node)l.get(2), (Node)l.get(3));
		}
		complete.setDone();
		return res;
	}

	/** Find all the triples corresponding to a given reified node.
	 * In a perfect world, there would only ever be one, but when a user calls
	 * add(Triple) there is nothing in RDF that prevents them from adding several
	 * subjects,predicates or objects for the same statement.
	 * 
	 * The resulting Triples may be incomplete, in which case some of the 
	 * nodes may be Node_ANY.
	 * 
	 * For example, if an application had previously done:
	 * add( new Triple( a, rdf.subject A )) and
	 * add( new Triple( a, rdf.object B )) and
	 * add( new Triple( a, rdf.object B2 ))
	 * 
	 * Then the result of findReifiedTriple(a, flag) will be an iterator containing
	 * Triple(A, ANY, B) and Triple(ANY, ANY, B2).
	 * 
	 * @param n is the Node for which we are querying.
	 * @param complete is true if we know we've returned all the triples which may exist.
	 * @return ExtendedIterator.
	 */
	public ExtendedIterator findReifiedTriples(Node n, CompletionFlag complete) {
		complete.setDone();
		return m_reif.findReifStmt(n, false, my_GID);
	}

	/** 
	 * Attempt to add all the triples from a graph to the specialized graph
	 * 
	 * Caution - this call changes the graph passed in, deleting from 
	 * it each triple that is successfully added.
	 * 
	 * Node that when calling add, if complete is true, then the entire
	 * graph was added successfully and the graph g will be empty upon
	 * return.  If complete is false, then some triples in the graph could 
	 * not be added.  Those triples remain in g after the call returns.
	 * 
	 * If the triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw
	 * a runtime exception.
	 * 
	 * @param g is a graph containing triples to be added
	 * @param complete is true if a subsequent call to contains(triple) will return true for any triple in g.
	 */
	public void add(Graph g, CompletionFlag complete) {
		throw new RuntimeException("sorry, not implemented");
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(Triple frag, CompletionFlag complete) throws Reifier.AlreadyReifiedException {
		StmtMask fragMask = new StmtMask(frag);
		if (fragMask.hasNada())
			return;
			
		boolean fragHasType = fragMask.hasType();
		Node stmtURI = frag.getSubject();
		ReifCache cachedFrag = m_reifCache.load(stmtURI);
		if (cachedFrag == null) {
			// not in database
			m_reif.storeFrag(stmtURI, frag, fragMask, my_GID);
			complete.setDone();

		} else {
			StmtMask cachedMask = cachedFrag.getStmtMask();
			if (cachedMask.hasIntersect(fragMask)) {
				// see if this is a duplicate fragment
				boolean dup = fragHasType && cachedMask.hasType();
				if (dup == false) {
					// not a type fragement; have to search db
					ExtendedIterator it = m_reif.findFrag (stmtURI, frag, fragMask, my_GID);
					dup = it.hasNext();
				}
				if (!dup && cachedMask.isStmt()) {
					throw new Reifier.AlreadyReifiedException(frag.getSubject());
				}
				// cannot perform a reificiation
				m_reif.storeFrag(stmtURI, frag, fragMask, my_GID);
				m_reifCache.flush(cachedFrag);
			} else {
				// reification may be possible; update if possible, else compact
				if (cachedFrag.canMerge(fragMask)) {
					if ( cachedFrag.canUpdate(fragMask) ) {
						m_reif.updateFrag(stmtURI, frag, fragMask, my_GID);
						cachedFrag.setMerge(fragMask);
					} else
						fragCompact(stmtURI);					
				} else {
					// reification not possible
					m_reif.storeFrag(stmtURI, frag, fragMask, my_GID);
				}
			}
		}
		complete.setDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(com.hp.hpl.jena.graph.Triple, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(Triple frag, CompletionFlag complete) {
		StmtMask fragMask = new StmtMask(frag);
		if (fragMask.hasNada())
			return;
			
		boolean fragHasType = fragMask.hasType();
		Node stmtURI = frag.getSubject();
		
		ResultSetTripleIterator it = m_reif.findFrag(stmtURI, frag, fragMask, my_GID);
		if ( it.hasNext() ) {
			it.next();
			Triple dbFrag = it.m_triple;
			StmtMask dbMask = new StmtMask(dbFrag);
			if ( dbMask.equals(fragMask) ) {
				/* last fragment in this tuple; can just delete it */
				it.deleteRow(); it.close();
			} else {
				/* remove fragment from row */
				m_reif.nullifyFrag(stmtURI, fragMask, my_GID);
				
				/* compact remaining fragments, if possible */
				it.close();
				fragCompact(stmtURI);
			}
			// remove cache entry, if any
			ReifCache cachedFrag = m_reifCache.lookup(stmtURI);
			if ( cachedFrag != null ) m_reifCache.flush(cachedFrag);		
		}
		complete.setDone();
	}
	
	
	/* fragCompact
	 * 
	 * Compact fragments for a given statement URI.
	 * 
	 * first, find the unique row for stmtURI that with the HasType Statement fragment.
	 * if no such row exists, we are done. then, get all fragments for stmtURI and
	 * try to merge them with the hasType fragment, deleting each as they are merged.
	 */
	protected void fragCompact ( Node stmtURI ) {
		ResultSetTripleIterator itHasType;
		
		itHasType = m_reif.findReifStmt(stmtURI,true,my_GID);
		if ( itHasType.hasNext() ) {
			/* something to do */
			itHasType.next();
			if ( itHasType.hasNext() ) throw new RuntimeException("Multiple HasType fragments for URI");			
			StmtMask htMask = new StmtMask(itHasType.m_triple);
			itHasType.close();
					
			// now, look at fragments and try to merge them with the hasType fragement 
			ResultSetTripleIterator itFrag = m_reif.findReifStmt(stmtURI,false,my_GID);
			StmtMask upMask = new StmtMask();
			while ( itFrag.hasNext() ) {
				StmtMask fm = new StmtMask(itFrag.m_triple);
				if ( fm.hasType() ) continue;  // got the hasType fragment again
				if ( htMask.hasIntersect(fm) )
					break; // can't merge all fragments
				// at this point, we can merge in the current fragment
				m_reif.updateFrag(stmtURI, itFrag.m_triple, fm, my_GID);
				htMask.setMerge(fm);
				itFrag.deleteRow();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#add(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void add(List triples, CompletionFlag complete) {
		ArrayList remainingTriples = new ArrayList();
		for( int i=0; i< triples.size(); i++) {
			CompletionFlag partialResult = new CompletionFlag();
			add( (Triple)triples.get(i), partialResult);
			if( !partialResult.isDone())
				remainingTriples.add(triples.get(i));
		}
		triples.clear();
		if( remainingTriples.isEmpty())
			complete.setDone();		
		else
			triples.addAll(remainingTriples);			
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#delete(java.util.List, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public void delete(List triples, CompletionFlag complete) {
		boolean result = true;
		Iterator it = triples.iterator();
		while(it.hasNext()) {
			CompletionFlag partialResult = new CompletionFlag();
			delete( (Triple)it.next(), partialResult);
			result = result && partialResult.isDone();
		}
		if( result )
			complete.setDone();		
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#tripleCount()
	 */
	public int tripleCount() {
		// A very inefficient, but simple implementation
		ExtendedIterator it = find(new StandardTripleMatch(null, null, null), new CompletionFlag());
		int count = 0;
		while (it.hasNext()) {
			count++;
		}
		it.close();
		return count;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.SpecializedGraph.CompletionFlag)
	 */
	public ExtendedIterator find(TripleMatch t, CompletionFlag complete) {
		
		Node stmtURI = t.getSubject();	// note: can be null

		ExtendedIterator nodes = m_reif.findReifNodes(stmtURI, my_GID);
		ExtendedIterator allTriples = new MapMany(nodes, new ExpandReifiedTriples(this));

		return allTriples.filterKeep(new TripleMatchFilter(t));
	}

	public class ExpandReifiedTriples implements MapFiller {

		SpecializedGraphReifier_RDB m_sgr;
		
		ExpandReifiedTriples( SpecializedGraphReifier_RDB sgr) { 
			m_sgr = sgr; 
		}
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.util.iterator.MapFiller#refill(java.lang.Object, java.util.ArrayList)
		 */
		public boolean refill(Object x, ArrayList pending) {
			Node node = (Node) x;
			boolean addedToPending = false;
			
			ResultSetTripleIterator it = m_reif.findReifStmt(node, false, my_GID);
			
			while( it.hasNext()) {
				it.next();
				if ( it.getHasType() ) {
					pending.add( new Triple( node, Reifier.type, Reifier.Statement ));
					addedToPending = true;					
				}
				Triple t = (Triple)it.getRow();
				if( !t.getSubject().equals(Node.ANY)) {
					pending.add( new Triple( node, Reifier.subject, t.getSubject() ));
					addedToPending = true;
				}
				if( !t.getPredicate().equals(Node.ANY)) {
					pending.add( new Triple( node, Reifier.predicate, t.getPredicate() ));
					addedToPending = true;
				}
				if( !t.getObject().equals(Node.ANY)) {
					pending.add( new Triple( node, Reifier.object, t.getObject() ));
					addedToPending = true;
				}					
			}
			return addedToPending;
		}

	}
	/**
	 * Tests if a triple is contained in the specialized graph.
	 * @param t is the triple to be tested
	 * @param complete is true if the graph can guarantee that 
	 *  no other specialized graph 
	 * could hold any matching triples.
	 * @return boolean result to indicte if the tripple was contained
	 */
	public boolean contains(Triple t, CompletionFlag complete) {
		// A very inefficient, but simple implementation
		TripleMatch m = new StandardTripleMatch(t.getSubject(), t.getPredicate(), t.getObject());
		ExtendedIterator it = find(m, complete);
		boolean result = it.hasNext();
		it.close();
		return result;
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#getLSetName()
	 */
	public String getLSetName() {
		return m_lsetName;
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#getClassName()
	 */
	public String getClassName() {
		return m_className;
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#close()
	 */
	public void close() {
		m_reif.close();
	}

	/*
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#clear()
	 */
	public void clear() {
		m_reif.removeStatementsFromDB(my_GID);
	}

	public class ReifCacheMap {
		protected int cacheSize = 1;
		protected ReifCache[] cache;
		protected boolean[] inUse;

		ReifCacheMap(int size) {
			int i;
			inUse = new boolean[size];
			for (i = 0; i < size; i++)
				inUse[i] = false;
		}

		ReifCache lookup(Node stmtURI) {
			int i;
			for (i = 0; i < cacheSize; i++) {
				if (inUse[i] && (cache[i].getStmtURI().equals(stmtURI)))
					return cache[i];
			}
			return null;
		}

		public void flushAll() {
			int i;
			for (i = 0; i < cacheSize; i++)
				inUse[i] = false;
		}

		public void flush(ReifCache entry) {
			flushAll(); // optimize later
		}

		public ReifCache load(Node stmtURI) {

			ReifCache entry = lookup(stmtURI);
			if (entry != null)
				return entry;
			flushAll();
			StmtMask m = new StmtMask();
			Triple t;
			int cnt = 0;
			ResultSetTripleIterator it = m_reif.findReifStmt(stmtURI,false,my_GID);
			while (it.hasNext()) {
				cnt++;
				StmtMask n = new StmtMask((Triple) it.next());
				if ( it.getHasType() ) n.setHasType();
				if ( n.hasNada() ) throw new RuntimeException("Fragment has no data");
				m.setMerge(n);
			}
			if (m.hasSPOT() && (cnt == 1))
				m.setIsStmt();

			inUse[0] = true;
			cache[0] = new ReifCache(stmtURI, m, cnt);
			return cache[0];
		}

		protected Triple fragToTriple(Triple t, StmtMask s) {
			Triple res;
			Node_URI n = (Node_URI) t.getSubject();
			if (s.hasProp())
				return new Triple(n, t.getPredicate(), Node.ANY);
			else if (s.hasObj())
				return new Triple(n, Node.ANY, t.getObject());
			else
				return new Triple(n, Node.ANY, Node.ANY);
		}

	}

	class ReifCache {
	
			protected Node stmtURI;
			protected StmtMask mask;
			protected int tripleCnt;
		
			ReifCache( Node s, StmtMask m, int cnt )
				{ stmtURI = s; mask = m; tripleCnt = cnt; }
		
			public StmtMask getStmtMask() { return mask; }
			public int getCnt() { return tripleCnt; }
			public Node getStmtURI() { return stmtURI; }
			public void setMask ( StmtMask m ) { mask = m; }
			public void setCnt ( int cnt ) { tripleCnt = cnt; }
			public void incCnt ( int cnt ) { tripleCnt++; }
			public void decCnt ( int cnt ) { tripleCnt--; }
			public boolean canMerge ( StmtMask fragMask ) {
				return (!mask.hasIntersect(fragMask)); }			
			public boolean canUpdate ( StmtMask fragMask ) {
				return ( canMerge(fragMask) && (tripleCnt == 1)); }			
			public void setMerge ( StmtMask fragMask ) {
				mask.setMerge(fragMask);  }			
	}

	static boolean isReifProp ( Node_URI p ) {
		return p.equals(Reifier.subject) ||
			p.equals(Reifier.predicate)||
			p.equals(Reifier.object) || 
			p.equals(Reifier.type);			
	}
				
	class StmtMask {
		
			protected int mask;
				
			public static final int HasSubj = 1;
			public static final int HasProp = 2;
			public static final int HasObj = 4;
			public static final int HasType = 8;
			public static final int HasSPOT = 15;
			public static final int IsStmt = 16;
			public static final int HasNada = 0;
		
			public boolean hasSubj () { return (mask ^ HasSubj) == HasSubj; };
			public boolean hasProp () { return (mask ^ HasProp) == HasProp; };
			public boolean hasObj () { return (mask ^ HasObj) == HasObj; };
			public boolean hasType () { return (mask ^ HasType) == HasType; };
			public boolean hasSPOT () { return (mask ^ HasSPOT) == HasSPOT; };
			public boolean isStmt () { return (mask ^ IsStmt) == IsStmt; };
			public boolean hasNada () { return mask == HasNada; };
			public boolean hasOneBit () { return ( (mask == HasSubj) ||
				(mask == HasProp) || (mask == HasObj) || ( mask == HasType) );
			}
				
			// note: have SPOT does not imply a reification since
			// 1) there may be multiple fragments for prop, obj
			// 2) the fragments may be in multiple tuples
		
			StmtMask ( Triple t ) {
				mask = HasNada;
				Node_URI p = (Node_URI) t.getPredicate();
				if ( p != null ) {
					if ( p.equals(Reifier.subject) ) mask = HasSubj;
					else if ( p.equals(Reifier.predicate) ) mask = HasProp; 
					else if ( p.equals(Reifier.object) ) mask = HasObj; 
					else if ( p.equals(Reifier.type) ) {
							Node_URI o = (Node_URI) t.getObject();
							if ( o.equals(Reifier.Statement) ) mask = HasType;
					}
				}			
			}
		
			StmtMask () { mask = HasNada; };
		
			public void setMerge ( StmtMask m ) {
				mask |= m.mask;	
			}
				
			public void setHasType () {
				mask |= HasType;	
			}
		
			public void setIsStmt () {
				mask |= IsStmt;	
			}
		
			public boolean hasIntersect ( StmtMask m ) {
				return (mask & m.mask) != 0;	
			}
		
			public boolean equals ( StmtMask m ) {
				return mask == m.mask;	
			}

	}

}

	/*
	 *  (c) Copyright Hewlett-Packard Company 2003
	 *  All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 * 1. Redistributions of source code must retain the above copyright
	 *    notice, this list of conditions and the following disclaimer.
	 * 2. Redistributions in binary form must reproduce the above copyright
	 *    notice, this list of conditions and the following disclaimer in the
	 *    documentation and/or other materials provided with the distribution.
	 * 3. The name of the author may not be used to endorse or promote products
	 *    derived from this software without specific prior written permission.
	
	 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */
