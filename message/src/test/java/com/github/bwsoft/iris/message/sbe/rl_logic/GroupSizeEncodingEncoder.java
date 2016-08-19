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
package com.github.bwsoft.iris.message.sbe.rl_logic;

import uk.co.real_logic.sbe.codec.java.*;
import uk.co.real_logic.agrona.MutableDirectBuffer;

@SuppressWarnings("all")
public class GroupSizeEncodingEncoder
{
    public static final int ENCODED_LENGTH = 3;
    private MutableDirectBuffer buffer;
    private int offset;
    public GroupSizeEncodingEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        return this;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public static int blockLengthNullValue()
    {
        return 65535;
    }

    public static int blockLengthMinValue()
    {
        return 0;
    }

    public static int blockLengthMaxValue()
    {
        return 65534;
    }
    public GroupSizeEncodingEncoder blockLength(final int value)
    {
        CodecUtil.uint16Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static short numInGroupNullValue()
    {
        return (short)255;
    }

    public static short numInGroupMinValue()
    {
        return (short)0;
    }

    public static short numInGroupMaxValue()
    {
        return (short)254;
    }
    public GroupSizeEncodingEncoder numInGroup(final short value)
    {
        CodecUtil.uint8Put(buffer, offset + 2, value);
        return this;
    }
}
