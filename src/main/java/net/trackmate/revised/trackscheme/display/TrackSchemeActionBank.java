package net.trackmate.revised.trackscheme.display;

import java.awt.event.ActionEvent;

import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import bdv.util.AbstractNamedAction;

public class TrackSchemeActionBank
{

	/*
	 * SELECT CURRENT FOCUS
	 */

	public static final AbstractNamedAction getToggleSelectionOfHighlightAction( final TrackSchemeFocus focus, final TrackSchemeSelection selection )
	{
		return new AbstractNamedAction( "toggleSelectionOfFocus" )
		{
			private static final long serialVersionUID = 3749694022046537514L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						final int id = focus.getFocusedVertexId();
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

	public static final AbstractNamedAction getNavigateToChildAction( final TrackSchemeNavigator selectionNavigator )
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

	public static final AbstractNamedAction getNavigateToParentAction( final TrackSchemeNavigator selectionNavigator )
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

	public static final AbstractNamedAction getNavigateToRightSiblingAction( final TrackSchemeNavigator selectionNavigator )
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

	public static final AbstractNamedAction getNavigateToLeftSiblingAction( final TrackSchemeNavigator selectionNavigator )
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

	public static final AbstractNamedAction getAddChildToSelectionAction( final TrackSchemeNavigator selectionNavigator, final TrackSchemeSelection selection )
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

	public static final AbstractNamedAction getAddParentToSelectionAction( final TrackSchemeNavigator selectionNavigator, final TrackSchemeSelection selection )
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

	public static final AbstractNamedAction getAddRightSiblingToSelectionAction( final TrackSchemeNavigator selectionNavigator, final TrackSchemeSelection selection )
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

	public static final AbstractNamedAction getAddLeftSiblingToSelectionAction( final TrackSchemeNavigator selectionNavigator, final TrackSchemeSelection selection )
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
