import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import processing.core.PApplet;

public class Window extends Canvas implements ComponentListener {

  smartClipper applet;

  JFrame container;

  private static final long serialVersionUID = 1L;
  private BufferStrategy strategy;
  public Graphics2D g;

  int width = 500;
  int height = 600;

  boolean running = true;

  Button[] buttons = new Button[3];
  Slider[] sliders = new Slider[5];
  Histogram histogram;
  Waveform gain;
  Waveform maxes;
  Waveform overview;

  CompressionParameters cParam = new CompressionParameters();

  boolean mousePressed = false;
  int mouseX = 0;
  int mouseY = 0;
  int mouseButton = 0;

  private int sampleLength = 0;
  private int frameSize;
  private int nbChannels;

  byte[] buffer;

  double[][] samples;

  @SuppressWarnings("unused")
    private boolean loading = false;

  // WavFile currentWavFile;

  public Window(smartClipper applet) {
    this.applet = applet;
    container = new JFrame("Smart clipper");
    JPanel panel = (JPanel) container.getContentPane();
    panel.setPreferredSize(new Dimension(width, height));
    panel.setLayout(null);
    setBounds(0, 0, width, height);
    panel.add(this);
    setIgnoreRepaint(true);
    container.pack();
    container.setResizable(true);
    container.setVisible(true);
    container.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    }
    );
    requestFocus();
    createBufferStrategy(2);
    strategy = getBufferStrategy();
    MouseEvents mouseEvents = new MouseEvents();
    addMouseListener(mouseEvents);
    addMouseMotionListener(mouseEvents);
    container.addComponentListener(this);
    init();
    display();
  }

  private void init() {
    int wU = width / 500;
    int hU = height / 600;
    buttons[0] = new Button(this, 10 * wU, 10 * hU, 50 * wU, 20 * hU,
      "LOAD");
    buttons[1] = new Button(this, 70 * wU, 10 * hU, 50 * wU, 20 * hU,
      "SAVE");
    buttons[2] = new Button(this, 130 * wU, 10 * hU, 50 * wU, 20 * hU,
      "LIST");
    histogram = new Histogram(this, 10 * wU, 40 * hU, 100 * wU, 410 * hU);
    gain = new Waveform(this, 120 * wU, 40 * hU, 350 * wU, 200 * hU);
    maxes = new Waveform(this, 120 * wU, 250 * hU, 350 * wU, 200 * hU);
    overview = new Waveform(this, 210 * wU, 460 * hU, 260 * wU, 50 * hU);
    // compression sliders
    sliders[0] = new Slider(this, 10 * wU, 460 * hU, 50 * wU, 50 * hU,
      false, Color.red);
    sliders[1] = new Slider(this, 70 * wU, 460 * hU, 30 * wU, 50 * hU,
      true, Color.green);
    sliders[2] = new Slider(this, 110 * wU, 460 * hU, 30 * wU, 50 * hU,
      true, Color.red);
    sliders[3] = new Slider(this, 150 * wU, 460 * hU, 50 * wU, 50 * hU,
      false, Color.green);
    sliders[0].setValue(0);
    sliders[1].setValue(0.5f);
    sliders[2].setValue(0.5f);
    sliders[3].setValue(0);
    sliders[0].setDialogA("attack (in samples)");
    sliders[1].setDialogA("low gain ratio (0-1)");
    sliders[2].setDialogA("high gain ratio (0-1)");
    sliders[3].setDialogA("release (in samples)");
    sliders[0].setRange(0, 2000);
    sliders[3].setRange(0, 8000);
    sliders[4] = new Slider(this, 190 * wU, 10 * hU, 50 * wU, 20 * hU,
      false, Color.green);
    sliders[4].setRange(0, 3);
  }

  private void run() {
    while (running) {
      timer();
    }
  }

  private void mouse() {
    if (mousePressed) {
      histogram.click(mouseX, mouseY);
      if (overview.click(mouseX, mouseY, mouseButton)) {
        compute();
      }
      for (int i = 0; i < sliders.length; i++) {
        sliders[i].click(mouseX, mouseY);
      }
    }
  }

  public void updateParameters() {
    // puts the sliders values
    // in the compressionParameters instance
    cParam.setAttack((int) sliders[0].getValue());
    cParam.setExpand(sliders[1].getValue());
    cParam.setCompress(sliders[2].getValue());
    cParam.setRelease((int) sliders[3].getValue());
    cParam.setLimit(histogram.getLimit());
    cParam.setCompType((int) sliders[4].getValue());
  }

  private void load()
  {
    // loads an audio file
    Frame frame = new Frame();
    FileDialog fileDialog = new FileDialog(frame, "open", FileDialog.LOAD);
    fileDialog.setVisible(true);

    // if the directory exists
    if (fileDialog.getDirectory() != null)
    {
      String directoryPath = fileDialog.getDirectory();
      String fileName = fileDialog.getFile();
      String fullPath = directoryPath + fileName;
      samples = applet.loadFile(fullPath);

      nbChannels = samples.length;
      sampleLength = samples[0].length;

      /*
      AudioInputStream inputStream = null;
       buffer = null;
       try
       {
       File inputFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
       // currentWavFile = WavFile.readFromFilePath(inputFile.getAbsolutePath());
       inputStream = AudioSystem.getAudioInputStream(inputFile);
       frameSize = inputStream.getFormat().getFrameSize();
       nbChannels = inputStream.getFormat().getChannels();
       buffer = new byte[inputStream.available()];
       sampleLength = buffer.length / frameSize;
       inputStream.read(buffer);
       }
       catch (Exception e)
       {
       e.printStackTrace();
       }
       */

      overview.setTokens(0, sampleLength);
      overview.setCompType(3);
      computeOverview();
      compute();
      display();
    }
  }

  public double streamSample(int i, boolean left) {
    if (left) return samples[0][i];
    else return samples[1%nbChannels][i];
  }

  private void computeOverview()
  {
    VisualizationParameters vParam = new VisualizationParameters(0, sampleLength, overview.getW(), 0, 0, false, sampleLength);
    ProcessingResult originalArray = Processing.generateOriginal(this, vParam, cParam);

    //crop the arrays to the visible part only
    overview.setAverageFrom(originalArray.getAverage());
  }

  private int unsignedByte(int b)
  {
    int result = b;
    if (result < 0)
      result = 256 + b;
    return result;
  }

  @SuppressWarnings("unused")
    private int unsignedByte(byte b)
  {
    int result = b;
    if (result < 0)
      result = 256 + b;
    return result;
  }

  private void timer()
  {
    // takes care of all the timing stuff
    try
    {
      Thread.sleep(10);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public void display()
  {
    // displays the interface
    g = (Graphics2D) strategy.getDrawGraphics();
    // clears the screen
    g.setColor(new Color(0xAAAAAA));
    g.fillRect(0, 0, width, height);
    displayElements();
    // puts all these stuff on the screen
    g.dispose();
    strategy.show();
  }

  private void displayElements()
  {
    for (int i = 0; i < buttons.length; i++)
    {
      buttons[i].display();
    }
    for (int i = 0; i < sliders.length; i++)
    {
      sliders[i].display();
    }
    histogram.display();
    gain.displayGain();
    maxes.displayMaxes();
    gain.displayCompression();
    overview.displayGain();
    overview.displayTokens();
  }

  /*
  public static void main(String argv[])
   {
   Window w = new Window();
   w.run();
   }
   */

  public void compute()
  {
    loading = true;
    updateParameters();
    // if the sample has a length
    if (sampleLength > 0)
    {
      histogram.compute();

      // since the gain and maxes waveforms have the same width, create only one single computation for both
      // using only gain to create a set of visualization parameters all the values below are in samples
      int viewStart = overview.getTokenSamples()[0];
      int viewEnd = overview.getTokenSamples()[1];
      int waveformWidth = gain.getW();
      int attack = cParam.getAttack();
      int release = cParam.getRelease();
      boolean left = false;
      VisualizationParameters vParam = new VisualizationParameters(viewStart, viewEnd, waveformWidth, attack, release, left, sampleLength);
      ProcessingResult originalArray = Processing.generateOriginal(this, vParam, cParam);
      ProcessingResult compressed = Processing.generateCompressed(originalArray, cParam, vParam);

      // crop the arrays to the visible part only
      compressed = Processing.cropToVisible(compressed, vParam);
      gain.setAverageFrom(compressed.getAverage());
      gain.setOverFrom(compressed.getOver());
      gain.setEnvelope(attack, release, vParam.oneBarDuration);
      maxes.setMaxFrom(compressed.getMax());
      maxes.setOverFrom(compressed.getOver());
      maxes.setEnvelope(attack, release, vParam.oneBarDuration);
    }
    loading = false;
  }

  private void save()
  {
    Frame frame = new Frame();
    FileDialog fileDialog = new FileDialog(frame, "save", FileDialog.SAVE);
    fileDialog.setVisible(true);
    File destination = new File(fileDialog.getDirectory() + fileDialog.getFile());

    if (fileDialog.getDirectory() != null)
    {

      try
      {

        // destination.createNewFile();
        // FileOutputStream destinationFile = new FileOutputStream(destination);

        //writeHeader(destinationFile, sampleLength);

        double[][] finalSample = new double[nbChannels][sampleLength];
        int finalI = 0;

        int bufferLength = 1000;
        for (int i = 0; i < sampleLength; i += bufferLength)
        {
          // System.out.println(i + " / " + sampleLength);
          bufferLength = Math.min(sampleLength - i, bufferLength);

          // left
          VisualizationParameters vParam = new VisualizationParameters(i, i + bufferLength, bufferLength, cParam.getAttack(), cParam.getRelease(), false, sampleLength);
          ProcessingResult original = Processing.generateOriginal(this, vParam, cParam);
          ProcessingResult compressed = Processing.generateCompressed(original, cParam, vParam);
          double[] valuesL = Processing.cropToVisible(compressed, vParam).getReal();

          // right
          double[] valuesR = null;
          if (nbChannels>1) {
            vParam = new VisualizationParameters(i, i + bufferLength, bufferLength, cParam.getAttack(), cParam.getRelease(), true, sampleLength);
            original = Processing.generateOriginal(this, vParam, cParam);
            compressed = Processing.generateCompressed(original, cParam, vParam);
            valuesR = Processing.cropToVisible(compressed, vParam).getReal();
          }

          for (int i2 = 0; i2 < bufferLength; i2++)
          {
            double[] stereoValue = null;
            if (nbChannels == 1) {
              stereoValue = new double[1];
              stereoValue[0] = valuesL[i2];
              finalSample[0][finalI++] = valuesL[i2];  // Increment finalI here
            }
            if (nbChannels == 2) {
              stereoValue = new double[2];
              stereoValue[0] = valuesL[i2];
              stereoValue[1] = valuesR[i2];
              finalSample[0][finalI] = valuesL[i2];
              finalSample[1][finalI++] = valuesR[i2];  // Increment finalI here
            }
            //currentByte = valueToFrame(stereoValue); This line might not be needed
            // destinationFile.write(currentByte); This is commented out, hence, the above line might not be necessary
          }
        }

        applet.exportSample(finalSample, destination.getAbsolutePath());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  class ReverseDataOutputStream extends DataOutputStream
  {
    public ReverseDataOutputStream(OutputStream out)
    {
      super(out);
    }

    public void writeReverseInt(int value) throws Exception
    {
      writeInt(Integer.reverseBytes(value));
    }
    public void writeReverseShort(short value) throws Exception
    {
      writeShort(Short.reverseBytes(value));
    }
  }

  private void writeHeader(FileOutputStream destinationFile, int count_of_samples) throws Exception
  {		
    int header_length = 8; //8 are missing out of 16, at least thats the spec from wikipedia (RIFF wav format)
    int fmt_length = 16;

    short 	format 			= 1; //PCM 0x001
    int 	sample_rate 	= 44100;						
    short 	channels 		= (short) nbChannels; 					
    short 	bits_per_sample = 16;							
    short 	block_align 	= (short) ( channels * (bits_per_sample / 8));
    int 	bytes_per_second = sample_rate * block_align;

    int data_length = count_of_samples * block_align;		// is this correct? please check!

    ReverseDataOutputStream rdos = new ReverseDataOutputStream(destinationFile);		

    rdos.write				("RIFF".getBytes()); 						//4 bytes as array	

    //file length: audio data + 36 for the rest of the header
    rdos.writeReverseInt	(header_length + fmt_length + data_length);	//4 bytes int

    rdos.write				("WAVE".getBytes()); 						//4 bytes as array
    rdos.write				("fmt ".getBytes()); 						//4 bytes as array

    rdos.writeReverseInt	(fmt_length);								//4 bytes int
    rdos.writeReverseShort	(format); 									//2 bytes
    rdos.writeReverseShort	(channels); 								//2 bytes
    rdos.writeReverseInt	(sample_rate);								//4 bytes int
    rdos.writeReverseInt	(bytes_per_second);							//4 bytes int
    rdos.writeReverseShort	(block_align);								//2 bytes
    rdos.writeReverseShort	(bits_per_sample);							//2 bytes

    rdos.write				("data".getBytes());						//4 bytes as array
    rdos.writeReverseInt	(data_length);								//4 bytes int
  }

  /* someone else in the web did use this code:
   	
   	    float thisSample = audioBuffer[a];
   	    thisSample = Math.min(1.0F, Math.max(-1.0F, thisSample));
   	    int intSample = Math.round(thisSample * 32767.0F);
   	    byte high = (byte) ((intSample >> 8) & 0xFF);
   	    byte low = (byte) (intSample & 0xFF);
   	
   	 */

  private byte[] valueToFrame(double[] sample)
  {
    // saves the sample as: little-endian, 16 bit, signed
    byte[] result = new byte[2 * nbChannels];

    for (int i=0; i<sample.length; i++) {
      sample[i] = Math.min(1.0, Math.max(-1.0, sample[i]));
      int intSample = (int) Math.round(sample[i] * 32767.0); //math round returns long when rounding from double. but we clamped to -32k to +32, which definitely fits in 32bit int!
      result[i*2+1] = (byte) ((intSample >> 8) & 0xFF); 	//high byte
      result[i*2+0] = (byte) (intSample & 0xFF);	//low byte
    }

    return result;
  }

  public class MouseEvents extends MouseInputAdapter {

    public void mouseDragged(MouseEvent e) {
      update(e);
      mouse();
      display();
    }

    public void mousePressed(MouseEvent e) {
      mouseButton = e.getButton();
      mousePressed = true;
      mouseX = e.getX();
      mouseY = e.getY();
      mouse();
      if (e.getButton() == 1) {
        if (buttons[0].isAt(mouseX, mouseY))
          load();
        if (buttons[1].isAt(mouseX, mouseY))
          save();
      }
      if (e.getButton() == 3) {
        for (int i = 0; i < sliders.length; i++) {
          sliders[i].rightClick(mouseX, mouseY);
        }
      }
    }

    public void mouseReleased(MouseEvent e) {
      mousePressed = false;
      mouse();
      compute();
      display();
    }

    void update(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();
    }
  }

  public int getSampleLength() {
    return sampleLength;
  }

  /*
  public float streamSample(int i, boolean left)
   {
   
   float value = 0;
   for (int i2 = 0; i2 < frameSize; i2++)
   {
   boolean goodChannel = false;// is true if it's the requested channel
   
   if (nbChannels == 1)
   {
   // true if there is only one channel
   goodChannel = true;
   } else
   {
   if (left ^ i2 < frameSize / nbChannels)
   goodChannel = true;
   }
   
   // if the current channel is the one requested
   if (goodChannel)
   {
   int thisByte = buffer[(i * frameSize) + i2];
   if (frameSize / nbChannels == 1)
   {
   thisByte = unsignedByte(thisByte);
   value += thisByte;
   } else if (frameSize / nbChannels > 1)
   {
   if (i2 % (frameSize / nbChannels) < frameSize / nbChannels - 1)
   thisByte = unsignedByte(thisByte);
   
   value += Math.pow(0x0100, i2) * thisByte;
   // TODO handle big-endian samples
   }
   }
   }
   if (frameSize / nbChannels == 1)
   {
   value = (float) value / 128 - 1;
   } else if (frameSize / nbChannels == 2)
   {
   value = (float) value / 32768;
   }
   return value;
   }
   */

  public void componentHidden(ComponentEvent e) {
  }

  public void componentMoved(ComponentEvent e) {
    display();
  }

  public void componentResized(ComponentEvent e) {
    width = e.getComponent().getWidth();
    height = e.getComponent().getHeight();
    JPanel panel = (JPanel) container.getContentPane();
    panel.setPreferredSize(new Dimension(width, height));
    panel.setLayout(null);
    setBounds(0, 0, width, height);
    panel.add(this);
    int wU = width / 50;
    int hU = height / 60;
    buttons[0].setPosition(1 * wU, 1 * hU, 5 * wU, 2 * hU);
    buttons[1].setPosition(7 * wU, 1 * hU, 5 * wU, 2 * hU);
    buttons[2].setPosition(13 * wU, 1 * hU, 5 * wU, 2 * hU);
    histogram.setPosition(1 * wU, 4 * hU, 10 * wU, 41 * hU);
    gain.setPosition(12 * wU, 4 * hU, 35 * wU, 20 * hU);
    maxes.setPosition(12 * wU, 25 * hU, 35 * wU, 20 * hU);
    overview.setPosition(21 * wU, 46 * hU, 26 * wU, 5 * hU);
    sliders[0].setPosition(1 * wU, 46 * hU, 5 * wU, 5 * hU);
    sliders[1].setPosition(7 * wU, 46 * hU, 3 * wU, 5 * hU);
    sliders[2].setPosition(11 * wU, 46 * hU, 3 * wU, 5 * hU);
    sliders[3].setPosition(15 * wU, 46 * hU, 5 * wU, 5 * hU);
    sliders[4].setPosition(19 * wU, 1 * hU, 5 * wU, 2 * hU);
    compute();
    display();
  }

  public void componentShown(ComponentEvent e) {
  }
}
