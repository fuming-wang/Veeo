start_service() {
    local port=$1
    local jar=$2
    local name=$3

    if lsof -i:$port > /dev/null 2>&1; then
        echo "端口 $port 已被占用，程序 $name 不会启动。"
    else
        echo "端口 $port 未被占用，开始启动 $name..."
        nohup java -Xms256M -Xmx256M -XX:+UseZGC -jar $jar >/dev/null 2>&1 &
        echo "$name 已启动"
    fi
}

start_service 11000 gateway-service.jar gateway-service
start_service 11002 interest-service.jar interest-service
start_service 11005 user-service.jar user-service
start_service 11006 video-service.jar video-service
start_service 11001 file-service.jar file-service

echo "所有服务已启动完成。"