package com.rox.emu.processor.mos6502.com.rox.emu.env

import com.rox.emu.env.RoxByte
import spock.lang.Specification
import spock.lang.Unroll


class RoxByteSpec extends Specification {
    @Unroll
    def "#description: #value becomes a #expectedValue RoxByte that represents #expectedIntRepresentation"() {
        when:
        final RoxByte myByte = RoxByte.signedFrom(value)

        then:
        myByte != null
        myByte.getFormat() == RoxByte.ByteFormat.SIGNED_TWOS_COMPLIMENT
        myByte.getRawValue() == expectedValue
        myByte.getAsInt() == expectedIntRepresentation

        where:
        value || expectedValue | expectedIntRepresentation | description
        1     || 1             | 1                         | "Simplest value"
        50    || 50            | 50                        | "Standard value"
        //-2    || -2            | -2                        | "Negative value"          XXX: We dont take account of the sign bit
        127   || 127           | 127                       | "Largest positive value"
        //-128  || -128          | -127                      | "Smallest negative value" XXX: We dont take account of the sign bit
    }
}
