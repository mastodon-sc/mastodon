package net.trackmate.io;

import org.jdom2.Element;

public class XmlHelpers
{
	// TODO: move to XmlHelpers
	public static double getDoubleAttribute( final Element parent, final String name )
	{
		return Double.parseDouble( parent.getAttributeValue( name ) );
	}

	// TODO: move to XmlHelpers
	public static double[] getDoubleArrayAttribute( final Element parent, final String name )
	{
		final String text = parent.getAttributeValue( name );
		final String[] entries = text.split( "\\s+" );
		final double[] array = new double[ entries.length ];
		for ( int i = 0; i < entries.length; ++i )
			array[ i ] = Double.parseDouble( entries[ i ] );
		return array;
	}

	// TODO: move to XmlHelpers
	public static int getIntAttribute( final Element parent, final String name )
	{
		return Integer.parseInt( parent.getAttributeValue( name ) );
	}
}
