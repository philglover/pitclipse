package org.pitest.pitclipse.pitrunner.client;

import org.pitest.pitclipse.pitrunner.PitRequest;
import org.pitest.pitclipse.pitrunner.server.PitServer;

public class PitCommunicator implements Runnable {

    private final PitRequest request;
    private final PitResultHandler resultHandler;
    private final PitServer server;

    public PitCommunicator(PitServer server, PitRequest request, PitResultHandler resultHandler) {
        this.server = server;
        this.request = request;
        this.resultHandler = resultHandler;
    }

    @Override
    public void run() {
        try {
            server.listen();
            server.sendRequest(request);
            resultHandler.handle(server.receiveResults());
        } finally {
            server.close();
        }
    }
}