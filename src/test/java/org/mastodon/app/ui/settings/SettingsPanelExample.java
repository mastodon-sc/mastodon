package org.mastodon.app.ui.settings;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.VisualEditorPanel;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

public class SettingsPanelExample
{
	static class DummySettingsPage implements SettingsPage
	{
		private final String treePath;

		private final JPanel panel;

		private final Listeners.List< ModificationListener > modificationListeners;

		public DummySettingsPage( String treePath )
		{
			this.treePath = treePath;
			panel = new JPanel( new BorderLayout() );
			modificationListeners = new Listeners.SynchronizedList<>();

			final JButton button = new JButton( treePath );
			button.setEnabled( false );
			panel.add( button, BorderLayout.CENTER );
		}

		@Override
		public String getTreePath()
		{
			return treePath;
		}

		@Override
		public JPanel getJPanel()
		{
			return panel;
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public void cancel()
		{
		}

		@Override
		public void apply()
		{
		}
	}

	static class DefaultSettingsPage implements SettingsPage
	{
		private final String treePath;

		private final JPanel panel;

		private final Listeners.List< ModificationListener > modificationListeners;

		public DefaultSettingsPage( String treePath, JPanel panel )
		{
			this.treePath = treePath;
			this.panel = panel;
			modificationListeners = new Listeners.SynchronizedList<>();
		}

		@Override
		public String getTreePath()
		{
			return treePath;
		}

		@Override
		public JPanel getJPanel()
		{
			return panel;
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public void cancel()
		{
			System.out.println( "DefaultSettingsPage.cancel" );
		}

		@Override
		public void apply()
		{
			System.out.println( "DefaultSettingsPage.apply" );
		}
	}

	public static void main( final String[] arg )
	{
		SettingsPanel settings = new SettingsPanel();
		final VisualEditorPanel keyconfEditor = new VisualEditorPanel( getDemoConfig(), getDemoActions(), getDemoContexts() );
		keyconfEditor.setButtonPanelVisible( false );
		settings.addPage( new DefaultSettingsPage( "keymap", keyconfEditor ) );
		settings.addPage( new DummySettingsPage( "other" ) );
		settings.addPage( new DummySettingsPage( "views > bdv" ) );
		settings.addPage( new DummySettingsPage( "views > trackscheme" ) );

		JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
		dialog.getContentPane().add( settings, BorderLayout.CENTER );
		dialog.pack();
		dialog.setVisible( true );
	}

	private static InputTriggerConfig getDemoConfig()
	{
		final StringReader reader = new StringReader( "---\n" +
				"- !mapping" + "\n" +
				"  action: fluke" + "\n" +
				"  contexts: [all]" + "\n" +
				"  triggers: [F]" + "\n" +
				"- !mapping" + "\n" +
				"  action: drag1" + "\n" +
				"  contexts: [all]" + "\n" +
				"  triggers: [button1, win G]" + "\n" +
				"- !mapping" + "\n" +
				"  action: scroll1" + "\n" +
				"  contexts: [all]" + "\n" +
				"  triggers: [scroll]" + "\n" +
				"- !mapping" + "\n" +
				"  action: scroll1" + "\n" +
				"  contexts: [trackscheme, mamut]" + "\n" +
				"  triggers: [shift D]" + "\n" +
				"- !mapping" + "\n" +
				"  action: destroy the world" + "\n" +
				"  contexts: [unknown context, mamut]" + "\n" +
				"  triggers: [control A]" + "\n" +
				"" );
		final List< InputTriggerDescription > triggers = YamlConfigIO.read( reader );
		final InputTriggerConfig config = new InputTriggerConfig( triggers );
		return config;
	}

	private static Map< String, String > getDemoActions()
	{
		final Map< String, String > actions = new HashMap<>();
		actions.put( "drag1", "Move an item around the editor." );
		actions.put( "scroll1", null );
		actions.put( "destroy the world", "Make a disgusting coffee for breakfast. \n"
				+ "For this one, you are by yourself. Good luck and know that we are with you. This is a long line. Hopefully long engouh.\n"
				+ "Hey, what about we add:\n"
				+ "tabulation1\ttabulation2\n"
				+ "lalallala\ttrollololo." );
		actions.put( "ride the dragon", "Go to work by bike." );
		actions.put( "make some coffee", null );
		return actions;
	}

	private static Set< String > getDemoContexts()
	{
		final Set< String > contexts = new HashSet<>();
		contexts.add( "all" );
		contexts.add( "mamut" );
		contexts.add( "trackscheme" );
		return contexts;
	}
}
