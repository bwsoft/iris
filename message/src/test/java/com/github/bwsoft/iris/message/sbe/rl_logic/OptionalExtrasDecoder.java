/* Generated SBE (Simple Binary Encoding) message codec */
package com.github.bwsoft.iris.message.sbe.rl_logic;

import org.agrona.DirectBuffer;

@javax.annotation.Generated(value = {"com.github.bwsoft.iris.message.sbe.rl_logic.OptionalExtrasDecoder"})
@SuppressWarnings("all")
public class OptionalExtrasDecoder
{
    public static final int ENCODED_LENGTH = 1;
    private DirectBuffer buffer;
    private int offset;

    public OptionalExtrasDecoder wrap(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;

        return this;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public boolean sunRoof()
    {
        return 0 != (buffer.getByte(offset) & (1 << 0));
    }

    public boolean sportsPack()
    {
        return 0 != (buffer.getByte(offset) & (1 << 1));
    }

    public boolean cruiseControl()
    {
        return 0 != (buffer.getByte(offset) & (1 << 2));
    }
    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        builder.append('{');
        boolean atLeastOne = false;
        if (sunRoof())
        {
            if (atLeastOne)
            {
                builder.append(',');
            }
            builder.append("sunRoof");
            atLeastOne = true;
        }
        if (sportsPack())
        {
            if (atLeastOne)
            {
                builder.append(',');
            }
            builder.append("sportsPack");
            atLeastOne = true;
        }
        if (cruiseControl())
        {
            if (atLeastOne)
            {
                builder.append(',');
            }
            builder.append("cruiseControl");
            atLeastOne = true;
        }
        builder.append('}');

        return builder;
    }
}
