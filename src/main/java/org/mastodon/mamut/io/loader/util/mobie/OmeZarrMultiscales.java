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
package org.mastodon.mamut.io.loader.util.mobie;

import java.util.List;

import mpicbg.spim.data.sequence.VoxelDimensions;

public class OmeZarrMultiscales
{

    // key in json for multiscales
    public static final String MULTI_SCALE_KEY = "multiscales";

    public transient ZarrAxes axes;

    public List< ZarrAxis > zarrAxisList;

    public Dataset[] datasets;

    public String name;

    public String type;

    public String version;

    public CoordinateTransformations[] coordinateTransformations;

    public OmeZarrMultiscales()
    {}

    public OmeZarrMultiscales( final ZarrAxes axes, final String name, final String type, final String version,
            final VoxelDimensions voxelDimensions, final double[][] resolutions, final String timeUnit,
            final double frameInterval )
    {
        this.version = version;
        this.name = name;
        this.type = type;
        this.axes = axes;
        this.zarrAxisList = axes.toAxesList( voxelDimensions.unit(), timeUnit );
        generateDatasets( voxelDimensions, frameInterval, resolutions );
    }

    private void generateDatasets( final VoxelDimensions voxelDimensions, final double frameInterval, final double[][] resolutions )
    {

        final Dataset[] datasets = new Dataset[ resolutions.length ];
        for ( int i = 0; i < resolutions.length; i++ )
        {
            final Dataset dataset = new Dataset();

            final CoordinateTransformations coordinateTransformations = new CoordinateTransformations();
            coordinateTransformations.scale = getScale( voxelDimensions, frameInterval, resolutions[ i ] );
            coordinateTransformations.type = "scale";

            dataset.path = "s" + i;
            dataset.coordinateTransformations = new CoordinateTransformations[] { coordinateTransformations };
            datasets[ i ] = dataset;
        }
        this.datasets = datasets;
    }

    private double[] getScale( final VoxelDimensions voxelDimensions, final double frameInterval, final double[] xyzScale )
    {
        final int nDimensions = zarrAxisList.size();
        final double[] scale = new double[ nDimensions ];
        if ( axes.timeIndex() != -1 )
        {
            scale[ axes.timeIndex() ] = frameInterval;
        }

        if ( axes.channelIndex() != -1 )
        {
            scale[ axes.channelIndex() ] = 1;
        }

        for ( int i = 0; i < 3; i++ )
        {
            final double dimension = voxelDimensions.dimension( i ) * xyzScale[ i ];
            scale[ nDimensions - ( i + 1 ) ] = dimension;
        }

        return scale;
    }

    public static class Dataset
    {
        public String path;

        public CoordinateTransformations[] coordinateTransformations;
    }

    public static class CoordinateTransformations
    {
        public String type;

        public double[] scale;

        public double[] translation;

        public String path;
    }
}
