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
import uk.co.real_logic.agrona.MutableDirectBuffer;

@GroupOrder({CarEncoder.FuelFiguresEncoder.class, CarEncoder.PerformanceFiguresEncoder.class})
@SuppressWarnings("all")
public class CarEncoder
{
    public static final int BLOCK_LENGTH = 45;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;

    private final CarEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    protected int offset;
    protected int limit;
    protected int actingBlockLength;
    protected int actingVersion;

    public int sbeBlockLength()
    {
        return BLOCK_LENGTH;
    }

    public int sbeTemplateId()
    {
        return TEMPLATE_ID;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String sbeSemanticType()
    {
        return "";
    }

    public int offset()
    {
        return offset;
    }

    public CarEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);
        return this;
    }

    public int encodedLength()
    {
        return limit - offset;
    }

    public int limit()
    {
        return limit;
    }

    public void limit(final int limit)
    {
        buffer.checkLimit(limit);
        this.limit = limit;
    }

    public static long serialNumberNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long serialNumberMinValue()
    {
        return 0x0L;
    }

    public static long serialNumberMaxValue()
    {
        return 0xfffffffffffffffeL;
    }
    public CarEncoder serialNumber(final long value)
    {
        CodecUtil.uint64Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int modelYearNullValue()
    {
        return 65535;
    }

    public static int modelYearMinValue()
    {
        return 0;
    }

    public static int modelYearMaxValue()
    {
        return 65534;
    }
    public CarEncoder modelYear(final int value)
    {
        CodecUtil.uint16Put(buffer, offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }
    public CarEncoder available(final BooleanType value)
    {
        CodecUtil.uint8Put(buffer, offset + 10, value.value());
        return this;
    }
    public CarEncoder code(final Model value)
    {
        CodecUtil.charPut(buffer, offset + 11, value.value());
        return this;
    }

    public static int someNumbersNullValue()
    {
        return -2147483648;
    }

    public static int someNumbersMinValue()
    {
        return -2147483647;
    }

    public static int someNumbersMaxValue()
    {
        return 2147483647;
    }

    public static int someNumbersLength()
    {
        return 5;
    }

    public void someNumbers(final int index, final int value)
    {
        if (index < 0 || index >= 5)
        {
            throw new IndexOutOfBoundsException("index out of range: index=" + index);
        }

        CodecUtil.int32Put(buffer, this.offset + 12 + (index * 4), value, java.nio.ByteOrder.LITTLE_ENDIAN);
    }

    public static byte vehicleCodeNullValue()
    {
        return (byte)0;
    }

    public static byte vehicleCodeMinValue()
    {
        return (byte)32;
    }

    public static byte vehicleCodeMaxValue()
    {
        return (byte)126;
    }

    public static int vehicleCodeLength()
    {
        return 6;
    }

    public void vehicleCode(final int index, final byte value)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfBoundsException("index out of range: index=" + index);
        }

        CodecUtil.charPut(buffer, this.offset + 32 + (index * 1), value);
    }

    public static String vehicleCodeCharacterEncoding()
    {
        return "ASCII";
    }
    public CarEncoder putVehicleCode(final byte[] src, final int srcOffset)
    {
        final int length = 6;
        if (srcOffset < 0 || srcOffset > (src.length - length))
        {
            throw new IndexOutOfBoundsException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        CodecUtil.charsPut(buffer, this.offset + 32, src, srcOffset, length);
        return this;
    }

    private final OptionalExtrasEncoder extras = new OptionalExtrasEncoder();

    public OptionalExtrasEncoder extras()
    {
        extras.wrap(buffer, offset + 38);
        return extras;
    }

    private final EngineEncoder engine = new EngineEncoder();

    public EngineEncoder engine()
    {
        engine.wrap(buffer, offset + 39);
        return engine;
    }

    private final FuelFiguresEncoder fuelFigures = new FuelFiguresEncoder();

    public static long fuelFiguresId()
    {
        return 9;
    }

    public FuelFiguresEncoder fuelFiguresCount(final int count)
    {
        fuelFigures.wrap(parentMessage, buffer, count);
        return fuelFigures;
    }

    public static class FuelFiguresEncoder
    {
        private static final int HEADER_SIZE = 3;
        private final GroupSizeEncodingEncoder dimensions = new GroupSizeEncodingEncoder();
        private CarEncoder parentMessage;
        private MutableDirectBuffer buffer;
        private int blockLength;
        private int actingVersion;
        private int count;
        private int index;
        private int offset;

        public void wrap(final CarEncoder parentMessage, final MutableDirectBuffer buffer, final int count)
        {
            this.parentMessage = parentMessage;
            this.buffer = buffer;
            actingVersion = SCHEMA_VERSION;
            dimensions.wrap(buffer, parentMessage.limit());
            dimensions.blockLength((int)6);
            dimensions.numInGroup((short)count);
            index = -1;
            this.count = count;
            blockLength = 6;
            parentMessage.limit(parentMessage.limit() + HEADER_SIZE);
        }

        public static int sbeHeaderSize()
        {
            return HEADER_SIZE;
        }

        public static int sbeBlockLength()
        {
            return 6;
        }

        public FuelFiguresEncoder next()
        {
            if (index + 1 >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + blockLength);
            ++index;

            return this;
        }

        public static int speedNullValue()
        {
            return 65535;
        }

        public static int speedMinValue()
        {
            return 0;
        }

        public static int speedMaxValue()
        {
            return 65534;
        }
        public FuelFiguresEncoder speed(final int value)
        {
            CodecUtil.uint16Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
            return this;
        }

        public static float mpgNullValue()
        {
            return Float.NaN;
        }

        public static float mpgMinValue()
        {
            return 1.401298464324817E-45f;
        }

        public static float mpgMaxValue()
        {
            return 3.4028234663852886E38f;
        }
        public FuelFiguresEncoder mpg(final float value)
        {
            CodecUtil.floatPut(buffer, offset + 2, value, java.nio.ByteOrder.LITTLE_ENDIAN);
            return this;
        }
    }

    private final PerformanceFiguresEncoder performanceFigures = new PerformanceFiguresEncoder();

    public static long performanceFiguresId()
    {
        return 12;
    }

    public PerformanceFiguresEncoder performanceFiguresCount(final int count)
    {
        performanceFigures.wrap(parentMessage, buffer, count);
        return performanceFigures;
    }
@GroupOrder({PerformanceFiguresEncoder.AccelerationEncoder.class})

    public static class PerformanceFiguresEncoder
    {
        private static final int HEADER_SIZE = 3;
        private final GroupSizeEncodingEncoder dimensions = new GroupSizeEncodingEncoder();
        private CarEncoder parentMessage;
        private MutableDirectBuffer buffer;
        private int blockLength;
        private int actingVersion;
        private int count;
        private int index;
        private int offset;

        public void wrap(final CarEncoder parentMessage, final MutableDirectBuffer buffer, final int count)
        {
            this.parentMessage = parentMessage;
            this.buffer = buffer;
            actingVersion = SCHEMA_VERSION;
            dimensions.wrap(buffer, parentMessage.limit());
            dimensions.blockLength((int)1);
            dimensions.numInGroup((short)count);
            index = -1;
            this.count = count;
            blockLength = 1;
            parentMessage.limit(parentMessage.limit() + HEADER_SIZE);
        }

        public static int sbeHeaderSize()
        {
            return HEADER_SIZE;
        }

        public static int sbeBlockLength()
        {
            return 1;
        }

        public PerformanceFiguresEncoder next()
        {
            if (index + 1 >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + blockLength);
            ++index;

            return this;
        }

        public static short octaneRatingNullValue()
        {
            return (short)255;
        }

        public static short octaneRatingMinValue()
        {
            return (short)90;
        }

        public static short octaneRatingMaxValue()
        {
            return (short)110;
        }
        public PerformanceFiguresEncoder octaneRating(final short value)
        {
            CodecUtil.uint8Put(buffer, offset + 0, value);
            return this;
        }

        private final AccelerationEncoder acceleration = new AccelerationEncoder();

        public static long accelerationId()
        {
            return 14;
        }

        public AccelerationEncoder accelerationCount(final int count)
        {
            acceleration.wrap(parentMessage, buffer, count);
            return acceleration;
        }

        public static class AccelerationEncoder
        {
            private static final int HEADER_SIZE = 3;
            private final GroupSizeEncodingEncoder dimensions = new GroupSizeEncodingEncoder();
            private CarEncoder parentMessage;
            private MutableDirectBuffer buffer;
            private int blockLength;
            private int actingVersion;
            private int count;
            private int index;
            private int offset;

            public void wrap(final CarEncoder parentMessage, final MutableDirectBuffer buffer, final int count)
            {
                this.parentMessage = parentMessage;
                this.buffer = buffer;
                actingVersion = SCHEMA_VERSION;
                dimensions.wrap(buffer, parentMessage.limit());
                dimensions.blockLength((int)6);
                dimensions.numInGroup((short)count);
                index = -1;
                this.count = count;
                blockLength = 6;
                parentMessage.limit(parentMessage.limit() + HEADER_SIZE);
            }

            public static int sbeHeaderSize()
            {
                return HEADER_SIZE;
            }

            public static int sbeBlockLength()
            {
                return 6;
            }

            public AccelerationEncoder next()
            {
                if (index + 1 >= count)
                {
                    throw new java.util.NoSuchElementException();
                }

                offset = parentMessage.limit();
                parentMessage.limit(offset + blockLength);
                ++index;

                return this;
            }

            public static int mphNullValue()
            {
                return 65535;
            }

            public static int mphMinValue()
            {
                return 0;
            }

            public static int mphMaxValue()
            {
                return 65534;
            }
            public AccelerationEncoder mph(final int value)
            {
                CodecUtil.uint16Put(buffer, offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
                return this;
            }

            public static float secondsNullValue()
            {
                return Float.NaN;
            }

            public static float secondsMinValue()
            {
                return 1.401298464324817E-45f;
            }

            public static float secondsMaxValue()
            {
                return 3.4028234663852886E38f;
            }
            public AccelerationEncoder seconds(final float value)
            {
                CodecUtil.floatPut(buffer, offset + 2, value, java.nio.ByteOrder.LITTLE_ENDIAN);
                return this;
            }
        }
    }

    public static int makeId()
    {
        return 17;
    }

    public static String makeCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String makeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int putMake(final uk.co.real_logic.agrona.DirectBuffer src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public int putMake(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public CarEncoder make(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);

        return this;
    }

    public static int modelId()
    {
        return 18;
    }

    public static String modelCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String modelMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int putModel(final uk.co.real_logic.agrona.DirectBuffer src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public int putModel(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public CarEncoder model(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);

        return this;
    }

    public static int activationCodeId()
    {
        return 19;
    }

    public static String activationCodeCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String activationCodeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public int putActivationCode(final uk.co.real_logic.agrona.DirectBuffer src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public int putActivationCode(final byte[] src, final int srcOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);

        return length;
    }

    public CarEncoder activationCode(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        final int sizeOfLengthField = 1;
        final int limit = limit();
        limit(limit + sizeOfLengthField + length);
        CodecUtil.uint8Put(buffer, limit, (short)length);
        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);

        return this;
    }
}
