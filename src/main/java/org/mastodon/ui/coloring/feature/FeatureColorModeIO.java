/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.coloring.feature;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.io.yaml.WorkaroundConstructor;
import org.mastodon.io.yaml.WorkaroundRepresent;
import org.mastodon.io.yaml.WorkaroundRepresenter;
import org.mastodon.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.VertexColorMode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class FeatureColorModeIO
{
	private static class FeatureColorModeRepresenter extends WorkaroundRepresenter
	{
		public FeatureColorModeRepresenter()
		{
			putRepresent( new RepresentFeatureProjectionId( this ) );
			putRepresent( new RepresentFeatureColorMode( this ) );
		}
	}

	private static class FeatureColorModeConstructor extends WorkaroundConstructor
	{
		public FeatureColorModeConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructFeatureProjectionId( this ) );
			putConstruct( new ConstructFeatureColorMode( this ) );
		}
	}

	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new FeatureColorModeRepresenter();
		final Constructor constructor = new FeatureColorModeConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	public static final Tag FEATURE_PROJECTION_ID_TAG = new Tag( "!featureprojection" );

	public static class RepresentFeatureProjectionId extends WorkaroundRepresent
	{
		public RepresentFeatureProjectionId( final WorkaroundRepresenter r )
		{
			super( r, FEATURE_PROJECTION_ID_TAG, FeatureProjectionId.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final FeatureProjectionId p = ( FeatureProjectionId ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();
			mapping.put( "feature", p.getFeatureKey() );
			mapping.put( "projection", p.getProjectionKey() );
			mapping.put( "target", p.getTargetType().name() );
			mapping.put( "i0", p.getI0() );
			mapping.put( "i1", p.getI1() );
			final Node node = representMapping( getTag(), mapping, FLOW );
			return node;
		}
	}

	public static class ConstructFeatureProjectionId extends AbstractWorkaroundConstruct
	{
		public ConstructFeatureProjectionId( final WorkaroundConstructor c )
		{
			super( c, FEATURE_PROJECTION_ID_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String featureKey = getString( mapping, "feature" );
				final String projectionKey = getString( mapping, "projection" );
				final String targetStr = getString( mapping, "target" );
				final TargetType targetType = TargetType.valueOf( targetStr );
				final int i0 = getInt( mapping, "i0" ); 
				final int i1 = getInt( mapping, "i1" );
				return new FeatureProjectionId( featureKey, projectionKey, targetType, i0, i1 );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	private static final Tag FEATURECOLORMODE_TAG = new Tag( "!featurecolormode" );

	private static class RepresentFeatureColorMode extends WorkaroundRepresent
	{
		public RepresentFeatureColorMode( final WorkaroundRepresenter r )
		{
			super( r, FEATURECOLORMODE_TAG, FeatureColorMode.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final FeatureColorMode s = ( FeatureColorMode ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			mapping.put( "name", s.getName() );

			mapping.put( "vertexColorMode", s.getVertexColorMode().name() );
			mapping.put( "vertexFeatureProjection", s.getVertexFeatureProjection() );
			mapping.put( "vertexColorMap", s.getVertexColorMap() );
			mapping.put( "vertexFeatureRange", new double[] { s.getVertexRangeMin(), s.getVertexRangeMax() } );
			mapping.put( "edgeColorMode", s.getEdgeColorMode().name() );
			mapping.put( "edgeFeatureProjection", s.getEdgeFeatureProjection() );
			mapping.put( "edgeColorMap", s.getEdgeColorMap() );
			mapping.put( "edgeFeatureRange", new double[] { s.getEdgeRangeMin(), s.getEdgeRangeMax() } );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructFeatureColorMode extends AbstractWorkaroundConstruct
	{
		public ConstructFeatureColorMode( final WorkaroundConstructor c )
		{
			super( c, FEATURECOLORMODE_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = getString( mapping, "name" ); 
				final FeatureColorMode s = FeatureColorMode.defaultMode().copy( name );

				s.setVertexColorMode( VertexColorMode.valueOf( getString( mapping, "vertexColorMode" ) ) );
				s.setVertexFeatureProjection( ( FeatureProjectionId ) mapping.get( "vertexFeatureProjection" ) );
				s.setVertexColorMap( getString( mapping, "vertexColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > vertexRange = ( List< Double > ) mapping.get( "vertexFeatureRange" );
				s.setVertexRange( vertexRange.get( 0 ), vertexRange.get( 1 ) );
				s.setEdgeColorMode( EdgeColorMode.valueOf( getString( mapping, "edgeColorMode" ) ) );
				s.setEdgeFeatureProjection( ( FeatureProjectionId ) mapping.get( "edgeFeatureProjection" ) );
				s.setEdgeColorMap( getString( mapping, "edgeColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > edgeRange = ( List< Double > ) mapping.get( "edgeFeatureRange" );
				s.setEdgeRange( edgeRange.get( 0 ), edgeRange.get( 1 ) );

				return s;
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}
