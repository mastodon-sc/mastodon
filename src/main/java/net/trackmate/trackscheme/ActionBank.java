package net.trackmate.trackscheme;

import java.awt.event.ActionEvent;

public class ActionBank
{
	/*
	 * EDIT MODEL.
	 */

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

	/*
	 * NAVIGATE WITH SELECTION.
	 */

	public static final AbstractNamedAction getNavigateToChildAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "navigateToChild" )
		{
			private static final long serialVersionUID = -8748889039516159858L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.child( true );
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
				trackscheme.selectionNavigator.parent( true );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToRightSibblingAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "navigateToRightSibbling" )
		{
			private static final long serialVersionUID = 4628178914912235537L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.rightSibbling( true );
			}
		};
	}

	public static final AbstractNamedAction getNavigateToLeftSibblingAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "navigateToLeftSibbling" )
		{
			private static final long serialVersionUID = -34234313547655566L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.leftSibbling( true );
			}
		};
	}

	public static final AbstractNamedAction getAddChildToSelectionAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "addChildToSelection" )
		{
			private static final long serialVersionUID = 2974983361072182473L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.child( false );
			}
		};
	}

	public static final AbstractNamedAction getAddParentToSelectionAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "addParentToSelection" )
		{
			private static final long serialVersionUID = -1699923865575611814L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.parent( false );
			}
		};
	}

	public static final AbstractNamedAction getAddRightSibblingToSelectionAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "addRightSibblingToSelection" )
		{
			private static final long serialVersionUID = -5851501792062577225L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.rightSibbling( false );
			}
		};
	}

	public static final AbstractNamedAction getAddLeftSibblingToSelectionAction( final ShowTrackScheme trackscheme )
	{
		return new AbstractNamedAction( "addLeftSibblingToSelection" )
		{
			private static final long serialVersionUID = 5740293861004609589L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.selectionNavigator.leftSibbling( false );
			}
		};
	}

	/*
	 * ZOOM.
	 */

	public static final AbstractNamedAction getZoomOutAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomOut" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoom( zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomInAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomIn" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoom( 1 / zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomOutQuickAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomOutQuick" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoom( zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomInQuickAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomInQuick" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoom( 1 / zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomOutXQuickAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomOutXQuick" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoomX( zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomInXQuickAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomInXQuick" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoomX( 1 / zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomOutYQuickAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomOutYQuick" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoomY( zoomSpeed );
			}
		};
	}

	public static final AbstractNamedAction getZoomInYQuickAction( final ShowTrackScheme trackscheme, final double zoomSpeed )
	{
		return new AbstractNamedAction( "zoomInYQuick" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				trackscheme.transformHandler.zoomY( 1 / zoomSpeed );
			}
		};
	}


	private ActionBank()
	{}
}
