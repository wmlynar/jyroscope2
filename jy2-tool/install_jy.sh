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

cp jy /opt/jy2/bin
