package com.bytedompteur.documentfinder.interprocesscommunication.dagger;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCClient;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCService;
import com.bytedompteur.documentfinder.interprocesscommunication.core.IPCClientImpl;
import com.bytedompteur.documentfinder.interprocesscommunication.core.IPCServiceImpl;
import com.bytedompteur.documentfinder.interprocesscommunication.core.SocketAddressService;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Module
public abstract class IPCBaseModule {

  @Binds
  @Singleton
  abstract IPCService provideIPCService(IPCServiceImpl value);

  @Binds
  @Singleton
  abstract IPCClient provideIPCClient(IPCClientImpl value);

  @Provides
  @Singleton
  static SocketAddressService provideSocketAddressService(@Named("applicationHomeDirectory") String value) {
    return new SocketAddressService(value);
  }

}
