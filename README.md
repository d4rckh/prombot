# PromBot

Bring your Prometheus metrics inside your Discord server!

- /metrics command to view configured metrics values
- track metrics in real time on your channel names

### docker-compose.yaml

```yaml
services:
  prombot:
    image: ghcr.io/d4rckh/prombot:latest
    volumes:
      - ./config.yml:/app/config.yml:ro
    environment:
      - DISCORD_TOKEN=my_token
```

### Sample config.yml

```yaml
prometheusUrl: prometheus:9090

trackChannels:
  - channelId: 1394686399839731782
    name: "Memory usage: {Memory Usage}"
  - channelId: 1394686421373288468
    name: "CPU usage: {CPU Usage}"
  - channelId: 1394695038038184078
    name: "Network in: {Network in}"
  - channelId: 1394695049392029727
    name: "Network out: {Network out}"

metrics:
  - name: Memory Usage
    query: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100
    format: percentage
  - name: CPU Usage
    query: 100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[2m])) * 100)
    format: percentage
  - name: Network in
    query: sum(rate(node_network_receive_bytes_total[1m])*8)
    format: dataspeed
  - name: Network out
    query: sum(rate(node_network_transmit_bytes_total[1m])*8)
    format: dataspeed
```