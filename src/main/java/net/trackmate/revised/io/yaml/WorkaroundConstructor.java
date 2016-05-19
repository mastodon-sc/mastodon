package net.trackmate.revised.io.yaml;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

public class WorkaroundConstructor extends Constructor
{
	public WorkaroundConstructor( final Class< ? > theRoot )
	{
		super( theRoot );
	}

	@Override
	protected List< ? > constructSequence( final SequenceNode node )
	{
		return super.constructSequence( node );
	}

	@Override
	protected Map< Object, Object > constructMapping( final MappingNode node )
	{
		return super.constructMapping( node );
	}

	public void putConstruct( final AbstractWorkaroundConstruct construct )
	{
		this.yamlConstructors.put( construct.getTag(), construct );
	}
}
