package org.mastodon.revised.mamut;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.mastodon.app.MastodonView;
import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

public class MamutView< VG extends ViewGraph< Spot, Link, V, E >, V extends Vertex< E >, E extends Edge< V > > extends MastodonView< MamutAppModel, VG, Spot, Link, V, E  >
{
	private MamutViewFrame frame;

	public MamutView( final MamutAppModel appModel, final VG viewGraph )
	{
		super( appModel, viewGraph );
	}

	protected final Listeners.List< MamutViewListener > listeners = new Listeners.SynchronizedList<>();

	public Listeners< MamutViewListener > listeners()
	{
		return listeners;
	}

	public MamutViewFrame getFrame()
	{
		return frame;
	}

	protected void setFrame( final MamutViewFrame frame )
	{
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				listeners.list.forEach( MamutViewListener::onClose );
			}
		} );
		this.frame = frame;

		final Actions appActions = appModel.getAppActions();
		frame.keybindings.addActionMap( "app", new WrappedActionMap( appActions.getActionMap() ) );
		frame.keybindings.addInputMap( "app", new WrappedInputMap( appActions.getInputMap() ) );
	}
}
