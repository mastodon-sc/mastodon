package org.mastodon.revised.ui.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.revised.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.revised.io.yaml.WorkaroundConstructor;
import org.mastodon.revised.io.yaml.WorkaroundRepresent;
import org.mastodon.revised.io.yaml.WorkaroundRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Serialization / Deserialization of {@link ColorMap}.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class ColorMapIO
{

	private static final Tag COLOR_TAG = new Tag( "!color" );

	private static final Tag COLORMAP_TAG = new Tag( "!colormap" );

	private static class ColorMapDataConstructor extends WorkaroundConstructor
	{
		public ColorMapDataConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructColor( this ) );
			putConstruct( new ConstructColorMapData( this ) );
		}
	}

	private static class ColorMapDataRepresenter extends WorkaroundRepresenter
	{
		public ColorMapDataRepresenter()
		{
			putRepresent( new RepresentColor( this ) );
			putRepresent( new RepresentColorMapData( this ) );
		}
	}

	private static class RepresentColor extends WorkaroundRepresent
	{
		public RepresentColor( final WorkaroundRepresenter r )
		{
			super( r, COLOR_TAG, Color.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final Color c = ( Color ) data;
			final float[] comps = new float[ 4 ];
			c.getRGBComponents( comps );
			final List< Float > rgba = Arrays.asList( comps[ 0 ], comps[ 1 ], comps[ 2 ], comps[ 3 ] );
			return representSequence( getTag(), rgba, Boolean.TRUE );
		}
	}

	private static class RepresentColorMapData extends WorkaroundRepresent
	{

		public RepresentColorMapData( final WorkaroundRepresenter r )
		{
			super( r, COLORMAP_TAG, ColorMap.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final ColorMap s = ( ColorMap ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			final Color[] colors = s.colors;
			final double[] alphas = s.alphas;
			final float[][] c = new float[ alphas.length ][ 5 ];
			final float[] comps = new float[ 4 ];
			for ( int i = 0; i < alphas.length; i++ )
			{
				c[ i ][ 0 ] = ( float ) alphas[ i ];
				colors[ i ].getRGBComponents( comps );
				c[ i ][ 1 ] = comps[ 0 ];
				c[ i ][ 2 ] = comps[ 1 ];
				c[ i ][ 3 ] = comps[ 2 ];
				c[ i ][ 4 ] = comps[ 3 ];
			}

			mapping.put( "name", s.name );
			mapping.put( "missingColor", s.missingColor );
			mapping.put( "notApplicableColor", s.notApplicableColor );
			mapping.put( "colors", c );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructColorMapData extends AbstractWorkaroundConstruct
	{
		public ConstructColorMapData( final WorkaroundConstructor c )
		{
			super( c, COLORMAP_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = ( String ) mapping.get( "name" );
				final Color missingColor = ( Color ) mapping.get( "missingColor" );
				final Color notApplicableColor = ( Color ) mapping.get( "notApplicableColor" );

				@SuppressWarnings( "unchecked" )
				final List< List< Double > > cs = ( List< List< Double > > ) mapping.get( "colors" );
				final Color[] colors = new Color[ cs.size() ];
				final double[] alphas = new double[ cs.size() ];
				for ( int i = 0; i < alphas.length; i++ )
				{
					final List< Double > c = cs.get( i );
					alphas[ i ] = c.get( 0 );
					colors[ i ] = new Color(
							c.get( 1 ).floatValue(),
							c.get( 2 ).floatValue(),
							c.get( 3 ).floatValue(),
							c.get( 4 ).floatValue() );
				}

				final ColorMap cm = new ColorMap( name, colors, alphas, missingColor, notApplicableColor );
				return cm;
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	private static class ConstructColor extends AbstractWorkaroundConstruct
	{
		public ConstructColor( final WorkaroundConstructor c )
		{
			super( c, COLOR_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				@SuppressWarnings( "unchecked" )
				final List< Float > rgba = ( List< Float > ) constructSequence( ( SequenceNode ) node );
				return new Color( rgba.get( 0 ), rgba.get( 1 ), rgba.get( 2 ), rgba.get( 3 ) );
			}
			catch ( final Exception e )
			{}
			return null;
		}
	}

	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new ColorMapDataRepresenter();
		final Constructor constructor = new ColorMapDataConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}
}