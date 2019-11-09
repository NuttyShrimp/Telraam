package telraam.beacon;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.net.Socket;
import java.nio.Buffer;
import java.util.List;
import java.util.ArrayList;

/**
 * Beacon is socket wrapper that listens to the sockets and emits BeaconMessages
 * when enough bytes are read.
 *
 * Beacons are closed at the first Exception encountered. This could be changed
 * if need be.
 *
 * @author Arthur Vercruysse
 */
public class Beacon extends EventGenerator<BeaconMessage> implements Runnable {
    private Socket s;
    private int messageSize = BeaconMessage.MESSAGESIZE;
    private byte[] startTag = BeaconMessage.STARTTAG;
    private byte[] endTag = BeaconMessage.ENDTAG;

    public Beacon(Socket socket, Callback<Void, Event<BeaconMessage>> h) {
        super(h);

        this.s = socket;

        new Thread(this).start();
    }

    // This function is slow as fuck please FIXME
    private void rewriteBuffer(byte[] buf, int from, int to, int size) {
        if (to + size >= buf.length)
            throw new IndexOutOfBoundsException(to + size);

        if (from + size >= buf.length)
            throw new IndexOutOfBoundsException(from + size);

        if (from < to) { // Please do not overwrite data
            for (int i = size - 1; i >= 0; i--)
                buf[to + i] = buf[from + i];
        } else {
            for (int i = 0; i < size; i++)
                buf[to + i] = buf[from + i];
        }
    }

    public void run() {
        this.connect();

        boolean readingMsg = false;

        List<Byte> msgBuf = new ArrayList<>(messageSize);
        int sTagIndex = 0;
        int eTagIndex = 0;
        byte[] buf = new byte[1024];

        BufferedInputStream is;

        try {
            is = new BufferedInputStream(s.getInputStream());
        } catch (IOException e) {
            error(e);
            return;
        }

        try {
            while (true) {
                int c = is.read(buf);
                if (c < 0)
                    throw new EOFException();

                for (int i = 0; i < c; i++) {
                    byte b = buf[i];
                    msgBuf.add(b);

                    if (b == startTag[sTagIndex]) {
                        sTagIndex++;

                        // A complete start tag is found
                        // Delete current msgBuf content and start over
                        if (sTagIndex == startTag.length) {
                            sTagIndex = 0;
                            msgBuf.clear();
                            readingMsg = true;
                        }
                    } else {
                        sTagIndex = 0;
                    }

                    if (b == endTag[eTagIndex]) {
                        eTagIndex++;

                        // A complete end tag is found
                        // Flush the msgBuffer
                        if (eTagIndex == endTag.length) {
                            eTagIndex = 0;

                            if (readingMsg) {

                                // Remove end tag from message
                                for (int k = 0; k < endTag.length; k++) {
                                    msgBuf.remove(msgBuf.size() - 1);
                                }

                                this.data(new BeaconMessage(msgBuf));
                                readingMsg = false;
                            }
                        }

                    } else {
                        eTagIndex = 0;
                    }
                }
            }
        } catch (IOException e) {
            exit();
        }
    }
}
