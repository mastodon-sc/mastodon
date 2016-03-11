package net.trackmate.io.yaml;

import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class WorkaroundRepresenter extends Representer
{
	@Override
	protected Node representSequence( final Tag tag, final Iterable< ? extends Object > sequence, final Boolean flowStyle )
	{
		// TODO Auto-generated method stub
		return super.representSequence( tag, sequence, flowStyle );
	}

	@Override
	protected Node representMapping( final Tag tag, final Map< ? extends Object, Object > mapping, final Boolean flowStyle )
	{
		// TODO Auto-generated method stub
		return super.representMapping( tag, mapping, flowStyle );
	}

	protected Boolean getDefaultFlowStyle()
	{
		return defaultFlowStyle;
	}

	protected void putRepresent( final WorkaroundRepresent r )
	{
		this.representers.put( r.getRepresentedClass(), r );
	}
}
