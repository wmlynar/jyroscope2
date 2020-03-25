#!/bin/bash

mvn install

sudo chown -R $(id -u):$(id -g) /opt
mkdir -p /opt/jy2/bin

if [[ ":$PATH:" == *":/opt/jy2/bin:"* ]]; then
 echo "Your path is correctly set"
else
 echo "Your path is missing /opt/jy2/bin, adding it."

 echo "export PATH=/opt/jy2/bin:\${PATH}" >> ~/.bashrc
 export PATH=/opt/jy2/bin:${PATH}
fi

#rm -rf /opt/jy2/bin/jy2-launch-*.jar
#cp target/jy2-launch-*.jar /opt/jy2/bin
cp jylaunch /opt/jy2/bin
