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

import java.awt.Font;
import java.awt.Desktop;
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
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;

import org.mastodon.app.MastodonIcons;
import org.mastodon.ui.util.RecentProjects;

import net.miginfocom.swing.MigLayout;

public class RecentProjectsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	static final RecentProjects recentProjects = new RecentProjects();

	public RecentProjectsPanel( final Consumer< String > projectOpener )
	{
		setLayout( new MigLayout( "fill, wrap 3", "[grow, fill][shrink 0][shrink 0]", "[][grow, fill][]" ) );
		remakeGUI( projectOpener );
	}

	private void remakeGUI( final Consumer< String > projectOpener )
	{
		removeAll();

		final JLabel lblTitle = new JLabel( "Recent projects" );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getStyle() | Font.BOLD ) );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		add( lblTitle, "span, wrap" );

		final JPanel listPanel = new JPanel( new MigLayout( "fillx, wrap 3", "[fill][shrink 0][shrink 0]", "" ) );
		final JScrollPane scrollPane = new JScrollPane( listPanel );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		add( scrollPane, "span, wrap" );

		if ( !recentProjects.isempty() )
		{
			final JLabel lblTitleHint = new JLabel( "Double-click to open the containing folder." );
			lblTitleHint.setFont( lblTitleHint.getFont().deriveFont( lblTitleHint.getFont().getStyle() | Font.ITALIC ) );
			lblTitleHint.setHorizontalAlignment( SwingConstants.CENTER );
			listPanel.add( lblTitleHint, "span, wrap" );

			for ( final String projectPath : recentProjects )
			{
				final JTextArea ta = new JTextArea( projectPath );
				ta.setEditable( true );
				ta.setLineWrap( true );
				ta.addMouseListener( new MouseDblClickOpenPath( ta.getText() ) );
				listPanel.add( ta, "" );

				final JButton btnOpen = new JButton( MastodonIcons.LOAD_ICON_SMALL );
				btnOpen.addActionListener( l -> projectOpener.accept( ta.getText() ) ); // Recent projects will be updated in the launcher method.
				add( btnOpen );

				final JButton btnClear = new JButton( MastodonIcons.REMOVE_ICON );
				btnClear.addActionListener( l -> {
					recentProjects.remove( projectPath );
					remakeGUI( projectOpener );
				} );
				listPanel.add( btnClear, "wrap" );
			}
		}
		else
		{
			final JLabel lblNo = new JLabel( "No recent projects." );
			lblNo.setFont( getFont().deriveFont( getFont().getStyle() | Font.ITALIC ) );
			lblNo.setHorizontalAlignment( SwingConstants.CENTER );
			listPanel.add( lblNo, "span, wrap" );
		}

		add( new JSeparator(), "span, wrap" );

		final JLabel lblTitle2 = new JLabel( "Open another project" );
		lblTitle2.setHorizontalAlignment( SwingConstants.CENTER );
		lblTitle2.setFont( lblTitle2.getFont().deriveFont( lblTitle2.getFont().getStyle() | Font.BOLD ) );
		add( lblTitle2 );

		final JButton btnBrowse = new JButton( MastodonIcons.LOAD_ICON_SMALL );
		btnBrowse.addActionListener( l -> projectOpener.accept( null ) );
		add( btnBrowse );

		revalidate();
		repaint();
	}

	public static final class MouseDblClickOpenPath implements MouseListener
	{
		final String Uri;

		public MouseDblClickOpenPath( final String pathOnDblClick )
		{
			Uri = Paths.get( pathOnDblClick ).getParent().toUri().toString();
		}

		@Override
		public void mouseClicked( MouseEvent mouseEvent )
		{
			if ( mouseEvent.getClickCount() == 2 )
				openUrl( Uri );
		}

		@Override
		public void mousePressed( MouseEvent mouseEvent )
		{ /* empty */ }

		@Override
		public void mouseReleased( MouseEvent mouseEvent )
		{ /* empty */ }

		@Override
		public void mouseEntered( MouseEvent mouseEvent )
		{ /* empty */ }

		@Override
		public void mouseExited( MouseEvent mouseEvent )
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
