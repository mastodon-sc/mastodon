package net.trackmate.revised.io.yaml;

import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class WorkaroundRepresenter extends Representer
{
	@Override
	protected Node representSequence( final Tag tag, final Iterable< ? > sequence, final Boolean flowStyle )
	{
		return super.representSequence( tag, sequence, flowStyle );
	}

	@Override
	protected Node representMapping( final Tag tag, final Map< ?, ? > mapping, final Boolean flowStyle )
	{
		return super.representMapping( tag, mapping, flowStyle );
	}

	protected void putRepresent( final WorkaroundRepresent r )
	{
		this.representers.put( r.getRepresentedClass(), r );
	}
}
