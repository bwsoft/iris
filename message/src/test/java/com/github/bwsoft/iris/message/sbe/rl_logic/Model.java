/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
/* Generated SBE (Simple Binary Encoding) message codec */
package com.github.bwsoft.iris.message.sbe.rl_logic;

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
