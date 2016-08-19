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
