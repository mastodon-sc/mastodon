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
import org.mastodon.revised.model.AbstractSpot;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

/**
 * A {@link MastodonView} that is displayed in a {@link ViewFrame} (instead of
 * just a panel, for instance).
 *
 * @param <M>
 * @param <VG>
 * @param <MV>
 *            model vertex type
 * @param <ME>
 *            model edge type
 * @param <V>
 *            view vertex type
 * @param <E>
 *            view edge type
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

		final Actions appActions = appModel.getAppActions();
		frame.keybindings.addActionMap( "app", new WrappedActionMap( appActions.getActionMap() ) );
		frame.keybindings.addInputMap( "app", new WrappedInputMap( appActions.getInputMap() ) );

		viewActions = new Actions( getKeyConfig(), getKeyConfigContexts() );
		viewActions.install( frame.keybindings, "view" );

		viewBehaviours = new Behaviours( getKeyConfig(), getKeyConfigContexts() );
		viewBehaviours.install( frame.triggerbindings, "view" );
	}

	M getAppModel()
	{
		return appModel;
	}

	InputTriggerConfig getKeyConfig()
	{
		return appModel.getKeyConfig();
	}

	String[] getKeyConfigContexts()
	{
		return keyConfigContexts;
	}
}
