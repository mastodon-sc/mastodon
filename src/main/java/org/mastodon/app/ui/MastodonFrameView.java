/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.app.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mastodon.app.MastodonAppModel;
import org.mastodon.app.MastodonView;
import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.model.AbstractSpot;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.keymap.Keymap.UpdateListener;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

/**
 * A {@link MastodonView} that is displayed in a {@link ViewFrame} (instead of
 * just a panel, for instance).
 *
 * @param <M>
 *            the type of the mastodon-app model.
 * @param <VG>
 *            the type of the view-graph displayed in this view.
 * @param <MV>
 *            model vertex type.
 * @param <ME>
 *            model edge type.
 * @param <V>
 *            view vertex type.
 * @param <E>
 *            view edge type.
 *
 * @author Tobias Pietzsch
 */
public class MastodonFrameView<
		M extends MastodonAppModel< ?, MV, ME >,
		VG extends ViewGraph< MV, ME, V, E >,
		MV extends AbstractSpot< MV, ME, ?, ?, ? >,
		ME extends AbstractListenableEdge< ME, MV, ?, ? >,
		V extends Vertex< E >,
		E extends Edge< V > >
	extends MastodonView< M, VG, MV, ME, V, E >
{
	protected ViewFrame frame;

	protected final String[] keyConfigContexts;

	protected Actions viewActions;

	protected Behaviours viewBehaviours;

	public MastodonFrameView(
			final M appModel,
			final VG viewGraph,
			final String[] keyConfigContexts )
	{
		super( appModel, viewGraph );

		final Set< String > c = new LinkedHashSet<>( Arrays.asList( appModel.getKeyConfigContexts() ) );
		c.addAll( Arrays.asList( keyConfigContexts ) );
		this.keyConfigContexts = c.toArray( new String[] {} );
	}

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

		final Actions globalActions = appModel.getGlobalActions();
		if ( globalActions != null )
		{
			frame.keybindings.addActionMap( "global", new WrappedActionMap( globalActions.getActionMap() ) );
			frame.keybindings.addInputMap( "global", new WrappedInputMap( globalActions.getInputMap() ) );
		}

		final Actions pluginActions = appModel.getPlugins().getPluginActions();
		if ( pluginActions != null )
		{
			frame.keybindings.addActionMap( "plugin", new WrappedActionMap( pluginActions.getActionMap() ) );
			frame.keybindings.addInputMap( "plugin", new WrappedInputMap( pluginActions.getInputMap() ) );
		}

		final Actions appActions = appModel.getAppActions();
		frame.keybindings.addActionMap( "app", new WrappedActionMap( appActions.getActionMap() ) );
		frame.keybindings.addInputMap( "app", new WrappedInputMap( appActions.getInputMap() ) );

		final Keymap keymap = appModel.getKeymap();

		viewActions = new Actions( keymap.getConfig(), getKeyConfigContexts() );
		viewActions.install( frame.keybindings, "view" );

		viewBehaviours = new Behaviours( keymap.getConfig(), getKeyConfigContexts() );
		viewBehaviours.install( frame.triggerbindings, "view" );

		final UpdateListener updateListener = () -> {
			viewBehaviours.updateKeyConfig( keymap.getConfig() );
			viewActions.updateKeyConfig( keymap.getConfig() );
		};
		keymap.updateListeners().add( updateListener );
		onClose( () -> keymap.updateListeners().remove( updateListener ) );
	}

	Keymap getKeymap()
	{
		return appModel.getKeymap();
	}

	M getAppModel()
	{
		return appModel;
	}

	String[] getKeyConfigContexts()
	{
		return keyConfigContexts;
	}
}
