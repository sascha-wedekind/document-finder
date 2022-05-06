package com.bytedompteur.documentfinder.interprocesscommunication.dagger;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCConnectionException;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServer;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(includes = IPCBaseModule.class)
public abstract class IPCClientModule {

  @Provides
  @Singleton
  static IPCServer provideIPCServer() {
    return new IPCServer() {
      @Override
      public void start() throws IPCConnectionException {
        // IGNORE
      }

      @Override
      public void stop() {
        // IGNORE
      }

      @Override
      public boolean isRunning() {
        return false;
      }
    };
  }

}
