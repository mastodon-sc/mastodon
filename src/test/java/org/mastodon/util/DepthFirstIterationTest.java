/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.util;

import org.junit.Test;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link DepthFirstIteration}.
 *
 * @author Matthias Arzt
 */
public class DepthFirstIterationTest
{
	private final ModelGraph graph;

	private final Spot root;

	private static final String expected =
			"first visit a, first visit b, leaf c, leaf d, leaf e, second visit b, second visit a";

	public DepthFirstIterationTest()
	{
		graph = new ModelGraph();
		Spot a = addNode( graph, "a" );
		Spot b = addNode( graph, "b" );
		Spot c = addNode( graph, "c" );
		Spot d = addNode( graph, "d" );
		Spot e = addNode( graph, "e" );
		graph.addEdge( a, b );
		graph.addEdge( b, c );
		graph.addEdge( b, d );
		graph.addEdge( b, e );
		this.root = a;
	}

	@Test
	public void testRunForRoot()
	{
		StringJoiner log = new StringJoiner( ", " );
		for ( DepthFirstIteration.Step< Spot > step : DepthFirstIteration.forRoot( graph, root ) )
		{
			Spot node = step.node();
			log.add( stage( step ) + " " + node.getLabel() );
		}
		assertEquals( expected, log.toString() );
	}

	private String stage( DepthFirstIteration.Step< Spot > step )
	{
		if ( step.isLeaf() )
			return "leaf";
		if ( step.isFirstVisit() )
			return "first visit";
		return "second visit";
	}

	@Test
	public void testRunTwice()
	{
		StringJoiner log = new StringJoiner( ", " );
		Iterable< DepthFirstIteration.Step< Spot > > df = DepthFirstIteration.forRoot( graph, root );
		for ( DepthFirstIteration.Step< Spot > step : df )
		{
			Spot node = step.node();
			log.add( stage( step ) + " " + node.getLabel() );
		}
		for ( DepthFirstIteration.Step< Spot > step : df )
		{
			Spot node = step.node();
			log.add( stage( step ) + " " + node.getLabel() );
		}
		assertEquals( expected + ", " + expected, log.toString() );
	}

	@Test
	public void testTruncate()
	{
		StringJoiner log = new StringJoiner( ", " );
		for ( DepthFirstIteration.Step< Spot > step : DepthFirstIteration.forRoot( graph, root ) )
		{
			Spot node = step.node();
			log.add( stage( step ) + " " + node.getLabel() );
			if ( node.getLabel().equals( "b" ) )
				step.truncate();
		}
		assertEquals( "first visit a, first visit b, second visit a", log.toString() );
	}

	@Test
	public void testTruncateRoot()
	{
		StringJoiner log = new StringJoiner( ", " );
		for ( DepthFirstIteration.Step< Spot > step : DepthFirstIteration.forRoot( graph, root ) )
		{
			Spot node = step.node();
			log.add( stage( step ) + " " + node.getLabel() );
			step.truncate();
		}
		assertEquals( "first visit a", log.toString() );
	}

	private Spot addNode( ModelGraph graph, String label )
	{
		Spot a = graph.addVertex();
		a.setLabel( label );
		return a;
	}
}
