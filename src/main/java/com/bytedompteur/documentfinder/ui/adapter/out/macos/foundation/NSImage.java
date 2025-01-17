package com.bytedompteur.documentfinder.ui.adapter.out.macos.foundation;

import com.bytedompteur.documentfinder.ui.adapter.out.macos.appkit.NSData;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

@SuppressWarnings("unused")
public class NSImage extends NSObject.Releasable {
    // NSImage -> https://developer.apple.com/documentation/appkit/nsimage?language=objc
    private static final Pointer nativeClass = Foundation.INSTANCE.objc_getClass("NSImage");

    // [NSImage imageNamed:]; -> https://developer.apple.com/documentation/appkit/nsimage/1524576-imagenamed?language=objc
    private static final Pointer imageNamedSelector = Foundation.INSTANCE.sel_registerName("imageNamed:");

  // [NSImage TIFFRepresentation]; -> https://developer.apple.com/documentation/appkit/nsimage/tiffrepresentation?language=objc
  private static final Pointer tiffRepresentationSelector = Foundation.INSTANCE.sel_registerName("TIFFRepresentation");

    public NSImage(NativeLong id) {
        super(id);
    }

    public static NSImage imageNamed(String name) {
        return new NSImage(Foundation.INSTANCE.objc_msgSend(nativeClass, imageNamedSelector, name));
    }

  public NSData TIFFRepresentation() {
    return new NSData(Foundation.INSTANCE.objc_msgSend(getId(), tiffRepresentationSelector));
  }
}
