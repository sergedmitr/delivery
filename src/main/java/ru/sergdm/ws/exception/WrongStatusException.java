package ru.sergdm.ws.exception;

public class WrongStatusException extends Exception {
	public WrongStatusException() {
	}

	public WrongStatusException(String msg) {
		super(msg);
	}
}
