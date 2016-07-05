package net.trackmate.revised.trackscheme.display.ui;

import javax.swing.DefaultComboBoxModel;

import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;

public class TrackSchemeStyleChooserModel extends DefaultComboBoxModel< TrackSchemeStyle >
{

	public TrackSchemeStyleChooserModel()
	{
		init();
	}

	private void init()
	{
		addElement( TrackSchemeStyle.defaultStyle() );
		addElement( TrackSchemeStyle.modernStyle() );
		addElement( TrackSchemeStyle.howMuchDoYouKnowStyle() );
	}
}
