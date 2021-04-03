# JavsApp

## WhatsApp Web Reverse Engineering in Java

### Introduction

This project is initiated to implement the WhatsApp Web's complete implementation in Java By this, WhatsApp Rest APIs
can be built and can be used in Business Promotions, Newsletters, Notifications, etc

### Inspiration

The inspiration behind this project is
[Sigalor's whatsapp-web-reveng](https://github.com/sigalor/whatsapp-web-reveng)
, [adiwajshing's Baileys](https://github.com/adiwajshing/Baileys)
and [JicuNull's WhatsJava](https://github.com/JicuNull/WhatsJava)

### Version and dependencies

This project is completely depend on Java 8. WhatsApp uses Proto Buffer. So a separate Repo containing the Proto Jar is
included. All the other dependencies are as in [pom.xml](pom.xml)

# FEATURES

- [x] Send Text Messages
- [x] Send Images
- [x] Send Videos
- [ ] Send Documents
- [x] Send Stickers
- [x] Group and broadcast support

# TODO

- [x] Automatic Reconnect when mobile is reconnected
- [x] Maintain the disconnected state
- [ ] Get proper response when connection is not proper
- [ ] Maintain the current status of the socket
- [ ] When a new message is received, the sender's number is alone available. Name has to be fetched from the contacts
- [ ] Build a framework to make transactions synchronized
- [ ] Make the timeout settings configurable by providing more interfaces
- [ ] A Full Documentation

# Sample Code

```java
WhatsApp app=new WhatsApp("Whatsapp",new WhatsAppEventHandlers() {
    @Override public void qr(WhatsApp app,String message) {
        System.out.println("https://api.qrserver.com/v1/create-qr-code/?size=450x450&data="+Encode.forUriComponent(message));
    }
});
```

Open the URL and Scan the QR from WhatsApp Mobile App After Connecting,

```java
app.sendText(phone, text); //Send Text Message
app.sendImage(String number, byte[] media, String caption, String mime); //Send Image  
app.sendSticker(String number, byte[] media); // Send Sticker  
app.sendDocument(String number, byte[] media, String title, String mime); //Send document
```