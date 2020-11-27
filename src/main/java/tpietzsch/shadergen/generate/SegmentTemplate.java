package tpietzsch.shadergen.generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.stringtemplate.v4.ST;

import static tpietzsch.shadergen.generate.StringTemplateUtils.clearAttributes;

public class SegmentTemplate
{
	private final ST st;

	private final List< String > keys;

	private static final AtomicInteger idGen = new AtomicInteger();

	public SegmentTemplate(
			final String resourceName,
			final String ... keys )
	{
		this( tryGetContext(), resourceName, Arrays.asList( keys ) );
	}

	public SegmentTemplate(
			final String resourceName,
			final List< String > keys )
	{
		this( tryGetContext(), resourceName, keys );
	}

	public SegmentTemplate(
			final Class< ? > resourceContext,
			final String resourceName,
			final String ... keys )
	{
		this( resourceContext, resourceName, Arrays.asList( keys ) );
	}

	public SegmentTemplate(
		final Class< ? > resourceContext,
		final String resourceName,
		final List< String > keys )
	{
		try
		{
			st = StringTemplateUtils.loadAndPatchSnippet( resourceContext, resourceName, keys );
			this.keys = keys;
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	static class Identifier
	{
		private final boolean isList;

		private ArrayList< String > values = new ArrayList<>();

		public Identifier( final String identifier )
		{
			this.isList = false;
			values.add( identifier );
		}

		public Identifier()
		{
			this.isList = true;
		}

		public void put( int index, String identifier )
		{
			while ( index >= values.size() )
				values.add( "" );
			values.set( index, identifier );
		}

		public Object value()
		{
			return isList ? values : values.get( 0 );
		}

		public boolean isList()
		{
			return isList;
		}
	}

	public Segment instantiate()
	{
		return instantiate( proposeKeyToIdentifierMap() );
	}

	private Segment instantiate( final Map< String, Identifier > keyToIdentifier )
	{
		return new Segment( this, keyToIdentifier );
	}

	private Map< String, Identifier > proposeKeyToIdentifierMap()
	{
		final Map< String, Identifier > keyToIdentifier = new HashMap<>();
		int baseId = idGen.getAndAdd( keys.size() );
		for ( final String key : keys )
		{
			final String instance = String.format( "%s_x_%d_x_", key, baseId++ );
			keyToIdentifier.put( key, new Identifier( instance ) );
		}
		return keyToIdentifier;
	}

	static Identifier proposeIdentifiers( String key, int num )
	{
		final Identifier identifier = new Identifier();
		int baseId = idGen.getAndAdd( num );
		for ( int i = 0; i < num; i++ )
		{
			final String instance = String.format( "%s_x_%d_x_", key, baseId++ );
			identifier.put( i, instance );
		}
		return identifier;
	}

	String render( final Map< String, Identifier > keyToIdentifier )
	{
		clearAttributes( st );
		keys.forEach( key -> st.add( key, keyToIdentifier.get( key ).value() ) );
		return st.render();
	}

	private static Class<?> tryGetContext()
	{
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		try
		{
			final Class< ? > klass = SegmentTemplate.class.getClassLoader().loadClass( stackTrace[ 3 ].getClassName() );
			return klass;
		}
		catch ( final ClassNotFoundException e )
		{
			throw new RuntimeException( e );
		}
	}
}
