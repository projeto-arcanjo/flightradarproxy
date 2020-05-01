#! /bin/sh

docker run --name flightradarproxy --hostname=flightradarproxy --network arcanjo \
    -e FEDERATION_NAME=ArcanjoFederation \
    -e FEDERATE_NAME=flightradarproxy \
	-e FIRST_COLLECT=2 \
	-e COLLECT_INTERVAL=3 \
	-v /etc/localtime:/etc/localtime:ro \
	-p 36001:8080 \
	-d projetoarcanjo/flightradarproxy:1.0	



