public class CompressionParameters {

	double limit;// threshold of the compression

	int attack;// attack time in samples
	int release;// release time in samples
	float compress;// occurs on the red part of the waveform
	float expand;// occurs on the green part of the waveform

	// compType changes the way in which the
	// values affects the waveform
	// 0 = compression that never exceeds the threshold
	// 1 = average compression
	// 2 = clipping
	// 3 = nothing
	int compType;

	public double getLimit() {
		return limit;
	}

	public void setLimit(double limit) {
		this.limit = limit;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public int getRelease() {
		return release;
	}

	public void setRelease(int release) {
		this.release = release;
	}

	public float getCompress() {
		return compress;
	}

	public void setCompress(float compress) {
		this.compress = compress;
	}

	public float getExpand() {
		return expand;
	}

	public void setExpand(float expand) {
		this.expand = expand;
	}

	public int getCompType() {
		return compType;
	}

	public void setCompType(int compType) {
		this.compType = compType;
	}

	public void setEnvelope(int attack, int release) {
		this.attack = attack;
		this.release = release;
	}

}
