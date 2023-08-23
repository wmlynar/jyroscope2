#!/bin/bash
for d in jy2*
do
    ( cd "$d" && mvn deploy -P deploy )
done

