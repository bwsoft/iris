/* Generated SBE (Simple Binary Encoding) message codec */
package com.github.bwsoft.iris.message.sbe.rl_logic;

@javax.annotation.Generated(value = {"com.github.bwsoft.iris.message.sbe.rl_logic.Model"})
public enum Model
{
    A((byte)65),
    B((byte)66),
    C((byte)67),
    NULL_VAL((byte)0);

    private final byte value;

    Model(final byte value)
    {
        this.value = value;
    }

    public byte value()
    {
        return value;
    }

    public static Model get(final byte value)
    {
        switch (value)
        {
            case 65: return A;
            case 66: return B;
            case 67: return C;
        }

        if ((byte)0 == value)
        {
            return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
