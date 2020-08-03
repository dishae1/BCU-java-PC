package io;

import java.util.ArrayDeque;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

public class BCPlayer implements LineListener{
	private static final int FACTOR = 20;
	
	private static float getVol(int vol) {
		return FACTOR * ((float) Math.log10(vol) - 2);
	}
	
	private final int ind;
	private final Clip c;
	private final FloatControl master;
	private final int loop;
	private final boolean isLooping;
	
	private boolean rewinding = false;
	private boolean playing = false;
	
	protected BCPlayer(Clip c, int ind, boolean looping) {
		this.ind = ind;
		this.c = c;
		this.c.addLineListener(this);
		this.master = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
		this.loop = 0;
		this.isLooping = looping;
	}
	
	protected BCPlayer(Clip c, int ind, int loop, boolean looping) {
		this.ind = ind;
		this.c = c;
		this.c.addLineListener(this);
		this.master = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
		this.loop = loop;
		this.isLooping = looping;
		
		if(loop > 0 && loop * 1000 < c.getMicrosecondLength()) {
			c.setLoopPoints(milliToFrame(loop), -1);
		} else if(loop * 1000 >= c.getMicrosecondLength()) {
			c.loop(0);
		}
	}
	
	protected void rewind() {
		if(rewinding) {
			return;
		}
		
		rewinding = true;
		c.setFramePosition(0);
		rewinding = false;
	}
	
	protected void setVolume(int vol) {
		master.setValue(getVol(vol));
	}
	
	protected void release() {
		playing = false;
		rewinding = false;
		c.close();
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public void stop() {
		playing = false;
		c.stop();
	}
	
	protected void start() {
		if(playing) {
			return;
		}
		
		playing = true;
		c.start();
	}
	
	protected void setLineListener(LineListener l) {
		c.addLineListener(l);
	}

	@Override
	public void update(LineEvent event) {
		if (event.getType() == Type.STOP) {
			playing = false;
			stop();
			
			if(ind >= 0 && ind != 20 && ind != 21 && ind != 22) {
				synchronized (BCMusic.class) {
					ArrayDeque<BCPlayer> players = BCMusic.sounds.get(ind);
					
					if(players != null) {
						players.push(this);
					}
				}
			} else if(isLooping && loop >= c.getMicrosecondLength()) {
				return;
			}
		}
	}
	
	private int milliToFrame(long milli) {
		long rate = (long) c.getFormat().getFrameRate();
		
		return (int) ((double)milli/1000.0) * (int) rate;
	}
}