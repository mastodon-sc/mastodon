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
package org.mastodon.feature.update;

import java.util.ArrayList;
import java.util.List;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.properties.PropertyChangeListener;

/**
 * Listeners for incremental changes.
 */
public class GraphFeatureUpdateListeners
{

	/**
	 * Returns a new {@link PropertyChangeListener} that will remove vertices
	 * and their neighbor edges from a feature model if their property are
	 * modified.
	 * 
	 * @param featureModel
	 *            the feature model.
	 * @param vertexClass
	 *            the class of vertices in the model.
	 * @param edgeClass
	 *            the class of edges in the model.
	 * @return a new {@link PropertyChangeListener}.
	 * @param <V>
	 *            the type of vertices.
	 * @param <E>
	 *            the type of edges.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > PropertyChangeListener< V > vertexPropertyListener(
			final FeatureModel featureModel, final Class< V > vertexClass, final Class< E > edgeClass )
	{
		return new MyVertexPropertyChangeListener<>( featureModel, vertexClass, edgeClass );
	}

	private static final class MyVertexPropertyChangeListener< V extends Vertex< E >, E extends Edge< V > >
			implements PropertyChangeListener< V >, FeatureModelListener
	{

		private final FeatureModel featureModel;

		private final Class< V > vertexClass;

		private final Class< E > edgeClass;

		private final List< Feature< E > > edgeFeatures;

		private final List< Feature< V > > vertexFeatures;

		public MyVertexPropertyChangeListener( final FeatureModel featureModel, final Class< V > vertexClass,
				final Class< E > edgeClass )
		{
			this.featureModel = featureModel;
			this.vertexClass = vertexClass;
			this.edgeClass = edgeClass;
			this.edgeFeatures = new ArrayList<>();
			this.vertexFeatures = new ArrayList<>();
			featureModelChanged();
			featureModel.listeners().add( this );
		}

		@Override
		public void propertyChanged( final V v )
		{
			vertexFeatures.forEach( f -> f.invalidate( v ) );
			for ( final Feature< E > f : edgeFeatures )
				v.edges().forEach( e -> f.invalidate( e ) );
		}

		@Override
		public void featureModelChanged()
		{
			featuresOfTarget( featureModel, vertexClass, vertexFeatures );
			featuresOfTarget( featureModel, edgeClass, edgeFeatures );
		}

		@SuppressWarnings( "unchecked" )
		private static < O > void featuresOfTarget(
				final FeatureModel featureModel,
				final Class< O > targetClass,
				final List< Feature< O > > featureList )
		{
			featureList.clear();
			for ( final FeatureSpec< ?, ? > fs : featureModel.getFeatureSpecs() )
				if ( fs.getTargetClass().equals( targetClass ) )
					featureList.add( ( Feature< O > ) featureModel.getFeature( fs ) );
		}
	}
}
