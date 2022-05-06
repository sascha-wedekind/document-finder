package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in;

import java.io.IOException;

public interface IPCClient {
  void senMessage(CharSequence message) throws IOException;
}
