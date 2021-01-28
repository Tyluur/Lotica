package com.runescape.workers.game.login;

import com.runescape.workers.game.core.CoresManager;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the submission of all login requests to the game world. They are placed in a queue and then
 * executed in order of priority.
 *
 * @author Tyluur<itstyluur@gmail.com>
 */
public class LoginRequestProcessor implements Runnable {

	/**
	 * Login request instance
	 */
	private final static LoginRequestProcessor SINGLETON = new LoginRequestProcessor();

	/** The rate at which this will cycle at */
	private static final long PROCESS_TIME = 20;

	/**
	 * A login task BlockingQueue
	 */
	private final Queue<LoginRequest> loginQueue = new LinkedBlockingDeque<>();

	/**
	 * Runs the service
	 */
	@Override
	public void run() {
		try {
			processLoginQueue();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method iterates through all values in the {@link #loginQueue} and processes them to log players in
	 */
	private void processLoginQueue() {
		try {
			LoginRequest request;
			while((request = loginQueue.poll()) != null) {
				request.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct the service
	 */
	private LoginRequestProcessor() {

	}

	/**
	 * Gets the singleton instance
	 *
	 * @return The login service
	 */
	public static LoginRequestProcessor getSingleton() {
		return SINGLETON;
	}

	/**
	 * Starts the services
	 */
	public void init() {
		System.out.println("Login Request Processor initialized at " + PROCESS_TIME + "ms/cycle");
		CoresManager.schedule(this::run, PROCESS_TIME, TimeUnit.MILLISECONDS);
	}

	/**
	 * Offers a login task to be processed
	 */
	public void submit(LoginRequest task) {
		getLoginQueue().add(task);
	}

	public Queue<LoginRequest> getLoginQueue() {
		return this.loginQueue;
	}
}