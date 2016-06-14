package net.trackmate.revised.model;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.features.Features;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.undo.UndoIdBimap;
import net.trackmate.undo.GraphUndoRecorder;
import net.trackmate.undo.GraphUndoSerializer;

public class ModelUndoRecorder<
		V extends AbstractSpot< V, E, ?, ? >,
		E extends AbstractListenableEdge< E, V, ? > >
	extends GraphUndoRecorder< V, E, ModelUndoableEditList<V,E> >
	implements AbstractSpotListener< V >
{
	private static final int defaultCapacity = 1000;

	public ModelUndoRecorder(
			final AbstractModelGraph< ?, ?, ?, V, E, ? > graph,
			final Features< V > vertexFeatures,
			final Features< E > edgeFeatures,
			final GraphIdBimap< V, E > idmap,
			final GraphUndoSerializer< V, E > serializer )
	{
		super( graph, vertexFeatures, edgeFeatures,
				new ModelUndoableEditList< >(
						defaultCapacity, graph, vertexFeatures, edgeFeatures, serializer,
						new UndoIdBimap< >( idmap.vertexIdBimap() ),
						new UndoIdBimap< >( idmap.edgeIdBimap() ) ) );
		graph.addAbstractSpotListener( this );
	}

	@Override
	public void beforePositionChange( final V vertex )
	{
		if ( recording )
		{
//			System.out.println( "UndoRecorder.beforePositionChange()" );
			edits.recordSetPosition( vertex );
		}
	}
}
