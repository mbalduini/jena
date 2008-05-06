/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator;
import java.util.Stack;

import arq.sparql;
import arq.sse_query;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.sse.SSE;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.update.*;

public class Run
{
    public static void main(String[] argv) throws Exception
    {
        UpdateRequest r = UpdateFactory.read("update.ru") ;
        GraphStore gs = GraphStoreFactory.create() ;
        UpdateProcessor uProc = UpdateFactory.create(r, gs) ;
        uProc.execute() ;

        SSE.write(gs) ;
        System.exit(0) ;
        
        
        //code() ; System.exit(0) ;
        runUpdate() ; System.exit(0) ;
        
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data=D.ttl",
            "-query=Q.rq" ,
        } ;

        sparql.main(a) ;
        System.exit(0) ;
    }
    
    // Can be a visitor because we dont change the Op structure.  But we don't do that do we? 
    static class TransformBGP extends TransformCopy
    {
        // Scope stack of mentioned variables (fixed and optional)
        private Stack scope = new Stack() ;
        
        public TransformBGP()
        { }
        
        public Op transform(OpJoin opJoin, Op left, Op right)
        { return super.transform(opJoin, left, right) ; }
        
        public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)
        { return super.transform(opLeftJoin, left, right) ; }
        
        public Op transform(OpBGP opBGP)
        {
            BasicPattern pattern = opBGP.getPattern() ;
            BasicPattern pattern2 = new BasicPattern() ;
            
            // Choose order.
            for ( Iterator iter = pattern.getList().listIterator() ; iter.hasNext() ; )
            {
                Triple triple = (Triple)iter.next();
                System.out.println("Process: "+triple) ;
                pattern2.add(triple) ;
            }
            return new OpBGP(pattern2) ; 
            
            //return super.transform(opBGP) ;
        }
    }
    
    public static void code()
    {
        Transform t = new TransformBGP() ;
        
        // see Rewrite.java
        Query q = QueryFactory.create("SELECT * { ?s ?p ?o FILTER(sameTerm(?o,3)) }") ;
        Op op = Algebra.compile(q, true) ;
        op = Algebra.optimize(op) ;
        
        op = Transformer.transform(new TransformBGP(), op) ;
        
        System.out.println(op) ; 
        
    }
    private static void runQParse()
    {
        String []a = { "--engine=ref", "--file=Q.rq"/*, "--print=op"*/ } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQParseARQ()
    {
        String []a = { "--file=Q.arq", "--out=arq", "--print=op", "--print=query" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runUpdate()
    {
        String a[] = {/*"--desc=etc/graphstore.ttl",*/ "--update=update.ru", "--dump"} ;
        arq.update.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQTest()
    {
        String DIR = "/home/afs/W3C/DataAccess/tests/data-r2/expr-equals/" ;
        String []a1 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-2.rq",
            "--result="+DIR+"result-eq2-2.ttl"} ;

        String []a2 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-graph-1.rq",
            "--result="+DIR+"result-eq2-graph-1.ttl"} ;

        arq.qtest.main(a1) ;
        System.exit(0 ) ; 
  
    }

    private static void execQuery(String datafile, String queryfile)
    {
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sparql.main(a) ;
        System.exit(0) ;
        
    }
    
    private static void execQuerySSE(String datafile, String queryfile)
    {
        //com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sse_query.main(a) ;
        System.exit(0) ;
        
    }
    
    private static void execQueryCode(String datafile, String queryfile)
    {
        Model model = FileManager.get().loadModel(datafile) ;
        Query query = QueryFactory.read(queryfile) ;
        
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("s", model.createResource("http://example.org/x2")) ;
        initialBinding.add("o", model.createResource("http://example.org/z")) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, model, initialBinding) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        System.exit(0) ;
    }
}



/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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