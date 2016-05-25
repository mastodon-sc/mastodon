package net.trackmate.revised.model.mamut;

import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.revised.model.AbstractModelGraph;
import net.trackmate.undo.UndoIdBimap;
import net.trackmate.undo.UndoRecorder;
import net.trackmate.undo.UndoSerializer;

public class ModelSpotUndoRecorder extends UndoRecorder< Spot, Link, ModelSpotUndoableEditList >
		implements AbstractSpotCovarianceListener
{
	private static final int defaultCapacity = 1000;

	public ModelSpotUndoRecorder(
			final AbstractModelGraph< ?, ?, ?, Spot, Link, ? > graph,
			final GraphFeatures< Spot, Link > graphFeatures,
			final GraphIdBimap< Spot, Link > idmap,
			final UndoSerializer< Spot, Link > serializer )
	{
		super( graph,
				graphFeatures,
				new ModelSpotUndoableEditList(
						defaultCapacity, graph, graphFeatures, serializer,
						new UndoIdBimap< >( idmap.vertexIdBimap() ),
						new UndoIdBimap< >( idmap.edgeIdBimap() ) ) );
		graph.addAbstractSpotListener( this );
	}

	@Override
	public void beforePositionChange( final Spot vertex )
	{
		if ( recording )
		{
			System.out.println( "UndoRecorder.beforePositionChange()" );
			edits.recordSetPosition( vertex );
		}
	}

	@Override
	public void beforeCovarianceChange( final Spot vertex )
	{
		if ( recording )
		{
			System.out.println( "UndoRecorder.beforeCovarianceChange()" );
			edits.recordSetCovariance( vertex );
		}
	}

}
