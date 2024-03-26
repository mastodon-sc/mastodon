/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.model;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;

/**
 * The robustness of the undo and redo methods is crucial for Mastodon. This
 * test thoroughly tests the undo and redo methods of the {@link Model} class.
 */
public class ModelUndoRedoTest
{
	/**
	 * Test if undo and redo works for basic operations: adding spots and edges.
	 */
	@Test
	public void testUndoRedo()
	{
		final Model model = new Model();
		final ModelGraph graph = model.getGraph();
		model.setUndoPoint();
		final Spot a = graph.addVertex().init( 0, new double[ 3 ], 1 );
		a.setLabel( "A" );
		model.setUndoPoint();
		assertEquals( "A", graphAsString( graph ) );
		final Spot b = graph.addVertex().init( 0, new double[ 3 ], 1 );
		b.setLabel( "B" );
		model.setUndoPoint();
		assertEquals( "A, B", graphAsString( graph ) );
		graph.addEdge( a, b ).init();
		model.setUndoPoint();
		assertEquals( "A, B, A->B", graphAsString( graph ) );
		model.undo();
		assertEquals( "A, B", graphAsString( graph ) );
		model.undo();
		assertEquals( "A", graphAsString( graph ) );
		model.undo();
		assertEquals( "", graphAsString( graph ) );
		model.redo();
		assertEquals( "A", graphAsString( graph ) );
		model.redo();
		assertEquals( "A, B", graphAsString( graph ) );
		model.redo();
		assertEquals( "A, B, A->B", graphAsString( graph ) );
	}

	/**
	 * Returns a readable sting representation of the graph.
	 */
	private String graphAsString( final ModelGraph graph )
	{
		final List< String > spots = new ArrayList<>();
		for ( final Spot spot : graph.vertices() )
			spots.add( spot.getLabel() );
		Collections.sort( spots );
		final List< String > links = new ArrayList<>();
		for ( final Link link : graph.edges() )
			links.add( link.getSource().getLabel() + "->" + link.getTarget().getLabel() );
		Collections.sort( links );
		final StringJoiner joiner = new StringJoiner( ", " );
		spots.forEach( joiner::add );
		links.forEach( joiner::add );
		return joiner.toString();
	}

	/**
	 * This test makes various changes to the model and then tests if the undo
	 * and redo methods work as expected.
	 */
	@Test
	public void testUndoRedoOnEmptyModel()
	{
		final Model model = new Model();
		testUndoRedo( model );
	}

	/**
	 * This method makes various changes to the graph and the tag set model. The
	 * changes are divided into several steps, and at each step, an undo point is
	 * set. Then, the undo and redo methods are called to test if the model is
	 * restored to the intended state.
	 */
	private static void testUndoRedo( final Model model )
	{
		final List< String > states = new ArrayList<>();
		makeVariousChangesAndRecordUndoPoints( model, states );
		undoAllRecordedPointsAndVerifyCorrectness( model, states );
		redoAllRecordedPointsAndVerifyCorrectness( model, states );
	}

	private static void makeVariousChangesAndRecordUndoPoints( final Model model, final List< String > states )
	{
		final ModelGraph graph = model.getGraph();
		// add spot A
		final Spot a = graph.addVertex().init( 2, new double[] { 1, 2, 3 }, 1.5 );
		a.setLabel( "spot A" );
		setUndoPointAndRecordState( model, states );

		// add spot B
		final Spot b = graph.addVertex().init( 3, new double[] { 1, 2, 4 }, 1.7 );
		b.setLabel( "spot B" );
		setUndoPointAndRecordState( model, states );

		// add edge
		final Link edge = graph.addEdge( a, b ).init();
		setUndoPointAndRecordState( model, states );

		// add tag set
		final TagSetStructure.TagSet tagset = TagSetUtils.addNewTagSetToModel( model, "tag set 1", Arrays.asList(
				Pair.of( "tag1", Color.red.getRGB() ),
				Pair.of( "tag2", Color.green.getRGB() ) ) );
		final TagSetStructure.Tag tag1 = tagset.getTags().get( 0 );
		final TagSetStructure.Tag tag2 = tagset.getTags().get( 1 );
		TagSetUtils.tagSpot( model, tagset, tag1, a );
		TagSetUtils.tagSpot( model, tagset, tag2, b );
		TagSetUtils.tagLinks( model, tagset, tag1, Collections.singletonList( edge ) );
		setUndoPointAndRecordState( model, states );

		// change label
		a.setLabel( "A0" );
		b.setLabel( "B0" );
		setUndoPointAndRecordState( model, states );

		// change tag
		TagSetUtils.tagSpot( model, tagset, tag2, a );
		setUndoPointAndRecordState( model, states );

		// change covariance
		final double[][] cov = { { 2, 0.1, 0 }, { 0.1, 2.5, 0 }, { 0, 0, 2.1 } };
		a.setCovariance( cov );
		setUndoPointAndRecordState( model, states );

		// change spot position
		b.setPosition( new double[] { 2.1, 2.3, 2.2 } );
		setUndoPointAndRecordState( model, states );

		// remove spot B
		graph.remove( b );
		setUndoPointAndRecordState( model, states );
	}

	public static void setUndoPointAndRecordState( final Model model, final List< String > states )
	{
		model.setUndoPoint();
		states.add( modelAsString( model ) );
	}

	public static void undoAllRecordedPointsAndVerifyCorrectness( final Model model, final List< String > states )
	{
		for ( int i = states.size() - 1; i > 0; i-- )
		{
			assertEquals( states.get( i ), modelAsString( model ) );
			model.undo();
		}
		assertEquals( states.get( 0 ), modelAsString( model ) );
	}

	public static void redoAllRecordedPointsAndVerifyCorrectness( final Model model, final List< String > states )
	{
		assertEquals( states.get( 0 ), modelAsString( model ) );
		for ( int i = 1; i < states.size(); i++ )
		{
			model.redo();
			assertEquals( states.get( i ), modelAsString( model ) );
		}
	}

	private static String modelAsString( final Model model )
	{
		return ModelUtils.dump( model, ModelUtils.DumpFlags.PRINT_TAGS );
	}

	/**
	 * This test ensures that the undo and redo methods work as expected even
	 * after the running ModelImporter.
	 * <p>
	 * The test first performs various changes to the model. Simply to fill the
	 * undo-redo history with many different entries. Then running the ModelImporter
	 * will reset the model and the undo-redo history.
	 * This test ensures that the undo and redo methods work as expected even after
	 * the model has been reset by the ModelImporter.
	 */
	@Test
	public void testUndoRedoAfterModelImporter()
	{
		final Model model = new Model();
		// Fill the undo-redo history with many different entries:
		makeVariousChangesAndRecordUndoPoints( model, new ArrayList<>() );
		addFourSpotGraph( model );
		// Reset the model by running the ModelImporter / Simulate an import operation:
		new ModelImporter( model )
		{{
			this.startImport();
			addThreeSpotGraph( model );
			this.finishImport();
		}};
		// Test if the undo and redo still work as expected:
		testUndoRedo( model );
	}

	/**
	 * Adds three spots and two edges to the model.
	 */
	private void addThreeSpotGraph( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { 1, 0, 0 }, 1 );
		a.setLabel( "A" );
		final Spot b = graph.addVertex().init( 1, new double[] { 0, 2, 0 }, 1 );
		b.setLabel( "B" );
		final Spot c = graph.addVertex().init( 1, new double[] { 0, 0, 3 }, 1 );
		c.setLabel( "C" );
		graph.addEdge( a, b ).init();
		graph.addEdge( a, c ).init();
		model.setUndoPoint();
	}

	/**
	 * Adds four spots and three edges to the model.
	 */
	private void addFourSpotGraph( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { 1, 2, 3 }, 1 );
		a.setLabel( "A" );
		final Spot b = graph.addVertex().init( 1, new double[] { 2, 2, 3 }, 1 );
		b.setLabel( "B" );
		final Spot c = graph.addVertex().init( 2, new double[] { 3, 2, 3 }, 1 );
		c.setLabel( "C" );
		final Spot d = graph.addVertex().init( 3, new double[] { 4, 2, 3 }, 1 );
		d.setLabel( "D" );
		graph.addEdge( a, b ).init();
		graph.addEdge( b, c ).init();
		graph.addEdge( c, d ).init();
		model.setUndoPoint();
	}
}
