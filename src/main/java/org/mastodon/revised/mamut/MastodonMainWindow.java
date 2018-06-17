package org.mastodon.revised.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.windowMenu;
import static org.mastodon.revised.mamut.MastodonIcons.TITLE_ICON;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.mastodon.app.ui.ViewMenu;
import org.mastodon.revised.ui.keymap.Keymap;

public class MastodonMainWindow extends JFrame
{

	private static final long serialVersionUID = 1L;

	public MastodonMainWindow( final WindowManager windowManager )
	{
		setTitle( "Mastodon-app" );
		setIconImage( MastodonIcons.TITLE_ICON.getImage() );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		final JPanel contentPane = new JPanel();
		contentPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		contentPane.setLayout( new BorderLayout( 10, 10 ) );
		setContentPane( contentPane );

		final JLabel lblTitle = new JLabel( "Mastodon-app" );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getStyle() | Font.BOLD, lblTitle.getFont().getSize() + 2f ) );
		lblTitle.setIcon( TITLE_ICON );
		contentPane.add( lblTitle, BorderLayout.NORTH );

		final MainButtonPanel mainButtonPanel = new MainButtonPanel( windowManager );
		contentPane.add( mainButtonPanel, BorderLayout.WEST );

		final DefaultMastodonLogger mastodonLogger = windowManager.getContext().getService( DefaultMastodonLogger.class );
		contentPane.add( mastodonLogger.getMastodonLogPanel(), BorderLayout.CENTER );

		final JMenuBar menubar = new JMenuBar();
		setJMenuBar( menubar );

		final Keymap keymap = windowManager.getKeymapManager().getForwardDefaultKeymap();
		final ViewMenu menu = new ViewMenu( menubar, keymap, KeyConfigContexts.MASTODON );
		keymap.updateListeners().add( menu::updateKeymap );
		final ActionMap actionMap = windowManager.getGlobalAppActions().getActionMap();
		addMenus( menu, actionMap );
		windowManager.getPlugins().addMenus( menu );

		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				if ( windowManager != null )
					windowManager.closeAllWindows();
			}
		} );

		pack();
		setLocationByPlatform( true );
	}

	public static void addMenus( final ViewMenu menu, final ActionMap actionMap )
	{
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						item( ProjectManager.CREATE_PROJECT ),
						item( ProjectManager.LOAD_PROJECT ),
						item( ProjectManager.SAVE_PROJECT ),
						separator(),
						item( ProjectManager.IMPORT_TGMM ),
						item( ProjectManager.IMPORT_SIMI ),
						item( ProjectManager.IMPORT_MAMUT ),
						item( ProjectManager.EXPORT_MAMUT ),
						separator(),
						item( WindowManager.PREFERENCES_DIALOG ) ),
				windowMenu(
						item( WindowManager.NEW_BDV_VIEW ),
						item( WindowManager.NEW_TRACKSCHEME_VIEW ) ) );
	}
}
