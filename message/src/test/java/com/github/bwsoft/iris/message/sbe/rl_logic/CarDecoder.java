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

@GroupOrder({CarDecoder.FuelFiguresDecoder.class, CarDecoder.PerformanceFiguresDecoder.class})
@SuppressWarnings("all")
public class CarDecoder
{
    public static final int BLOCK_LENGTH = 45;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;

    private final CarDecoder parentMessage = this;
    private DirectBuffer buffer;
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

    public CarDecoder wrap(
        final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.actingBlockLength = actingBlockLength;
        this.actingVersion = actingVersion;
        limit(offset + actingBlockLength);

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

    public static int serialNumberId()
    {
        return 1;
    }

    public static String serialNumberMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
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

    public long serialNumber()
    {
        return CodecUtil.uint64Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int modelYearId()
    {
        return 2;
    }

    public static String modelYearMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
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

    public int modelYear()
    {
        return CodecUtil.uint16Get(buffer, offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int availableId()
    {
        return 3;
    }

    public static String availableMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public BooleanType available()
    {
        return BooleanType.get(CodecUtil.uint8Get(buffer, offset + 10));
    }


    public static int codeId()
    {
        return 4;
    }

    public static String codeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    public Model code()
    {
        return Model.get(CodecUtil.charGet(buffer, offset + 11));
    }


    public static int someNumbersId()
    {
        return 5;
    }

    public static String someNumbersMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
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

    public int someNumbers(final int index)
    {
        if (index < 0 || index >= 5)
        {
            throw new IndexOutOfBoundsException("index out of range: index=" + index);
        }

        return CodecUtil.int32Get(buffer, this.offset + 12 + (index * 4), java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int vehicleCodeId()
    {
        return 6;
    }

    public static String vehicleCodeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
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

    public byte vehicleCode(final int index)
    {
        if (index < 0 || index >= 6)
        {
            throw new IndexOutOfBoundsException("index out of range: index=" + index);
        }

        return CodecUtil.charGet(buffer, this.offset + 32 + (index * 1));
    }


    public static String vehicleCodeCharacterEncoding()
    {
        return "ASCII";
    }

    public int getVehicleCode(final byte[] dst, final int dstOffset)
    {
        final int length = 6;
        if (dstOffset < 0 || dstOffset > (dst.length - length))
        {
            throw new IndexOutOfBoundsException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        CodecUtil.charsGet(buffer, this.offset + 32, dst, dstOffset, length);
        return length;
    }


    public static int extrasId()
    {
        return 7;
    }

    public static String extrasMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    private final OptionalExtrasDecoder extras = new OptionalExtrasDecoder();

    public OptionalExtrasDecoder extras()
    {
        extras.wrap(buffer, offset + 38);
        return extras;
    }

    public static int engineId()
    {
        return 8;
    }

    public static String engineMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
        }

        return "";
    }

    private final EngineDecoder engine = new EngineDecoder();

    public EngineDecoder engine()
    {
        engine.wrap(buffer, offset + 39);
        return engine;
    }

    private final FuelFiguresDecoder fuelFigures = new FuelFiguresDecoder();

    public static long fuelFiguresDecoderId()
    {
        return 9;
    }

    public FuelFiguresDecoder fuelFigures()
    {
        fuelFigures.wrap(parentMessage, buffer);
        return fuelFigures;
    }

    public static class FuelFiguresDecoder
    implements Iterable<FuelFiguresDecoder>, java.util.Iterator<FuelFiguresDecoder>
    {
        private static final int HEADER_SIZE = 3;
        private final GroupSizeEncodingDecoder dimensions = new GroupSizeEncodingDecoder();
        private CarDecoder parentMessage;
        private DirectBuffer buffer;
        private int blockLength;
        private int actingVersion;
        private int count;
        private int index;
        private int offset;

        public void wrap(
            final CarDecoder parentMessage, final DirectBuffer buffer)
        {
            this.parentMessage = parentMessage;
            this.buffer = buffer;
            dimensions.wrap(buffer, parentMessage.limit());
            blockLength = dimensions.blockLength();
            count = dimensions.numInGroup();
            index = -1;
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

        public int actingBlockLength()
        {
            return blockLength;
        }

        public int count()
        {
            return count;
        }

        @Override
        public java.util.Iterator<FuelFiguresDecoder> iterator()
        {
            return this;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext()
        {
            return (index + 1) < count;
        }

        @Override
        public FuelFiguresDecoder next()
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

        public static int speedId()
        {
            return 10;
        }

        public static String speedMetaAttribute(final MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case EPOCH: return "unix";
                case TIME_UNIT: return "nanosecond";
                case SEMANTIC_TYPE: return "";
            }

            return "";
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

        public int speed()
        {
            return CodecUtil.uint16Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
        }


        public static int mpgId()
        {
            return 11;
        }

        public static String mpgMetaAttribute(final MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case EPOCH: return "unix";
                case TIME_UNIT: return "nanosecond";
                case SEMANTIC_TYPE: return "";
            }

            return "";
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

        public float mpg()
        {
            return CodecUtil.floatGet(buffer, offset + 2, java.nio.ByteOrder.LITTLE_ENDIAN);
        }

    }

    private final PerformanceFiguresDecoder performanceFigures = new PerformanceFiguresDecoder();

    public static long performanceFiguresDecoderId()
    {
        return 12;
    }

    public PerformanceFiguresDecoder performanceFigures()
    {
        performanceFigures.wrap(parentMessage, buffer);
        return performanceFigures;
    }
@GroupOrder({PerformanceFiguresDecoder.AccelerationDecoder.class})

    public static class PerformanceFiguresDecoder
    implements Iterable<PerformanceFiguresDecoder>, java.util.Iterator<PerformanceFiguresDecoder>
    {
        private static final int HEADER_SIZE = 3;
        private final GroupSizeEncodingDecoder dimensions = new GroupSizeEncodingDecoder();
        private CarDecoder parentMessage;
        private DirectBuffer buffer;
        private int blockLength;
        private int actingVersion;
        private int count;
        private int index;
        private int offset;

        public void wrap(
            final CarDecoder parentMessage, final DirectBuffer buffer)
        {
            this.parentMessage = parentMessage;
            this.buffer = buffer;
            dimensions.wrap(buffer, parentMessage.limit());
            blockLength = dimensions.blockLength();
            count = dimensions.numInGroup();
            index = -1;
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

        public int actingBlockLength()
        {
            return blockLength;
        }

        public int count()
        {
            return count;
        }

        @Override
        public java.util.Iterator<PerformanceFiguresDecoder> iterator()
        {
            return this;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext()
        {
            return (index + 1) < count;
        }

        @Override
        public PerformanceFiguresDecoder next()
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

        public static int octaneRatingId()
        {
            return 13;
        }

        public static String octaneRatingMetaAttribute(final MetaAttribute metaAttribute)
        {
            switch (metaAttribute)
            {
                case EPOCH: return "unix";
                case TIME_UNIT: return "nanosecond";
                case SEMANTIC_TYPE: return "";
            }

            return "";
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

        public short octaneRating()
        {
            return CodecUtil.uint8Get(buffer, offset + 0);
        }


        private final AccelerationDecoder acceleration = new AccelerationDecoder();

        public static long accelerationDecoderId()
        {
            return 14;
        }

        public AccelerationDecoder acceleration()
        {
            acceleration.wrap(parentMessage, buffer);
            return acceleration;
        }

        public static class AccelerationDecoder
        implements Iterable<AccelerationDecoder>, java.util.Iterator<AccelerationDecoder>
        {
            private static final int HEADER_SIZE = 3;
            private final GroupSizeEncodingDecoder dimensions = new GroupSizeEncodingDecoder();
            private CarDecoder parentMessage;
            private DirectBuffer buffer;
            private int blockLength;
            private int actingVersion;
            private int count;
            private int index;
            private int offset;

            public void wrap(
                final CarDecoder parentMessage, final DirectBuffer buffer)
            {
                this.parentMessage = parentMessage;
                this.buffer = buffer;
                dimensions.wrap(buffer, parentMessage.limit());
                blockLength = dimensions.blockLength();
                count = dimensions.numInGroup();
                index = -1;
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

            public int actingBlockLength()
            {
                return blockLength;
            }

            public int count()
            {
                return count;
            }

            @Override
            public java.util.Iterator<AccelerationDecoder> iterator()
            {
                return this;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext()
            {
                return (index + 1) < count;
            }

            @Override
            public AccelerationDecoder next()
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

            public static int mphId()
            {
                return 15;
            }

            public static String mphMetaAttribute(final MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case EPOCH: return "unix";
                    case TIME_UNIT: return "nanosecond";
                    case SEMANTIC_TYPE: return "";
                }

                return "";
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

            public int mph()
            {
                return CodecUtil.uint16Get(buffer, offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
            }


            public static int secondsId()
            {
                return 16;
            }

            public static String secondsMetaAttribute(final MetaAttribute metaAttribute)
            {
                switch (metaAttribute)
                {
                    case EPOCH: return "unix";
                    case TIME_UNIT: return "nanosecond";
                    case SEMANTIC_TYPE: return "";
                }

                return "";
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

            public float seconds()
            {
                return CodecUtil.floatGet(buffer, offset + 2, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public static int makeHeaderLength()
    {
        return 1;
    }

    public int makeLength()
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);

        return CodecUtil.uint8Get(buffer, limit);
    }

    public int getMake(final uk.co.real_logic.agrona.MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getMake(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public String make()
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        limit(limit + sizeOfLengthField + dataLength);
        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + sizeOfLengthField, tmp, 0, dataLength);

        final String value;
        try
        {
            value = new String(tmp, "UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        return value;
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

    public static int modelHeaderLength()
    {
        return 1;
    }

    public int modelLength()
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);

        return CodecUtil.uint8Get(buffer, limit);
    }

    public int getModel(final uk.co.real_logic.agrona.MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getModel(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public String model()
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        limit(limit + sizeOfLengthField + dataLength);
        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + sizeOfLengthField, tmp, 0, dataLength);

        final String value;
        try
        {
            value = new String(tmp, "UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        return value;
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

    public static int activationCodeHeaderLength()
    {
        return 1;
    }

    public int activationCodeLength()
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);

        return CodecUtil.uint8Get(buffer, limit);
    }

    public int getActivationCode(final uk.co.real_logic.agrona.MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getActivationCode(final byte[] dst, final int dstOffset, final int length)
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        final int bytesCopied = Math.min(length, dataLength);
        limit(limit + sizeOfLengthField + dataLength);
        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public String activationCode()
    {
        final int sizeOfLengthField = 1;
        final int limit = limit();
        buffer.checkLimit(limit + sizeOfLengthField);
        final int dataLength = CodecUtil.uint8Get(buffer, limit);
        limit(limit + sizeOfLengthField + dataLength);
        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + sizeOfLengthField, tmp, 0, dataLength);

        final String value;
        try
        {
            value = new String(tmp, "UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        return value;
    }
}
