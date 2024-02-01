/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
