package net.trackmate.undo;

import net.trackmate.graph.Edge;
import net.trackmate.graph.FeatureChangeListener;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.VertexWithFeatures;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.revised.model.ModelGraph_HACK_FIX_ME;

/**
 * TODO: javadoc
 * TODO: figure out, when mappings can be removed from UndoIdBimaps.
 * TODO: move to package model.undo
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class UndoRecorder< V extends VertexWithFeatures< V, E >, E extends Edge< V > >
		implements GraphListener< V, E >, FeatureChangeListener< V, E >
{
	private static final int defaultCapacity = 1000;

	private boolean recording;

	private final DefaultUndoableEditList< V, E > edits;

	public UndoRecorder(
			final ModelGraph_HACK_FIX_ME< V, E > graph,
			final GraphFeatures< V, E > graphFeatures,
			final GraphIdBimap< V, E > idmap,
			final UndoSerializer< V, E > serializer )
	{
		final UndoIdBimap< V > vertexUndoIdBimap = new UndoIdBimap<>( idmap.vertexIdBimap() );
		final UndoIdBimap< E > edgeUndoIdBimap = new UndoIdBimap<>( idmap.edgeIdBimap() );
		edits = new DefaultUndoableEditList<>( defaultCapacity, graph, graphFeatures, serializer, vertexUndoIdBimap, edgeUndoIdBimap );
		recording = true;
		graph.addGraphListener( this );
	}

	public void setUndoPoint()
	{
		edits.setUndoPoint();
	}

	public void undo()
	{
		recording = false;
		edits.undo();
		recording = true;
	}

	public void redo()
	{
		recording = false;
		edits.redo();
		recording = true;
	}

	@Override
	public void graphRebuilt()
	{
		System.out.println( "Model.UndoRecorder.graphRebuilt()" );
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.vertexAdded()" );
			edits.recordAddVertex( vertex );
		}
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.vertexRemoved()" );
			edits.recordRemoveVertex( vertex );
		}
	}

	@Override
	public void edgeAdded( final E edge )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.edgeAdded()" );
			edits.recordAddEdge( edge );
		}
	}

	@Override
	public void edgeRemoved( final E edge )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.edgeRemoved()" );
			edits.recordRemoveEdge( edge );
		}
	}

	@Override
	public void beforeFeatureChange( final VertexFeature< ?, V, ? > feature, final V vertex )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.beforeFeatureChange()" );
			edits.recordSetFeature( feature, vertex );
		}
	}
}
