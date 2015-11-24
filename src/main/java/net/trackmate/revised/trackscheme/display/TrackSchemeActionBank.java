package net.trackmate.revised.trackscheme.display;

import java.awt.event.ActionEvent;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.spatial.HasTimepoint;
import bdv.util.AbstractNamedAction;

public class TrackSchemeActionBank
{

	/*
	 * SELECT CURRENT HIGHLIGHT
	 */

	public static final AbstractNamedAction getToggleSelectionOfHighlightAction( final TrackSchemeHighlight highlight, final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "toggleSelectionOfHighlight" )
		{
			private static final long serialVersionUID = -8558462430720129257L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						final int id = highlight.getHighlightedVertexId();
						if ( id >= 0 )
							selection.toggleVertex( id );
					}
				}.start();
			}
		};
	}

	/*
	 * NAVIGATE WITH HIGHLIGHT.
	 */

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getNavigateToChildAction( final HighlightNavigator< V, E > selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToChild" )
		{
			private static final long serialVersionUID = 2501943103873265924L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.child();
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getNavigateToParentAction( final HighlightNavigator< V, E > selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToParent" )
		{
			private static final long serialVersionUID = -2192196689286789410L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.parent();
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getNavigateToRightSiblingAction( final HighlightNavigator< V, E > selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToRightSibling" )
		{
			private static final long serialVersionUID = -357160008620454907L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.rightSibling();
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getNavigateToLeftSiblingAction( final HighlightNavigator< V, E > selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToLeftSibling" )
		{
			private static final long serialVersionUID = -8829626037712844926L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.leftSibling();
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getAddChildToSelectionAction( final HighlightNavigator< V, E > selectionNavigator, final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addChildToSelection" )
		{
			private static final long serialVersionUID = 3028016888295534595L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						final int id = selectionNavigator.child();
						if ( id >= 0 )
							selection.setVertexSelected( id, true );
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getAddParentToSelectionAction( final HighlightNavigator< V, E > selectionNavigator, final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addParentToSelection" )
		{
			private static final long serialVersionUID = -454383452845176573L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						final int id = selectionNavigator.parent();
						if ( id >= 0 )
							selection.setVertexSelected( id, true );
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getAddRightSiblingToSelectionAction( final HighlightNavigator< V, E > selectionNavigator, final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addRightSiblingToSelection" )
		{
			private static final long serialVersionUID = 6379431524819061875L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						final int id = selectionNavigator.rightSibling();
						if ( id >= 0 )
							selection.setVertexSelected( id, true );
					}
				}.start();
			}
		};
	}

	public static final < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > AbstractNamedAction getAddLeftSiblingToSelectionAction( final HighlightNavigator< V, E > selectionNavigator, final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "addLeftSiblingToSelection" )
		{
			private static final long serialVersionUID = -6558360734713816879L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						final int id = selectionNavigator.leftSibling();
						if ( id >= 0 )
							selection.setVertexSelected( id, true );
					}
				}.start();
			}
		};
	}

	private TrackSchemeActionBank()
	{}
}
