package com.bytedompteur.documentfinder.interprocesscommunication.dagger;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServer;
import com.bytedompteur.documentfinder.interprocesscommunication.core.IPCMessageRequestListener;
import com.bytedompteur.documentfinder.interprocesscommunication.core.IPCServerImpl;
import com.bytedompteur.documentfinder.interprocesscommunication.core.RequestListener;
import dagger.Binds;
import dagger.Module;

import javax.inject.Singleton;

@Module(includes = IPCBaseModule.class)
public abstract class IPCModule {

  @Binds
  @Singleton
  abstract IPCServer provideIPCServer(IPCServerImpl value);

  @Binds
  @Singleton
  abstract RequestListener provideRequestListener(IPCMessageRequestListener value);
}
