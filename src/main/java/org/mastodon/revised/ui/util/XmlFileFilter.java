package org.mastodon.revised.ui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class XmlFileFilter extends FileFilter
{
	@Override
	public String getDescription()
	{
		return "xml files";
	}

	@Override
	public boolean accept( final File f )
	{
		if ( f.isDirectory() )
			return true;
		if ( f.isFile() )
		{
			final String s = f.getName();
			final int i = s.lastIndexOf( '.' );
			if ( i > 0 && i < s.length() - 1 )
			{
				final String ext = s.substring( i + 1 ).toLowerCase();
				return ext.equals( "xml" );
			}
		}
		return false;
	}
}
