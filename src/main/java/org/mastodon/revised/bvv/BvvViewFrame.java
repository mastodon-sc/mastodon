package org.mastodon.revised.bvv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.WindowConstants;
import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.grouping.GroupHandle;
import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.util.InputActionBindings;

import static com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation.ANONYMOUS.optional;

public class BvvViewFrame extends ViewFrame
{
	private final BvvPanel bvvPanel;

	public BvvViewFrame(
			final String windowTitle,
			final GroupHandle groupHandle,
			final BvvOptions optional )
	{
		super( windowTitle );

		bvvPanel = new BvvPanel( optional );
		add( bvvPanel, BorderLayout.CENTER );

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				bvvPanel.stop();
			}
		} );

		final Component display = bvvPanel.getDisplay();
		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		mouseAndKeyHandler.setKeypressManager( optional.values.getKeyPressedManager(), display );
		display.addKeyListener( mouseAndKeyHandler );
		display.addMouseListener( mouseAndKeyHandler );
		display.addMouseWheelListener( mouseAndKeyHandler );
		display.addMouseMotionListener( mouseAndKeyHandler );
		display.addFocusListener( mouseAndKeyHandler );
	}

	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	public BvvPanel getBvvPanel()
	{
		return bvvPanel;
	}
}
