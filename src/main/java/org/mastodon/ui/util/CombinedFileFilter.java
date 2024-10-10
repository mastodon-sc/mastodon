package org.mastodon.ui.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class CombinedFileFilter extends FileFilter
{

    private FileFilter[] fileFilters;

    public CombinedFileFilter( FileFilter... fileFilters )
    {
        this.fileFilters = fileFilters;
    }

    @Override
    public boolean accept( File f )
    {
        for ( FileFilter fileFilter : fileFilters )
        {
            if ( fileFilter.accept( f ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder();
        for ( FileFilter fileFilter : fileFilters )
        {
            if ( sb.length() > 0 )
            {
                sb.append( " or " );
            }
            sb.append( fileFilter.getDescription() );
        }
        return sb.toString();
    }

}
