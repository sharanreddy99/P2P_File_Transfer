#!/bin/bash

function execute() {

    echo "starting peer $1"
    if [[ "$4" == "true" ]]; then
        ssh kondas@$2 "cd cn_project && make runCompile"
        sleep 60
    fi

    ssh kondas@$2 "cd cn_project && make runPeer peerid=$1"
    echo "ending peer $1"
}

execute "$@"
