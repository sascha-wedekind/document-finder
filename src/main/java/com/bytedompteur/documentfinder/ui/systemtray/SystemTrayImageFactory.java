package com.bytedompteur.documentfinder.ui.systemtray;

import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayScope;
import com.jthemedetecor.OsThemeDetector;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@SystemTrayScope
public class SystemTrayImageFactory {

	private final JavaFxPlatformAdapter platformAdapter;
	private final OsThemeDetector detector;

	public Image loadImage() {
		try {
			String resourceName = platformAdapter.isMacOs() ? getMacResourceName() : getWinOrLinuxResourceName();
			var imageResource = getClass().getResource(resourceName);
			return ImageIO.read(imageResource);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getMacResourceName() {
		// When no MacOs template images supported.
//		return detector.isDark() ? "/images/TrayIconMacDark.png" : "/images/TrayIconMacLight.png";
		return "/images/TrayIconMacDark.png"; // Newer MacOs uses template images
	}

	private String getWinOrLinuxResourceName() {
		return "/images/TrayIconWinLinux.png";
	}

}
