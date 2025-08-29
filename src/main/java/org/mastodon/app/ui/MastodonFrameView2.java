package org.mastodon.app.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.TagSetMenu;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColorBarOverlayMenu;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.Keymap.UpdateListener;

public class MastodonFrameView2<
			M extends MastodonModel< ?, MV, ME >,
			VG extends ViewGraph< MV, ME, V, E >,
			MV extends AbstractListenableVertex< MV, ME, ?, ? > & HasTimepoint,
			ME extends AbstractListenableEdge< ME, MV, ?, ? >,
			V extends Vertex< E >,
			E extends Edge< V > >
		extends MastodonView2< M, VG, MV, ME, V, E >
		implements HasFrame
{

	protected ViewFrame frame;

	protected final String[] keyConfigContexts;

	protected Actions viewActions;

	protected Behaviours viewBehaviours;

	public MastodonFrameView2( final M dataModel, final UIModel uiModel, final VG viewGraph, final ReentrantReadWriteLock lock, final String[] keyConfigContexts )
	{
		super( dataModel, uiModel, viewGraph, lock );

		final Set< String > c = new LinkedHashSet<>( Arrays.asList( uiModel.getKeyConfigContexts() ) );
		c.addAll( Arrays.asList( keyConfigContexts ) );
		this.keyConfigContexts = c.toArray( new String[] {} );
	}

	@Override
	public ViewFrame getFrame()
	{
		return frame;
	}

	protected void setFrame( final ViewFrame frame )
	{
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );
		this.frame = frame;

		final Actions projectActions = uiModel.getProjectActions();
		if ( projectActions != null )
		{
			frame.keybindings.addActionMap( "project", new WrappedActionMap( projectActions.getActionMap() ) );
			frame.keybindings.addInputMap( "project", new WrappedInputMap( projectActions.getInputMap() ) );
		}

		final Actions pluginActions = uiModel.getPlugins().getPluginActions();
		if ( pluginActions != null )
		{
			frame.keybindings.addActionMap( "plugin", new WrappedActionMap( pluginActions.getActionMap() ) );
			frame.keybindings.addInputMap( "plugin", new WrappedInputMap( pluginActions.getInputMap() ) );
		}

		final Actions modelActions = uiModel.getModelActions();
		frame.keybindings.addActionMap( "model", new WrappedActionMap( modelActions.getActionMap() ) );
		frame.keybindings.addInputMap( "model", new WrappedInputMap( modelActions.getInputMap() ) );

		final Keymap keymap = uiModel.getKeymap();

		viewActions = new Actions( keymap.getConfig(), keyConfigContexts );
		viewActions.install( frame.keybindings, "view" );

		viewBehaviours = new Behaviours( keymap.getConfig(), keyConfigContexts );
		viewBehaviours.install( frame.triggerbindings, "view" );

		final UpdateListener updateListener = () -> {
			viewBehaviours.updateKeyConfig( keymap.getConfig() );
			viewActions.updateKeyConfig( keymap.getConfig() );
		};
		keymap.updateListeners().add( updateListener );
		onClose( () -> keymap.updateListeners().remove( updateListener ) );
	}

	/*
	 * Coloring methods.
	 *
	 * Since they require a JMenuHandle, their place is here.
	 */

	/**
	 * Registers a {@link ColorBarOverlayMenu} in the specified menu handle, and
	 * links it to the specified {@link ColorBarOverlay}.
	 *
	 * @param colorBarOverlay
	 *            the {@link ColorBarOverlay} to link to the menu.
	 * @param menuHandle
	 *            the menu handle where to create the menu.
	 * @param refresh
	 *            a {@link Runnable} that refreshes the view when the color bar
	 *            visibility is modified.
	 */
	protected void registerColorbarOverlay(
			final ColorBarOverlay colorBarOverlay,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final ColorBarOverlayMenu menu = new ColorBarOverlayMenu( menuHandle.getMenu(), colorBarOverlay, refresh );
		colorBarOverlay.listeners().add( menu );
	}

	/**
	 * Registers a {@link TagSetMenu} in the specified menu handle, if the data
	 * model supports it. Otherwise does nothing.
	 *
	 * @param menuHandle
	 *            the
	 * @param refresh
	 *            a {@link Runnable} that refreshes the view when a tag is set.
	 */
	protected void registerTagSetMenu(
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		if ( dataModel instanceof UndoPointMarker )
		{
			final UndoPointMarker undo = ( UndoPointMarker ) dataModel;
			final SelectionModel< MV, ME > selectionModel = dataModel.getSelectionModel();
			final TagSetModel< MV, ME > tagSetModel = dataModel.getTagSetModel();
			final TagSetMenu< MV, ME > tagSetMenu = new TagSetMenu< MV, ME >( menuHandle.getMenu(), tagSetModel, selectionModel,
					lock, undo, refresh );
			tagSetModel.listeners().add( tagSetMenu );
			onClose( () -> tagSetModel.listeners().remove( tagSetMenu ) );
		}
	}
}
