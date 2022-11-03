open module firok.tool.alloywrench {
	requires transitive javafx.controls;
	requires transitive org.kordamp.ikonli.core;
	requires transitive org.kordamp.ikonli.javafx;
	requires transitive org.kordamp.ikonli.materialdesign2;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.annotation;
	requires transitive java.desktop;
	requires transitive firok.topaz;

	requires org.locationtech.jts;

	requires lombok;
	requires spatial4j;

//	opens firok.tool.alloywrench to javafx.controls;
//	opens firok.tool.alloywrench;
}
