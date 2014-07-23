#!/bin/bash

for i in `ls *.spg`; do

    echo -n "$i -> ${i%.spg}.ok ... "
    java -jar pgi.jar < $i > "${i%.spg}.ok"
    echo "done"

    echo -n "${i%.spg}.kg -> ${i%.spg}.out ..."
    java -jar kgi.jar < "${i%.spg}.kg" > "${i%.spg}.out"
    echo "done"

    echo "differences on ${i%.spg}.ok , ${i%.spg}.out"
    diff "${i%.spg}.ok" "${i%.spg}.out"

    rm -f "${i%.spg}.ok" "${i%.spg}.out"
done
