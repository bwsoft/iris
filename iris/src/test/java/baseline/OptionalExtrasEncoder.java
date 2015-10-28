/* Generated SBE (Simple Binary Encoding) message codec */
package baseline;

import uk.co.real_logic.sbe.codec.java.*;
import uk.co.real_logic.agrona.MutableDirectBuffer;

@SuppressWarnings("all")
public class OptionalExtrasEncoder
{
    public static final int ENCODED_LENGTH = 1;
    private MutableDirectBuffer buffer;
    private int offset;
    public OptionalExtrasEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        return this;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public OptionalExtrasEncoder clear()
    {
        CodecUtil.uint8Put(buffer, offset, (short)0);
        return this;
    }

    public OptionalExtrasEncoder sunRoof(final boolean value)
    {
        CodecUtil.uint8PutChoice(buffer, offset, 0, value);
        return this;
    }

    public OptionalExtrasEncoder sportsPack(final boolean value)
    {
        CodecUtil.uint8PutChoice(buffer, offset, 1, value);
        return this;
    }

    public OptionalExtrasEncoder cruiseControl(final boolean value)
    {
        CodecUtil.uint8PutChoice(buffer, offset, 2, value);
        return this;
    }
}
