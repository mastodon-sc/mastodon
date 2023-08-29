package org.mastodon.mamut.io.importer.graphml;

import org.jdom2.Element;

public class GraphMLKey
{

	public static final String ID_TAG = "id";

	public static final String FOR_TAG = "for";

	public static final String FOR_NODE = "node";

	public static final String FOR_EDGE = "edge";

	public static final String NAME_TAG = "attr.name";

	public static final String TYPE_TAG = "attr.type";

	public static final String INT_TYPE = "int";

	public static final String FLOAT_TYPE = "float";

	public static GraphMLKey fromXML( final Element keyEl )
	{
		final String id = keyEl.getAttributeValue( ID_TAG );
		final GraphMLObject target = GraphMLObject.from( keyEl.getAttributeValue( FOR_TAG ) );
		final String name = keyEl.getAttributeValue( NAME_TAG );
		final GraphMLValueType type = GraphMLValueType.from( keyEl.getAttributeValue( TYPE_TAG ) );
		return new GraphMLKey( id, target, name, type );
	}

	public final String id;

	public final GraphMLObject target;

	public final String name;

	public final GraphMLValueType type;

	public enum GraphMLValueType
	{
		INT, FLOAT;

		public static GraphMLValueType from( final String str )
		{
			switch ( str.toLowerCase() )
			{
			case INT_TYPE:
				return INT;
			case FLOAT_TYPE:
				return FLOAT;
			default:
				throw new IllegalArgumentException( "Unknown value type for GraphML: " + str );
			}
		}
	}

	public enum GraphMLObject
	{
		NODE, EDGE;

		public static GraphMLObject from( final String str )
		{
			switch ( str.toLowerCase() )
			{
			case FOR_NODE:
				return NODE;
			case FOR_EDGE:
				return EDGE;
			default:
				throw new IllegalArgumentException( "Unknown target type for GraphML: " + str );
			}
		}
	}

	public GraphMLKey( final String id, final GraphMLObject target, final String name, final GraphMLValueType type )
	{
		this.id = id;
		this.target = target;
		this.name = name;
		this.type = type;
	}

}
