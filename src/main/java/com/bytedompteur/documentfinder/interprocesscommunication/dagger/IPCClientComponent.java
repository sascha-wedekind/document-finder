package com.bytedompteur.documentfinder.interprocesscommunication.dagger;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCService;
import dagger.BindsInstance;
import dagger.Component;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Component(modules = IPCClientModule.class)
@Singleton
public interface IPCClientComponent {

  IPCService ipcService();

  @Component.Builder
  interface Builder {

    @BindsInstance
    IPCClientComponent.Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    IPCClientComponent build();
  }

}
