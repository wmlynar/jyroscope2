# Jyroscope2

![Java CI with Maven](https://github.com/wmlynar/jyroscope2/workflows/Java%20CI%20with%20Maven/badge.svg)

In order to remove the warning message caused by the FST serialization
```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.nustaq.serialization.FSTClazzInfo (file:/opt/jy2/repo/fst-2.56.jar) to field java.lang.String.value
WARNING: Please consider reporting this to the maintainers of org.nustaq.serialization.FSTClazzInfo
```
please add following line to your .bashrc
```
export JDK_JAVA_OPTIONS='--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED'
```
