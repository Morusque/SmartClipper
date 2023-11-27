
import java.io.*;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public double[][] loadFile(String url) {

  AudioFormat format;
  int audioDataLength;
  boolean isBigEndian;
  int bytePerSample;
  float maxSampleValue;
  int nbChannels;
  double[][] nSample = null;

  try {

    // Read the audio file
    AudioInputStream ais = AudioSystem.getAudioInputStream(new File(url));
    format = ais.getFormat();

    // Get the raw audio data
    int frameLength = (int)ais.getFrameLength();
    byte[] audioData = new byte[frameLength * format.getFrameSize()];
    audioDataLength = audioData.length;
    ais.read(audioData);

    nbChannels = format.getChannels();

    int sampleSizeInBits = format.getSampleSizeInBits();
    isBigEndian = format.isBigEndian();
    bytePerSample = 1;

    if (sampleSizeInBits == 8) {
      // For 8-bit audio, values range from -128 to 127
      maxSampleValue = 127f;
      bytePerSample = 1;
    } else if (sampleSizeInBits == 16) {
      // For 16-bit audio, values range from -32768 to 32767
      maxSampleValue = 32767f;
      bytePerSample = 2;
    } else if (sampleSizeInBits == 24) {
      // For 24-bit audio, values range from -8388608 to 8388607
      maxSampleValue = 8388607f;
      bytePerSample = 3;
    } else {
      throw new IllegalArgumentException("Unsupported bit depth: " + sampleSizeInBits);
    }

    nSample = new double[nbChannels][audioData.length/(bytePerSample*nbChannels)];
    for (int i = 0; i < audioData.length; i += bytePerSample*nbChannels) {
      for (int c = 0; c < nbChannels; c++) {
        int offset = i + c*bytePerSample;
        double sampleValue = 0.0;
        if (bytePerSample == 1) {
          sampleValue = (double)audioData[offset] / maxSampleValue;
        } else if (bytePerSample == 2) {
          short sample = ByteBuffer.wrap(audioData, offset, 2).order(isBigEndian?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN).getShort();
          sampleValue = (double)sample / maxSampleValue;
        } else if (bytePerSample == 3) {
          int sample = ByteBuffer.wrap(audioData, offset, 3).order(isBigEndian?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN).getInt();
          sampleValue = (double)sample / maxSampleValue;
        }
        nSample[c][i/(bytePerSample*nbChannels)] = sampleValue;
      }
    }

    ais.close();

    /*
    println("nbChannels : "+nbChannels);
     println("nSample : "+nSample[0].length);
     println("bytePerSample : "+bytePerSample);
     println("audioData.length : "+audioData.length);
     */
  }
  catch (Exception e) {
    println(e);
  }

  return nSample;
}

public void exportSample(double[][] nSampleProcessed, String url) {
  try {
    boolean isBigEndian = false;
    int bytePerSample = 2;
    float maxSampleValue = 32767f;
    int nbChannels = nSampleProcessed.length;
    int audioDataLength = nSampleProcessed[0].length;

    // Specify the audio format
    AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, nbChannels, nbChannels * 2, 44100f, false);

    // Calculate the total size required for all channels and samples
    int totalSize = audioDataLength * nbChannels * bytePerSample;
    // Allocate the byteData array with the total size
    byte[] byteData = new byte[totalSize];
    println(byteData.length);
    println("---");

    // For each sample index i
    for (int i = 0; i < nSampleProcessed[0].length; i++) {
      // For each channel c
      for (int c = 0; c < nbChannels; c++) {
        int sampleAsInt = (int)(nSampleProcessed[nbChannels-c-1][i] * maxSampleValue);// swap channels to fix a problem but not sure where the problem comes from

        byte[] sampleBytes;
        switch (bytePerSample) {
        case 1:
          sampleBytes = new byte[] {(byte) sampleAsInt};
          break;
        case 2:
          sampleBytes = ByteBuffer.allocate(2).order(isBigEndian?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN).putShort((short) sampleAsInt).array();
          break;
        case 3:
          sampleBytes = new byte[3];
          sampleBytes[0] = (byte) (sampleAsInt & 0xFF);
          sampleBytes[1] = (byte) ((sampleAsInt >> 8) & 0xFF);
          sampleBytes[2] = (byte) ((sampleAsInt >> 16) & 0xFF);
          break;
        default:
          throw new IllegalArgumentException("Unsupported byte depth: " + bytePerSample);
        }

        // Determine where this sample's bytes should go in byteData
        int byteDataIndex = i * nbChannels * bytePerSample + c * bytePerSample;

        // Copy this sample's bytes to byteData
        System.arraycopy(sampleBytes, 0, byteData, byteDataIndex, bytePerSample);
      }
    }
    
    // Create a new AudioInputStream from the byte data
    ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
    // AudioInputStream outputAis = new AudioInputStream(bais, format, audioDataLength / format.getFrameSize()); // old incorrect line
    AudioInputStream outputAis = new AudioInputStream(bais, format, totalSize);

    // Write the AudioInputStream to a file
    AudioSystem.write(outputAis, AudioFileFormat.Type.WAVE, new File(url));
  }
  catch(Exception e) {
    println(e);
  }
}
