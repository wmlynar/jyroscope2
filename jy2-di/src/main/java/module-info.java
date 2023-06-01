module aww.jyroscope.di {
	exports com.github.jy2.di;
	requires aww.jyroscope.api;
	requires aww.jyroscope.core;
	requires aww.jyroscope.messages;
	requires org.yaml.snakeyaml;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jdk8;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires java.management;
	requires commons.logging;
	requires aww.jyroscope.base;
	requires com.fasterxml.jackson.dataformat.yaml;
}