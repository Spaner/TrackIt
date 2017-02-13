package com.jb12335.trackit.business.domain;

public class TrackStatus {
	
	private boolean picturesChanged;
	private boolean audioChanged;
	private boolean moviesChanged;
	private boolean notesChanged;
	private boolean trackChanged;
	private boolean renamed;
	
	static private boolean changesEnabled = true;
	
	public synchronized static void enableChanges() {
		changesEnabled = true;
	}
	
	public synchronized static void disableChanges() {
		changesEnabled = false;
	}
	
	public static boolean changesAreEnabled() {
		return changesEnabled;
	}
	
	public TrackStatus() {
		resetChanges();
	}
	
	public TrackStatus( TrackStatus status) {
		setStatus( status);
	}
	
	public void setStatus( TrackStatus status) {
		this.trackChanged    = status.trackChanged;
		this.audioChanged    = status.audioChanged;
		this.picturesChanged = status.picturesChanged;
		this.moviesChanged   = status.moviesChanged;
		this.notesChanged    = status.notesChanged;
		this.renamed         = status.renamed;
	}

	public void resetChanges() {
		picturesChanged = audioChanged = moviesChanged = notesChanged
				        = trackChanged = renamed = false;
	}
	
	public void setTrackAsChanged() {
		if ( changesEnabled )
			trackChanged = true;
	}
	
	public boolean trackWasChanged() {
		return trackChanged;
	}
	
	public void setTrackAsRenamed() {
		if ( changesEnabled )
			renamed = true;
	}

	public void setTrackAsUnrenamed() {
		if ( changesEnabled )
			renamed = false;
	}
	
	public boolean wasRenamed() {
		return renamed;
	}
	
	public void setPicturesAsChanged() {
		if ( changesEnabled )
			picturesChanged = true;
	}
	
	public boolean picturesWereChanged() {
		return picturesChanged;
	}

	public void setAudioAsChanged() {
		if ( changesEnabled )
			picturesChanged = true;
	}
	
	public boolean audioWasChanged() {
		return picturesChanged;
	}

	public void setMoviesAsChanged() {
		if ( changesEnabled )
			moviesChanged = true;
	}
	
	public boolean moviesWereChanged() {
		return moviesChanged;
	}

	public void setNotesAsChanged() {
		if ( changesEnabled )
			notesChanged = true;
	}
	
	public boolean notesWereChanged() {
		return notesChanged;
	}
	
	public boolean mediaWasChanged() {
		return picturesChanged | moviesChanged | notesChanged;
	}

}
