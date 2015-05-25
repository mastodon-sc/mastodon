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
				trackscheme.frame.repaint();
			}
		};
	}

	public static final AbstractNamedAction getNavigateToChildAction( final ShowTrackScheme trackscheme, final boolean clear )
	{
		return new AbstractNamedAction( "navigateToChild" )
		{
			private static final long serialVersionUID = 2758338015205616122L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.child( clear );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToParentAction( final ShowTrackScheme trackscheme, final boolean clear )
	{
		return new AbstractNamedAction( "navigateToParent" )
		{
			private static final long serialVersionUID = -3519859437852750226L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.parent( clear );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToRightSibblingAction( final ShowTrackScheme trackscheme, final boolean clear )
	{
		return new AbstractNamedAction( "navigateToRightSibbling" )
		{
			private static final long serialVersionUID = 5817858324118528078L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.rightSibbling( clear );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToLeftSibblingAction( final ShowTrackScheme trackscheme, final boolean clear )
	{
		return new AbstractNamedAction( "navigateToLeftSibbling" )
		{
			private static final long serialVersionUID = -4497884382060384048L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.leftSibbling( clear );
			}
		};
	}

	private ActionBank()
	{}
}
