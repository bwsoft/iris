/* Generated SBE (Simple Binary Encoding) message codec */
package baseline;

import uk.co.real_logic.sbe.codec.java.*;
import uk.co.real_logic.agrona.DirectBuffer;

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
        return CodecUtil.uint8GetChoice(buffer, offset, 0);
    }


    public boolean sportsPack()
    {
        return CodecUtil.uint8GetChoice(buffer, offset, 1);
    }


    public boolean cruiseControl()
    {
        return CodecUtil.uint8GetChoice(buffer, offset, 2);
    }

}
