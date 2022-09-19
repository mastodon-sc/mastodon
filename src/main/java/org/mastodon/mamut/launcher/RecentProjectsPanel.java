package org.mastodon.mamut.launcher;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.mastodon.app.MastodonIcons;
import org.mastodon.ui.util.RecentProjects;

public class RecentProjectsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	static final RecentProjects recentProjects = new RecentProjects();

	public RecentProjectsPanel( final Consumer< String > projectOpener )
	{
		remakeGUI( projectOpener );
	}

	private void remakeGUI( final Consumer< String > projectOpener )
	{
		removeAll();

		/*
		 * Row heights and weights.
		 */

		final int[] rowHeights = new int[ 1 // title.
				+ ( recentProjects.isempty() ? 1 : recentProjects.size() )
				+ 1 // separator
				+ 1 // open other
				+ 1 ]; // fill blank

		rowHeights[ 0 ] = 35;
		for ( int i = 0; i < Math.max( 1, recentProjects.size() ); i++ )
			rowHeights[ 0 ] = 60;

		rowHeights[ rowHeights.length - 3 ] = 65;
		rowHeights[ rowHeights.length - 2 ] = 35;
		rowHeights[ rowHeights.length - 1 ] = 10;

		final double[] rowWeights = new double[ rowHeights.length ];
		Arrays.fill( rowWeights, 0. );
		rowWeights[ rowWeights.length - 1 ] = 1.;

		/*
		 * GUI
		 */

		final GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 1., 0., 0. };
		gbl.rowHeights = rowHeights;
		gbl.rowWeights = rowWeights;
		setLayout( gbl );

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets( 5, 5, 5, 5 );
		gbc.fill = GridBagConstraints.HORIZONTAL;

		final JLabel lblTitle = new JLabel( "Recent projects" );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getStyle() | Font.BOLD ) );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		add( lblTitle, gbc );

		/*
		 * List of recent projects.
		 */

		if ( recentProjects != null && !recentProjects.isempty() )
		{
			gbc.gridy++;
			final JLabel lblTitleHint = new JLabel( "Double-click to open the containing folder." );
			lblTitleHint.setFont( lblTitleHint.getFont().deriveFont( lblTitleHint.getFont().getStyle() | Font.ITALIC ) );
			lblTitleHint.setHorizontalAlignment( SwingConstants.CENTER );
			add( lblTitleHint, gbc );

			for ( final String projectPath : recentProjects )
			{
				gbc.gridy++;
				gbc.gridx = 0;
				final JTextArea ta = new JTextArea( projectPath );
				ta.setEditable( false );
				ta.setLineWrap( true );
				ta.addMouseListener( new MouseDblClickOpenPath( projectPath ) );
				add( ta, gbc );

				gbc.gridx = 1;
				final JButton btnOpen = new JButton( MastodonIcons.LOAD_ICON_SMALL );
				btnOpen.addActionListener( l -> {
					projectOpener.accept( projectPath );
					// Recent projects will be updated in the launcher method.
				} );
				add( btnOpen, gbc );

				gbc.gridx = 2;
				final JButton btnClear = new JButton( MastodonIcons.REMOVE_ICON );
				btnClear.addActionListener( l -> {
					recentProjects.remove( projectPath );
					remakeGUI( projectOpener );
				} );
				add( btnClear, gbc );
			}
		}
		else
		{
			final JLabel lblNo = new JLabel( "No recent projects." );
			lblNo.setFont( getFont().deriveFont( getFont().getStyle() | Font.ITALIC ) );
			lblNo.setHorizontalAlignment( SwingConstants.CENTER );

			gbc.gridy++;
			gbc.gridwidth = 3;
			add( lblNo, gbc );
			gbc.gridwidth = 1;
		}

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		add( new JSeparator(), gbc );

		gbc.gridy++;
		gbc.gridwidth = 1;
		final JLabel lblTitle2 = new JLabel( "Open another project" );
		lblTitle2.setHorizontalAlignment( SwingConstants.CENTER );
		lblTitle2.setFont( lblTitle2.getFont().deriveFont( lblTitle2.getFont().getStyle() | Font.BOLD ) );
		add( lblTitle2, gbc );

		gbc.gridx = 1;
		final JButton btnBrowse = new JButton( MastodonIcons.LOAD_ICON_SMALL );
		btnBrowse.addActionListener( l -> projectOpener.accept( null ) );
		add( btnBrowse, gbc );

		revalidate();
		repaint();
	}


	public static final class MouseDblClickOpenPath implements MouseListener {
		final String Uri;
		public MouseDblClickOpenPath(final String pathOnDblClick) {
			Uri = Paths.get(pathOnDblClick).getParent().toUri().toString();
		}

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			if (mouseEvent.getClickCount() == 2) openUrl( Uri );
		}

		@Override
		public void mousePressed(MouseEvent mouseEvent) { /* empty */ }
		@Override
		public void mouseReleased(MouseEvent mouseEvent) { /* empty */ }
		@Override
		public void mouseEntered(MouseEvent mouseEvent) { /* empty */ }
		@Override
		public void mouseExited(MouseEvent mouseEvent) { /* empty */ }
	}

	public static void openUrl(final String url) {
		final String myOS = System.getProperty("os.name").toLowerCase();
		try {
			if (myOS.contains("mac")) {
				Runtime.getRuntime().exec("open "+url);
			}
			else if (myOS.contains("nux") || myOS.contains("nix")) {
				Runtime.getRuntime().exec("xdg-open "+url);
			}
			else if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			}
			else {
				System.out.println("Please, open this URL yourself: "+url);
			}
		} catch (IOException | URISyntaxException ignored) {}
	}
}
