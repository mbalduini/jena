/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.SinkNull ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.ErrorHandlerLib ;
import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.riot.ParserFactory ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.riot.lang.LangParseRDFXML ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.riot.out.SinkQuadOutput ;
import com.hp.hpl.jena.riot.out.SinkTripleOutput ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Parse to triples/quads for Turtle, TriG, N-Triples, N-Quads */ 
public class riot extends LangParse
{
    
    public static void main(String... argv)
    {
        new riot(argv).mainRun() ;
    }    
    
    protected riot(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected void parseRIOT(String baseURI, String filename, InputStream in)
    {
        Checker checker = null ;
        if ( modLangParse.checking() )
        {
            if ( modLangParse.stopOnBadTerm() )
                checker = new Checker(ErrorHandlerLib.errorHandlerStd)  ;
            else
                checker = new Checker(ErrorHandlerLib.errorHandlerWarn) ;
        }
        
        
        Lang lang = Lang.guess(filename, Lang.NQUADS) ; 
        
        if ( lang.equals(Lang.RDFXML) )
        {
            // Does not count output.
            modTime.startTimer() ;
            // Support RDF/XML.
            long n = LangParseRDFXML.parseRDFXML(baseURI, filename, checker.getHandler(), in, !modLangParse.toBitBucket()) ;
            long x = modTime.endTimer() ;
            
            if ( modTime.timingEnabled() )
                output(filename, n, x) ;
            totalMillis += x ;
            totalTuples += n ;
            return ;
        }
        
        SinkCounting<?> sink ;
        LangRIOT parser ;

        // Uglyness because quads and triples aren't subtype of some Tuple<Node>
        // That would change a lot (Triples came several years before Quads). 
        if ( lang.isTriples() )
        {
            Sink <Triple> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkTripleOutput(System.out) ;
            SinkCounting<Triple> sink2 = new SinkCounting<Triple>(s) ;
            parser = ParserFactory.createParserTriples(in, lang, baseURI, sink2) ;
            sink = sink2 ;
        }
        else
        {
            Sink <Quad> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkQuadOutput(System.out) ;
            SinkCounting<Quad> sink2 = new SinkCounting<Quad>(s) ;
            parser = ParserFactory.createParserQuads(in, lang, baseURI, sink2) ;
            sink = sink2 ;
        }
        
        modTime.startTimer() ;
        try
        {
            parser.parse() ;
        }
        catch (RiotException ex)
        {
            if ( modLangParse.stopOnBadTerm() )
                return ;
        }
        finally {
            IO.close(in) ;
            sink.close() ;
        }
        long x = modTime.endTimer() ;
        long n = sink.getCount() ;
        

        if ( modTime.timingEnabled() )
            output(filename, n, x) ;
        
        totalMillis += x ;
        totalTuples += n ;
    }
    
    @Override
    protected String getCommandName()
    {
        return Utils.classShortName(riot.class) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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