sensors=$(sensors -j)
state="http://192.168.3.162:8080/mqtt/sensors/pve"
curl -H "Content-Type: application/json" -X POST -d "$sensors" $state