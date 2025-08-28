package org.mastodon.app.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mastodon.app.AppModel;
import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.model.MastodonModel;
import org.mastodon.spatial.HasTimepoint;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.Keymap.UpdateListener;

public class MastodonFrameView2<
			AM extends AppModel< ?, ?, MV, ME >,
			M extends MastodonModel< ?, MV, ME >,
			VG extends ViewGraph< MV, ME, V, E >,
			MV extends AbstractListenableVertex< MV, ME, ?, ? > & HasTimepoint,
			ME extends AbstractListenableEdge< ME, MV, ?, ? >,
			V extends Vertex< E >,
			E extends Edge< V > >
		extends MastodonView2< AM, M, VG, MV, ME, V, E >
		implements HasFrame
{

	protected ViewFrame frame;

	protected final String[] keyConfigContexts;

	protected Actions viewActions;

	protected Behaviours viewBehaviours;

	public MastodonFrameView2( final AM model, final VG viewGraph, final String[] keyConfigContexts )
	{
		super( model, viewGraph );

		final Set< String > c = new LinkedHashSet<>( Arrays.asList( model.uiModel().getKeyConfigContexts() ) );
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

		final Actions projectActions = model.uiModel().getProjectActions();
		if ( projectActions != null )
		{
			frame.keybindings.addActionMap( "project", new WrappedActionMap( projectActions.getActionMap() ) );
			frame.keybindings.addInputMap( "project", new WrappedInputMap( projectActions.getInputMap() ) );
		}

		final Actions pluginActions = model.uiModel().getPlugins().getPluginActions();
		if ( pluginActions != null )
		{
			frame.keybindings.addActionMap( "plugin", new WrappedActionMap( pluginActions.getActionMap() ) );
			frame.keybindings.addInputMap( "plugin", new WrappedInputMap( pluginActions.getInputMap() ) );
		}

		final Actions modelActions = model.uiModel().getModelActions();
		frame.keybindings.addActionMap( "model", new WrappedActionMap( modelActions.getActionMap() ) );
		frame.keybindings.addInputMap( "model", new WrappedInputMap( modelActions.getInputMap() ) );

		final Keymap keymap = model.uiModel().getKeymap();

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
}
