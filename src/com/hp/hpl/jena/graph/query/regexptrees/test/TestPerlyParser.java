/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestPerlyParser.java,v 1.2 2004-08-16 18:31:44 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.regexptrees.test;

import java.util.Arrays;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.query.regexptrees.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;

/**
     TestPerlyParser - tests for the parser of Perl REs into RegexpTrees.
     @author kers
*/
public class TestPerlyParser extends GraphTestBase
    {
    public TestPerlyParser( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestPerlyParser.class ); }
    
    public void testLit()
        {
        assertEquals( new Text( "a" ), new Text( "a" ) );
        assertDiffer( new Text( "a" ), new Text( "b" ) );
        assertEquals( new Text( "aga" ).hashCode(), new Text( "aga" ).hashCode() );
        }
    
    public void testInitialParserState()
        {
        assertEquals( 0, new PerlPatternParser( "hello" ).getPointer() );
        assertEquals( "hello", new PerlPatternParser( "hello" ).getString() );
        }
    
    public void testLetterAtoms()
        {
        for (char ch = 0; ch < 256; ch += 1)
            if (Character.isLetter( ch ))
                {
                PerlPatternParser p = new PerlPatternParser( "" + ch );
                assertEquals( new Text( "" + ch ), p.parseAtom() );
                assertEquals( 1, p.getPointer() );
                }
        }
    
    public void testDotAtom()
        { testSimpleSpecialAtom( new AnySingle(), "." ); }
    
    public void testHatAtom()
        { testSimpleSpecialAtom( new StartOfLine(), "^" );  }
    
    public void testDollarAtom()
        { testSimpleSpecialAtom( new EndOfLine(), "$" ); }
    
    public void testClassAtoms()
        {
        
        }
    
    public void testBackslahedAtoms()
        {
        
        }
    
    public void testParentheses()
        {
        
        }
    
    public void testNonAtoms()
        {
        
        }
    
    public void testNoQuantifier()
        {
        RegexpTree d = new AnySingle();
        assertSame( d, new PerlPatternParser( "" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "x" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "[" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "(" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "." ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "\\" ).parseQuantifier( d ) );
        }
    
    public void testStarQuantifier()
        {
        RegexpTree d = new EndOfLine();
        assertEquals( new ZeroOrMore( d ), new PerlPatternParser( "*" ).parseQuantifier( d ) );
        }
    
    public void testPlusQuantifier()
        {
        RegexpTree d = new StartOfLine();
        assertEquals( new OneOrMore( d ), new PerlPatternParser( "+" ).parseQuantifier( d ) );
        }

    public void testQueryQuantifier()
        {
        RegexpTree d = new AnySingle();
        assertEquals( new Optional( d ), new PerlPatternParser( "?" ).parseQuantifier( d ) );
        }

    public void testUnitSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "x" );
        assertEquals( new Text( "x" ), p.parseSeq() );
        }
    
    public void testSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "^.$" );
        assertEquals( seq3( new StartOfLine(), new AnySingle(), new EndOfLine() ), p.parseSeq() );
        }
    
    protected RegexpTree seq3( RegexpTree a, RegexpTree b, RegexpTree c )
        {
        return Sequence.create( Arrays.asList( new RegexpTree[] {a, b, c} ) );
        }
    
    public void testOldSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "hello" );
        assertEquals( new Text( "h" ), p.parseAtom() );
        assertEquals( 1, p.getPointer() );
        assertEquals( new Text( "e" ), p.parseAtom() );
        assertEquals( 2, p.getPointer() );
        assertEquals( new Text( "l" ), p.parseAtom() );
        assertEquals( 3, p.getPointer() );
        assertEquals( new Text( "l" ), p.parseAtom() );
        assertEquals( 4, p.getPointer() );
        assertEquals( new Text( "o" ), p.parseAtom() );
        assertEquals( 5, p.getPointer() );
        assertEquals( null, p.parseAtom() );
        }
    
    public void testSimpleSpecialAtom( Object wanted, String toParse )
        {
        PerlPatternParser p = new PerlPatternParser( toParse );
        assertEquals( wanted, p.parseAtom() );
        assertEquals( 1, p.getPointer() );
        }
    
    }


/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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