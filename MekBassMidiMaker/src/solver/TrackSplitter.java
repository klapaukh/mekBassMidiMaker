package solver;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import main.Parser;

/**
 *
 * @author Elliot Wilde and Andrew Palmer
 *
 */
public class TrackSplitter {

	/**
	 * Splits the sequence into the correct number of tracks and moves the bass track to the new sequence
	 * @param seq - The sequence for splitting
	 * @param trackCount - The number of tracks to split to
	 * @param bassTrack - The bass track to copy
	 * @return	The split sequence
	 * @throws InvalidMidiDataException
	 */
	public static Sequence split(Sequence seq, int trackCount, int bassTrack) throws InvalidMidiDataException {
		Sequence output = new Sequence(seq.getDivisionType(), seq.getResolution(), trackCount+1);
		Track inTrack = seq.getTracks()[bassTrack];
		int pos = 0;
		do {
			output.getTracks()[0].add(inTrack.get(pos));
		} while ((++pos)<inTrack.size());
		
		
		for (int j=0; j<seq.getTracks().length; ++j){
			Track tr = seq.getTracks()[j];
			if (j==bassTrack) continue;

			for (int i=0; i<tr.size(); ++i){
				MidiMessage midmsg = tr.get(i).getMessage();
				if (midmsg instanceof MetaMessage){
					MetaMessage m = (MetaMessage) midmsg;
					if (m.getType() == 0x51 || m.getType() == 0x06){
						output.getTracks()[0].add(tr.get(i));
					}
				}
			}
		}
		
		
		// pass to solver
		System.out.printf("Missed %d events? of %d\n", inTrack.size()-output.getTracks()[0].size(), inTrack.size() );
		return output;
	}
/*
	input: n tracks
	output: m tracks

*/
}
