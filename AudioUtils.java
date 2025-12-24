import java.io.*;
import javax.sound.sampled.*;

public class AudioUtils {
    private static AudioFormat getFormat() {
        return new AudioFormat(16000, 16, 1, true, true); // 采样率16k, 16位, 单声道
    }

    // 录音并返回字节数组
    public static byte[] recordAudio(int durationMillis) throws Exception {
        AudioFormat format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < durationMillis) {
            int count = line.read(buffer, 0, buffer.length);
            if (count > 0) out.write(buffer, 0, count);
        }

        line.stop();
        line.close();
        return out.toByteArray();
    }

    public static void playAudio(byte[] audioData) {
        new Thread(() -> {
            try {
                AudioFormat format = getFormat();
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(audioData, 0, audioData.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}