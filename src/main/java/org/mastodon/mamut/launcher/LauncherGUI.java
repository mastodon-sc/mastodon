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
package org.mastodon.mamut.launcher;

import static org.mastodon.app.MastodonIcons.HELP_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.LOAD_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.MAINWINDOW_BG;
import static org.mastodon.app.MastodonIcons.MAMUT_IMPORT_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.NEW_FROM_URL_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.NEW_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.TGMM_IMPORT_ICON_MEDIUM;
import static org.mastodon.mamut.WindowManager.DOCUMENTATION_URL;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

class LauncherGUI extends JPanel
{

	private static final long serialVersionUID = 1L;

	static final String WELCOME_PANEL_KEY = "Welcome";

	static final String NEW_MASTODON_PROJECT_KEY = "NewMastodonProject";

	static final String NEW_FROM_URL_KEY = "NewFromURL";

	static final String LOGGER_KEY = "Logger";

	static final String RECENT_PROJECTS_KEY = "MastodonRecentProjects";

	static final String IMPORT_TGMM_KEY = "ImportTGMM";

	static final String IMPORT_SIMI_KEY = "ImportSimi";

	final JButton btnNew;

	final JButton btnLoad;

	final JButton btnOpenURL;

	final JButton btnImportTgmm;

	final JButton btnImportMamut;

	final JButton btnImportSimi;

	private final JPanel centralPanel;

	final JPanel sidePanel;

	final NewMastodonProjectPanel newMastodonProjectPanel;

	final NewFromUrlPanel newFromUrlPanel;

	final LoggerPanel logger;

	final ImportTGMMPanel importTGMMPanel;

	final ImportSimiBioCellPanel importSimiBioCellPanel;

	final JButton btnHelp;

	private final RecentProjectsPanel recentProjectsPanel;

	public LauncherGUI( final Consumer< String > projectOpener )
	{
		setLayout( new BorderLayout( 5, 5 ) );

		sidePanel = new JPanel();
		sidePanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		add( sidePanel, BorderLayout.WEST );
		final GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[] { 53, 123 };
		gbl.rowHeights = new int[] { 44, 44, 26, 44, 16, 0, 20, 35, 0 };
		gbl.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		sidePanel.setLayout( gbl );

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets( 5, 5, 5, 5 );
		c.gridx = 0;
		c.gridy = 0;

		btnNew = new JButton( NEW_ICON_MEDIUM );
		sidePanel.add( btnNew, c );

		c.gridx = 1;
		c.gridy = 0;
		sidePanel.add( new JLabel( "new Mastodon project" ), c );

		c.gridx = 0;
		c.gridy++;
		btnOpenURL = new JButton( NEW_FROM_URL_ICON_MEDIUM );
		sidePanel.add( btnOpenURL, c );

		c.gridx = 1;
		sidePanel.add( new JLabel( "new from OME-NGFF..." ), c );

		c.gridx = 0;
		c.gridy++;
		btnLoad = new JButton( LOAD_ICON_MEDIUM );
		sidePanel.add( btnLoad, c );

		c.gridx = 1;
		sidePanel.add( new JLabel( "open Mastodon project" ), c );

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		sidePanel.add( new JSeparator(), c );

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		btnImportTgmm = new JButton( TGMM_IMPORT_ICON_MEDIUM );
		sidePanel.add( btnImportTgmm, c );

		c.gridx = 1;
		sidePanel.add( new JLabel( "import TGMM" ), c );

		c.gridx = 0;
		c.gridy++;
		btnImportMamut = new JButton( MAMUT_IMPORT_ICON_MEDIUM );
		sidePanel.add( btnImportMamut, c );

		c.gridx = 1;
		sidePanel.add( new JLabel( "<html>import TrackMate<br>&amp; MaMuT</html>" ), c );

		c.gridx = 0;
		c.gridy++;
		btnImportSimi = new JButton( TGMM_IMPORT_ICON_MEDIUM );
		sidePanel.add( btnImportSimi, c );

		c.gridx = 1;
		sidePanel.add( new JLabel( "import Simi-BioCell" ), c );

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy++;
		sidePanel.add( new JSeparator(), c );

		c.gridwidth = 1;
		c.gridy++;
		btnHelp = new JButton( HELP_ICON_MEDIUM );
		sidePanel.add( btnHelp, c );

		centralPanel = new JPanel();
		centralPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		add( centralPanel, BorderLayout.CENTER );
		final CardLayout cardLayout = new CardLayout( 0, 0 );
		centralPanel.setLayout( cardLayout );

		final WelcomePanel welcomePanel = new WelcomePanel();
		centralPanel.add( welcomePanel, WELCOME_PANEL_KEY );

		newMastodonProjectPanel = new NewMastodonProjectPanel( "New Mastodon project", "create" );
		centralPanel.add( newMastodonProjectPanel, NEW_MASTODON_PROJECT_KEY );

		newFromUrlPanel = new NewFromUrlPanel( "New from OME-NGFF / Zarr / N5 / HDF5", "create" );
		centralPanel.add( newFromUrlPanel, NEW_FROM_URL_KEY );

		recentProjectsPanel = new RecentProjectsPanel( projectOpener );
		centralPanel.add( recentProjectsPanel, RECENT_PROJECTS_KEY );

		importTGMMPanel = new ImportTGMMPanel();
		centralPanel.add( importTGMMPanel, IMPORT_TGMM_KEY );

		logger = new LoggerPanel();
		centralPanel.add( logger, LOGGER_KEY );

		importSimiBioCellPanel = new ImportSimiBioCellPanel();
		centralPanel.add( importSimiBioCellPanel, IMPORT_SIMI_KEY );

		showPanel( WELCOME_PANEL_KEY );
	}

	void showPanel( final String key )
	{
		final CardLayout layout = ( CardLayout ) centralPanel.getLayout();
		layout.show( centralPanel, key );
	}

	private class LoggerPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JTextPane textPane;

		public LoggerPanel()
		{
			super( new BorderLayout() );
			final BorderLayout layout = new BorderLayout();
			this.setLayout( layout );
			this.setPreferredSize( new java.awt.Dimension( 270, 500 ) );

			final JScrollPane scrollPane = new JScrollPane();
			this.add( scrollPane );
			scrollPane.setPreferredSize( new java.awt.Dimension( 262, 136 ) );

			textPane = new JTextPane();
			textPane.setEditable( true );
			scrollPane.setViewportView( textPane );
			textPane.setBackground( this.getBackground() );

			final AbstractDocument doc = ( AbstractDocument ) textPane.getDocument();
			doc.setDocumentFilter( new WrapDocumentFilter() );

		}
	}

	private static class WrapDocumentFilter extends DocumentFilter
	{
		@Override
		public void insertString( final FilterBypass fb, final int offset, final String string, final AttributeSet attr ) throws BadLocationException
		{
			if ( string == null || string.isEmpty() )
			{ return; }
			final StringBuilder modifiedText = new StringBuilder();
			for ( final char c : string.toCharArray() )
			{
				if ( c == '/' || c == '\\' )
				{
					modifiedText.append( c ).append( ' ' );
				}
				else
				{
					modifiedText.append( c );
				}
			}
			super.insertString( fb, offset, modifiedText.toString(), attr );
		}

		@Override
		public void replace( final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs ) throws BadLocationException
		{
			if ( text == null || text.isEmpty() )
			{ return; }
			final StringBuilder modifiedText = new StringBuilder();
			for ( final char c : text.toCharArray() )
			{
				if ( c == '/' )
				{
					modifiedText.append( ' ' ).append( c ).append( ' ' );
				}
				else
				{
					modifiedText.append( c );
				}
			}
			super.replace( fb, offset, length, modifiedText.toString(), attrs );
		}
	}

	private class WelcomePanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private static final String DOCUMENTATION_STR = "Mastodon online documentation";

		private static final String DOCUMENTATION_LINK =
				"<html><a href='" + DOCUMENTATION_URL + "'>" + DOCUMENTATION_STR + "</html>";

		public WelcomePanel()
		{
			final GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 194, 0 };
			gridBagLayout.rowHeights = new int[] { 16, 0, 42, 42 };
			setLayout( gridBagLayout );

			final JLabel lblMastodon = new JLabel( "Mastodon" );
			lblMastodon.setFont( lblMastodon.getFont().deriveFont( lblMastodon.getFont().getSize() + 10f ) );
			final GridBagConstraints gbc_lblMastodon = new GridBagConstraints();
			gbc_lblMastodon.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblMastodon.fill = GridBagConstraints.VERTICAL;
			gbc_lblMastodon.gridx = 0;
			gbc_lblMastodon.gridy = 0;
			add( lblMastodon, gbc_lblMastodon );

			final JLabel lblV = new JLabel( "v" + MastodonLauncher.MASTODON_VERSION );
			final GridBagConstraints gbc_lblV = new GridBagConstraints();
			gbc_lblV.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblV.gridx = 0;
			gbc_lblV.gridy = 1;
			add( lblV, gbc_lblV );

			final JLabel lblTobiasPietzsch = new JLabel( "<html><p align=\"center\">"
					+ "Tobias Pietzsch & Jean-Yves Tinevez"
					+ "<br> "
					+ "Ko Sugawara & Matthias Arzt"
					+ "<br>"
					+ "Vladimír Ulman & Stefan Hahmann"
					+ "</html>" );
			final GridBagConstraints gbc_lblTobiasPietzsch = new GridBagConstraints();
			gbc_lblTobiasPietzsch.anchor = GridBagConstraints.SOUTH;
			gbc_lblTobiasPietzsch.gridx = 0;
			gbc_lblTobiasPietzsch.gridy = 2;
			add( lblTobiasPietzsch, gbc_lblTobiasPietzsch );

			final JLabel hyperlink = new JLabel( DOCUMENTATION_LINK );
			hyperlink.setForeground( Color.BLUE.darker() );
			hyperlink.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
			hyperlink.setToolTipText( DOCUMENTATION_URL );
			hyperlink.addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( final java.awt.event.MouseEvent e )
				{
					try
					{
						Desktop.getDesktop().browse( new URI( DOCUMENTATION_URL ) );
					}
					catch ( IOException | URISyntaxException e1 )
					{
						e1.printStackTrace();
					}
				}
			} );
			final GridBagConstraints gbcHyperlilnk = new GridBagConstraints();
			gbcHyperlilnk.anchor = GridBagConstraints.SOUTH;
			gbcHyperlilnk.gridx = 0;
			gbcHyperlilnk.gridy = 3;
			add( hyperlink, gbcHyperlilnk );

		}

		@Override
		public void paintComponent( final Graphics g )
		{
			super.paintComponent( g );
			final int x = getWidth() - MAINWINDOW_BG.getWidth( null );
			g.drawImage( MAINWINDOW_BG, x, 0, this );
		}
	}

	/*
	 * Logger methods.
	 */

	public static final Color NORMAL_COLOR = Color.BLACK;

	public static final Color ERROR_COLOR = new Color( 0.8f, 0, 0 );

	public static final Color GREEN_COLOR = new Color( 0, 0.6f, 0 );

	public static final Color BLUE_COLOR = new Color( 0, 0, 0.7f );

	private static final int MAX_N_CHARS = 10_000;

	public void error( final String message )
	{
		log( message, ERROR_COLOR );
	}

	public void log( final String string )
	{
		log( string, NORMAL_COLOR );
	}

	public void log( final String message, final Color color )
	{
		final StyleContext sc = StyleContext.getDefaultStyleContext();
		final AttributeSet aset = sc.addAttribute( SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color );
		final AbstractDocument doc = ( AbstractDocument ) logger.textPane.getStyledDocument();
		final int len = doc.getLength();
		final int l = message.length();

		logger.textPane.setEditable( true );
		if ( len + l > MAX_N_CHARS )
		{
			final int delta = Math.max( 0, Math.min( l - 1, len + l - MAX_N_CHARS ) );
			try
			{
				doc.remove( 0, delta );
			}
			catch ( final BadLocationException e )
			{
				e.printStackTrace();
			}
		}
		logger.textPane.setCaretPosition( doc.getLength() );
		logger.textPane.setCharacterAttributes( aset, false );
		logger.textPane.replaceSelection( message );
		logger.textPane.setEditable( false );
		showPanel( LOGGER_KEY );
	}

	public void setStatus( final String status )
	{
		log( status, GREEN_COLOR );
	}

	public void setLog( final String string )
	{
		logger.textPane.setEditable( true );
		logger.textPane.setText( string );
		logger.textPane.setEditable( false );
	}

	public void clearLog()
	{
		setLog( "" );
	}
}
