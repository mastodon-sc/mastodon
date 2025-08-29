package org.mastodon.app.ui;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class UIUtils
{

	/**
	 * Possibly appends the project name to a given window title, making sure we
	 * do not append to an already appended project name.
	 *
	 * @param title
	 *            the initial window name.
	 * @param projectName
	 *            the project name.
	 * @return an adjusted window name.
	 */
	private static final String adjustTitle( final String title, final String projectName )
	{
		if ( projectName == null || projectName.isEmpty() )
			return title;

		final String separator = " - ";
		final int index = title.indexOf( separator );
		final String prefix = ( index < 0 ) ? title : title.substring( 0, index );
		return prefix + separator + projectName;
	}

	public static void adjustTitle( final JDialog dialog, final String projectName )
	{
		dialog.setTitle( adjustTitle( dialog.getTitle(), projectName ) );
	}

	public static void adjustTitle( final JFrame frame, final String projectName )
	{
		frame.setTitle( adjustTitle( frame.getTitle(), projectName ) );
	}

	public static void adjustTitle( final Window w, final String projectName )
	{
		if ( w instanceof JDialog )
			adjustTitle( ( JDialog ) w, projectName );
		else if ( w instanceof JFrame )
			adjustTitle( ( JFrame ) w, projectName );
	}

}
