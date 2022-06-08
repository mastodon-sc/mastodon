/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme;

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

	private static final String expected = "before a, before b, leaf c, leaf d, leaf e, after b, after a";

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

	private DepthFirstIteration<Spot> setupLoggingDepthFirstIteration( StringJoiner log )
	{
		DepthFirstIteration<Spot> df = new DepthFirstIteration<>( graph );
		df.setVisitNodeBeforeChildrenAction( node -> {
			log.add( "before " + node.getLabel() );
		} );
		df.setVisitLeafAction( leaf -> {
			log.add( "leaf " + leaf.getLabel() );
		} );
		df.setVisitNodeAfterChildrenAction( (node, children) -> {
			log.add( "after " + node.getLabel() );
		} );
		return df;
	}

	@Test
	public void testRunForRoot() {
		StringJoiner log = new StringJoiner( ", " );
		DepthFirstIteration<Spot> df = setupLoggingDepthFirstIteration( log );
		df.runForRoot( root );
		assertEquals( expected, log.toString());
	}

	@Test
	public void testRunTwice() {
		StringJoiner log = new StringJoiner( ", " );
		DepthFirstIteration<Spot> df = setupLoggingDepthFirstIteration( log );
		df.runForRoot( root );
		df.runForRoot( root );
		assertEquals(expected + ", " + expected, log.toString());
	}

	@Test
	public void testExcludeNode() {
		StringJoiner log = new StringJoiner( ", " );
		DepthFirstIteration<Spot> df = setupLoggingDepthFirstIteration( log );
		df.setExcludeNodeAction( node -> {
			log.add( "test " + node.getLabel() );
			return node.getLabel().equals( "b" );
		} );
		df.runForRoot( root );
		assertEquals( "test a, before a, test b, after a", log.toString() );
	}

	private Spot addNode( ModelGraph graph, String label )
	{
		Spot a = graph.addVertex();
		a.setLabel( label );
		return a;
	}
}
