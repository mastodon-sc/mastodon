package tpietzschx.shadergen.generate;

import java.util.HashMap;
import java.util.Map;
import tpietzschx.shadergen.generate.SegmentTemplate.Identifier;

public class SegmentedShaderBuilder
{
	static final Object NOT_UNIQUE = new Object();

	// maps uniform name to either NOT_UNIQUE or instantiated identifier (String
	private final Map< String, Object > uniforms = new HashMap<>();

	private final StringBuilder vpCode = new StringBuilder();

	private final StringBuilder fpCode = new StringBuilder();

	private void add( final Segment segment, final StringBuilder code )
	{
		final Map< String, Identifier > map = segment.getKeyToIdentifierMap();
		map.forEach( ( name, identifier ) -> {
			uniforms.compute( name, ( n, value ) -> {
				if ( value == null && !identifier.isList() )
					return ( String ) identifier.value();
				else
					return NOT_UNIQUE;
			} );
		} );

		code.append( segment.getCode() );
	}

	public SegmentedShaderBuilder fragment( final Segment segment )
	{
		add( segment, fpCode );
		return this;
	}

	public SegmentedShaderBuilder vertex( final Segment segment )
	{
		add( segment, vpCode );
		return this;
	}

	public SegmentedShader build()
	{
		return new SegmentedShader( vpCode, fpCode, uniforms );
	}
}
