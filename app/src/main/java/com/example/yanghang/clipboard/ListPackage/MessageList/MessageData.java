package com.example.yanghang.clipboard.ListPackage.MessageList;

/**
 * Created by yanghang on 2016/12/4.
 */
public class MessageData {
    private MessageType messageType;
    private String messageText;
    private MessageKind messageKind;
    public MessageData(MessageType messageType, String messageText) {
        this.messageType = messageType;
        this.messageText = messageText;
    }

    public MessageData(MessageType messageType, String messageText, MessageKind messageKind) {
        this.messageType = messageType;
        this.messageText = messageText;
        this.messageKind = messageKind;
    }

    public MessageKind getMessageKind() {
        return messageKind;
    }

    public void setMessageKind(MessageKind messageKind) {
        this.messageKind = messageKind;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public enum MessageType {
        COMPUTER, YOU
    }
    public enum MessageKind{
        FILE,MESSAGE
    }
}
