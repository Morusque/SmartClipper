class Processing {

	public static ProcessingResult generateOriginal(Window parent,
			VisualizationParameters vParam, CompressionParameters cParam) {
		// this method creates an array of the non compressed values
		// it crops it and scales it
		// according to parameters in the param instance
		float oneBarDuration = vParam.oneBarDuration;
		boolean left = vParam.left;
		int start = vParam.start;
		// including scaling and attack and release exceeding
		int lengthPx = vParam.lengthPx;
		double limit = cParam.getLimit();
		// creates arrays to be filled with values
		float max[][] = new float[lengthPx][2];// peaks -1 to 1
		float average[] = new float[lengthPx];// gain 0 to 1
		double real[] = new double[lengthPx];// actual waveform -1 to 1
		boolean over[] = new boolean[lengthPx];// peaks (true if overdriving)
		// gets the peaks
		for (int i = 0; i < lengthPx; i++) {
			max[i][0] = 1;// peaks under 0
			max[i][1] = -1;// peaks over 0
			for (int i2 = 0; i2 < oneBarDuration; i2++) {
				// checks if it's still in the bounds of the sample
				if (start + i * oneBarDuration + i2 < parent.getSampleLength()) {
					max[i][0] = Math.min((parent.streamSample((int) (start + i
							* oneBarDuration + i2), left)), max[i][0]);
					max[i][1] = Math.max((parent.streamSample((int) (start + i
							* oneBarDuration + i2), left)), max[i][1]);
				}
			}
		}
		// gets the average and real values
		for (int i = 0; i < lengthPx; i++) {
			average[i] = 0;
			for (int i2 = 0; i2 < oneBarDuration; i2++) {
				if (start + i * oneBarDuration + i2 < parent.getSampleLength()) {
					double thisSample = parent.streamSample((int) (i
							* oneBarDuration + start + i2), left);
					average[i] += Math.abs(thisSample);
					// only sets the real values if it's note scaled
					if (oneBarDuration == 1) {
						real[i] = thisSample;
					}
				}
			}
			average[i] = average[i] / oneBarDuration;
		}
		// then spot the threshold sections
		for (int i = 0; i < lengthPx; i++) {
			over[i] = false;
			if ((float) Math.abs(max[i][0]) > limit
					|| (float) Math.abs(max[i][1]) > limit) {
				over[i] = true;
			}
		}
		// creates an instance to store the results
		ProcessingResult result = new ProcessingResult(average, max, over, real);
		return result;
	}

	public static ProcessingResult generateCompressed(
			ProcessingResult originalArray, CompressionParameters cParam,
			VisualizationParameters vParam) {
		float max[][] = originalArray.getMax();// peaks -1 to 1
		float average[] = originalArray.getAverage();// gain 0 to 1
		double real[] = originalArray.getReal();// actual waveform -1 to 1
		boolean over[] = originalArray.getOver();// peaks (true if overdriving)
		int attack = (int) (cParam.getAttack() / vParam.oneBarDuration);
		int release = (int) (cParam.getRelease() / vParam.oneBarDuration);
		float compress = cParam.getCompress();
		float expand = cParam.getExpand();
		double limit = cParam.getLimit();// threshold from 0 to 1
		// including scaling and attack and release exceedings
		int length = vParam.lengthPx;
		int compType = cParam.getCompType();
		for (int i = 0; i < length; i++) {
			// then apply the compression
			if (compType == 0) {
				// brutal hardcore compression
				int nextOverS = -1;// next overdriving sample
				for (int i2 = attack; i2 > 0; i2--) {
					// if inside the bounds
					if (i + i2 >= 0 && i + i2 < length) {
						if (over[i + i2]) {
							nextOverS = i + i2;
						}
					}
				}
				int previousOverS = -1;// previous overdriving sample
				for (int i2 = -release; i2 < 0; i2++) {
					// if inside the bounds
					if (i + i2 >= 0 && i + i2 < length) {
						if (over[i + i2]) {
							previousOverS = i + i2;
						}
					}
				}
				// compression based on the attack
				float attAmount, relAmount;
				if (over[i]) {
					attAmount = compress;
					relAmount = compress;
				} else {
					attAmount = expand;
					relAmount = expand;
				}
				if (nextOverS != -1 && attack > 0) {
					attAmount = compress
							* constrain((float) (i + attack - nextOverS)
									/ attack, 0, 1) + expand
							* constrain((float) (nextOverS - i) / attack, 0, 1);
				}
				// compression based on the release
				if (previousOverS != -1 && release > 0) {
					relAmount = compress
							* constrain((float) (previousOverS + release - i)
									/ release, 0, 1)
							+ expand
							* constrain((float) (i - previousOverS) / release,
									0, 1);
				}
				// take either one or average of both envelopes
				float compAmount;
				if (nextOverS != -1 && attack > 0) {
					if (previousOverS != -1 && release > 0) {
						compAmount = (attAmount + relAmount) / 2;
					} else {
						compAmount = attAmount;
					}
				} else {
					compAmount = relAmount;
				}
				// if the current sample is overdriving
				// then always take the compression amount
				if (over[i]) {
					compAmount = compress;
				}
				average[i] *= compAmount * 2;
				max[i][0] *= compAmount * 2;
				max[i][1] *= compAmount * 2;
				real[i] *= compAmount * 2;
			} else if (compType == 1) {
				// average compression
				float compAmount = 0;
				int compDivider = 0;
				for (int i2 = -attack; i2 <= release; i2++) {
					// if it's not out of bound
					if (i + i2 >= 0 && i + i2 < length) {
						compDivider++;
						if (over[i + i2]) {
							compAmount += compress;
						} else {
							compAmount += expand;
						}
					}
				}
				compAmount /= compDivider;
				average[i] *= compAmount * 2;
				max[i][0] *= compAmount * 2;
				max[i][1] *= compAmount * 2;
				real[i] *= compAmount * 2;
			} else if (compType == 2) {
				// clipping
				if (over[i]) {
					float limitCrop = (float) (limit + ((float) average[i] - limit)
							* compress);
					// highest value of the normal samples
					float cropBase = (float) (limit * expand * 2);
					float proxi = 1;// proximity of a normal sample (1=long)
					for (int i2 = attack; i2 > 0; i2--) {
						// if inside the bounds
						if (i + i2 >= 0 && i + i2 < length) {
							if (!over[i + i2]) {
								proxi = Math.min(proxi, (float) i2 / attack);
							}
						}
					}
					for (int i2 = -release; i2 < 0; i2++) {
						// if inside the bounds
						if (i + i2 >= 0 && i + i2 < length) {
							if (!over[i + i2]) {
								proxi = Math.min(proxi, (float) -i2 / release);
							}
						}
					}
					float limitAbs = limitCrop * proxi + cropBase * (1 - proxi);
					average[i] = Math.min(average[i] * expand * 2, limitAbs);
					max[i][0] = constrain(max[i][0] * expand * 2, -limitAbs,
							limitAbs);
					max[i][1] = constrain(max[i][1] * expand * 2, -limitAbs,
							limitAbs);
					real[i] = constrain((float) real[i] * expand * 2,
							-limitAbs, limitAbs);
				} else {
					average[i] *= expand * 2;
					max[i][0] *= expand * 2;
					max[i][1] *= expand * 2;
					real[i] *= expand * 2;
				}
			}
		}
		// normalize
		float greater = 0;
		for (int i = 0; i < length; i++) {
			greater = Math.max(Math.abs(max[i][0]), greater);
			greater = Math.max(Math.abs(max[i][1]), greater);
		}
		// apply normalization if max value is greater than the maximum
		if (greater > 1) {
			for (int i = 0; i < length; i++) {
				average[i] /= ((float) greater);
				max[i][0] /= ((float) greater);
				max[i][1] /= ((float) greater);
				real[i] /= ((float) greater);
			}
		}
		// creates an instance to store the results
		ProcessingResult result = new ProcessingResult(average, max, over, real);
		return result;
	}

	public static ProcessingResult cropToVisible(ProcessingResult arrays,
			VisualizationParameters vParam) {
		// crops arrays to only their visible part
		int startingPoint = (vParam.viewStartPx - vParam.startPx);
		int length = vParam.viewLengthPx;
		float cropMax[][] = new float[length][2];// peaks -1 to 1
		float cropAverage[] = new float[length];// gain 0 to 1
		double cropReal[] = new double[length];// actual waveform -1 to 1
		boolean cropOver[] = new boolean[length];// peaks (true if overdriving)
		for (int i = 0; i < length; i++) {
			cropMax[i][0] = arrays.getMax()[i + startingPoint][0];
			cropMax[i][1] = arrays.getMax()[i + startingPoint][1];
			cropAverage[i] = arrays.getAverage()[i + startingPoint];
			cropReal[i] = arrays.getReal()[i + startingPoint];
			cropOver[i] = arrays.getOver()[i + startingPoint];
		}
		// creates a new ProcessingResult object to store the cropped arrays
		ProcessingResult cropArrays = new ProcessingResult(cropAverage,
				cropMax, cropOver, cropReal);
		return cropArrays;
	}

	private static float constrain(float a, float b, float c) {
		return Math.max(Math.min(a, c), b);
	}

}

class ProcessingResult {

	public ProcessingResult(float[] average, float[][] max, boolean[] over,
			double[] real) {
		this.average = average;
		this.max = max;
		this.over = over;
		this.real = real;
	}

	float[] average;
	float[][] max;
	boolean[] over;
	double[] real;

	public float[] getAverage() {
		return average;
	}

	public void setAverage(float[] average) {
		this.average = average;
	}

	public float[][] getMax() {
		return max;
	}

	public void setMax(float[][] max) {
		this.max = max;
	}

	public boolean[] getOver() {
		return over;
	}

	public void setOver(boolean[] over) {
		this.over = over;
	}

	public double[] getReal() {
		return real;
	}

	public void setReal(double[] real) {
		this.real = real;
	}

}
