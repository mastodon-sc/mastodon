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

import ucar.units.PrefixDBException;
import ucar.units.SpecificationException;
import ucar.units.Unit;
import ucar.units.UnitDBException;
import ucar.units.UnitFormat;
import ucar.units.UnitFormatManager;
import ucar.units.UnitSystemException;

public enum UnitTypes
{
    ANGSTROM( "angstrom" ),
    ATTOMETER( "attometer" ),
    CENTIMETER( "centimeter" ),
    DECIMETER( "decimeter" ),
    EXAMETER( "exameter" ),
    FEMTOMETER( "femtometer" ),
    FOOT( "foot" ),
    GIGAMETER( "gigameter" ),
    HECTOMETER( "hectometer" ),
    INCH( "inch" ),
    KILOMETER( "kilometer" ),
    MEGAMETER( "megameter" ),
    METER( "meter" ),
    MICROMETER( "micrometer" ),
    MILE( "mile" ),
    MILLIMETER( "millimeter" ),
    NANOMETER( "nanometer" ),
    PARSEC( "parsec" ),
    PETAMETER( "petameter" ),
    PICOMETER( "picometer" ),
    TERAMETER( "terameter" ),
    YARD( "yard" ),
    YOCTOMETER( "yoctometer" ),
    YOTTAMETER( "yottameter" ),
    ZEPTOMETER( "zeptometer" ),
    ZETTAMETER( "zettameter" ),

    ATTOSECOND( "attosecond" ),
    CENTISECOND( "centisecond" ),
    DAY( "day" ),
    DECISECOND( "decisecond" ),
    EXASECOND( "exasecond" ),
    FEMTOSECOND( "femtosecond" ),
    GIGASECOND( "gigasecond" ),
    HECTOSECOND( "hectosecond" ),
    HOUR( "hour" ),
    KILOSECOND( "kilosecond" ),
    MEGASECOND( "megasecond" ),
    MICROSECOND( "microsecond" ),
    MILLISECOND( "millisecond" ),
    MINUTE( "minute" ),
    NANOSECOND( "nanosecond" ),
    PETASECOND( "petasecond" ),
    PICOSECOND( "picosecond" ),
    SECOND( "second" ),
    TERASECOND( "terasecond" ),
    YOCTOSECOND( "yoctosecond" ),
    YOTTASECOND( "yottasecond" ),
    ZEPTOSECOND( "zeptosecond" ),
    ZETTASECOND( "zettasecond" );

    private final String typeName;

    UnitTypes( final String typeName )
    {
        this.typeName = typeName;
    }

    public static boolean contains( final String test )
    {
        for ( final UnitTypes c : UnitTypes.values() )
        {
            if ( c.typeName.equals( test ) )
            {
                return true;
            }
        }
        return false;
    }

    public static UnitTypes convertUnit( final String unit )
    {
        // Convert the mu symbol into "u".
        final String unitString = unit.replace( "\u00B5", "u" );

        try
        {
            final UnitFormat unitFormatter = UnitFormatManager.instance();
            final Unit inputUnit = unitFormatter.parse( unitString );

            for ( final UnitTypes unitType : UnitTypes.values() )
            {
                final Unit zarrUnit = unitFormatter.parse( unitType.typeName );
                if ( zarrUnit.getCanonicalString().equals( inputUnit.getCanonicalString() ) )
                {
                    System.out.println( "Converted unit: " + unit + " to recommended ome-zarr unit: " + unitType.getTypeName() );
                    return unitType;
                }
            }
        }
        catch ( SpecificationException | UnitDBException | PrefixDBException | UnitSystemException e )
        {
            e.printStackTrace();
        }

        System.out.println( unit + " is not one of the recommended units for ome-zarr" );
        return null;
    }

    public String getTypeName()
    {
        return typeName;
    }
}
