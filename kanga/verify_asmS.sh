#!/bin/bash

for i in `ls *.kg`; do

    echo -n "$i -> ${i%.kg}.ok ... "
    java -jar kgi.jar < $i > "${i%.kg}.ok"
    echo "done"

    echo -n "${i%.kg}.mips -> ${i%.kg}.out ..."
    /usr/local/bin/spim -file "${i%.kg}.mips" | grep -v "exceptions.s" > "${i%.kg}.out"
    echo "done"

    echo "differences on ${i%.kg}.ok , ${i%.kg}.out"
    diff "${i%.kg}.ok" "${i%.kg}.out"

    rm -f "${i%.kg}.ok" "${i%.kg}.out"
done
cd mine;
./verify_asmS.sh
