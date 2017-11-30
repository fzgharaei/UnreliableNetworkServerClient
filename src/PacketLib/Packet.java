package PacketLib;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Packet represents a simulated network packet.
 * As we don't have unsigned types in Java, we can achieve this by using a larger type.
 */
public class Packet {

    public static final int MIN_LEN = 11;
    public static final int MAX_LEN = 11 + 1024;

    private  int type;
    private int sequenceNumber;
    private final int seqN;
    private final int ackN;
    private final InetAddress peerAddress;
    private final int peerPort;
    private final byte[] payload;


    public Packet(int type, int seqN, int ackN, InetAddress peerAddress, int peerPort, byte[] payload) {
        this.type = type;
        this.seqN = seqN;
        this.ackN = ackN;
        this.sequenceNumber = mkBigSeqN();
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        this.payload = payload;
    }

    public int mkBigSeqN(){
    	System.out.println("in make big seq, seqN "+ seqN);
    	long lseqN = (long)seqN;
    	long lackN = (long)ackN;
//    	return lseqN | (lackN << 16);
    	return seqN | (ackN << 8);
    }
    
    public int getType() {
        return type;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public InetAddress getPeerAddress() {
        return peerAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public byte[] getPayload() {
        return payload;
    }
    public int getAckN(){
    	return this.ackN;
    }
    public int getSeqN(){
    	return this.seqN;
    }
    /**
     * Creates a builder from the current packet.
     * It's used to create another packet by re-using some parts of the current packet.
     */
    public Builder toBuilder(){
        return new Builder()
        		.setType(type)
                .setPeerAddress(peerAddress)
                .setPortNumber(peerPort)
                .setPayload(payload);
    }

    /**
     * Writes a raw presentation of the packet to byte buffer.
     * The order of the buffer should be set as BigEndian.
     */
    private void write(ByteBuffer buf) {
    	buf.put((byte) type);
        buf.putInt((int) sequenceNumber);
        buf.put(peerAddress.getAddress());
        buf.putShort((short) peerPort);
        buf.put(payload);
    }

    /**
     * Create a byte buffer in BigEndian for the packet.
     * The returned buffer is flipped and ready for get operations.
     */
    public ByteBuffer toBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        write(buf);
        buf.flip();
        return buf;
    }

    /**
     * Returns a raw representation of the packet.
     */
    public byte[] toBytes() {
        ByteBuffer buf = toBuffer();
        byte[] raw = new byte[buf.remaining()];
        buf.get(raw);
        return raw;
    }

    /**
     * fromBuffer creates a packet from the given ByteBuffer in BigEndian.
     */
    public static Packet fromBuffer(ByteBuffer buf) throws IOException {
        if (buf.limit() < MIN_LEN || buf.limit() > MAX_LEN) {
            throw new IOException("Invalid length");
        }
//        System.out.println("here3.1");
        Builder builder = new Builder();

        builder.setType(Byte.toUnsignedInt(buf.get()));
        builder.setSequenceNumber(Integer.toUnsignedLong(buf.getInt()));
//        System.out.println("here3.2");
        byte[] host = new byte[]{buf.get(), buf.get(), buf.get(), buf.get()};
        builder.setPeerAddress(Inet4Address.getByAddress(host));
        builder.setPortNumber(Short.toUnsignedInt(buf.getShort()));
//        System.out.println("here3.3");
        byte[] payload = new byte[buf.remaining()];
        buf.get(payload);
        builder.setPayload(payload);
//        System.out.println("here3.4");
        return builder.create();
    }

    /**
     * fromBytes creates a packet from the given array of bytes.
     */
    public static Packet fromBytes(byte[] bytes) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        buf.put(bytes);
        buf.flip();
        return fromBuffer(buf);
    }

    @Override
    public String toString() {
//    	System.out.println("here2.4");
        return String.format("#%d peer=%s:%d, size=%d", sequenceNumber, peerAddress, peerPort, payload.length);
    }

    public static class Builder {
        private int type;
        private long sequenceNumber;
        private InetAddress peerAddress;
        private int portNumber;
        private byte[] payload;
        private int seqN;
        private int ackN;
        private int AckFlag;
        private int SynFlag;
        private int FinFlag;
        private int dataFlag;
        public Builder setType(int type) {
            this.type = type;
            
	       	byte btype = (byte)type;
	       	
	       	  this.AckFlag = ((btype & 0x1) != 0)?1:0; 
	          this.SynFlag = ((btype & 0x4) != 0)?1:0;
	          this.FinFlag = ((btype & 0x10) != 0)?1:0;
	          this.dataFlag = ((btype & 0x40) != 0)?1:0;

            
            return this;
        }
        public Builder setType(){
        	this.type = AckFlag | (SynFlag << 2) | (FinFlag << 4) | (dataFlag << 6);
//        	System.out.println("in setType()" + type);
        	return this;
        }
        public Builder flipAcknSeq(){
        	getackN();
        	getseqN();
        	int temp = ackN;
        	this.ackN = seqN;
        	this.seqN = temp;
        	return this;
        }
        public Builder setSequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            System.out.println("Big seq "+this.sequenceNumber);
            byte[] data = new byte[2]; // <- assuming "in" value in 0..65535 range and we can use 2 bytes only

            data[0] = (byte)(sequenceNumber & 0xFF);
            data[1] = (byte)((sequenceNumber >> 8) & 0xFF);

            this.ackN = data[1] >= 0 ? data[1] : 256 + data[1];
            this.seqN = data[0] >= 0 ? data[0] : 256 + data[0];
//            this.seqN = ((int) (sequenceNumber << 16))/);
//            this.ackN =  (int) (this.sequenceNumber >> 16);
//            System.out.println("in s ack N "+this.ackN);
//            System.out.println("in s seq N "+this.seqN);
            return this;
        }

        public Builder setPeerAddress(InetAddress peerAddress) {
            this.peerAddress = peerAddress;
            return this;
        }

        public Builder setPortNumber(int portNumber) {
            this.portNumber = portNumber;
            return this;
        }

        public Builder setPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }
        public Builder setSeqN(int SeqN){
        	this.seqN = SeqN;
        	return this;
        }
        public Builder setAckN(int AckN){
        	this.ackN = AckN;
        	return this;
        }
        
        public int getseqN(){
        	this.seqN = (int) (sequenceNumber);
        	return this.seqN;
        }
        
        public int getackN(){
        	this.ackN =  (int) (sequenceNumber >> 16);
        	return this.ackN;
        }
       
        public Builder setAckFlag(boolean flag){
        	if(flag) this.AckFlag = 1;
        	else this.AckFlag = 0;
        	return this;
        }
        public Builder setSynFlag(boolean flag){
        	if(flag) this.SynFlag = 1;
        	else this.SynFlag = 0;
        	return this;
        }
        public Builder setFinFlag(boolean flag){
        	if(flag) this.FinFlag = 1;
        	else this.FinFlag = 0;
        	return this;
        }
        public Builder setDataFlag(boolean flag){
        	if(flag) this.dataFlag = 1;
        	else this.dataFlag = 0;
        	return this;
        }
        
        public boolean hasAckFlag(){
        	return this.AckFlag == 1;
        }
        public boolean hasSynFlag(){
        	return this.SynFlag == 1;
        }
        public boolean hasDataFlag(){
        	return this.dataFlag == 1;
        }
        public boolean hasFinFlag(){
        	return this.FinFlag == 1;
        }
        public Packet create() {
//        	System.out.println("ack N "+this.ackN);
//            System.out.println("seq N "+this.seqN);
            
            return new Packet(type, seqN, ackN, peerAddress, portNumber, payload);
        }
    }
}
