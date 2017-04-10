# NetSys-project

## Goal
Create reliable file transfer using UDP protocol.

## Requirements
- Upload/download files from your client to the Pi (file size >= 100 MB)
- Do NOT use TCP/IP. Use UDP, together with an ARQ protocol (use a window size > 1)
- The client should be able to ask for all available files on the Pi. Aka. some form of interaction with the server should be possible, eg. list the files and download a specific one.
- Pause and resume paused/aborted downloads
- Server should be able to send several files at the same time
- Prove that the file you download from the server is exactly the same as the one on the server, and the other way around (data integrity).
- Your client should be able to find the Raspberry Pi without providing an IP address. A [multi-cast DNS](https://tools.ietf.org/html/rfc6762) protocol must be used.
- You should be able to provide statistics about download speeds, packet loss, retransmissions, etc.
- Provide a README with your code.

## Bonus
- Encrypted file transfer. Prove this by transferring a text file and creating a Wireshark dump.
- Mesh network support. Download a file from a Raspberry Pi out of range of the WiFi from your laptop. Connect with an intermediate Raspberry Pi, which can see both the other Pi and your laptop. (Hint: It is possible to simulate a Raspberry Pi out of range by blacklisting a Pi from the client.)

## Reflection
Write a reflective report of max 5 pages which contains:
- A sequence diagram
- Improvement suggestions
- Made choices:
  * What went well and is worth remembering?
  * What would you do differently the next time around?


## Automatic repeat request (ARQ)
`(From Wikipedia)`

also known as Automatic Repeat Query, is an error-control method for data transmission that uses 
acknowledgements (messages sent by the receiver indicating that it has correctly received a data 
frame or packet) and timeouts (specified periods of time allowed to elapse before an acknowledgment 
is to be received) to achieve reliable data transmission over an unreliable service. If the sender 
does not receive an acknowledgment before the timeout, it usually re-transmits the frame/packet 
until the sender receives an acknowledgment or exceeds a predefined number of re-transmissions.

The types of ARQ protocols include Stop-and-wait ARQ, Go-Back-N ARQ, and 
Selective Repeat ARQ / Selective Reject.

All three protocols usually use some form of sliding window protocol to tell the transmitter to 
determine which (if any) packets need to be retransmitted.

These protocols reside in the Data Link or Transport Layers of the OSI model.

For more info see [Wikipedia](https://en.wikipedia.org/wiki/Automatic_repeat_request)
