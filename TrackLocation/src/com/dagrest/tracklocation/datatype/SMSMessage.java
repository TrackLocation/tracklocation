package com.dagrest.tracklocation.datatype;

//	0	:    _id
//	1	:    thread_id
//	2	:    address
//	3	:    person
//	4	:    date
//	5	:    protocol
//	6	:    read
//	7	:    status
//	8	:    type
//	9 	:    reply_path_present
//	10	:    subject
//	11	:    body
//	12	:    service_center
//	13	:    locked

public class SMSMessage {
	private String messageId;
	private String messageThreadId;
	private String messageNumber;
	private String messageContent;
	private String messageDate;
	private String messagePerson;
	
	public String getMessagePerson() {
		return messagePerson;
	}
	public void setMessagePerson(String messagePerson) {
		this.messagePerson = messagePerson;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getMessageThreadId() {
		return messageThreadId;
	}
	public void setMessageThreadId(String messageThreadId) {
		this.messageThreadId = messageThreadId;
	}
	public String getMessageNumber() {
		return messageNumber;
	}
	public void setMessageNumber(String messageNumber) {
		this.messageNumber = messageNumber;
	}
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	public String getMessageDate() {
		return messageDate;
	}
	public void setMessageDate(String messageDate) {
		this.messageDate = messageDate;
	}
}
