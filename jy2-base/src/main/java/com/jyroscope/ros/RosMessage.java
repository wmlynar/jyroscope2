package com.jyroscope.ros;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import com.jyroscope.Log;
import com.jyroscope.ros.types.RosStringType;

public final class RosMessage {
    
    private static final int DEFAULT_INITIAL_SIZE = 256;
    
    private byte[] buffer;
    private int position;
    private int limit;
    
    public RosMessage() {
		this(DEFAULT_INITIAL_SIZE);
    }
    
	public RosMessage(int initialSize) {
        if (initialSize <= 0)
            initialSize = DEFAULT_INITIAL_SIZE;
        this.buffer = new byte[initialSize];
    }
    
    private void checkBounds(int arrayLength, int elementSize, int readLength) {
        if ((arrayLength | readLength | (arrayLength - readLength)) < 0)
            throw new ArrayIndexOutOfBoundsException();
        if (position + readLength * elementSize >= buffer.length) {
            int newLength = Math.max(3 * (position + readLength * elementSize) / 2, buffer.length * 2);
            byte[] newBuffer = new byte[newLength];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
    
    private void checkBounds(int elementSize) {
        if (position + elementSize >= buffer.length) {
            int newLength = buffer.length * 2;
            byte[] newBuffer = new byte[newLength];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
    
    public void reset() {
        position = 0;
    }
    
    public void skip(int distance) {
        position += distance;
    }
    
    public void ensureCapacity(int length) {
        if (length > buffer.length) {
            byte[] newBuffer = new byte[length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
    
    public void zero(int length) {
        checkBounds(length);
		Arrays.fill(buffer, position, position + length, (byte) 0);
        position += length;
    }
    
    public byte[] array() {
        return buffer;
    }
    
    public int position() {
        return position;
    }
    
    public int limit() {
        return limit;
    }
    
    public void flip() {
        limit = position;
        position = 0;
    }
    
    public byte getByte() {
        return buffer[position++];
    }
    
    public void getByte(byte[] dst) {
        System.arraycopy(buffer, position, dst, 0, dst.length);
        position += dst.length;
    }
    
    public byte[] getByte(int length) {
        if (length == -1)
            length = getInt();
        byte[] dst = new byte[length];
        getByte(dst);
        return dst;
    }
    
    public char getChar() {
        return (char)((buffer[position++] & 0xff) | (buffer[position++] << 8));
    }
    
    public void getChar(char[] dst) {
        for (int i=0; i<dst.length; i++)
            dst[i] = (char)((buffer[position++] & 0xff) | (buffer[position++] << 8));
    }
    
    public char[] getChar(int length) {
        if (length == -1)
            length = getInt();
        char[] dst = new char[length];
        getChar(dst);
        return dst;
    }
    
    public short getShort() {
        return (short)((buffer[position++] & 0xff) | (buffer[position++] << 8));
    }
    
    public void getShort(short[] dst){
        for (int i=0; i<dst.length; i++)
            dst[i] = (short)((buffer[position++] & 0xff) | (buffer[position++] << 8));
    }
    
    public short[] getShort(int length) {
        if (length == -1)
            length = getInt();
        short[] dst = new short[length];
        getShort(dst);
        return dst;
    }
    
    public int getInt() {
        return (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
    }
    
    public void getInt(int[] dst) {
        for (int i=0; i<dst.length; i++)
            dst[i] = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
    }
    
    public int[] getInt(int length) {
        if (length == -1)
            length = getInt();
        int[] dst = new int[length];
        getInt(dst);
        return dst;
    }
    
    public long getLong() {
        // Doing upper and lower ints separately is faster according to quick benchmarking
        long lower = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
        long upper = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
        return (upper << 32) | (lower & 0xffffffffL);
    }

    public void getLong(long[] dst) {
        for (int i=0; i<dst.length; i++) {
            long lower = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
            long upper = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
            dst[i] = (upper << 32) | (lower & 0xffffffffL);
        }
    }
    
    public long[] getLong(int length) {
        if (length == -1)
            length = getInt();
        long[] dst = new long[length];
        getLong(dst);
        return dst;
    }
    
    public float getFloat() {
        return Float.intBitsToFloat((buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24));
    }
    
    public void getFloat(float[] dst) {
        for (int i=0; i<dst.length; i++)
            dst[i] = Float.intBitsToFloat((buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24));
    }
    
    public float[] getFloat(int length) {
        if (length == -1)
            length = getInt();
        float[] dst = new float[length];
        getFloat(dst);
        return dst;
    }
    
    public double getDouble() {
        long lower = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
        long upper = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
        return Double.longBitsToDouble((upper << 32) | (lower & 0xffffffffL));
    }
    
    public void getDouble(double[] dst) {
        for (int i=0; i<dst.length; i++) {
            long lower = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
            long upper = (buffer[position++] & 0xff) | ((buffer[position++] & 0xff) << 8)  | ((buffer[position++] & 0xff) << 16) | (buffer[position++] << 24);
            dst[i] = Double.longBitsToDouble((upper << 32) | (lower & 0xffffffffL));
        }
    }
    
    public double[] getDouble(int length) {
        if (length == -1)
            length = getInt();
        double[] dst = new double[length];
        getDouble(dst);
        return dst;
    }
    
    public void putByte(byte v) {
        checkBounds(1);
        buffer[position++] = v;
    }
    public void putByte(byte[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new byte[0];
    		} else {
        		src = new byte[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 1, length); // woj bug fix
        } else
            checkBounds(src.length, 1, length);
        System.arraycopy(src, 0, buffer, position, length);
        position += length;
    }
    
    public void putChar(char v) {
        checkBounds(2);
        buffer[position++] = (byte)v;
        buffer[position++] = (byte)(v >> 8);
    }
    
    public void putChar(char[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new char[0];
    		} else {
        		src = new char[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 2, length); // woj bug fix
        } else
            checkBounds(src.length, 2, length);
        for (int i=0; i<length; i++) {
            char v = src[i];
            buffer[position++] = (byte)v;
            buffer[position++] = (byte)(v >> 8);
        }
    }
    
    public void putShort(short v) {
        checkBounds(2);
        buffer[position++] = (byte)v;
        buffer[position++] = (byte)(v >> 8);
    }
    
    public void putShort(short[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new short[0];
    		} else {
        		src = new short[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 2, length); // woj bug fix
        } else
            checkBounds(src.length, 2, length);
        for (int i=0; i<length; i++) {
            short v = src[i];
            buffer[position++] = (byte)v;
            buffer[position++] = (byte)(v >> 8);
        }
    }
    
    public void putInt(int v) {
        checkBounds(4);
        buffer[position++] = (byte)v;
        buffer[position++] = (byte)(v >> 8);
        buffer[position++] = (byte)(v >> 16);
        buffer[position++] = (byte)(v >> 24);
    }
    
    public void putInt(int[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new int[0];
    		} else {
        		src = new int[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 4, length); // woj bug fix
        } else
            checkBounds(src.length, 4, length);
        for (int i=0; i<length; i++) {
            int v = src[i];
            buffer[position++] = (byte)v;
            buffer[position++] = (byte)(v >> 8);
            buffer[position++] = (byte)(v >> 16);
            buffer[position++] = (byte)(v >> 24);
        }
    }
    
    public void putLong(long v) {
        checkBounds(8);
        buffer[position++] = (byte)v;
        buffer[position++] = (byte)(v >> 8);
        buffer[position++] = (byte)(v >> 16);
        buffer[position++] = (byte)(v >> 24);
        buffer[position++] = (byte)(v >> 32);
        buffer[position++] = (byte)(v >> 40);
        buffer[position++] = (byte)(v >> 48);
        buffer[position++] = (byte)(v >> 56);
    }
    
    public void putLong(long[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new long[0];
    		} else {
        		src = new long[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 8, length); // woj bug fix
        } else
            checkBounds(src.length, 8, length);
        for (int i=0; i<length; i++) {
            long v = src[i];
            buffer[position++] = (byte)v;
            buffer[position++] = (byte)(v >> 8);
            buffer[position++] = (byte)(v >> 16);
            buffer[position++] = (byte)(v >> 24);
            buffer[position++] = (byte)(v >> 32);
            buffer[position++] = (byte)(v >> 40);
            buffer[position++] = (byte)(v >> 48);
            buffer[position++] = (byte)(v >> 56);
        }
    }
    
    public void putFloat(float v) {
        checkBounds(4);
        int iv = Float.floatToRawIntBits(v);
        buffer[position++] = (byte)iv;
        buffer[position++] = (byte)(iv >> 8);
        buffer[position++] = (byte)(iv >> 16);
        buffer[position++] = (byte)(iv >> 24);
    }
    
    public void putFloat(float[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new float[0];
    		} else {
        		src = new float[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 4, length); // woj bug fix
        } else
            checkBounds(src.length, 4, length);
        for (int i=0; i<length; i++) {
            int v = Float.floatToRawIntBits(src[i]);
            buffer[position++] = (byte)v;
            buffer[position++] = (byte)(v >> 8);
            buffer[position++] = (byte)(v >> 16);
            buffer[position++] = (byte)(v >> 24);
        }
    }
    
    public void putDouble(double v) {
        checkBounds(8);
        long lv = Double.doubleToRawLongBits(v);
        buffer[position++] = (byte)lv;
        buffer[position++] = (byte)(lv >> 8);
        buffer[position++] = (byte)(lv >> 16);
        buffer[position++] = (byte)(lv >> 24);
        buffer[position++] = (byte)(lv >> 32);
        buffer[position++] = (byte)(lv >> 40);
        buffer[position++] = (byte)(lv >> 48);
        buffer[position++] = (byte)(lv >> 56);
    }
    
    public void putDouble(double[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new double[0];
    		} else {
        		src = new double[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 8, length); // woj bug fix
        } else
            checkBounds(src.length, 8, length);
        for (int i=0; i<length; i++) {
            long v = Double.doubleToRawLongBits(src[i]);
            buffer[position++] = (byte)v;
            buffer[position++] = (byte)(v >> 8);
            buffer[position++] = (byte)(v >> 16);
            buffer[position++] = (byte)(v >> 24);
            buffer[position++] = (byte)(v >> 32);
            buffer[position++] = (byte)(v >> 40);
            buffer[position++] = (byte)(v >> 48);
            buffer[position++] = (byte)(v >> 56);
        }
    }
    
    public Duration getDuration() {
        return Duration.ofSeconds(getInt(), getInt());
    }
    
    public void getDuration(Duration[] dst) {
        for (int i=0; i<dst.length; i++)
            dst[i] = getDuration();
    }
    
    public Duration[] getDuration(int length) {
        if (length == -1)
            length = getInt();
        Duration[] dst = new Duration[length];
        getDuration(dst);
        return dst;
    }
    
    public Instant getInstant() {
        return Instant.ofEpochSecond(getInt(), getInt());
    }
    
    public void getInstant(Instant[] dst) {
        for (int i=0; i<dst.length; i++)
            dst[i] = getInstant();
    }
    
    public Instant[] getInstant(int length) {
        if (length == -1)
            length = getInt();
        Instant[] dst = new Instant[length];
        getInstant(dst);
        return dst;
    }
    
    public void putDuration(Duration duration) {
    	//woj start
    	if(duration == null) {
    		duration = Duration.ZERO;
    	}
    	//woj end
        putInt((int)duration.getSeconds());
        putInt(duration.getNano());
    }
    
    public void putDuration(Duration[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new Duration[0];
    		} else {
        		src = new Duration[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 8, length); // woj bug fix
        } else
            checkBounds(src.length, 8, length);
        for (int i=0; i<length; i++) {
            Duration v = src[i];
            putInt((int)v.getSeconds());
            putInt(v.getNano());
        }
    }
    
    public void putInstant(Instant instant) {
    	//woj start
    	if(instant == null) {
    		instant = Instant.MIN;
    	}
    	//woj end
        putInt((int)instant.getEpochSecond());
        putInt(instant.getNano());
    }
    
    public void putInstant(Instant[] src, int length) {
    	//woj start
    	if(src == null) {
    		if(length==-1) {
        		src = new Instant[0];
    		} else {
        		src = new Instant[length];
    		}
    	}
    	//woj end
        if (length == -1) {
            length = src.length;
            putInt(length);
            checkBounds(src.length, 8, length); // woj bug fix
        } else
            checkBounds(src.length, 8, length);
        for (int i=0; i<length; i++) {
            Instant v = src[i];
            putInt((int)v.getEpochSecond());
            putInt(v.getNano());
        }
    }    
    
    public String getString() {
        return new String(getStringBytes(), RosStringType.STRING_CHARSET);
    }
    
    public void putString(String string) {
    	//woj start
    	if(string == null) {
    		string = "";
    	}
    	//woj end
        putStringBytes(string.getBytes(RosStringType.STRING_CHARSET));
    }
    
    public byte[] getStringBytes() {
        int length = getInt();
        byte[] data = new byte[length];
        getByte(data);
        return data;
    }
    
    public void putStringBytes(byte[] ascii) {
    	//woj start
    	if(ascii == null) {
    		ascii = new byte[0];
    	}
    	//woj end
        putInt(ascii.length);
        putByte(ascii, ascii.length);
    }
    
    
    private static void int32(int value, OutputStream os) throws IOException {
        os.write(value);
        os.write(value >>> 8);
        os.write(value >>> 16);
        os.write(value >>> 24);
    }
    
    private static int int32(InputStream is) throws IOException {
        return (is.read() & 0xff) | ((is.read() & 0xff) << 8) | ((is.read() & 0xff) << 16) | ((is.read() & 0xff) << 24);
    }
    
    public void writeOut(OutputStream os) throws IOException {
        position = 0;
        int32(limit, os);
        os.write(buffer, 0, limit);
    }
    
    public boolean readIn(InputStream is) throws IOException {
        int length = int32(is);
        // TODO check what happens if length < 0?
        if (length < 0)
            return false;
        ensureCapacity(length);
        position = 0;
        while (position < length) {
            int count = is.read(buffer, position, length - position);
            if (count < 0) {
                Log.warn(this, "Unexpected end of input stream while reading ROS message");
                return false;
            }
            position += count;
        }
        flip();
        return true;
    }

}
