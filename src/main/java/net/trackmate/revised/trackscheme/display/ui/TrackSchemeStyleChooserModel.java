package net.trackmate.revised.trackscheme.display.ui;

import javax.swing.DefaultComboBoxModel;

import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;

public class TrackSchemeStyleChooserModel extends DefaultComboBoxModel< TrackSchemeStyle >
{
	private static final long serialVersionUID = 1L;

	public TrackSchemeStyleChooserModel()
	{
		init();
	}

	private void init()
	{
		for ( final TrackSchemeStyle style : TrackSchemeStyle.defaults )
			addElement( style );
	}
}
