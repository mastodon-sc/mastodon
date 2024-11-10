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
package org.mastodon.app;

import java.awt.Image;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

import org.mastodon.feature.ui.FeatureComputationPanel;
import org.mastodon.feature.ui.FeatureTable;
import org.mastodon.model.tag.ui.AbstractTagTable;

/**
 * Collection of static fields pointing to the icons to use in the Mastodon-app.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class MastodonIcons
{

	public static final ImageIcon MASTODON_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "mastodon-logo-512x512.png" ) );

	public static final ImageIcon MASTODON_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "mastodon-logo-32x32.png" ) );

	public static final ImageIcon MASTODON_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "mastodon-logo-16x16.png" ) );

	/*
	 * Small.
	 */

	public static final ImageIcon BVV_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-volume-16x16.png" ) );

	public static final ImageIcon BDV_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-slicing-16x16.png" ) );

	public static final ImageIcon FEATURES_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-compute-values-16x16.png" ) );

	public static final ImageIcon TRACKSCHEME_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-lineage-view-16x16.png" ) );

	public static final ImageIcon NEW_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-project-16x16.png" ) );

	public static final ImageIcon NEW_FROM_URL_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-remote-image-project-16x16.png" ) );

	public static final ImageIcon SAVE_AS_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-save-as-16x16.png" ) );

	public static final ImageIcon SAVE_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-save-16x16.png" ) );

	public static final ImageIcon LOAD_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-16x16.png" ) );

	public static final ImageIcon TABLE_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-table-16x16.png" ) );

	public static final ImageIcon TAGS_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-tag-16x16.png" ) );

	public static final ImageIcon MAMUT_IMPORT_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-mamut-import-16x16.png" ) );

	public static final ImageIcon MAMUT_EXPORT_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-mamut-export-16x16.png" ) );

	public static final ImageIcon TGMM_IMPORT_ICON_SMALL =
			new ImageIcon( MastodonIcons.class.getResource( "icon-tgmm-import-16x16.png" ) );

	/*
	 * Medium.
	 */

	public static final ImageIcon BVV_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-volume-32x32.png" ) );

	public static final ImageIcon BDV_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-slicing-32x32.png" ) );

	public static final ImageIcon FEATURES_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-compute-values-32x32.png" ) );

	public static final ImageIcon TRACKSCHEME_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-lineage-view-32x32.png" ) );

	public static final ImageIcon NEW_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-project-32x32.png" ) );

	public static final ImageIcon NEW_FROM_URL_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-remote-image-project-32x32.png" ) );

	public static final ImageIcon LOAD_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-32x32.png" ) );

	public static final ImageIcon SAVE_AS_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-save-as-32x32.png" ) );

	public static final ImageIcon SAVE_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-save-32x32.png" ) );

	public static final ImageIcon TABLE_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-table-32x32.png" ) );

	public static final ImageIcon TAGS_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-tag-32x32.png" ) );

	public static final ImageIcon MAMUT_IMPORT_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-mamut-import-32x32-2.png" ) );

	public static final ImageIcon MAMUT_EXPORT_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-mamut-export-32x32.png" ) );

	public static final ImageIcon TGMM_IMPORT_ICON_MEDIUM =
			new ImageIcon( MastodonIcons.class.getResource( "icon-tgmm-import-32x32.png" ) );

	public static final ImageIcon HELP_ICON_MEDIUM = new ImageIcon( MastodonIcons.class.getResource( "help.png" ) );

	/*
	 * Large.
	 */

	public static final ImageIcon BVV_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-volume-512x512.png" ) );

	public static final ImageIcon BDV_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-BDV-slicing-512x512.png" ) );

	public static final ImageIcon FEATURES_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-compute-values-512x512.png" ) );

	public static final ImageIcon TRACKSCHEME_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-lineage-view-512x512.png" ) );

	public static final ImageIcon NEW_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-project-512x512.png" ) );

	public static final ImageIcon NEW_FROM_URL_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-new-remote-image-project-512x512.png" ) );

	public static final ImageIcon LOAD_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-load-512x512.png" ) );

	public static final ImageIcon SAVE_AS_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-save-as-512x512.png" ) );

	public static final ImageIcon SAVE_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-save-512x512.png" ) );

	public static final ImageIcon TABLE_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-table-512x512.png" ) );

	public static final ImageIcon TAGS_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-tag-512x512.png" ) );

	public static final ImageIcon MAMUT_IMPORT_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-mamut-import-512x512.png" ) );

	public static final ImageIcon MAMUT_EXPORT_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-mamut-export-512x512.png" ) );

	public static final ImageIcon TGMM_IMPORT_ICON_LARGE =
			new ImageIcon( MastodonIcons.class.getResource( "icon-tgmm-import-512x512.png" ) );

	/*
	 * Images icons for JFrames and dialogs.
	 */

	public static final List< Image > BDV_VIEW_ICON = Arrays
			.asList( new Image[] { BDV_ICON_SMALL.getImage(), BDV_ICON_MEDIUM.getImage(), BDV_ICON_LARGE.getImage() } );

	public static final List< Image > BVV_VIEW_ICON = Arrays
			.asList( new Image[] { BVV_ICON_SMALL.getImage(), BVV_ICON_MEDIUM.getImage(), BVV_ICON_LARGE.getImage() } );

	public static final List< Image > TRACKSCHEME_VIEW_ICON =
			Arrays.asList( new Image[] { TRACKSCHEME_ICON_SMALL.getImage(), TRACKSCHEME_ICON_MEDIUM.getImage(),
					TRACKSCHEME_ICON_LARGE.getImage() } );

	public static final List< Image > TABLE_VIEW_ICON = Arrays.asList(
			new Image[] { TABLE_ICON_SMALL.getImage(), TABLE_ICON_MEDIUM.getImage(), TABLE_ICON_LARGE.getImage() } );

	public static final List< Image > MASTODON_ICON = Arrays.asList( new Image[] { MASTODON_ICON_SMALL.getImage(),
			MASTODON_ICON_MEDIUM.getImage(), MASTODON_ICON_LARGE.getImage() } );

	public static final List< Image > TAGS_ICON = Arrays.asList(
			new Image[] { TAGS_ICON_SMALL.getImage(), TAGS_ICON_MEDIUM.getImage(), TAGS_ICON_LARGE.getImage() } );

	public static final List< Image > FEATURES_ICON = Arrays.asList( new Image[] { FEATURES_ICON_SMALL.getImage(),
			FEATURES_ICON_MEDIUM.getImage(), FEATURES_ICON_LARGE.getImage() } );

	/*
	 * Background for the main window.
	 */

	public static final Image MAINWINDOW_BG =
			new ImageIcon( MastodonIcons.class.getResource( "MastodonMainWindowBG.png" ) ).getImage();

	/*
	 * General use icons.
	 */

	public static final ImageIcon GO_ICON =
			new ImageIcon( FeatureComputationPanel.class.getResource( "bullet_green.png" ) );

	public static final ImageIcon CANCEL_ICON =
			new ImageIcon( FeatureComputationPanel.class.getResource( "cancel.png" ) );

	public static final ImageIcon UP_TO_DATE_ICON =
			new ImageIcon( FeatureTable.class.getResource( "bullet_green.png" ) );

	public static final ImageIcon NOT_UP_TO_DATE_ICON = new ImageIcon( FeatureTable.class.getResource( "time.png" ) );

	public static final ImageIcon ADD_ICON = new ImageIcon( AbstractTagTable.class.getResource( "add.png" ) );

	public static final ImageIcon REMOVE_ICON = new ImageIcon( AbstractTagTable.class.getResource( "delete.png" ) );

}
