/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/* Generated SBE (Simple Binary Encoding) message codec */
package baseline;

import uk.co.real_logic.sbe.codec.java.*;
import uk.co.real_logic.agrona.DirectBuffer;

@SuppressWarnings("all")
public class VarDataEncodingDecoder
{
    public static final int ENCODED_LENGTH = -1;
    private DirectBuffer buffer;
    private int offset;
    public VarDataEncodingDecoder wrap(final DirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        return this;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public static short lengthNullValue()
    {
        return (short)255;
    }

    public static short lengthMinValue()
    {
        return (short)0;
    }

    public static short lengthMaxValue()
    {
        return (short)254;
    }

    public short length()
    {
        return CodecUtil.uint8Get(buffer, offset + 0);
    }


    public static short varDataNullValue()
    {
        return (short)255;
    }

    public static short varDataMinValue()
    {
        return (short)0;
    }

    public static short varDataMaxValue()
    {
        return (short)254;
    }
}
