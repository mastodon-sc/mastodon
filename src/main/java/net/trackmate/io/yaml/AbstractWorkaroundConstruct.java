package net.trackmate.io.yaml;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public abstract class AbstractWorkaroundConstruct extends AbstractConstruct
{
	private final WorkaroundConstructor c;

	private final Tag tag;

	public AbstractWorkaroundConstruct( final WorkaroundConstructor c, final Tag tag  )
	{
		this.c = c;
		this.tag = tag;
	}

	protected List< ? > constructSequence( final SequenceNode node )
	{
		return c.constructSequence( node );
	}

	protected Map< Object, Object > constructMapping( final MappingNode node )
	{
		return c.constructMapping( node );
	}

	protected Tag getTag()
	{
		return tag;
	}
}
