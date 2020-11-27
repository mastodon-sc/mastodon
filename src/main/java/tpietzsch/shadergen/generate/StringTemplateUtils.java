package tpietzsch.shadergen.generate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.stringtemplate.v4.ST;

public class StringTemplateUtils
{
	public static ST loadAndPatchSnippet(
			final Class< ? > resourceContext,
			final String resourceName,
			final List< String > keys )
			throws IOException
	{
		final ArrayList< String > searchList = new ArrayList<>();
		final ArrayList< String > replacementList = new ArrayList<>();
		for ( final String key : keys )
		{
			searchList.add( key );
			replacementList.add( "$" + key + "$" );
		}
		final String[] search = searchList.toArray( new String[ 0 ] );
		final String[] replace = replacementList.toArray( new String[ 0 ] );

		searchList.add( "}" );
		replacementList.add( "\\}" );
		final String[] searchInRepeat = searchList.toArray( new String[ 0 ] );
		final String[] replaceInRepeat = replacementList.toArray( new String[ 0 ] );

		final String REPEAT = "repeat";
		final String PAT_REPEAT = "$"+REPEAT+":{";
		final int REPEAT_START = 1;

		final String INSERT = "insert";
		final String PAT_INSERT = "$"+INSERT+"{";
		final int INSERT_START = PAT_INSERT.length();
		final char PAT_INSERT_END = '}';

		final InputStream stream = resourceContext.getResourceAsStream( resourceName );
		final BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
		final StringBuilder builder = new StringBuilder();
		String line;
		boolean inRepeatBlock = false;
		while ( ( line = reader.readLine() ) != null )
		{
			final int ii = line.indexOf( PAT_INSERT );
			if ( ii != -1 )
			{
				// length of leading whitespace
				int lws = 0;
				while ( lws < line.length() && line.charAt( lws ) <= ' ' )
					++lws;
				final int ie = line.indexOf( PAT_INSERT_END, ii );
				line = line.substring( 0, lws ) + line.substring( ii + INSERT_START, ie );
			}

			final int ri = line.indexOf( PAT_REPEAT );
			if ( ri != -1 )
			{
				if ( inRepeatBlock )
					throw new IllegalArgumentException();
				final int re = line.indexOf( "|", ri + PAT_REPEAT.length() );
				if ( re == -1 )
					throw new IllegalArgumentException();
				final String args = line.substring( ri + PAT_REPEAT.length(), re );
				line = line.substring( 0, ri + REPEAT_START )
						+ args
						+ line.substring( ri + REPEAT_START + REPEAT.length(), re + 1 )
						+ "\n";
				builder.append( line );
				inRepeatBlock = true;
			}
			else if ( inRepeatBlock )
			{
				if ( line.contains( "}$" ) )
					inRepeatBlock = false;
				else
					builder.append( StringUtils.replaceEach( line, searchInRepeat, replaceInRepeat ) );
			}

			if ( !inRepeatBlock )
				builder.append( StringUtils.replaceEach( line, search, replace ) );

			builder.append( "\n" );
		}
		final String snippet = builder.toString();

		return new ST( snippet, '$', '$' );
	}

	public static void clearAttributes( final ST st )
	{
		if ( st.getAttributes() != null )
			new ArrayList<>( st.getAttributes().keySet() ).forEach( st::remove );
	}
}
