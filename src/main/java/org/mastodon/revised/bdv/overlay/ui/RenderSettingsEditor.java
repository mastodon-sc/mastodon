package org.mastodon.revised.bdv.overlay.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.mastodon.revised.bdv.overlay.RenderSettings;

public class RenderSettingsEditor extends JDialog
{

	public static void main( final String[] args )
	{
		new RenderSettingsEditor( null, RenderSettings.defaultStyle() ).setVisible( true );
	}

	private static final long serialVersionUID = 1L;

	private final RenderSettings renderSettings;

	private final RenderSettingsPanel renderSettingsPanel;

	public RenderSettingsEditor( final JDialog owner, final RenderSettings renderSettings )
	{
		super( owner, "render settings", false );
		this.renderSettings = renderSettings;

		renderSettingsPanel = new RenderSettingsPanel( renderSettings );

		final JPanel content = new JPanel();
		content.setLayout( new BoxLayout( content, BoxLayout.PAGE_AXIS ) );
		content.add( renderSettingsPanel );
		getContentPane().add( content, BorderLayout.NORTH );

		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				setVisible( false );
			}
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
	}
}
