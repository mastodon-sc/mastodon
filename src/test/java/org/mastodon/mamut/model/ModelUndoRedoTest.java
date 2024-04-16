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
		final UndoVerifier undoVerifier = new UndoVerifier( new Model() );
		undoVerifier.makeChangesAndRecord();
		undoVerifier.undoAndVerifyCorrectness();
		undoVerifier.redoAndVerifyCorrectness();
	}

	private static class UndoVerifier
	{

		final Model model;

		final List< String > states = new ArrayList<>();

		public UndoVerifier( final Model model ) {this.model = model;}

		public void makeChangesAndRecord()
		{
			final ModelGraph graph = model.getGraph();
			// add spot A
			final Spot a = graph.addVertex().init( 2, new double[] { 1, 2, 3 }, 1.5 );
			a.setLabel( "spot A" );
			recordStep();

			// add spot B
			final Spot b = graph.addVertex().init( 3, new double[] { 1, 2, 4 }, 1.7 );
			b.setLabel( "spot B" );
			recordStep();

			// add edge
			final Link edge = graph.addEdge( a, b ).init();
			recordStep();

			// add tag set
			final TagSetStructure.TagSet tagset = TagSetUtils.addNewTagSetToModel( model, "tag set 1", Arrays.asList(
					Pair.of( "tag1", Color.red.getRGB() ),
					Pair.of( "tag2", Color.green.getRGB() ) ) );
			final TagSetStructure.Tag tag1 = tagset.getTags().get( 0 );
			final TagSetStructure.Tag tag2 = tagset.getTags().get( 1 );
			TagSetUtils.tagSpot( model, tagset, tag1, a );
			TagSetUtils.tagSpot( model, tagset, tag2, b );
			TagSetUtils.tagLinks( model, tagset, tag1, Collections.singletonList( edge ) );
			recordStep();

			// change label
			a.setLabel( "A0" );
			b.setLabel( "B0" );
			recordStep();

			// change tag
			TagSetUtils.tagSpot( model, tagset, tag2, a );
			recordStep();

			// change covariance
			final double[][] cov = { { 2, 0.1, 0 }, { 0.1, 2.5, 0 }, { 0, 0, 2.1 } };
			a.setCovariance( cov );
			recordStep();

			// change spot position
			b.setPosition( new double[] { 2.1, 2.3, 2.2 } );
			recordStep();

			// remove spot B
			graph.remove( b );
			recordStep();
		}

		private void recordStep()
		{
			model.setUndoPoint();
			states.add( modelAsString( model ) );
		}

		public void undoAndVerifyCorrectness()
		{
			for ( int i = states.size() - 2; i >= 0; i-- )
			{
				model.undo();
				assertEquals( states.get( i ), modelAsString( model ) );
			}
		}

		public void redoAndVerifyCorrectness()
		{
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

		// Fill the undo-redo history with many different entries. All these changes have to be properly cleared during the import operation.
		new UndoVerifier( model ).makeChangesAndRecord();
		addFourSpotGraph( model );

		// Simulate import operation, this will reset the model and the undo-redo history.
		new Importer( model ).run();

		// Test if the undo and redo still work as expected:
		final UndoVerifier undoVerifier = new UndoVerifier( model );
		undoVerifier.makeChangesAndRecord();
		undoVerifier.undoAndVerifyCorrectness();
		undoVerifier.redoAndVerifyCorrectness();
	}

	private class Importer extends ModelImporter
	{

		private final Model model;

		public Importer( final Model model )
		{
			super( model );
			this.model = model;
		}

		private void run()
		{
			startImport(); // Calls pauseListeners() and clear() on the ModelGraph and TagSetModel.
			addThreeSpotGraph( model ); // simulate some data import
			finishImport(); // Calls resumeListeners() and notifyGraphChanged(). resumeListeners() effectively clears the undo-redo history.
		}
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
