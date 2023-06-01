module aww.jyroscope.core {
	exports com.github.core.internal;
	exports com.github.core.log;

	requires aww.jyroscope.base;
	requires aww.jyroscope.api;

	requires java.xml.bind;
	requires java.logging;
	requires commons.logging;
}