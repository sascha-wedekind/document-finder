package com.bytedompteur.documentfinder.ui.systemtray;

import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayScope;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.awt.*;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@SystemTrayScope
public class SystemTrayImageFactory {

	private final JavaFxPlatformAdapter platformAdapter;

	public Image loadImage() {
		String resourceName = platformAdapter.isMacOs() ? getMacResourceName() : getWinOrLinuxResourceName();
		return Toolkit.getDefaultToolkit().getImage(getClass().getResource(resourceName));
	}

	private String getMacResourceName() {
		return "/images/TrayIconMac.png";
	}

	private String getWinOrLinuxResourceName() {
		return "/images/TrayIconWinLinux.png";
	}

}
