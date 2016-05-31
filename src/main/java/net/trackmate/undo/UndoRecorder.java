package net.trackmate.undo;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.GraphListener;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.VertexWithFeatures;
import net.trackmate.graph.features.unify.Feature;
import net.trackmate.graph.features.unify.FeatureChangeListener;
import net.trackmate.graph.features.unify.Features;

/**
 * TODO: javadoc
 * TODO: figure out, when mappings can be removed from UndoIdBimaps.
 * TODO: move to package model.undo
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class UndoRecorder< V extends VertexWithFeatures< V, E >, E extends Edge< V >, L extends DefaultUndoableEditList< V, E > >
		implements GraphListener< V, E >, UndoPointMarker
{
	private static final int defaultCapacity = 1000;

	protected boolean recording;

	protected final L edits;

	public static < V extends VertexWithFeatures< V, E >, E extends Edge< V > >
		UndoRecorder< V, E, DefaultUndoableEditList< V, E > > create(
				final ListenableGraph< V, E > graph,
				final Features< V > vertexFeatures,
				final Features< E > edgeFeatures,
				final GraphIdBimap< V, E > idmap,
				final UndoSerializer< V, E > serializer )
	{
		final UndoIdBimap< V > vertexUndoIdBimap = new UndoIdBimap<>( idmap.vertexIdBimap() );
		final UndoIdBimap< E > edgeUndoIdBimap = new UndoIdBimap<>( idmap.edgeIdBimap() );
		final DefaultUndoableEditList< V, E > edits = new DefaultUndoableEditList<>( defaultCapacity, graph, vertexFeatures, edgeFeatures, serializer, vertexUndoIdBimap, edgeUndoIdBimap );
		return new UndoRecorder<>( graph, vertexFeatures, edgeFeatures, edits );
	}

	public UndoRecorder(
			final L edits,
			final ListenableGraph< V, E > graph,
			final Features< V > vertexFeatures,
			final Features< E > edgeFeatures,
			final UndoSerializer< V, E > serializer )
	{
		recording = true;
		this.edits = edits;
		graph.addGraphListener( this );
		vertexFeatures.addFeatureChangeListener( beforeVertexFeatureChange );
		edgeFeatures.addFeatureChangeListener( beforeEdgeFeatureChange );
	}


	public UndoRecorder(
			final ListenableGraph< V, E > graph,
			final Features< V > vertexFeatures,
			final Features< E > edgeFeatures,
			final L edits )
	{
		recording = true;
		this.edits = edits;
		graph.addGraphListener( this );
		vertexFeatures.addFeatureChangeListener( beforeVertexFeatureChange );
		edgeFeatures.addFeatureChangeListener( beforeEdgeFeatureChange );
	}

	@Override
	public void setUndoPoint()
	{
		edits.setUndoPoint();
	}

	public void undo()
	{
//		System.out.println( "UndoRecorder.undo()" );
		recording = false;
		edits.undo();
		recording = true;
	}

	public void redo()
	{
//		System.out.println( "UndoRecorder.redo()" );
		recording = false;
		edits.redo();
		recording = true;
	}

	@Override
	public void graphRebuilt()
	{
		System.out.println( "UndoRecorder.graphRebuilt()" );
		System.out.println( "TODO!!!!" );
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.vertexAdded()" );
			edits.recordAddVertex( vertex );
		}
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.vertexRemoved()" );
			edits.recordRemoveVertex( vertex );
		}
	}

	@Override
	public void edgeAdded( final E edge )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.edgeAdded()" );
			edits.recordAddEdge( edge );
		}
	}

	@Override
	public void edgeRemoved( final E edge )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.edgeRemoved()" );
			edits.recordRemoveEdge( edge );
		}
	}

	private final FeatureChangeListener< V > beforeVertexFeatureChange = new FeatureChangeListener< V >()
	{
		@Override
		public void beforeFeatureChange( final Feature< ?, V, ? > feature, final V vertex )
		{
			if ( recording )
				edits.recordSetVertexFeature( feature, vertex );
		}
	};

	private final FeatureChangeListener< E > beforeEdgeFeatureChange = new FeatureChangeListener< E >()
	{
		@Override
		public void beforeFeatureChange( final Feature< ?, E, ? > feature, final E edge )
		{
			if ( recording )
				edits.recordSetEdgeFeature( feature, edge );
		}
	};
}
