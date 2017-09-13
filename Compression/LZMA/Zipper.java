package Compression.LZMA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Zipper {

    /**
     * 压缩函数
     *
     * @param inputStream:  要压缩的数据，如果源数据是byte[]，那么在外层要ByteArrayInputStream ins = new ByteArrayInputStream(yourbytes[], offset, effective_length)
     * @param outputStream: 压缩好的数据，可以通过outputs_bytes.toBytes变成byte数组
     * @param len:          inputs_bytes中的有效数据长度，用于写入压缩好的数据开头，解压时会先读出来这段大小，以便知道要解压的数据大小。注意数据类型
     */
    public void encode(ByteArrayInputStream inputStream, ByteArrayOutputStream outputStream, long len) throws Exception {
        Compression.LZMA.Encoder encoder = new Compression.LZMA.Encoder();
        //设置压缩参数。默认即可
        if (!encoder.SetAlgorithm(2))
            throw new Exception("Incorrect compression mode");
        if (!encoder.SetDictionarySize(1 << 23))
            throw new Exception("Incorrect dictionary size");
        if (!encoder.SetNumFastBytes(128))
            throw new Exception("Incorrect -fb value");
        if (!encoder.SetMatchFinder(1))
            throw new Exception("Incorrect -mf value");
        if (!encoder.SetLcLpPb(3, 0, 2))
            throw new Exception("Incorrect -lc or -lp or -pb value");
        encoder.SetEndMarkerMode(false);
        //首先会有5bytes的参数信息被写入
        encoder.WriteCoderProperties(outputStream);
        //接下来8bytes是要压缩的数据的长度，在解压时将被读取。注意这里len是long类型，如果是int，则最大可表示2GB的数据，因此采用long，但是里面每个byte在存储的时候，使用int即可。
        for (int j = 0; j < 8; j++)
            //无符号右移
            outputStream.write((int) (len >>> (8 * j)) & 0xFF);
        // inSize、outSize以及progress参数可以这样设置不用理会
        encoder.Code(inputStream, outputStream, -1, -1, null);
    }

    /**
     * 解压函数
     *
     * @param inputStream:  要解压的数据。要求同encode。
     * @param outputStream: 解压获得的数据。
     */
    public void decode(ByteArrayInputStream inputStream, ByteArrayOutputStream outputStream) throws Exception {
        Compression.LZMA.Decoder decoder = new Compression.LZMA.Decoder();
        //先读取5bytes设置
        int propertiesSize = 5;
        byte[] properties = new byte[propertiesSize];
        if (inputStream.read(properties, 0, propertiesSize) != propertiesSize)
            throw new Exception("input .lzma file is too short");
        if (!decoder.SetDecoderProperties(properties))
            throw new Exception("Incorrect stream properties");
        long outSize = 0;
        // 读取8bytes的要解压出来的文件长度（单位bytes）
        for (int j = 0; j < 8; j++) {
            int v = inputStream.read();
            if (v < 0)
                throw new Exception("Can't read stream size");
            outSize |= ((long) v) << (8 * j);
        }
        if (!decoder.Code(inputStream, outputStream, outSize)) {
            throw new Exception("Error in data stream");
        }
        //@todo： 这里不应该只是打印，应该throw error
        if (outputStream.size() != outSize) {
            System.out.println("实际解压大小和记录不同 outputstream.size " + outputStream.size() + "\toutsize" + outSize);
        }
    }

}
