package org.mastodon.app;

import java.awt.Image;
import java.util.Locale;

import javax.swing.ImageIcon;

/**
 * Collection of static fields pointing to the icons to use in the Mastodon-app.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MastodonIcons
{

	public static final ImageIcon BVV_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-volume-16x16.png" ) );

	public static final ImageIcon BDV_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-slicing-16x16.png" ) );

	public static final ImageIcon FEATURES_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-compute-values-16x16.png" ) );

	public static final ImageIcon TRACKSCHEME_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-lineage-view-16x16.png" ) );

	public static final ImageIcon LOAD_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-project-16x16.png" ) );

	public static final ImageIcon SAVE_AS_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-save-as-16x16.png" ) );

	public static final ImageIcon SAVE_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-save-16x16.png" ) );

	public static final ImageIcon TABLE_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-table-16x16.png" ) );

	public static final ImageIcon TAGS_ICON_SMALL = new ImageIcon( MastodonIcons.class.getResource( "icon-tag-16x16.png" ) );

	public static final ImageIcon BVV_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-volume-512x512.png" ) );

	public static final ImageIcon BDV_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-slicing-512x512.png" ) );

	public static final ImageIcon FEATURES_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-compute-values-512x512.png" ) );

	public static final ImageIcon TRACKSCHEME_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-lineage-view-512x512.png" ) );

	public static final ImageIcon LOAD_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-project-512x512.png" ) );

	public static final ImageIcon SAVE_AS_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-save-as-512x512.png" ) );

	public static final ImageIcon SAVE_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-save-512x512.png" ) );

	public static final ImageIcon TABLE_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-table-512x512.png" ) );

	public static final ImageIcon TAGS_ICON_LARGE = new ImageIcon( MastodonIcons.class.getResource( "icon-tag-512x512.png" ) );

	public static final Image BDV_VIEW_ICON = isMac() ? null : BDV_ICON_LARGE.getImage();

	public static final Image TRACKSCHEME_VIEW_ICON = isMac() ? null : BDV_ICON_LARGE.getImage();

	public static final Image TABLE_VIEW_ICON = isMac() ? null : BDV_ICON_LARGE.getImage();

	private static final boolean isMac()
	{
		final String OS = System.getProperty( "os.name", "generic" ).toLowerCase( Locale.ENGLISH );
		return ( OS.indexOf( "mac" ) >= 0 ) || ( OS.indexOf( "darwin" ) >= 0 );
	}

}
