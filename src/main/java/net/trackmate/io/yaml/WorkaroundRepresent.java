package net.trackmate.io.yaml;

import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

public abstract class WorkaroundRepresent implements Represent
{
	private final WorkaroundRepresenter r;

	private final Tag tag;

	private final Class< ? > clazz;

	public WorkaroundRepresent( final WorkaroundRepresenter r, final Tag tag, final Class< ? > clazz )
	{
		this.r = r;
		this.tag = tag;
		this.clazz = clazz;
	}

	protected Node representSequence( final Tag tag, final Iterable< ? > sequence, final Boolean flowStyle )
	{
		return r.representSequence( tag, sequence, flowStyle );
	}

	protected Node representMapping( final Tag tag, final Map< ?, ? > mapping, final Boolean flowStyle )
	{
		return r.representMapping( tag, mapping, flowStyle );
	}

	protected Boolean getDefaultFlowStyle()
	{
		return r.getDefaultFlowStyle().getStyleBoolean();
	}

	protected Tag getTag()
	{
		return tag;
	}

	protected Class< ? > getRepresentedClass()
	{
		return clazz;
	}
}
