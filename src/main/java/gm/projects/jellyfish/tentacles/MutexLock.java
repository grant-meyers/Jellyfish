package gm.projects.jellyfish.tentacles;

import java.util.concurrent.Semaphore;

public class MutexLock {
	private final Semaphore semaphore;
	
	public MutexLock() {
		semaphore = new Semaphore(0);
	}
	
	public Semaphore getSemaphore() {
		return semaphore;
	}
	
	public void releaseSemaphore() {
		semaphore.release();
	}

}
