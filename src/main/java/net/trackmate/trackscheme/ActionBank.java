package net.trackmate.trackscheme;

import java.awt.event.ActionEvent;

public class ActionBank
{
	public static final AbstractNamedAction getDeleteSelectionAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "deleteSelection" )
		{
			private static final long serialVersionUID = 7237432299287538771L;

			@Override
			public void actionPerformed( ActionEvent event )
			{
				final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel = trackscheme.selectionModel;
				final TrackSchemeGraph graph = trackscheme.graph;
				for ( final TrackSchemeVertex v : selectionModel.getSelectedVertices() )
				{
					graph.remove( v );
				}
				for ( final TrackSchemeEdge e : selectionModel.getSelectedEdges() )
				{
					graph.remove( e );
				}
				selectionModel.clearSelection();
				trackscheme.layout.layoutX();
				trackscheme.order.build();
			}
		};
	}

	private ActionBank()
	{}
}
