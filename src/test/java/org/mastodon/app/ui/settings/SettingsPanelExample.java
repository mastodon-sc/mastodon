/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app.ui.settings;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.StringReader;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.gui.VisualEditorPanel;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.ui.settings.ModificationListener;
import bdv.ui.settings.SettingsPage;
import bdv.ui.settings.SettingsPanel;

public class SettingsPanelExample
{
	static class DummySettingsPage implements SettingsPage
	{
		private final String treePath;

		private final JPanel panel;

		private final Listeners.List< ModificationListener > modificationListeners;

		public DummySettingsPage( final String treePath )
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
		{}

		@Override
		public void apply()
		{}
	}

	static class DefaultSettingsPage implements SettingsPage
	{
		private final String treePath;

		private final JPanel panel;

		private final Listeners.List< ModificationListener > modificationListeners;

		public DefaultSettingsPage( final String treePath, final JPanel panel )
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
		final SettingsPanel settings = new SettingsPanel();
		final VisualEditorPanel keyconfEditor = new VisualEditorPanel( getDemoConfig() );
		keyconfEditor.setButtonPanelVisible( false );
		settings.addPage( new DefaultSettingsPage( "keymap", keyconfEditor ) );
		settings.addPage( new DummySettingsPage( "other" ) );
		settings.addPage( new DummySettingsPage( "views > bdv" ) );
		settings.addPage( new DummySettingsPage( "views > trackscheme" ) );

		final JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
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
}
