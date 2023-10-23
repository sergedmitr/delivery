package ru.sergdm.ws.exception;

public class AgendaConflictException extends Exception {
	public AgendaConflictException() {
	}

	public AgendaConflictException(String msg) {
		super(msg);
	}
}
