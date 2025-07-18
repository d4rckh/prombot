# PromBot

**Bring your Prometheus metrics directly into your Discord server!**

PromBot is a powerful Java-based Discord bot built with [JDA](https://github.com/DV8FromTheWorld/JDA) and compiled natively using [GraalVM](https://www.graalvm.org/) for lightning-fast performance.

## Features

- 🔍 `/metrics` command — View your configured Prometheus metrics instantly.
- 📊 Real-time tracking — Automatically update your Discord **channel names** with live metric values.
- 🗒️ Real-time log streaming - Connect Loki to your text channels.
- ⚙️ Easy configuration — Simple YAML setup to define metrics and tracked channels.

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
prometheusUrl: "http://prometheus:9090"

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

logTracking:
  - channelId: 1395816967981633668
    lokiInstance: loki:3100
    query: >
      {job="caddy"}
      | json
      | line_format "{{.ts}} | {{.request_method}} | {{.request_remote_ip}} -> {{.request_host}} | {{.status}} | {{.duration}}s | {{.user_agent}}"
```