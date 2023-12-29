#!/bin/sh

set -e

sh -c "wget -nv https://github.com/thulab/IginX-benchmarks/raw/main/resources/apache-iotdb-0.12.6-server-bin.zip"

sh -c "unzip -qq apache-iotdb-0.12.6-server-bin.zip"

sh -c "sleep 10"

sh -c "ls ./"

sh -c "echo ========================="

sh -c "ls apache-iotdb-0.12.6-server-bin"

sh -c "sudo sed -i 's/# compaction_strategy=LEVEL_COMPACTION/compaction_strategy=NO_COMPACTION/' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

sh -c "sudo sed -i 's/# enable_wal=true/enable_wal=false/g' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

sh -c "sudo sed -i 's/# enable_mem_control=true/enable_mem_control=false/g' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

sh -c "sudo sed -i 's/# enable_timed_flush_unseq_memtable=true/enable_timed_flush_unseq_memtable=false/g' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

sh -c "sudo sed -i 's/# enable_timed_close_tsfile=true/enable_timed_close_tsfile=false/g' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

sh -c "sudo sed -i 's/# unseq_tsfile_size=1/seq_tsfile_size=167772160/g' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

sh -c "sudo sed -i 's/# seq_tsfile_size=1/unseq_tsfile_size=167772160/g' apache-iotdb-0.12.6-server-bin/conf/iotdb-engine.properties"

for port in "$@"
do
  sh -c "sudo cp -r apache-iotdb-0.12.6-server-bin/ apache-iotdb-0.12.6-server-bin-$port"

  sh -c "sudo sed -i 's/6667/$port/g' apache-iotdb-0.12.6-server-bin-$port/conf/iotdb-engine.properties"

  sudo sh -c "cd apache-iotdb-0.12.6-server-bin-$port/; nohup sbin/start-server.sh &"
done
