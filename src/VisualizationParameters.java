public class VisualizationParameters {

	// sometimes the calculation needs a bit more
	// information than the only samples that
	// will be resulted
	// because of the envelope settings

	// these values are in samples
	public int start;// where does the calculation start
	public int end;// where does the calculation end
	public int viewStart;// where does the view start (after the attack)
	public int viewEnd;// where does the view end (before the release)
	public float oneBarDuration;
	// returns the number of samples contained within one bar
	public int viewLength;// size of the displayed area
	public int length;// length of the whole waveform
	public int sampleLength;
	// Length of the whole sample, including non used parts

	// these values are in pixels, for the visualization
	public int startPx;// where does the calculation start
	public int endPx;// where does the calculation end
	public int viewStartPx;// where does the view start (after the attack)
	public int viewEndPx;// where does the view end (before the release)
	public int viewLengthPx;// pixel size of the waveform
	public int lengthPx;// length of the whole virtual waveform

	// these are other values
	public boolean left;// is true is the channel to be processed is the left

	public VisualizationParameters(int viewStart, int viewEnd,
			int waveformWidth, int attack, int release, boolean left,
			int sampleLength) {
		this.viewStart = viewStart;
		this.viewEnd = viewEnd;
		this.sampleLength = sampleLength;
		this.left = left;
		this.viewLength = viewEnd - viewStart;
		int margin = Math.max(attack, release);
		this.start = Math.max(viewStart - margin, 0);
		this.end = Math.min(viewEnd + margin, sampleLength);
		this.length = end - start;
		this.viewLengthPx = waveformWidth;
		this.oneBarDuration = viewLength / viewLengthPx;
		this.startPx = (int) (start / oneBarDuration);
		this.endPx = (int) (end / oneBarDuration);
		this.viewStartPx = (int) (viewStart / oneBarDuration);
		this.viewEndPx = viewStartPx + viewLengthPx;
		this.lengthPx = endPx - startPx;
	}
}
