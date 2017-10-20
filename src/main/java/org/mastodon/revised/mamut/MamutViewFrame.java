package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.util.InvokeOnEDT;
import net.imglib2.ui.util.GuiUtil;

public class MamutViewFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	protected final JPanel settingsPanel;

	private boolean isSettingsPanelVisible;

	protected final InputActionBindings keybindings;

	protected final TriggerBehaviourBindings triggerbindings;

	public MamutViewFrame( final String windowTitle )
	{
		super( windowTitle, GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL ) );
		getRootPane().setDoubleBuffered( true );

		keybindings = new InputActionBindings();
		triggerbindings = new TriggerBehaviourBindings();

		SwingUtilities.replaceUIActionMap( getRootPane(), keybindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keybindings.getConcatenatedInputMap() );

		settingsPanel = new JPanel();
		settingsPanel.setLayout( new BoxLayout( settingsPanel, BoxLayout.LINE_AXIS ) );
		add( settingsPanel, BorderLayout.NORTH );
		isSettingsPanelVisible = true;
	}

	public boolean isSettingsPanelVisible()
	{
		return isSettingsPanelVisible;
	}

	public void setSettingsPanelVisible( final boolean visible )
	{
		try
		{
			InvokeOnEDT.invokeAndWait( () -> setSettingsPanelVisibleSynchronized( visible ) );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	private synchronized void setSettingsPanelVisibleSynchronized( final boolean visible )
	{
		if ( isSettingsPanelVisible != visible )
		{
			isSettingsPanelVisible = visible;
			if ( visible )
				add( settingsPanel, BorderLayout.NORTH );
			else
				remove( settingsPanel );
			invalidate();
			pack();
		}
	}
}
