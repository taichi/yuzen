package org.koshinuke.yuzen;

import org.koshinuke.yuzen.util.ClassUtil;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author taichi
 */
public class Markers {

	public static final String PREFIX_PKG = ClassUtil
			.getPackageName(Markers.class);

	public static final Marker MARKER_ROOT = MarkerFactory
			.getMarker(PREFIX_PKG);

	/**
	 * 設計判断を伴う処理に関連する項目。アドインやプラグインに関連する部分など。
	 */
	public static final Marker DESIGN = MarkerFactory.getMarker(PREFIX_PKG
			+ ".design");

	/**
	 * ファイルやネットワーク、他の依存ライブラリに対するインターフェースに関連する項目
	 */
	public static final Marker BOUNDARY = MarkerFactory.getMarker(PREFIX_PKG
			+ ".boundary");

	/**
	 * ライブラリ内に定義されているオブジェクトのライフサイクルに関連する項目
	 */
	public static final Marker LIFECYCLE = MarkerFactory.getMarker(PREFIX_PKG
			+ ".lifecycle");

	public static final Marker HELP = MarkerFactory.getMarker(PREFIX_PKG
			+ ".help");

	static {
		Marker[] markers = { DESIGN, BOUNDARY, LIFECYCLE, HELP };
		for (Marker m : markers) {
			MARKER_ROOT.add(m);
		}
	}
}