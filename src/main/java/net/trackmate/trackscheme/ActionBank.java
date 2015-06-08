package net.trackmate.trackscheme;

import java.awt.event.ActionEvent;

import net.trackmate.graph.IncomingEdges;
import net.trackmate.graph.OutgoingEdges;
import net.trackmate.graph.collection.RefSet;

public class ActionBank
{
	public static final AbstractNamedAction getDeleteSelectionAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "deleteSelection" )
		{
			private static final long serialVersionUID = 7237432299287538771L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel = trackscheme.selectionModel;
				final TrackSchemeGraph graph = trackscheme.graph;
				for ( final TrackSchemeEdge e : selectionModel.getSelectedEdges() )
				{
					graph.remove( e );
				}
				for ( final TrackSchemeVertex v : selectionModel.getSelectedVertices() )
				{
					graph.remove( v );
				}
				selectionModel.clearSelection();
				trackscheme.layout.layoutX();
				trackscheme.order.build();
				trackscheme.repaint( true );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToChildAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "navigateToChild" )
		{
			private static final long serialVersionUID = -8748889039516159858L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel = trackscheme.selectionModel;
				final RefSet< TrackSchemeVertex > vertices = selectionModel.getSelectedVertices();
				if ( vertices.size() == 1 )
				{
					final TrackSchemeVertex vertex = vertices.iterator().next();
					final OutgoingEdges< TrackSchemeEdge > edges = vertex.outgoingEdges();
					if ( edges.size() > 0 )
					{
						final TrackSchemeEdge edge = edges.get( 0 );
						final TrackSchemeVertex child = edge.getTarget();
						selectionModel.clearSelection();
						vertex.setSelected( false );
						selectionModel.add( child );
						child.setSelected( true );
						trackscheme.refresh();
					}
				}
			}
		};
	}

	public static final AbstractNamedAction getNavigateToParentAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "navigateToParent" )
		{
			private static final long serialVersionUID = -819879165845995978L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel = trackscheme.selectionModel;
				final RefSet< TrackSchemeVertex > vertices = selectionModel.getSelectedVertices();
				if ( vertices.size() == 1 )
				{
					final TrackSchemeVertex vertex = vertices.iterator().next();
					final IncomingEdges< TrackSchemeEdge > edges = vertex.incomingEdges();
					if ( edges.size() > 0 )
					{
						final TrackSchemeEdge edge = edges.get( 0 );
						final TrackSchemeVertex child = edge.getSource();
						selectionModel.clearSelection();
						vertex.setSelected( false );
						selectionModel.add( child );
						child.setSelected( true );
						trackscheme.refresh();
					}
				}
			}
		};
	}

	private ActionBank()
	{}
}
