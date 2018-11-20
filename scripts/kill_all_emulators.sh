#!/bin/bash
echo "Asking emulators nicely to close down..."
adb devices | grep emulator | cut -f1 | while read emulator; do adb -s $emulator emu kill; done

sleep 5
echo "Killing all remaining emulators"

EMULATOR_PROCESS_NAME="qemu-system"
echo "$(ps eaxo etime,pid,comm | grep $EMULATOR_PROCESS_NAME)" | while read line
do
    if [ "${#line}" -gt 0 ]; then
        echo "Leftover emulator: $line"
        COLUMNS=()
        for word in $line
        do
            COLUMNS+=($word)
        done    
        PID=${COLUMNS[1]}    
        echo "KILLING PID $PID"
        kill -9 $PID
    fi
done