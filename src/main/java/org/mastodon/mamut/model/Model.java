/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.model;

import java.util.ArrayList;
import java.util.List;

import org.mastodon.feature.Dimension;
import org.mastodon.labels.LabelSets;
import org.mastodon.mamut.feature.LinkDeltaFrameFeature;
import org.mastodon.mamut.feature.LinkDisplacementFeature;
import org.mastodon.mamut.feature.LinkTargetIdFeature;
import org.mastodon.mamut.feature.LinkVelocityFeature;
import org.mastodon.mamut.feature.SpotFrameFeature;
import org.mastodon.mamut.feature.SpotNLinksFeature;
import org.mastodon.mamut.feature.SpotPositionFeature;
import org.mastodon.mamut.feature.SpotRadiusFeature;
import org.mastodon.mamut.feature.branch.BranchNDivisionsFeature;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.AbstractModelBranch;
import org.mastodon.model.tag.DefaultTagSetModel;
import org.mastodon.properties.Property;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.undo.GraphUndoRecorder;
import org.mastodon.undo.Recorder;
import org.mastodon.undo.UndoPointMarker;

import net.imglib2.RealLocalizable;

/**
 * A model built to manage a graph of {@link Spot}s and {@link Link}s.
 * <p>
 * Spots are the graph vertices. They are {@link RealLocalizable} 3D ellipsoids,
 * whose volume is specified through a covariance matrix. Graph edges are plain
 * links between spots.
 * <p>
 * On top of a graph structure for the spots, this model manages a
 * {@link SpatioTemporalIndex}, that can slice the model at some given
 * time-points.
 *
 *
 * @author Tobias Pietzsch
 */
public class Model extends AbstractModelBranch< ModelGraph, Spot, Link, ModelBranchGraph, BranchSpot, BranchLink > implements UndoPointMarker
{

	private static final int initialCapacity = 1024;

	private final GraphUndoRecorder< Spot, Link > undoRecorder;

	public Model()
	{
		this( "pixel", "frame" );
	}

	public Model( final String spaceUnits, final String timeUnits )
	{
		this( new ModelGraph( initialCapacity ), spaceUnits, timeUnits );
	}

	private Model( final ModelGraph graph, final String spaceUnits, final String timeUnits )
	{
		super( graph, new ModelBranchGraph( graph, initialCapacity ), spaceUnits, timeUnits );

		/*
		 * Feature model.
		 */

		declareDefaultFeatures();

		/*
		 * Undo / Redo.
		 */

		final List< Property< Spot > > vertexUndoableProperties = new ArrayList<>();
		vertexUndoableProperties.add( modelGraph.getVertexPool().positionProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().covarianceProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().boundingSphereRadiusSquProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().labelProperty() );

		final List< Property< Link > > edgeUndoableProperties = new ArrayList<>();
		vertexUndoableProperties.add(
				new DefaultTagSetModel.SerialisationAccess< Spot, Link >( tagSetModel )
				{
					@Override
					protected LabelSets< Spot, Integer > getVertexIdLabelSets()
					{
						return super.getVertexIdLabelSets();
					}
				}.getVertexIdLabelSets() );
		edgeUndoableProperties.add(
				new DefaultTagSetModel.SerialisationAccess< Spot, Link >( tagSetModel )
				{
					@Override
					protected LabelSets< Link, Integer > getEdgeIdLabelSets()
					{
						return super.getEdgeIdLabelSets();
					}
				}.getEdgeIdLabelSets() );

		undoRecorder = new GraphUndoRecorder<>(
				initialCapacity,
				modelGraph,
				modelGraph.idmap(),
				ModelSerializer.getInstance().getVertexSerializer(),
				ModelSerializer.getInstance().getEdgeSerializer(),
				vertexUndoableProperties,
				edgeUndoableProperties );

		final Recorder< DefaultTagSetModel.SetTagSetStructureUndoableEdit > recorder = undoRecorder.createGenericUndoableEditRecorder();
		tagSetModel.setUndoRecorder( recorder );
	}

	/**
	 * Declares a set of basic features that can return valid values without a
	 * computer.
	 */
	public void declareDefaultFeatures()
	{
		featureModel.declareFeature( new SpotPositionFeature( Dimension.POSITION.getUnits( spaceUnits, timeUnits ) ) );
		featureModel.declareFeature( new SpotRadiusFeature( Dimension.LENGTH.getUnits( spaceUnits, timeUnits ) ) );
		featureModel.declareFeature( new SpotFrameFeature() );
		featureModel.declareFeature( new SpotNLinksFeature() );
		featureModel.declareFeature( new LinkTargetIdFeature( modelGraph ) );
		featureModel.declareFeature( new LinkDisplacementFeature( modelGraph, Dimension.LENGTH.getUnits( spaceUnits, timeUnits ) ) );
		featureModel.declareFeature( new LinkVelocityFeature( modelGraph, Dimension.VELOCITY.getUnits( spaceUnits, timeUnits ) ) );
		featureModel.declareFeature( new LinkDeltaFrameFeature( modelGraph ) );
		featureModel.declareFeature( new BranchNDivisionsFeature() );
	}

	@Override
	public ModelSerializer getGraphSerializer()
	{
		return ModelSerializer.getInstance();
	}

	public void undo()
	{
		lock.writeLock().lock();
		try
		{
			undoRecorder.undo();
			modelGraph.notifyGraphChanged();
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	public void redo()
	{
		lock.writeLock().lock();
		try
		{
			undoRecorder.redo();
			modelGraph.notifyGraphChanged();
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	@Override
	public void setUndoPoint()
	{
		undoRecorder.setUndoPoint();
	}

	public void setSavePoint()
	{
		undoRecorder.setSavePoint();
	}

	public boolean isSavePoint()
	{
		return undoRecorder.isSavePoint();
	}

	public String getSpaceUnits()
	{
		return spaceUnits;
	}

	public String getTimeUnits()
	{
		return timeUnits;
	}
}
