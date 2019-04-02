package org.mastodon.revised.ui.coloring;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import org.mastodon.revised.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.revised.io.yaml.WorkaroundConstructor;
import org.mastodon.revised.io.yaml.WorkaroundRepresent;
import org.mastodon.revised.io.yaml.WorkaroundRepresenter;
import org.scijava.util.IntArray;
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

	private static final String X_KEY = "x";

	private static final String R_KEY = "r";

	private static final String G_KEY = "g";

	private static final String B_KEY = "b";

	private static final String A_KEY = "a";

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

		/*
		 * So we use color as ints internally, but we still want to save them as
		 * RGBA channels, for human readability.
		 */
		@Override
		public Node representData( final Object data )
		{
			final int c = ( int ) data;
			final float[] comps = new float[ 4 ];
			new Color( c, true ).getRGBComponents( comps );
			final List< Float > rgba = Arrays.asList( comps[ 0 ], comps[ 1 ], comps[ 2 ], comps[ 3 ] );
			return representSequence( getTag(), rgba, FLOW );
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

			final int[] colors = s.colors;
			final double[] alphas = s.alphas;
			final int naColor = s.notApplicableColor;
			final List< Map< String, Float > > c = new ArrayList<>( alphas.length );
			final float[] comps = new float[ 4 ];
			for ( int i = 0; i < alphas.length; i++ )
			{
				final Map< String, Float > m = new LinkedHashMap<>();
				m.put( X_KEY, ( float ) alphas[ i ] );
				new Color( colors[ i ], true ).getRGBComponents( comps );
				m.put( R_KEY, comps[ 0 ] );
				m.put( G_KEY, comps[ 1 ] );
				m.put( B_KEY, comps[ 2 ] );
				m.put( A_KEY, comps[ 3 ] );
				c.add( m );
			}
			new Color( naColor, true ).getRGBComponents( comps );
			final Map< String, Float > nac = new LinkedHashMap<>();
			nac.put( R_KEY, comps[ 0 ] );
			nac.put( G_KEY, comps[ 1 ] );
			nac.put( B_KEY, comps[ 2 ] );
			nac.put( A_KEY, comps[ 3 ] );

			mapping.put( "name", s.name );
			mapping.put( "notApplicableColor", nac );
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
				@SuppressWarnings( "unchecked" )
				final Map< String, Double > notApplicableColor = ( Map< String, Double > ) mapping.get( "notApplicableColor" );
				final float rna = notApplicableColor.get( R_KEY ).floatValue();
				final float gna = notApplicableColor.get( G_KEY ).floatValue();
				final float bna = notApplicableColor.get( B_KEY ).floatValue();
				final float ana = notApplicableColor.get( A_KEY ).floatValue();
				final int naColor = new Color( rna, gna, bna, ana ).getRGB();

				@SuppressWarnings( "unchecked" )
				final List< Map< String, Double > > cs = ( List< Map< String, Double > > ) mapping.get( "colors" );
				final int[] colors = new int[ cs.size() ];
				final double[] alphas = new double[ cs.size() ];
				for ( int i = 0; i < alphas.length; i++ )
				{
					final Map< String, Double > c = cs.get( i );
					final double x = c.get( X_KEY ).doubleValue();
					final float r = c.get( R_KEY ).floatValue();
					final float g = c.get( G_KEY ).floatValue();
					final float b = c.get( B_KEY ).floatValue();
					final float a = c.get( A_KEY ).floatValue();
					alphas[ i ] = x;
					colors[ i ] = new Color( r, g, b, a ).getRGB();
				}

				final ColorMap cm = new ColorMap( name, colors, alphas, naColor );
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
				return new Color( rgba.get( 0 ), rgba.get( 1 ), rgba.get( 2 ), rgba.get( 3 ) ).getRGB();
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

	static ColorMap importLUT( final String filename )
	{
		final Path path = Paths.get( filename );
		try (final Scanner scanner = new Scanner( path ))
		{
			String name = path.getFileName().toString();
			name = name.substring( 0, name.indexOf( '.' ) ).toLowerCase();

			final IntArray intColors = new IntArray();
			final IntArray intAlphas = new IntArray();
			final int naColor = Color.BLACK.getRGB();
			final AtomicInteger nLines = new AtomicInteger( 0 );

			while ( scanner.hasNext() )
			{
				if ( !scanner.hasNextInt() )
				{
					scanner.next();
					continue;
				}
				intAlphas.addValue( scanner.nextInt() );
				final int rgba = new Color( scanner.nextInt(), scanner.nextInt(), scanner.nextInt() ).getRGB();
				intColors.addValue( rgba );
				nLines.incrementAndGet();
			}

			if ( nLines.get() < 2 )
				return null;
			final double[] alphas = new double[ intAlphas.size() ];
			final int[] colors = new int[ alphas.length ];
			for ( int i = 0; i < alphas.length; i++ )
			{
				alphas[ i ] = ( double ) intAlphas.get( i ) / ( nLines.get() - 1 );
				colors[ i] = intColors.getValue( i );
			}
			return new ColorMap( name, colors, alphas, naColor );
		}
		catch ( final FileNotFoundException e )
		{
			e.printStackTrace();
			return null;
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}
}
