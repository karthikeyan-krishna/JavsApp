# Maharazhi - JavsApp

## WhatsApp Web Reverse Engineering in Java

### Introduction

This project is initiated to implement the WhatsApp Web's complete implementation in Java By this, WhatsApp Rest APIs
can be built and can be used in Business Promotions, Newsletters, Notifications, etc

### Inspiration

The inspiration behind this project is
[Sigalor's whatsapp-web-reveng](https://github.com/sigalor/whatsapp-web-reveng)
and [adiwajshing's Baileys](https://github.com/karthikeyan-krishna/Baileys)

### Version and dependencies

This project is completely depend on Java 8. WhatsApp uses Proto Buffer. So a separate Repo containing the Proto Jar is
included. All the other dependencies are as in [pom.xml](pom.xml)

# FEATURES

- [x] Send Text Messages
- [x] Send Images
- [x] Send Videos
- [ ] Send Documents
- [ ] Send Stickers
- [ ] Group and broadcast support

# TODO

- [x] Automatic Reconnect when mobile is reconnected
- [x] Maintain the disconnected state
- [ ] Get proper response when connection is not proper
- [ ] Maintain the current status of the socket
- [x] Once connected, never disconnect
- [ ] More Event Handlers
- [ ] When a new message is received, the sender's number is alone available. Name has to be fetched from the contacts
- [ ] Build a framework to make transactions synchronized
- [ ] Make the timeout settings configurable by providing more interfaces
- [ ] A Full Documentation