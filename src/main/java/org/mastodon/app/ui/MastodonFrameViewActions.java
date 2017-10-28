package org.mastodon.app.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.mastodon.revised.util.HasSelectedState;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

public class MastodonFrameViewActions
{
	public static final String TOGGLE_SETTINGS_PANEL = "toggle settings panel";

	static final String[] TOGGLE_SETTINGS_PANEL_KEYS = new String[] { "T" };

	private final MastodonFrameView< ?, ?, ?, ?, ?, ? > view;

	private final ToggleSettingsPanelAction toggleSettingsPanelAction;

	/**
	 * Create Mastodon view actions and install them in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param view
	 *            Actions are targeted at this view.
	 */
	public static void installActionBindings(
			final Actions actions,
			final MastodonFrameView< ?, ?, ?, ?, ?, ? > view )
	{
		final MastodonFrameViewActions ba = new MastodonFrameViewActions( view );

		actions.namedAction( ba.toggleSettingsPanelAction, TOGGLE_SETTINGS_PANEL_KEYS );
	}

	private MastodonFrameViewActions( final MastodonFrameView< ?, ?, ?, ?, ?, ? > view )
	{
		this.view = view;
		toggleSettingsPanelAction = new ToggleSettingsPanelAction( TOGGLE_SETTINGS_PANEL );

		// TODO: add group (lock...) select actions
	}

	private class ToggleSettingsPanelAction extends AbstractNamedAction implements HasSelectedState
	{
		private static final long serialVersionUID = 1L;

		protected final Listeners.List< Listener > selectListeners;

		public ToggleSettingsPanelAction( final String name )
		{
			super( name );
			selectListeners = new Listeners.SynchronizedList<>();
			view.frame.settingsPanel.addComponentListener( new ComponentAdapter()
			{
				@Override
				public void componentShown( final ComponentEvent e )
				{
					selectListeners.list.forEach( l -> l.setSelected( true ) );
				}

				@Override
				public void componentHidden( final ComponentEvent e )
				{
					selectListeners.list.forEach( l -> l.setSelected( false ) );
				}
			} );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			view.frame.setSettingsPanelVisible( !view.frame.isSettingsPanelVisible() );
		}

		@Override
		public boolean isSelected()
		{
			return view.frame.isSettingsPanelVisible();
		}

		@Override
		public Listeners< Listener > selectListeners()
		{
			return selectListeners;
		}
	}
}
