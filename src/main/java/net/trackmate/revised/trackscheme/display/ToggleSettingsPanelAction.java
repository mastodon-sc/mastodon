package net.trackmate.revised.trackscheme.display;

import java.awt.event.ActionEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.util.AbstractNamedAction;
import bdv.viewer.InputActionBindings;

// TODO: This should be constructed in WindowManager as part of larger package of actions to install
public class ToggleSettingsPanelAction extends AbstractNamedAction
{
	public static final String TOGGLE_SETTINGS_PANEL_NAME = "ts toggle settings panel";

	private final TrackSchemeFrame frame;

	public ToggleSettingsPanelAction( final TrackSchemeFrame frame )
	{
		super( TOGGLE_SETTINGS_PANEL_NAME );
		this.frame = frame;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		final boolean visible = !frame.isSettingsPanelVisible();
		frame.setSettingsPanelVisible( visible );
	}

	public void installActionBindings( final InputActionBindings keybindings, final KeyStrokeAdder.Factory keyConfig )
	{
		final ActionMap actionMap = new ActionMap();
		new NamedActionAdder( actionMap ).put( this );

		final InputMap inputMap = new InputMap();
		keyConfig.keyStrokeAdder( inputMap, "ts" ).put( TOGGLE_SETTINGS_PANEL_NAME, "T" );

		keybindings.addActionMap( "toggle settings", actionMap );
		keybindings.addInputMap( "toggle settings", inputMap );
	}
}
