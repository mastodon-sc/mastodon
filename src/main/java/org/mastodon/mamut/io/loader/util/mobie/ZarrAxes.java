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
package org.mastodon.mamut.io.loader.util.mobie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;

@JsonFormat( shape = JsonFormat.Shape.ARRAY )
public enum ZarrAxes
{
    YX( "[\"y\",\"x\"]" ),
    CYX( "[\"c\",\"y\",\"x\"]" ),
    TYX( "[\"t\",\"y\",\"x\"]" ),
    ZYX( "[\"z\",\"y\",\"x\"]" ),
    CZYX( "[\"c\",\"z\",\"y\",\"x\"]" ),
    TZYX( "[\"t\",\"z\",\"y\",\"x\"]" ),
    TCYX( "[\"t\",\"c\",\"y\",\"x\"]" ),
    TCZYX( "[\"t\",\"c\",\"z\",\"y\",\"x\"]" ); // v0.2

    private final String axes;

    ZarrAxes( final String axes )
    {
        this.axes = axes;
    }

    @JsonCreator
    public static ZarrAxes decode( final String axes )
    {
        return Stream.of( ZarrAxes.values() ).filter( targetEnum -> targetEnum.axes.equals( axes ) ).findFirst().orElse( TCZYX );
    }

    public List< String > getAxesList()
    {
        final String pattern = "([a-z])";
        final List< String > allMatches = new ArrayList<>();
        final Matcher m = Pattern.compile( pattern )
                .matcher( axes );
        while ( m.find() )
        {
            allMatches.add( m.group() );
        }
        return allMatches;
    }

    public List< ZarrAxis > toAxesList( final String spaceUnit, final String timeUnit )
    {
        final List< ZarrAxis > zarrAxesList = new ArrayList<>();
        final List< String > zarrAxesStrings = getAxesList();

        final String[] units = new String[] { spaceUnit, timeUnit };

        // convert to valid ome-zarr units, if possible, otherwise just go ahead with
        // given unit
        for ( int i = 0; i < units.length; i++ )
        {
            final String unit = units[ i ];
            if ( !UnitTypes.contains( unit ) )
            {
                final UnitTypes unitType = UnitTypes.convertUnit( unit );
                if ( unitType != null )
                {
                    units[ i ] = unitType.getTypeName();
                }
            }
        }

        for ( int i = 0; i < zarrAxesStrings.size(); i++ )
        {
            final String axisString = zarrAxesStrings.get( i );
            final AxesTypes axisType = AxesTypes.getAxisType( axisString );

            String unit;
            if ( axisType == AxesTypes.SPACE )
            {
                unit = units[ 0 ];
            }
            else if ( axisType == AxesTypes.TIME )
            {
                unit = units[ 1 ];
            }
            else
            {
                unit = null;
            }

            zarrAxesList.add( new ZarrAxis( i, axisString, axisType.getTypeName(), unit ) );
        }

        return zarrAxesList;
    }

    public boolean hasTimepoints()
    {
        return this.axes.equals( TCYX.axes ) || this.axes.equals( TZYX.axes ) || this.axes.equals( TYX.axes )
                || this.axes.equals( TCZYX.axes );
    }

    public boolean hasChannels()
    {
        return this.axes.equals( CZYX.axes ) || this.axes.equals( CYX.axes ) || this.axes.equals( TCYX.axes )
                || this.axes.equals( TCZYX.axes );
    }

    // the flag reverseAxes determines whether the index will be given w.r.t.
    // reversedAxes=true corresponds to the java/bdv axis convention
    // reversedAxes=false corresponds to the zarr axis convention
    public int axisIndex( final String axisName, final boolean reverseAxes )
    {
        if ( reverseAxes )
        {
            final List< String > reverseAxesList = Lists.reverse( getAxesList() );
            return reverseAxesList.indexOf( axisName );
        }
        return getAxesList().indexOf( axisName );
    }

    public int timeIndex()
    {
        return axisIndex( "t", true );
    }

    public int channelIndex()
    {
        return axisIndex( "c", true );
    }

    // spatial: 0,1,2 (x,y,z)
    public Map< Integer, Integer > spatialToZarr()
    {
        final HashMap< Integer, Integer > map = new HashMap<>();
        map.put( 0, 0 );
        map.put( 1, 1 );
        if ( hasZAxis() )
        {
            map.put( 2, 2 );
        }
        return map;
    }

    public boolean hasZAxis()
    {
        return this.axes.equals( TCZYX.axes ) || this.axes.equals( CZYX.axes ) || this.axes.equals( TZYX.axes )
                || this.axes.equals( ZYX.axes );
    }

    public int getNumDimension()
    {
        return getAxesList().size();
    }
}
