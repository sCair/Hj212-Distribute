package com.sctech.hj212distribute;

import org.smartboot.socket.transport.AioSession;

public class SocketClientModel {
    private String _mn;

    public String get_mn() {
        return _mn;
    }

    public void set_mn(String _mn) {
        this._mn = _mn;
    }

    public AioSession get_session() {
        return _session;
    }

    public void set_session(AioSession _session) {
        this._session = _session;
    }

    private AioSession _session;
}
