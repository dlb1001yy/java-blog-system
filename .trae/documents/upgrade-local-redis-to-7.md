# 本地 Redis 升级到 7（Windows）

## 现状
- 本地安装路径：`C:\Program Files\Redis`，版本 **3.0.504**（MSOpenTech 旧版，不支持 `GETDEL` 等新命令）
- 以 Windows 服务 `Redis`（名称：Redis）运行，开机自启，端口 6379，已设密码（`NOAUTH` 报错证明有 requirepass）
- 后端项目 docker-compose 用的是 redis:7，本地开发连的是这个 Windows 版 3.0.504

## 升级方案（用户选择：GitHub 下载 Windows 版覆盖本地服务）

使用 GitHub 社区维护的 `redis-windows/redis-windows` 项目（官方源码编译的 Windows 版）：
- 下载 **Redis 7.2.14** 的 `Redis-7.2.14-Windows-x64-cygwin-with-Service.zip`（带服务支持，SHA256: E786B1FF33EBE474B1AE756975249E5C0B4D966BA20F9BC6E159F1075983C7E0）
  下载地址：https://github.com/redis-windows/redis-windows/releases/tag/7.2.14
  （该仓库也有 7.4.x / 8.x，如需最新 7.x 可改用 7.4.6：https://github.com/DemoHubs/redis-windows/releases/tag/7.4.6）

## 执行步骤
1. **备份旧配置**：复制 `C:\Program Files\Redis\redis.windows.conf`（含密码设置）到临时目录。
2. **记录旧密码**：从旧配置读取 `requirepass` 值（后端连接串要用）。
3. **停止并卸载旧服务**（管理员权限）：
   ```powershell
   Stop-Service Redis
   sc.exe delete Redis
   ```
4. **下载并解压新版**到 `C:\Program Files\Redis-7`（新目录，不动旧目录 `C:\Program Files\Redis`，保留回滚能力）。
5. **写入新配置**：在新目录的 `redis.conf` 中设置：
   - `requirepass <旧密码>`（保持后端连接配置不变）
   - `port 6379`
   - `dir ./data`（数据目录）
   - `appendonly yes`
6. **安装并启动新服务**（管理员权限）：
   ```powershell
   # with-Service 包内含 RedisService.exe
   RedisService.exe install --port 6379 --dir "C:\Program Files\Redis-7\data"
   Start-Service Redis   # 或新服务实际名称，安装后用 Get-Service 确认
   ```
7. **验证**：
   ```powershell
   & "C:\Program Files\Redis-7\redis-cli.exe" -a <密码> ping   # PONG
   & "C:\Program Files\Redis-7\redis-cli.exe" -a <密码> GETDEL testkey  # 不再报 unknown command
   ```
8. **验证应用**：重启后端，登录接口（/api/auth/login 验证码校验）不再抛 `ERR unknown command 'GETDEL'`。
9. **回滚预案**：删除新服务，`sc.exe create Redis binPath= "C:\Program Files\Redis\redis-server.exe --service-run --configfile redis.windows.conf"` 恢复旧版。

## 说明
- Redis 3.x → 7.x 数据格式兼容（RDB/AOF 可直接加载），但本地缓存数据无需迁移（验证码/缓存丢失无影响）。
- 后端代码上一轮已把 `getAndDelete` 改成 GET+DELETE 兼容写法，升级后两种方式都可用；如需可改回 `getAndDelete`（可选，不做要求）。
- 环境变量 PATH 若指向旧目录，需更新为新目录。
