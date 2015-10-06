package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import static javax.sound.midi.ShortMessage.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.canvas.*;

import org.junit.Test;

import solver.GreedySolver;
import solver.MekString;

public class Simulation {

	private static int note_tag_width = 2;
	private static int note_tag_height = 8;
	private static int note_len_height = 2;

	private Sequence seq=null;
	List<Note>[] notes = null;
	int resolution;
	long position;
	long drawStartTime;

	long lastTickTime;

	private MekString[] strings = null;
	private float[] picks = null;

	private boolean playing = false;

	public Simulation(){
	}

	public boolean isPlaying(){
		return playing;
	}

	public void setStrings(MekString[] newStrings){
		strings = newStrings;
//		notes = Arrays.copyOfRange(notes, 1, notes.length);
		picks = new float[strings.length];
		// we cheat here and just sets the pick position to the first note on each string
//		for (int i=0; i<Math.min(notes.length, strings.length); ++i){
//			picks[i] = notes[i].get(0).note;
//		}
//		System.out.println("hi");
	}

	/**
	 *
	 * @param sequence
	 */
	@SuppressWarnings("unchecked")
	public void setSequence(Sequence sequence){
		if (sequence == null) return;
		this.resolution = sequence.getResolution();
		System.out.println("Resolution: " + resolution);
		int bpm = 0;
		this.seq = sequence;
//		this.position = 0;
//		this.drawStartTime = 0;
		// for each track, convert note_on and note_off to Note(3)

		Track[] tracks = seq.getTracks();
		notes = new List[tracks.length];


		// for each track
		for (int j=0; j<tracks.length; ++j){
			// get each note_on, pair it with a note_off, then create a Note, and add it to notes
			Map<Integer, Integer> progress = new HashMap<Integer, Integer>();
			notes[j] = new ArrayList<Note>();
			Track tr = tracks[j];
			for (int i=0; i<tr.size() ; ++i){
				MidiMessage mid = tr.get(i).getMessage();
				if (mid instanceof ShortMessage){
					ShortMessage shrt = (ShortMessage) mid;
					switch (shrt.getCommand()){
					case NOTE_ON: // if the note is a note on, store its index, so we can get it back
						progress.put(shrt.getData1(), i);
						break;
					case NOTE_OFF: // if the note is a notes off, get the note on, make a Note, and add it the the notes list
						MidiEvent on = tr.get(progress.get(shrt.getData1()));
						if (on == null){
//							System.out.println("Note: " + shrt.getData1() + ", No corresponding note on");
							break;
						}
						Note n = new Note(shrt.getData1(),
										  (long) (on.getTick() * (60000. / (bpm * resolution))),
										  (long) (tr.get(i).getTick()* (60000. / (bpm * resolution))) );
						notes[j].add(n);
						break;

						default:
//							System.out.println("other note");
					}
				} else if (mid instanceof MetaMessage){
//					sequence.getResolution()
					MetaMessage met = (MetaMessage) mid;
					if (met.getType() == 0x51){
						byte[] data = met.getData();
						bpm =  60000000 / ((data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff));
//						System.out.printf("Track %d, Timestamp: %d, Tempo Change: %d %f\n", j, tr.get(i).getTick(),bpm, (60000. / (bpm * resolution)));

					}
				}
			}
		}

		strings = Slave.getMekStringArray();
		if (strings != null) {
//			notes = Arrays.copyOfRange(notes, 1, notes.length);
			setStrings(strings);
		}
	}

	// something something threads
	public void play(){
		playing = true;
//		lastTickTime = System.currentTimeMillis();
	}

	public void pause(){
		playing = false;
		lastTickTime = 0;
	}

	public void stop(){
		playing = false;
		position = 0;
		drawStartTime = 0;
		lastTickTime = 0;
	}

	public long addDrawStartTime(long add){
		return drawStartTime+=add;
	}

	public long addTime(long add){
		return tick(position+=add);
	}

	public long setTime(long set){
		return position=set;
	}

	public void tick(){
		long time = System.currentTimeMillis();
		if (lastTickTime != 0) {
			addTime(time-lastTickTime);
			addDrawStartTime(time-lastTickTime);
		}
		lastTickTime = time;
	}

	public long tick(long time){
		if (picks==null) return time;
		// move the picks
		for (int i=0; i<picks.length; ++i){

		}


		return time;
	}

	public void draw(GraphicsContext gc, double hscale){
//		long time = System.currentTimeMillis();
		// get the dimensions of the canvas
		double width = gc.getCanvas().getWidth();
		double height= gc.getCanvas().getHeight();
		double left = 15.0;
		long drawEnd = drawStartTime + (long) (width * hscale); // need to calculate this (width and hscale?)
		// clear the draw area
		gc.clearRect(0, 0, width, height);
		// draw a vertical line on the right to seperate the pane from the settings
		gc.setFill(Color.GREY);
		gc.fillRect(width-1, 1, width, height);
		gc.setFont(Font.font(gc.getFont().getFamily(), 9));;


		// us/tick
		// correction to make the size of seqments timing independent
//		if (seq!=null) usPerTick = (double)seq.getMicrosecondLength()/seq.getTickLength();
//		hscale /= 4.0/usPerTick*1000.0; // 2.0 is magic
//		if (seq!=null) usPerTick = (double)seq.getTickLength()/seq.getMicrosecondLength();
//		hscale = 1; // 2.0 is magic
//		System.out.println(usPerTick);

		// put timing marks on the bottom
		long u= 0;
		gc.setFill(Color.GRAY);
//		System.out.println(u);
		do {
			u+=200;
//			System.out.println(u);
			double pos = u;
//			System.out.println(pos);
			if ((pos-drawStartTime)*hscale < width && (pos-drawStartTime)*hscale > left-100){
				gc.fillRect(left + (pos-drawStartTime)*hscale, height - 2, 2, height);
				gc.fillText(String.format("%#.2f", u/1000.0),
						left+(pos-drawStartTime-10)*hscale, height-10-(u%100)/20);
			}
		} while ((u-drawStartTime)*hscale < width);
		// then, if we don't have a sequence to display, we don't have anything to display
		if (seq==null) return;
		strings = Slave.getMekStringArray();

		if (strings !=null){
			// get information from strings
			int totalnotes = 0;
			for (int i=0; i<strings.length; ++i){
				totalnotes += strings[i].noteRange +1;
			}
			double offset = 0;
			int offsetNotes = 0;
			double noteDiv = height / totalnotes;


			////////////////////////////////////////
			// Draw the notes
			////////////////////////////////////////
			Note n;
			for (int t=0; t<Math.min(notes.length, strings.length); ++t){

				int length = notes[t].size();
	//			 get the first (fully) visible note
				int startIndex = 0;
				// and start at that point
				// set how far down we start
				if (t>0) offset += noteDiv*(strings[t-1].noteRange+1);
				for (int i=startIndex; i<length; ++i){
					n = notes[t].get(i);

					double start = (n.start-drawStartTime)*hscale;
					double end = (n.end-drawStartTime)*hscale;
//					System.out.println(start + "\t" + end);

					gc.setFill(Color.BLACK);
					gc.fillRect(left + start, offset+(n.note-strings[t].lowNote)*noteDiv-noteDiv/2 ,n.duration*hscale, note_tag_width);
					gc.setFill(Color.GREEN);
					gc.fillRect(left + start, offset+(n.note-strings[t].lowNote)*noteDiv-(noteDiv/2+note_tag_height/2), note_tag_width, note_tag_height);
					gc.setFill(Color.RED);
					gc.fillRect(left + end-note_tag_width, offset+(n.note-strings[t].lowNote)*noteDiv-(noteDiv/2+note_tag_height/2), note_tag_width, note_tag_height);
	//				}
				}

			}
			////////////////////////////////////////
			// Draw the note values on the left
			////////////////////////////////////////
			gc.clearRect(0, 0, left, height);
			gc.setFill(Color.GRAY);
			gc.fillRect(left, 0, 1, height);
			offset = 0;
			// go through each string
			for (int i=0; i<strings.length; ++i){
				// and draw the top playable note, and the bottom playable note
				for (int s=strings[i].lowNote, j=0; s<=strings[i].highNote; ++s, ++j){
					// s is the note value, j is just a loop counter
					gc.fillText(String.format("%d", s), 1, offset + (noteDiv)*j);
				}
				offsetNotes += strings[i].noteRange+1;
				offset = ((double)offsetNotes)*(noteDiv);
				// draw a divider between the strings
				if (offsetNotes < totalnotes) gc.fillRect(0, offset+1-noteDiv, width, 1);
			}

			//////////////////////////////////////////
			// Draw the picks
			//////////////////////////////////////////
			if (picks!=null){
				for (int i=0; i<strings.length; ++i){



				}
			}


		} else {

		// archive for visualisation
			Note n;
			for (int t=0; t<notes.length; ++t){
				int length = notes[t].size();
	//			 get the first (fully) visible note
				int startIndex = 0;
				// and start at that point
				for (int i=startIndex; i<length; ++i){
					n = notes[t].get(i);

					double start = (n.start-drawStartTime)*hscale;
					double end = (n.end-drawStartTime)*hscale;

					gc.setFill(Color.color(1.0/(t+1), 1.0-1.0/(t+1), 0.5));
					gc.fillRect(left + start, note_tag_height*n.note + t*note_tag_height/(double)notes.length ,n.duration*hscale, note_tag_width);
					gc.setFill(Color.GREEN);
					gc.fillRect(left + start, note_tag_height*n.note, note_tag_width, note_tag_height);
					gc.setFill(Color.RED);
					gc.fillRect(left + end-2, note_tag_height*n.note, note_tag_width, note_tag_height);
	//				}
				}

			}
		}
	}

	/**
	 * Gets the index of the note at or the next note after the specified time, or track.size
	 * @param time
	 * @param track
	 * @return
	 */
	private int getIndexAtTime(long time, List<Note> track){
		int ind = Arrays.binarySearch(track.toArray(), new Note(0, time, 0));
		if (ind < 0) return -ind-1;
		else return ind;
	}

	private class Note implements Comparable<Note>{
		final int note;
		final long start;
		final long end;
		final long duration;

		private Note(int note, long start, long end){
			this.note = note;
			this.start = start;
			this.end = end;
			this.duration = end-start;
		}

		@Override
		public int compareTo(Note other) {
			return Long.compare(this.start, other.start);
		}
	}

//	@Test
//	public void getIndexTest(){
//		List<Note> notes = new ArrayList<Note>();
//		notes.add(new Note(1, 10, 10));
//		notes.add(new Note(2, 20, 20));
//		notes.add(new Note(3, 30, 10));
//		notes.add(new Note(6, 60, 10));
//
//		for (int i=0; i<70; i+=10){
//			System.out.print(i + "\t" + getIndexAtTime(i, notes) + "\t");
//			if (getIndexAtTime(i, notes) < 0){
//				System.out.println(notes.get((-getIndexAtTime(i, notes))-1).start);
//			} else {
//				System.out.println(notes.get(getIndexAtTime(i, notes)).start);
//			}
//			System.out.print(i+1 + "\t" + getIndexAtTime(i+1, notes) + "\t");
//			if (getIndexAtTime(i+1, notes) < 0){
//				System.out.println(notes.get((-getIndexAtTime(i+1, notes))-1).start);
//			} else {
//				System.out.println(notes.get(getIndexAtTime(i+1, notes)).start);
//			}
//		}
//
//	}


}
