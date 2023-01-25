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
package org.mastodon.mamut.feature.branch;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Computes the {@link BranchDisplacementDurationFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchDisplacementDurationFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchDisplacementDurationFeature output;

	@Override
	public void createOutput()
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		if ( null == output )
			output = new BranchDisplacementDurationFeature(
					new DoublePropertyMap<>( branchGraph.vertices().getRefPool(), Double.NaN ),
					new DoublePropertyMap<>( branchGraph.vertices().getRefPool(), Double.NaN ),
					model.getSpaceUnits() );
	}

	@Override
	public void run()
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		final ModelGraph graph = model.getGraph();
		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();
		try
		{
			for ( final BranchSpot bs : branchGraph.vertices() )
				runForBranchSpot( bs, ref1, ref2 );
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
		}
	}

	private void runForBranchSpot( BranchSpot branchSpot, Spot ref1, Spot ref2 )
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();

		// get source spot
		Spot source = branchGraph.getFirstLinkedVertex( branchSpot, ref1 );
		if ( source.incomingEdges().size() == 1 )
			source = source.incomingEdges().iterator().next().getSource( ref1 );

		// get target spot
		final Spot target = branchGraph.getLastLinkedVertex( branchSpot, ref2 );

		output.dispMap.set( branchSpot, distance( source, target ) );
		output.durMap.set( branchSpot, duration( source, target ) );
	}

	private double distance( Spot source, Spot target )
	{
		double d2 = 0.;
		for ( int d = 0; d < 3; d++ )
		{
			final double dx = source.getDoublePosition( d ) - target.getDoublePosition( d );
			d2 += dx * dx;
		}
		return Math.sqrt( d2 );
	}

	private double duration( Spot source, Spot target )
	{
		final double t1 = target.getTimepoint();
		final double t2 = source.getTimepoint();
		double abs = Math.abs( t2 - t1 );
		return abs;
	}
}
