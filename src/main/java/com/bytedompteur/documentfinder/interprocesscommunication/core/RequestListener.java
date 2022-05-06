package com.bytedompteur.documentfinder.interprocesscommunication.core;

public interface RequestListener {

  void process(CharSequence requestPayload);

}
