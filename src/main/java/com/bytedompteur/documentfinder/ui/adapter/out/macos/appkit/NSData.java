package com.bytedompteur.documentfinder.ui.adapter.out.macos.appkit;

import com.bytedompteur.documentfinder.ui.adapter.out.macos.foundation.Foundation;
import com.bytedompteur.documentfinder.ui.adapter.out.macos.foundation.NSObject;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

@SuppressWarnings("unused")
public class NSData extends NSObject.Releasable {
    // NSData -> https://developer.apple.com/documentation/foundation/NSData?language=objc
    private static final Pointer nativeClass = Foundation.INSTANCE.objc_getClass("NSData");

    // [NSData dataWithBytes:length:]; -> https://developer.apple.com/documentation/foundation/nsdata/1547231-datawithbytes?language=objc
    private static final Pointer dataWithBytesSelector = Foundation.INSTANCE.sel_registerName("dataWithBytes:length:");

    // [NSData data]; -> https://developer.apple.com/documentation/foundation/nsdata/1547234-data?language=objc
    private static final Pointer dataSelector = Foundation.INSTANCE.sel_registerName("data");

    // [NSData length]; -> https://developer.apple.com/documentation/foundation/nsdata/1416769-length?language=objc
    private static final Pointer lengthSelector = Foundation.INSTANCE.sel_registerName("length");

    // [NSData getBytes:length:]; -> https://developer.apple.com/documentation/foundation/nsdata/1416746-getbytes?language=objc
    private static final Pointer getBytesLengthSelector = Foundation.INSTANCE.sel_registerName("getBytes:length:");

    public NSData(NativeLong id) {
        super(id);
    }

    /**
     * Creates a new NSData object from the given byte array.
     *
     * @param bytes The byte array to create the NSData object from.
     * @return if bytes is null or 0, return an empty NSData object, otherwise return a new NSData object.
     */
    public static NSData initWithBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return new NSData(Foundation.INSTANCE.objc_msgSend(nativeClass, dataSelector));

        return new NSData(Foundation.INSTANCE.objc_msgSend(nativeClass, dataWithBytesSelector, bytes, new NativeLong(bytes.length, true)));
    }

    public int length() {
        return Foundation.INSTANCE.objc_msgSend(getId(), lengthSelector).intValue();
    }

    public byte[] getBytes() {
        int length = length();
        byte[] bytes = new byte[length];
        Foundation.INSTANCE.objc_msgSend(getId(), getBytesLengthSelector, bytes, new NativeLong(length, true));
        return bytes;
    }
}
