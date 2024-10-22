package com.maharazhi.javsapp.whatsapp.message;

import com.maharazhi.javsapp.proto.ProtoBuf;

public class WebConversationMessage extends WebMessage {

    private final String text;
    private QuotedTextMessage quotedTextMessage;
    private String participant;

    public WebConversationMessage(ProtoBuf.WebMessageInfo message) {
        super(message);

        if (message.getMessage().hasConversation()) {
            text = message.getMessage().getConversation();
        } else {
            ProtoBuf.ExtendedTextMessage extendedMessage = message.getMessage().getExtendedTextMessage();
            text = extendedMessage.getText();

            if (extendedMessage.hasContextInfo()) {
                ProtoBuf.Message quoted = extendedMessage.getContextInfo().getQuotedMessage();

                String stanzaId = extendedMessage.getContextInfo().getStanzaId();
                String participant = extendedMessage.getContextInfo().getParticipant();
                String quotedMessage = quoted.hasExtendedTextMessage() ? quoted.getExtendedTextMessage().getText() :
                        quoted.getConversation();

                quotedTextMessage = new QuotedTextMessage(stanzaId, participant, quotedMessage);
            }
        }
        if (message.hasParticipant()) {
            participant = message.getParticipant();
        }
    }

    public String getText() {
        return text;
    }

    public boolean hasQuotedTextMessage() {
        return quotedTextMessage != null;
    }

    public QuotedTextMessage getQuotedTextMessage() {
        return quotedTextMessage;
    }

    public static class QuotedTextMessage {

        private final String stanzaId, participant, quotedMessage;

        public QuotedTextMessage(String stanzaId, String participant, String quotedMessage) {
            this.stanzaId = stanzaId;
            this.participant = participant;
            this.quotedMessage = quotedMessage;
        }

        public String getStanzaId() {
            return stanzaId;
        }

        public String getParticipant() {
            return participant;
        }

        public String getText() {
            return quotedMessage;
        }
    }

    public boolean isGroup() {
        return getRemoteJid().endsWith("@g.us");
    }

    public boolean isPersonal() {
        return getRemoteJid().endsWith("@s.whatsapp.net");
    }

    public boolean isStatus() {
        return getRemoteJid().equals("status@broadcast");
    }

    public boolean isBroadCast() {
        return getRemoteJid().endsWith("status@broadcast");
    }

    public String getParticipant() {
        return participant;
    }
}
