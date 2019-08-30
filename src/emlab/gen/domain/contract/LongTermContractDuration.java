/**
 * 
 */
package emlab.gen.domain.contract;

/**
 * @author ejlchappin
 *
 *
 */
public class LongTermContractDuration {

	private long duration;

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String toString(){
		return "duration: " + getDuration(); 
	}
}
