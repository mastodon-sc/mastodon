/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.launcher;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.mastodon.app.MastodonIcons;
import org.mastodon.ui.util.RecentProjects;

public class RecentProjectsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	static final RecentProjects recentProjects = new RecentProjects();

	public RecentProjectsPanel( final Consumer< String > projectOpener )
	{
		setLayout( new MigLayout( "fill", "[grow]", "[]10[grow]10[]10[]" ) );
		remakeGUI( projectOpener );
	}

	private void remakeGUI( final Consumer< String > projectOpener )
	{
		removeAll();

		final JLabel lblTitle = new JLabel( "Recent projects" );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getStyle() | Font.BOLD ) );
		add( lblTitle, "align center, wrap" );

		final JPanel listPanel;
		if ( !recentProjects.isempty() )
		{
			listPanel = new JPanel( new MigLayout( "fillx", "[grow]", "[]15[]" ) );
			final JLabel lblTitleHint = new JLabel( "Double-click to open the containing folder." );
			lblTitleHint.setFont( lblTitleHint.getFont().deriveFont( lblTitleHint.getFont().getStyle() | Font.ITALIC ) );
			listPanel.add( lblTitleHint, "align center, wrap" );

			// list of recent projects
			for ( final String projectPath : recentProjects )
			{
				ListItem listItem = new ListItem( projectPath, projectOpener );
				listPanel.add( listItem, "grow, wrap" );
			}
		}
		else
		{
			// No recent projects.
			listPanel = new JPanel( new GridBagLayout() );
			final JLabel lblNo = new JLabel( "No recent projects." );
			lblNo.setFont( getFont().deriveFont( getFont().getStyle() | Font.ITALIC ) );
			listPanel.add( lblNo, new GridBagConstraints() );
		}

		final JScrollPane scrollPane = new JScrollPane( listPanel );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		add( scrollPane, "grow, wrap" );

		add( new JSeparator(), "span" );
		add( getBottomPanel( projectOpener ), "right" );

		revalidate();
		repaint();
	}

	private class ListItem extends JPanel
	{
		private ListItem( final String path, final Consumer< String > projectOpener )
		{
			super( new MigLayout( "fill, ins 0, gapy 0", "[fill][][]", "" ) );

			final JTextArea ta = new JTextArea( path );
			ta.setEditable( true );
			ta.setLineWrap( true );
			ta.addMouseListener( new MouseDblClickOpenPath( ta.getText() ) );
			add( ta, "pushx, w 0:5" );

			final JButton btnOpen = new JButton( MastodonIcons.LOAD_ICON_SMALL );
			btnOpen.addActionListener( l -> projectOpener.accept( ta.getText() ) ); // Recent projects will be updated in the launcher method.
			add( btnOpen, "" );

			final JButton btnClear = new JButton( MastodonIcons.BIN_ICON );
			btnClear.addActionListener( l -> {
				recentProjects.remove( path );
				remakeGUI( projectOpener );
			} );
			add( btnClear, "wrap" );
		}
	}

	private static JPanel getBottomPanel( final Consumer< String > projectOpener )
	{
		JPanel bottomPanel = new JPanel( new MigLayout( "", "[][]", "" ) );

		final JLabel lblTitle2 = new JLabel( "Open another project" );
		lblTitle2.setHorizontalAlignment( SwingConstants.CENTER );
		lblTitle2.setFont( lblTitle2.getFont().deriveFont( lblTitle2.getFont().getStyle() | Font.BOLD ) );
		bottomPanel.add( lblTitle2, "" );

		final JButton btnBrowse = new JButton( MastodonIcons.LOAD_ICON_SMALL );
		btnBrowse.addActionListener( l -> projectOpener.accept( null ) );
		bottomPanel.add( btnBrowse, "" );
		return bottomPanel;
	}

	public static final class MouseDblClickOpenPath implements MouseListener
	{
		final String Uri;

		public MouseDblClickOpenPath( final String pathOnDblClick )
		{
			Uri = Paths.get( pathOnDblClick ).getParent().toUri().toString();
		}

		@Override
		public void mouseClicked( final MouseEvent mouseEvent )
		{
			if ( mouseEvent.getClickCount() == 2 )
				openUrl( Uri );
		}

		@Override
		public void mousePressed( final MouseEvent mouseEvent )
		{ /* empty */ }

		@Override
		public void mouseReleased( final MouseEvent mouseEvent )
		{ /* empty */ }

		@Override
		public void mouseEntered( final MouseEvent mouseEvent )
		{ /* empty */ }

		@Override
		public void mouseExited( final MouseEvent mouseEvent )
		{ /* empty */ }
	}

	public static void openUrl( final String url )
	{
		final String myOS = System.getProperty( "os.name" ).toLowerCase();
		try
		{
			if ( myOS.contains( "mac" ) )
			{
				Runtime.getRuntime().exec( "open " + url );
			}
			else if ( myOS.contains( "nux" ) || myOS.contains( "nix" ) )
			{
				Runtime.getRuntime().exec( "xdg-open " + url );
			}
			else if ( Desktop.isDesktopSupported() )
			{
				Desktop.getDesktop().browse( new URI( url ) );
			}
			else
			{
				System.out.println( "Please, open this URL yourself: " + url );
			}
		}
		catch ( IOException | URISyntaxException ignored )
		{}
	}
}
